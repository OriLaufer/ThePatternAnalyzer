package com.example.thepatternanalyzer;

// --- ייבוא הספריות הדרושות ---
import android.graphics.Color; // לשינוי צבעי טקסט (ירוק לרווח, אדום להפסד)
import android.view.LayoutInflater; // להמרת קובץ XML לתצוגה חיה (View)
import android.view.View; // מייצג רכיב ויזואלי במסך
import android.view.ViewGroup; // מיכל שמחזיק רכיבים אחרים
import android.widget.Button; // רכיב כפתור
import android.widget.TextView; // רכיב להצגת טקסט
import androidx.annotation.NonNull; // מוודא שפרמטר לא יהיה ריק
import androidx.recyclerview.widget.RecyclerView; // הרכיב שמציג רשימה נגללת יעילה
import java.util.List; // ממשק לרשימת נתונים
import java.util.Locale; // לפרמוט טקסטים (כמו הוספת סימן דולר וכדומה)

// --- המחלקה הראשית של המתאם (Adapter) ---
// המתאם אחראי לקחת את הנתונים (רשימת הטריידים) ולהציג אותם בתוך ה-RecyclerView בדשבורד.
public class DashboardTradesAdapter extends RecyclerView.Adapter<DashboardTradesAdapter.ViewHolder> {

    // רשימה שמחזיקה את אובייקטי ה-Trade שצריך להציג
    private List<Trade> tradeList;

    // מאזין (Listener) לאירועים - כדי שנוכל לדעת מתי המשתמש לחץ על כפתור "סגור עסקה"
    private OnTradeActionListener actionListener;

    // --- הגדרת הממשק (Interface) ---
    // זהו "חוזה" שמאפשר לפרגמנט (DashboardFragment) לקבל עדכון כשלוחצים על כפתור בתוך הרשימה.
    public interface OnTradeActionListener {
        void onClosePosition(Trade trade); // פונקציה שתופעל כשלוחצים על "Close Position"
    }

    // --- בנאי (Constructor) ---
    // מקבל את רשימת הטריידים ואת המאזין מהפרגמנט
    public DashboardTradesAdapter(List<Trade> tradeList, OnTradeActionListener actionListener) {
        this.tradeList = tradeList;
        this.actionListener = actionListener;
    }

    // --- יצירת "קופסה" לשורה חדשה (ViewHolder) ---
    // פונקציה זו נקראת כשהרשימה צריכה ליצור שורה חדשה בזיכרון (לפני מילוי הנתונים)
    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // טוען את קובץ העיצוב XML של השורה (item_dashboard_trade.xml) והופך אותו לאובייקט View
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_dashboard_trade, parent, false);
        // מחזיר את ה-ViewHolder החדש שמחזיק את העיצוב הזה
        return new ViewHolder(view);
    }

    // --- מילוי הנתונים בתוך השורה (Binding) ---
    // פונקציה זו נקראת עבור כל שורה ברשימה כדי להכניס את המידע הספציפי של הטרייד הנוכחי
    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        // שליפת הטרייד הנוכחי מהרשימה לפי המיקום (position)
        Trade trade = tradeList.get(position);

        // 1. הגדרת הלוגו ושם המניה
        String ticker = trade.getTicker();
        if (ticker != null) {
            // בלוגו העגול נציג רק את ההתחלה של השם (עד 4 תווים) כדי שייכנס יפה
            holder.tvDashLogo.setText(ticker.length() > 4 ? ticker.substring(0, 4) : ticker);
            // בשם המניה המלא נציג את השם עם דולר לפניו (למשל $AAPL)
            holder.tvDashTicker.setText("$" + ticker);
        }

        // 2. הגדרת פרטי העסקה (כמות ומחיר כניסה)
        // String.format יוצר טקסט מעוצב: %d למספר שלם (כמות), %.2f למספר עשרוני עם 2 ספרות (מחיר)
        String details = String.format(Locale.US, "%d shares @ $%.2f", trade.getQuantity(), trade.getEntryPrice());
        holder.tvDashDetails.setText(details);

        // --- חיבור לנתוני אמת מה-API! ---
        // כאן אנחנו משתמשים ב-NetworkManager כדי לקבל את המחיר העדכני של המניה מהאינטרנט

        // שלב א': הצגת מצב טעינה זמני עד שהנתונים יגיעו
        holder.tvDashProfit.setText("Loading...");
        holder.tvDashPercent.setText("...");
        holder.tvDashProfit.setTextColor(Color.GRAY); // צבע אפור ניטרלי

        // שלב ב': שליחת הבקשה ל-API
        NetworkManager.getInstance().getStockPrice(ticker, new NetworkManager.StockCallback() {

            // פונקציה זו תופעל כשהנתונים יחזרו בהצלחה מהאינטרנט
            @Override
            public void onSuccess(double currentPrice, double changePercent) {
                // חישוב הרווח/הפסד (P&L) בזמן אמת:
                // (מחיר נוכחי - מחיר כניסה) * כמות המניות
                double pnl = (currentPrice - trade.getEntryPrice()) * trade.getQuantity();

                // חישוב אחוז הרווח/הפסד:
                double investment = trade.getEntryPrice() * trade.getQuantity(); // סך ההשקעה
                double percent = 0;
                if (investment != 0) {
                    percent = (pnl / investment) * 100; // (רווח / השקעה) * 100
                }

                // עדכון הצבעים והטקסט לפי התוצאה (חיובי/שלילי)
                if (pnl >= 0) {
                    // אם יש רווח: צבע ירוק והוספת סימן פלוס (+)
                    holder.tvDashProfit.setTextColor(Color.parseColor("#10B981")); // ירוק
                    holder.tvDashPercent.setTextColor(Color.parseColor("#10B981"));
                    holder.tvDashProfit.setText("+$" + String.format(Locale.US, "%.2f", pnl));
                    holder.tvDashPercent.setText("+" + String.format(Locale.US, "%.1f%%", percent));
                } else {
                    // אם יש הפסד: צבע אדום והוספת סימן מינוס (-)
                    holder.tvDashProfit.setTextColor(Color.parseColor("#EF4444")); // אדום
                    holder.tvDashPercent.setTextColor(Color.parseColor("#EF4444"));
                    // Math.abs הופך את המספר לחיובי כדי שנוכל להוסיף את המינוס ידנית בפורמט שלנו
                    holder.tvDashProfit.setText("-$" + String.format(Locale.US, "%.2f", Math.abs(pnl)));
                    holder.tvDashPercent.setText(String.format(Locale.US, "%.1f%%", percent));
                }
            }

            // פונקציה זו תופעל אם הייתה שגיאה בקבלת הנתונים
            @Override
            public void onError(String error) {
                holder.tvDashProfit.setText("N/A"); // הצגת "לא זמין"
                holder.tvDashPercent.setText("-");
            }
        });

        // 3. הגדרת כפתור "Close Position"
        holder.btnClosePosition.setOnClickListener(v -> {
            // אם יש מאזין (מישהו בפרגמנט מחכה לשמוע), נודיע לו שלחצו
            if (actionListener != null) {
                actionListener.onClosePosition(trade); // מעבירים את הטרייד שרוצים לסגור
            }
        });
    }

    // --- החזרת מספר הפריטים ברשימה ---
    // אומר ל-RecyclerView כמה שורות הוא צריך לצייר סה"כ
    @Override
    public int getItemCount() {
        return tradeList.size();
    }

    // --- המחלקה שמחזיקה את הרכיבים הויזואליים של שורה אחת (ViewHolder) ---
    // תפקידה: למצוא את כל ה-Views ב-XML פעם אחת ולשמור אותם לשימוש חוזר (יעילות)
    public static class ViewHolder extends RecyclerView.ViewHolder {
        // משתנים לרכיבי השורה
        TextView tvDashLogo, tvDashTicker, tvDashDetails, tvDashProfit, tvDashPercent;
        Button btnClosePosition;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            // חיבור כל משתנה לרכיב המתאים בקובץ העיצוב item_dashboard_trade.xml
            tvDashLogo = itemView.findViewById(R.id.tvDashLogo);
            tvDashTicker = itemView.findViewById(R.id.tvDashTicker);
            tvDashDetails = itemView.findViewById(R.id.tvDashDetails);
            tvDashProfit = itemView.findViewById(R.id.tvDashProfit);
            tvDashPercent = itemView.findViewById(R.id.tvDashPercent);
            btnClosePosition = itemView.findViewById(R.id.btnClosePosition);
        }
    }
}