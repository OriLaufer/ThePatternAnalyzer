package com.example.thepatternanalyzer;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;
import java.util.Locale;

public class TradesAdapter extends RecyclerView.Adapter<TradesAdapter.TradeViewHolder> {

    private List<Trade> tradeList;

    // בנאי: מקבל את הרשימה של הטריידים
    public TradesAdapter(List<Trade> tradeList) {
        this.tradeList = tradeList;
    }

    @NonNull
    @Override
    public TradeViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // כאן אנחנו יוצרים את העיצוב של שורה אחת (משתמשים ב-layout שכבר יש לנו)
        // שים לב: אנחנו נצטרך ליצור קובץ XML קטן לשורה אחת בשם item_trade.xml
        // בינתיים נשתמש בזה, ותכף ניצור אותו.
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_trade, parent, false);
        return new TradeViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull TradeViewHolder holder, int position) {
        // כאן אנחנו "מלבישים" את הנתונים על השורה
        Trade trade = tradeList.get(position);

        holder.tvTicker.setText(trade.getTicker());
        holder.tvPattern.setText(trade.getPattern());
        holder.tvStatus.setText(trade.getStatus());

        // נחשב רווח/הפסד (כרגע אין לנו מחיר יציאה ב-Model, אז נציג רק מחיר כניסה וכמות)
        // בעתיד נוסיף מחיר יציאה ל-Trade.java כדי לחשב רווח אמיתי.
        String details = String.format(Locale.getDefault(), "%d @ $%.2f", trade.getQuantity(), trade.getEntryPrice());
        holder.tvDetails.setText(details);
    }

    @Override
    public int getItemCount() {
        return tradeList.size();
    }

    // המחלקה שמחזיקה את הרכיבים של שורה אחת
    static class TradeViewHolder extends RecyclerView.ViewHolder {
        TextView tvTicker, tvPattern, tvStatus, tvDetails;

        public TradeViewHolder(@NonNull View itemView) {
            super(itemView);
            // כאן אנחנו מוצאים את הרכיבים בתוך ה-XML של השורה
            tvTicker = itemView.findViewById(R.id.tvRowTicker);
            tvPattern = itemView.findViewById(R.id.tvRowPattern);
            tvStatus = itemView.findViewById(R.id.tvRowStatus);
            tvDetails = itemView.findViewById(R.id.tvRowDetails);
        }
    }
}