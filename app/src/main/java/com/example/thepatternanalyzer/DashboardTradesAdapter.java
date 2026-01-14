package com.example.thepatternanalyzer;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;
import java.util.Locale;

public class DashboardTradesAdapter extends RecyclerView.Adapter<DashboardTradesAdapter.ViewHolder> {

    private List<Trade> tradeList;
    private OnTradeActionListener actionListener;

    // ממשק (Interface) להעברת אירוע לחיצה על "סגור עסקה" לפרגמנט
    public interface OnTradeActionListener {
        void onClosePosition(Trade trade);
    }

    public DashboardTradesAdapter(List<Trade> tradeList, OnTradeActionListener actionListener) {
        this.tradeList = tradeList;
        this.actionListener = actionListener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // טעינת קובץ העיצוב של השורה בדשבורד
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_dashboard_trade, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Trade trade = tradeList.get(position);

        // 1. לוגו - האות הראשונה של המניה (או עד 4 תווים)
        String ticker = trade.getTicker();
        if (ticker != null) {
            holder.tvDashLogo.setText(ticker.length() > 4 ? ticker.substring(0, 4) : ticker);
            holder.tvDashTicker.setText("$" + ticker);
        }

        // 2. פרטים (כמות ומחיר כניסה)
        String details = String.format(Locale.US, "%d shares @ $%.2f", trade.getQuantity(), trade.getEntryPrice());
        holder.tvDashDetails.setText(details);

        // --- חישוב רווח/הפסד (סימולציה זמנית עד לחיבור API) ---
        // נניח מחיר נוכחי רנדומלי לצורך התצוגה (עולה/יורד קצת ממחיר הכניסה)
        double currentPrice = trade.getEntryPrice() * (Math.random() > 0.5 ? 1.05 : 0.98);

        double pnl = (currentPrice - trade.getEntryPrice()) * trade.getQuantity();
        double percent = ((currentPrice - trade.getEntryPrice()) / trade.getEntryPrice()) * 100;

        // עיצוב לפי רווח או הפסד
        if (pnl >= 0) {
            // רווח -> ירוק
            holder.tvDashProfit.setTextColor(Color.parseColor("#10B981"));
            holder.tvDashPercent.setTextColor(Color.parseColor("#10B981"));
            holder.tvDashProfit.setText("+$" + String.format(Locale.US, "%.2f", pnl));
            holder.tvDashPercent.setText("+" + String.format(Locale.US, "%.1f%%", percent));
        } else {
            // הפסד -> אדום
            holder.tvDashProfit.setTextColor(Color.parseColor("#EF4444"));
            holder.tvDashPercent.setTextColor(Color.parseColor("#EF4444"));
            holder.tvDashProfit.setText("-$" + String.format(Locale.US, "%.2f", Math.abs(pnl)));
            holder.tvDashPercent.setText(String.format(Locale.US, "%.1f%%", percent));
        }

        // 3. כפתור סגירה
        holder.btnClosePosition.setOnClickListener(v -> {
            if (actionListener != null) {
                // מעדכנים את מחיר היציאה למחיר ה"נוכחי" (בסימולציה) לפני השליחה
                trade.setExitPrice(currentPrice);
                actionListener.onClosePosition(trade);
            }
        });
    }

    @Override
    public int getItemCount() {
        return tradeList.size();
    }

    // המחלקה שמחזיקה את הרכיבים של שורה אחת
    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvDashLogo, tvDashTicker, tvDashDetails, tvDashProfit, tvDashPercent;
        Button btnClosePosition;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvDashLogo = itemView.findViewById(R.id.tvDashLogo);
            tvDashTicker = itemView.findViewById(R.id.tvDashTicker);
            tvDashDetails = itemView.findViewById(R.id.tvDashDetails);
            tvDashProfit = itemView.findViewById(R.id.tvDashProfit);
            tvDashPercent = itemView.findViewById(R.id.tvDashPercent);
            btnClosePosition = itemView.findViewById(R.id.btnClosePosition);
        }
    }
}