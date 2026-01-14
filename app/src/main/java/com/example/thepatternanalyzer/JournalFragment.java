package com.example.thepatternanalyzer;

import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

import java.util.ArrayList;
import java.util.List;

public class JournalFragment extends Fragment {

    private EditText etTicker, etQty, etPrice;
    private Spinner spinnerPattern;
    private TextView tvSpinnerHint;
    private Button btnAddTrade;
    private RecyclerView recyclerTrades;

    private FirebaseFirestore db;
    private FirebaseAuth auth;
    private TradesAdapter adapter;
    private List<Trade> tradeList;

    public JournalFragment() {
        // בנאי ריק חובה
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_journal, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // אתחול Firebase
        db = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();

        // חיבור למסך
        etTicker = view.findViewById(R.id.etTicker);
        etQty = view.findViewById(R.id.etQty);
        etPrice = view.findViewById(R.id.etPrice);
        spinnerPattern = view.findViewById(R.id.spinnerPattern);
        tvSpinnerHint = view.findViewById(R.id.tvSpinnerHint);
        btnAddTrade = view.findViewById(R.id.btnAddTrade);
        recyclerTrades = view.findViewById(R.id.recyclerTrades);

        // הגדרת הרכיבים
        setupSpinner();
        setupRecyclerView();

        // כפתור שמירה
        btnAddTrade.setOnClickListener(v -> saveTradeToFirebase());

        // האזנה לשינויים בנתונים
        listenToTrades();
    }

    // --- הגדרת ה-Spinner (עם צבעים מתוקנים) ---
    private void setupSpinner() {
        List<String> patterns = new ArrayList<>();
        patterns.add(""); // פריט ראשון ריק
        patterns.add("Momentum");
        patterns.add("Bull Flag");
        patterns.add("Gap & Go");
        patterns.add("Reversal");
        patterns.add("Breakout");

        // יצירת מתאם מותאם אישית כדי שהטקסט יהיה לבן כשהוא נבחר
        ArrayAdapter<String> spinnerAdapter = new ArrayAdapter<String>(requireContext(), android.R.layout.simple_spinner_item, patterns) {
            @NonNull
            @Override
            public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
                // העיצוב של הפריט שרואים בתוך התיבה הסגורה
                View view = super.getView(position, convertView, parent);
                TextView tv = (TextView) view;
                tv.setTextColor(Color.WHITE); // טקסט לבן!
                tv.setTextSize(16); // גודל טקסט כמו בשאר השדות
                return view;
            }

            @Override
            public View getDropDownView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
                // העיצוב של הרשימה שנפתחת
                View view = super.getDropDownView(position, convertView, parent);
                TextView tv = (TextView) view;
                if (position == 0) {
                    // מסתירים את הפריט הראשון הריק מהרשימה
                    tv.setHeight(0);
                } else {
                    tv.setTextColor(Color.BLACK); // שחור על רקע לבן (ברירת מחדל)
                }
                return view;
            }
        };

        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerPattern.setAdapter(spinnerAdapter);

        // הסתרת הרמז ("Select a pattern...") כשבוחרים משהו
        spinnerPattern.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (position == 0) {
                    tvSpinnerHint.setVisibility(View.VISIBLE);
                } else {
                    tvSpinnerHint.setVisibility(View.INVISIBLE);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                tvSpinnerHint.setVisibility(View.VISIBLE);
            }
        });
    }

    // --- הגדרת הרשימה והמחיקה ---
    private void setupRecyclerView() {
        tradeList = new ArrayList<>();

        // כאן אנחנו יוצרים את ה-Adapter ומעבירים לו את פונקציית המחיקה (deleteTrade)
        adapter = new TradesAdapter(tradeList, trade -> deleteTrade(trade));

        recyclerTrades.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerTrades.setAdapter(adapter);
    }

    // --- פונקציה למחיקת עסקה מ-Firebase ---
    private void deleteTrade(Trade trade) {
        if (auth.getCurrentUser() == null || trade.getId() == null) return;
        String userId = auth.getCurrentUser().getUid();

        // מחיקת המסמך הספציפי לפי ה-ID שלו
        db.collection("users").document(userId).collection("trades")
                .document(trade.getId())
                .delete()
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(getContext(), "Trade deleted", Toast.LENGTH_SHORT).show();
                    // לא צריך למחוק מהרשימה ידנית - הפונקציה listenToTrades תעשה את זה אוטומטית!
                })
                .addOnFailureListener(e ->
                        Toast.makeText(getContext(), "Error deleting: " + e.getMessage(), Toast.LENGTH_SHORT).show()
                );
    }

    // --- שמירת עסקה חדשה ---
    private void saveTradeToFirebase() {
        // 1. קריאת הנתונים
        String ticker = etTicker.getText().toString().trim().toUpperCase();
        String qtyStr = etQty.getText().toString().trim();
        String priceStr = etPrice.getText().toString().trim();

        String pattern = "";
        if (spinnerPattern.getSelectedItem() != null) {
            pattern = spinnerPattern.getSelectedItem().toString();
        }

        // 2. בדיקות תקינות
        if (ticker.isEmpty() || qtyStr.isEmpty() || priceStr.isEmpty()) {
            Toast.makeText(getContext(), "Please fill all fields", Toast.LENGTH_SHORT).show();
            return;
        }
        if (pattern.isEmpty()) {
            Toast.makeText(getContext(), "Please select a pattern", Toast.LENGTH_SHORT).show();
            return;
        }

        // 3. המרות
        int quantity;
        double price;
        try {
            quantity = Integer.parseInt(qtyStr);
            price = Double.parseDouble(priceStr);
        } catch (NumberFormatException e) {
            Toast.makeText(getContext(), "Invalid numbers", Toast.LENGTH_SHORT).show();
            return;
        }

        if (auth.getCurrentUser() == null) return;
        String userId = auth.getCurrentUser().getUid();
        long timestamp = System.currentTimeMillis();

        // 4. יצירת האובייקט
        Trade newTrade = new Trade(null, userId, ticker, quantity, price, pattern, timestamp);

        // 5. שמירה
        db.collection("users").document(userId).collection("trades")
                .add(newTrade)
                .addOnSuccessListener(documentReference -> {
                    Toast.makeText(getContext(), "Trade Logged Successfully!", Toast.LENGTH_SHORT).show();
                    clearForm(); // ניקוי הטופס
                })
                .addOnFailureListener(e ->
                        Toast.makeText(getContext(), "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show()
                );
    }

    // --- האזנה לנתונים (עדכון הרשימה) ---
    private void listenToTrades() {
        if (auth.getCurrentUser() == null) return;
        String userId = auth.getCurrentUser().getUid();

        db.collection("users").document(userId).collection("trades")
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .addSnapshotListener((value, error) -> {
                    if (error != null) {
                        Log.e("Firestore", "Listen failed.", error);
                        return;
                    }

                    if (value != null) {
                        tradeList.clear();
                        for (DocumentSnapshot doc : value.getDocuments()) {
                            Trade trade = doc.toObject(Trade.class);
                            if (trade != null) {
                                trade.setId(doc.getId()); // שומרים את ה-ID (חשוב למחיקה!)
                                tradeList.add(trade);
                            }
                        }
                        adapter.notifyDataSetChanged();
                    }
                });
    }

    // --- ניקוי הטופס ---
    private void clearForm() {
        etTicker.setText("");
        etQty.setText("10");
        etPrice.setText("");
        spinnerPattern.setSelection(0); // מחזיר להתחלה
        etTicker.requestFocus();
    }
}