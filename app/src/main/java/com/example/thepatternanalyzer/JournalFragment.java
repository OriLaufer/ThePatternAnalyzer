package com.example.thepatternanalyzer;

// --- 1. ייבוא הכלים (Imports) ---
// אלו הספריות שנותנות לנו את היכולת להשתמש ברכיבים כמו כפתורים, רשימות, ו-Firebase.
import android.os.Bundle;
import android.util.Log; // לכתיבת הודעות לוג (למפתחים)
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView; // לזיהוי לחיצות ברשימה
import android.widget.ArrayAdapter; // לחיבור נתונים לרשימה נפתחת
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast; // להודעות קופצות קטנות למשתמש

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

// --- המחלקה הראשית של מסך היומן ---
public class JournalFragment extends Fragment {

    // --- 2. הגדרת משתנים (ה"שלטים" לרכיבי המסך) ---
    private EditText etTicker;      // שדה הזנת שם המניה (כמו AAPL)
    private EditText etQty;         // שדה הזנת כמות
    private EditText etPrice;       // שדה הזנת מחיר
    private Spinner spinnerPattern; // הרשימה הנפתחת לבחירת תבנית
    private TextView tvSpinnerHint; // הטקסט "Select a pattern..." שמופיע כרמז
    private Button btnAddTrade;     // כפתור ההוספה הירוק
    private RecyclerView recyclerTrades; // הרשימה הנגללת למטה (ההיסטוריה)

    // --- 3. משתנים לניהול המידע ---
    private FirebaseFirestore db;   // החיבור למסד הנתונים בענן
    private FirebaseAuth auth;      // החיבור למערכת המשתמשים (מי מחובר כרגע?)
    private TradesAdapter adapter;  // ה"מנהל" שמסדר את הנתונים בתוך הרשימה הויזואלית
    private List<Trade> tradeList;  // רשימה בזיכרון המחשב שמחזיקה את כל העסקאות

    // בנאי ריק (חובה באנדרואיד כדי שהאפליקציה לא תקרוס)
    public JournalFragment() {
    }

    // --- 4. יצירת המראה (ניפוח ה-XML) ---
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // טוען את קובץ העיצוב fragment_journal.xml ומכין אותו לתצוגה
        return inflater.inflate(R.layout.fragment_journal, container, false);
    }

    // --- 5. הפונקציה הראשית שרצה כשהמסך מוכן ---
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // א. אתחול הכלים של Firebase
        db = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();

        // ב. חיבור המשתנים לרכיבים ב-XML לפי ה-ID שלהם
        etTicker = view.findViewById(R.id.etTicker);
        etQty = view.findViewById(R.id.etQty);
        etPrice = view.findViewById(R.id.etPrice);
        spinnerPattern = view.findViewById(R.id.spinnerPattern);
        tvSpinnerHint = view.findViewById(R.id.tvSpinnerHint);
        btnAddTrade = view.findViewById(R.id.btnAddTrade);
        recyclerTrades = view.findViewById(R.id.recyclerTrades);

        // ג. הגדרת הרשימה הנפתחת (מה יופיע כשלוחצים עליה?)
        setupSpinner();

        // ד. הכנת רשימת ההיסטוריה (שתהיה מוכנה לקבל נתונים)
        setupRecyclerView();

        // ה. *** השורה שמפעילה את הכפתור ***
        // אנחנו אומרים: "כשמישהו לוחץ (Click) על הכפתור, תפעיל את הפונקציה saveTradeToFirebase"
        btnAddTrade.setOnClickListener(v -> saveTradeToFirebase());

        // ו. הפעלת ההאזנה לענן (כדי שהרשימה תתעדכן אוטומטית אם יש שינויים)
        listenToTrades();
    }

    // --- פונקציה להגדרת הרשימה הנפתחת ---
    private void setupSpinner() {
        List<String> patterns = new ArrayList<>();
        // פריט ראשון ריק כדי שהמשתמש יראה את ה-Hint שלנו ("Select a pattern...")
        patterns.add("");
        patterns.add("Momentum");
        patterns.add("Bull Flag");
        patterns.add("Gap & Go");
        patterns.add("Reversal");
        patterns.add("Breakout");

        // מתאם פשוט שמחבר את הרשימה ל-Spinner
        ArrayAdapter<String> spinnerAdapter = new ArrayAdapter<>(requireContext(), android.R.layout.simple_spinner_dropdown_item, patterns);
        spinnerPattern.setAdapter(spinnerAdapter);

        // מאזין לבחירה ב-Spinner (כדי להעלים/להציג את הרמז)
        spinnerPattern.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (position == 0) {
                    // אם נבחר הפריט הראשון (הריק), נציג את ה-Hint
                    tvSpinnerHint.setVisibility(View.VISIBLE);
                } else {
                    // אחרת, נסתיר את ה-Hint כדי לראות את הבחירה
                    tvSpinnerHint.setVisibility(View.INVISIBLE);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                tvSpinnerHint.setVisibility(View.VISIBLE);
            }
        });
    }

    // --- פונקציה להכנת רשימת ההיסטוריה ---
    private void setupRecyclerView() {
        tradeList = new ArrayList<>(); // יצירת רשימה ריקה
        adapter = new TradesAdapter(tradeList); // יצירת המתאם וחיבורו לרשימה
        recyclerTrades.setLayoutManager(new LinearLayoutManager(getContext())); // סידור הפריטים אחד מתחת לשני
        recyclerTrades.setAdapter(adapter); // חיבור המתאם לרכיב הויזואלי
    }

    // --- פונקציה לשמירת העסקה (מופעלת בלחיצה על הכפתור) ---
    private void saveTradeToFirebase() {
        // 1. איסוף המידע: לוקחים את הטקסט מהשדות ומנקים רווחים מיותרים
        String ticker = etTicker.getText().toString().trim().toUpperCase(); // הופך לאותיות גדולות (AAPL)
        String qtyStr = etQty.getText().toString().trim();
        String priceStr = etPrice.getText().toString().trim();

        // בדיקה איזו תבנית נבחרה
        String pattern = "";
        if (spinnerPattern.getSelectedItem() != null) {
            pattern = spinnerPattern.getSelectedItem().toString();
        }

        // 2. בדיקות תקינות (Validations): מוודאים שהמשתמש לא שכח למלא שדות
        if (ticker.isEmpty()) {
            etTicker.setError("נא להזין שם מניה"); // מציג שגיאה אדומה בשדה
            return; // עוצר את הפונקציה כאן
        }
        if (qtyStr.isEmpty()) {
            etQty.setError("נא להזין כמות");
            return;
        }
        if (priceStr.isEmpty()) {
            etPrice.setError("נא להזין מחיר");
            return;
        }
        // בדיקה שהמשתמש בחר תבנית אמיתית ולא את הריקה
        if (pattern.isEmpty() || spinnerPattern.getSelectedItemPosition() == 0) {
            Toast.makeText(getContext(), "נא לבחור תבנית מסחר", Toast.LENGTH_SHORT).show();
            return;
        }

        // 3. המרת נתונים: הופכים את הטקסט ("10") למספרים שהמחשב מבין (10)
        int quantity;
        double price;
        try {
            quantity = Integer.parseInt(qtyStr);
            price = Double.parseDouble(priceStr);
        } catch (NumberFormatException e) {
            Toast.makeText(getContext(), "המספרים אינם תקינים", Toast.LENGTH_SHORT).show();
            return;
        }

        // 4. בדיקת זהות: מי המשתמש שמנסה לשמור?
        if (auth.getCurrentUser() == null) return;
        String userId = auth.getCurrentUser().getUid();

        long timestamp = System.currentTimeMillis(); // הזמן הנוכחי

        // 5. יצירת החבילה: בונים אובייקט Trade מסודר עם כל הנתונים
        Trade newTrade = new Trade(null, userId, ticker, quantity, price, pattern, timestamp);

        // 6. השליחה לענן (Firebase):
        // הנתיב: משתמשים -> [ה-ID שלך] -> trades -> [מסמך חדש]
        db.collection("users").document(userId).collection("trades")
                .add(newTrade)
                .addOnSuccessListener(documentReference -> {
                    // אם השמירה הצליחה:
                    Toast.makeText(getContext(), "העסקה נשמרה בהצלחה!", Toast.LENGTH_SHORT).show();
                    clearForm(); // מנקים את הטופס
                })
                .addOnFailureListener(e -> {
                    // אם הייתה שגיאה:
                    Toast.makeText(getContext(), "שגיאה בשמירה: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    // --- פונקציה להאזנה לנתונים בזמן אמת ---
    private void listenToTrades() {
        if (auth.getCurrentUser() == null) return;
        String userId = auth.getCurrentUser().getUid();

        // אנחנו אומרים ל-Firebase: "תסתכל על התיקייה הזו, ותודיע לי כל פעם שמשהו משתנה"
        db.collection("users").document(userId).collection("trades")
                .orderBy("timestamp", Query.Direction.DESCENDING) // סדר מהחדש לישן
                .addSnapshotListener((value, error) -> {
                    if (error != null) {
                        Log.e("Firestore", "Listen failed.", error);
                        return;
                    }

                    // אם הגיע מידע חדש
                    if (value != null) {
                        tradeList.clear(); // מוחקים את הרשימה הישנה בזיכרון
                        for (DocumentSnapshot doc : value.getDocuments()) {
                            // הופכים כל מסמך מהענן לאובייקט Trade
                            Trade trade = doc.toObject(Trade.class);
                            if (trade != null) {
                                trade.setId(doc.getId()); // שומרים את ה-ID האמיתי
                                tradeList.add(trade); // מוסיפים לרשימה
                            }
                        }
                        // מודיעים למתאם: "הנתונים השתנו! תרענן את התצוגה!"
                        adapter.notifyDataSetChanged();
                    }
                });
    }

    // --- פונקציית עזר לניקוי הטופס ---
    private void clearForm() {
        etTicker.setText("");
        etQty.setText("10"); // ברירת מחדל
        etPrice.setText("");
        spinnerPattern.setSelection(0); // מחזיר להתחלה
        etTicker.requestFocus(); // שם את הסמן בשדה הראשון לנוחות
    }
}