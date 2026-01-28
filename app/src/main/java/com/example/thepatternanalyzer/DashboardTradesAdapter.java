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
import java.util.HashMap; // מבנה נתונים לשמירת זוגות מפתח-ערך (עבור ה-PnL לכל מניה)
import java.util.List; // ממשק לרשימת נתונים
import java.util.Locale; // לפרמוט טקסטים (כמו הוספת סימן דולר וכדומה)
import java.util.Map; // ממשק למבנה נתונים מפה

// --- המחלקה הראשית של המתאם (Adapter) ---
// המתאם אחראי לקחת את הנתונים (רשימת הטריידים) ולהציג אותם בתוך ה-RecyclerView בדשבורד.
public class DashboardTradesAdapter extends RecyclerView.Adapter<DashboardTradesAdapter.ViewHolder> {

    // רשימה שמחזיקה את אובייקטי ה-Trade שצריך להציג
    private List<Trade> tradeList;

    // מאזין (Listener) לאירועים - כדי שנוכל לדעת מתי המשתמש לחץ על כפתור "סגור עסקה"
    private OnTradeActionListener actionListener;

    // מאזין (Listener) חדש לעדכון רווח כולל (כדי לעדכן את הדשבורד למעלה ברווח הפתוח)
    private OnPnlUpdateListener pnlListener;

    // מפה לשמירת הרווח של כל מניה בנפרד (המפתח הוא ה-ID של העסקה, הערך הוא הרווח שלה)
    // זה נחוץ כדי שנוכל לסכום את כל הרווחים של כל השורות בזמן אמת
    private Map<String, Double> currentPnls = new HashMap<>();

    // --- הגדרת הממשק (Interface) לפעולות מסחר ---
    // זהו "חוזה" שמאפשר לפרגמנט לקבל עדכון כשלוחצים על כפתור בתוך הרשימה.
    public interface OnTradeActionListener {
        void onClosePosition(Trade trade); // פונקציה שתופעל כשלוחצים על "Close Position"
    }

    // --- הגדרת הממשק (Interface) לעדכון רווח ---
    // זהו "חוזה" שמאפשר לפרגמנט לקבל את הסכום הכולל של הרווח הפתוח מכל השורות.
    public interface OnPnlUpdateListener {
        void onTotalPnlUpdated(double totalOpenPnl); // פונקציה שתופעל כשסכום הרווחים משתנה
    }

    // --- בנאי (Constructor) ---
    // מקבל את רשימת הטריידים ואת המאזינים מהפרגמנט ומאתחל את המשתנים
    public DashboardTradesAdapter(List<Trade> tradeList, OnTradeActionListener actionListener, OnPnlUpdateListener pnlListener) {
        this.tradeList = tradeList;
        this.actionListener = actionListener;
        this.pnlListener = pnlListener;
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

        // שליפת שם המניה (Ticker)
        String ticker = trade.getTicker();

        // בדיקה שהשם לא ריק לפני שמציגים אותו
        if (ticker != null) {
            // בלוגו העגול נציג רק את ההתחלה של השם (עד 4 תווים) כדי שייכנס יפה
            holder.tvDashLogo.setText(ticker.length() > 4 ? ticker.substring(0, 4) : ticker);
            // בשם המניה המלא נציג את השם עם דולר לפניו (למשל $AAPL)
            holder.tvDashTicker.setText("$" + ticker);
        }

        // הגדרת פרטי העסקה (כמות ומחיר כניסה) בפורמט קריא
        String details = String.format(Locale.US, "%d shares @ $%.2f", trade.getQuantity(), trade.getEntryPrice());
        holder.tvDashDetails.setText(details);

        // --- חיבור לנתוני אמת מה-API! ---

        // שלב א': הצגת מצב טעינה זמני עד שהנתונים יגיעו מהאינטרנט
        holder.tvDashProfit.setText("..."); // שלוש נקודות במקום מחיר
        holder.tvDashProfit.setTextColor(Color.GRAY); // צבע אפור ניטרלי

        // שלב ב': שליחת הבקשה ל-API לקבלת מחיר עדכני
        NetworkManager.getInstance().getStockPrice(ticker, new NetworkManager.StockCallback() {

            // פונקציה זו תופעל כשהנתונים יחזרו בהצלחה מהאינטרנט
            @Override
            public void onSuccess(double currentPrice, double changePercent) {
                // חישוב הרווח/הפסד (P&L) בזמן אמת: (מחיר נוכחי - מחיר כניסה) * כמות
                double pnl = (currentPrice - trade.getEntryPrice()) * trade.getQuantity();

                // עדכון המפה עם הרווח החדש של העסקה הזו (לצורך חישוב הסכום הכולל למעלה)
                currentPnls.put(trade.getId(), pnl);

                // קריאה לפונקציה שמחשבת את הסכום הכולל ומודיעה לדשבורד
                calculateTotalOpenPnl();

                // חישוב אחוז הרווח/הפסד ביחס להשקעה
                double investment = trade.getEntryPrice() * trade.getQuantity(); // סך ההשקעה
                double percent = 0;
                if (investment != 0) {
                    percent = (pnl / investment) * 100; // נוסחת האחוזים
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
                // במקרה שגיאה לא מעדכנים את ה-P&L הכללי
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

    // פונקציה פרטית שמחשבת את סך כל הרווחים הפתוחים מכל השורות
    private void calculateTotalOpenPnl() {
        double total = 0; // אתחול הסכום לאפס
        // לולאה שעוברת על כל הרווחים שנשמרו במפה
        for (Double val : currentPnls.values()) {
            total += val; // הוספת הרווח של כל מניה לסכום הכולל
        }
        // אם יש מי שמקשיב לעדכון (הדשבורד), שולחים לו את הסכום החדש
        if (pnlListener != null) {
            pnlListener.onTotalPnlUpdated(total);
        }
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

        // בנאי ה-ViewHolder
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