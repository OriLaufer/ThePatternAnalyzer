package com.example.thepatternanalyzer;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;
import java.util.Locale;

public class TradesAdapter extends RecyclerView.Adapter<TradesAdapter.TradeViewHolder> {

    private List<Trade> tradeList;
    private OnDeleteClickListener deleteListener; // משתנה למאזין המחיקה

    // ממשק (Interface) - דרך לתקשר עם ה-JournalFragment שצריך למחוק
    public interface OnDeleteClickListener {
        void onDeleteClick(Trade trade);
    }

    // בנאי: מקבל את הרשימה וגם את הפונקציה למחיקה
    public TradesAdapter(List<Trade> tradeList, OnDeleteClickListener deleteListener) {
        this.tradeList = tradeList;
        this.deleteListener = deleteListener;
    }

    @NonNull
    @Override
    public TradeViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // טעינת העיצוב החדש והיפה שיצרנו (item_trade.xml)
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_trade, parent, false);
        return new TradeViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull TradeViewHolder holder, int position) {
        Trade trade = tradeList.get(position);

        // 1. לוגו - הצגת שם המניה (עד 4 תווים כדי שלא יגלוש מהעיגול)
        String ticker = trade.getTicker();
        if (ticker != null) {
            holder.tvLogo.setText(ticker.length() > 4 ? ticker.substring(0, 4) : ticker);
        }

        // 2. מילוי הטקסטים
        holder.tvTicker.setText(trade.getTicker());
        holder.tvPattern.setText(trade.getPattern());
        holder.tvStatus.setText(trade.getStatus());

        // 3. פרטים מעוצבים (ללא שטרודל, כמו שביקשת)
        // דוגמה: "10 shares $150.00"
        String details = String.format(Locale.US, "%d shares $%.2f", trade.getQuantity(), trade.getEntryPrice());
        holder.tvDetails.setText(details);

        // 4. הגדרת לחיצה על כפתור המחיקה (הפח)
        holder.btnDelete.setOnClickListener(v -> {
            if (deleteListener != null) {
                // קורא לפונקציה ב-JournalFragment שמוחקת מ-Firebase
                deleteListener.onDeleteClick(trade);
            }
        });
    }

    @Override
    public int getItemCount() {
        return tradeList.size();
    }

    // המחלקה שמחזיקה את הרכיבים של שורה אחת
    public static class TradeViewHolder extends RecyclerView.ViewHolder {
        TextView tvLogo, tvTicker, tvStatus, tvPattern, tvDetails;
        ImageView btnDelete;

        public TradeViewHolder(@NonNull View itemView) {
            super(itemView);
            // קישור לרכיבים ב-item_trade.xml
            tvLogo = itemView.findViewById(R.id.tvRowLogo);
            tvTicker = itemView.findViewById(R.id.tvRowTicker);
            tvStatus = itemView.findViewById(R.id.tvRowStatus);
            tvPattern = itemView.findViewById(R.id.tvRowPattern);
            tvDetails = itemView.findViewById(R.id.tvRowDetails);
            btnDelete = itemView.findViewById(R.id.btnDelete);
        }
    }
}