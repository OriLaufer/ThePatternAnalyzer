package com.example.thepatternanalyzer;

// ייבוא הספריות הנחוצות לעבודה עם רכיבי UI, צבעים ורשימות
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;
import java.util.Locale;

// המחלקה הראשית של המתאם (Adapter)
// תפקידו: לקחת רשימה של מניות (נתונים) ולהציג אותן בתוך ה-RecyclerView (תצוגה)
public class SpotlightAdapter extends RecyclerView.Adapter<SpotlightAdapter.ViewHolder> {

    // --- מחלקה פנימית: מודל נתונים לתוצאת סריקה ---
    // מחלקה זו מגדירה אילו נתונים אנחנו שומרים עבור כל מניה שנמצאה בסריקה
    public static class StockResult {
        public String symbol;       // סימול המניה (למשל: AAPL)
        public double price;        // מחיר נוכחי
        public double changePercent; // אחוז השינוי היומי

        // בנאי (Constructor) ליצירת אובייקט חדש
        public StockResult(String symbol, double price, double changePercent) {
            this.symbol = symbol;
            this.price = price;
            this.changePercent = changePercent;
        }
    }

    // --- משתני המחלקה ---
    private List<StockResult> stockList; // הרשימה שמחזיקה את כל המניות שנמצאו
    private OnItemClickListener listener; // המאזין ללחיצות (כדי שנוכל להגיב כשלוחצים על הפלוס)

    // --- ממשק (Interface) ללחיצות ---
    // מגדיר "חוזה" שמאפשר למסך הראשי (SpotlightFragment) לדעת מתי לחצו על "הוסף"
    public interface OnItemClickListener {
        void onAddClick(StockResult stock);
    }

    // --- בנאי של המתאם (Constructor) ---
    // מקבל את רשימת המניות ואת המאזין ללחיצות מהפרגמנט
    public SpotlightAdapter(List<StockResult> stockList, OnItemClickListener listener) {
        this.stockList = stockList;
        this.listener = listener;
    }

    // --- יצירת המראה של שורה חדשה ---
    // פונקציה זו נקראת כשהרשימה צריכה ליצור "קופסה" חדשה לשורה (ViewHolder)
    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // טעינת קובץ העיצוב XML של שורה בודדת (item_spotlight_result.xml)
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_spotlight_result, parent, false);
        return new ViewHolder(view);
    }

    // --- מילוי הנתונים בתוך השורה ---
    // פונקציה זו נקראת עבור כל שורה ברשימה כדי להכניס את המידע הנכון
    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        // 1. שליפת המניה הנוכחית לפי המיקום ברשימה
        StockResult stock = stockList.get(position);

        // 2. הצגת הלוגו ושם המניה
        holder.tvLogo.setText(stock.symbol); // שם קצר בלוגו
        holder.tvTicker.setText("$" + stock.symbol); // שם מלא עם $

        // 3. הצגת המחיר (מפורמט עם $ ושתי ספרות אחרי הנקודה)
        holder.tvPrice.setText(String.format(Locale.US, "$%.2f", stock.price));

        // 4. הצגת אחוז השינוי וצביעה לפי רווח/הפסד
        if (stock.changePercent >= 0) {
            // אם השינוי חיובי (עלה):
            holder.tvChangePercent.setText("+" + String.format(Locale.US, "%.2f%%", stock.changePercent));
            holder.tvChangePercent.setTextColor(Color.parseColor("#10B981")); // צבע ירוק
        } else {
            // אם השינוי שלילי (ירד):
            holder.tvChangePercent.setText(String.format(Locale.US, "%.2f%%", stock.changePercent));
            holder.tvChangePercent.setTextColor(Color.parseColor("#EF4444")); // צבע אדום
        }

        // 5. הגדרת הפעולה כשלוחצים על כפתור הפלוס
        holder.btnAdd.setOnClickListener(v -> {
            if (listener != null) {
                // קריאה לפונקציה בפרגמנט שמוסיפה את המניה ליומן
                listener.onAddClick(stock);
            }
        });
    }

    // --- החזרת מספר הפריטים ברשימה ---
    // אומר ל-RecyclerView כמה שורות הוא צריך לצייר
    @Override
    public int getItemCount() {
        return stockList.size();
    }

    // --- המחלקה שמחזיקה את הרכיבים הויזואליים של שורה אחת (ViewHolder) ---
    // תפקידה: למצוא את כל ה-Views ב-XML פעם אחת ולשמור אותם לשימוש חוזר (יעילות)
    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvLogo, tvTicker, tvChangePercent, tvPrice;
        ImageView btnAdd;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            // חיבור לרכיבים בקובץ item_spotlight_result.xml
            tvLogo = itemView.findViewById(R.id.tvLogo);
            tvTicker = itemView.findViewById(R.id.tvTicker);
            tvChangePercent = itemView.findViewById(R.id.tvChangePercent);
            tvPrice = itemView.findViewById(R.id.tvPrice);
            btnAdd = itemView.findViewById(R.id.btnAdd);
        }
    }
}