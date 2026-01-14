package com.example.thepatternanalyzer;

// 1. ייבוא הספריות (הכלים) שאנחנו צריכים
import android.graphics.Color; // מאפשר לשנות צבעים (ירוק לרווח, אדום להפסד)
import android.os.Bundle;
import android.util.Log; // מאפשר לכתוב הודעות למערכת (לצורך בדיקה ודיבוג)
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView; // רכיב להצגת טקסט על המסך
import android.widget.Toast; // הודעה קופצת קטנה למשתמש

// כלים בסיסיים של אנדרואיד לניהול מסכים (Fragments)
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView; // הרכיב שמציג את הרשימה הנגללת

// הכלים של Firebase (הענן שלנו)
import com.google.firebase.auth.FirebaseAuth; // ניהול המשתמשים (מי מחובר?)
import com.google.firebase.firestore.DocumentSnapshot; // מייצג מסמך אחד מתוך המסד נתונים
import com.google.firebase.firestore.FirebaseFirestore; // החיבור הראשי למסד הנתונים

// כלים לניהול רשימות וחישובים מתמטיים
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale; // מאפשר לפרמט טקסט (למשל להוסיף $ ולדייק נקודה עשרונית)
import java.util.Map;

// המחלקה הראשית של הדשבורד (יורשת מ-Fragment כי זה חלק ממסך ראשי)
public class DashboardFragment extends Fragment {

    // --- הגדרת המשתנים (המגירות לרכיבי המסך) ---

    // כותרות ראשיות (המספרים הגדולים בכרטיסים)
    private TextView tvProfitValue;      // הטקסט של הרווח הכולל ($1250)
    private TextView tvWinRateValue;     // הטקסט של אחוזי ההצלחה (68%)
    private TextView tvActiveValue;      // הטקסט של עסקאות פעילות (3)
    private TextView tvTopPatternValue;  // הטקסט של התבנית המובילה (Momentum)

    // כותרות משניות (הטקסט הקטן למטה)
    private TextView tvProfitSubtitle;     // לדוגמה: "Based on 5 trades"
    private TextView tvWinSubtitle;        // לדוגמה: "5 total trades"
    private TextView tvTopPatternSubtitle; // לדוגמה: "$50.0 Avg"

    // הרשימה הנגללת של העסקאות הפעילות
    private RecyclerView recyclerActiveTrades;

    // משתנים לניהול הנתונים
    private FirebaseFirestore db;   // החיבור למסד הנתונים
    private FirebaseAuth auth;      // החיבור למשתמש
    private DashboardTradesAdapter activeTradesAdapter; // המתאם שמחבר בין הנתונים לרשימה הויזואלית
    private List<Trade> activeTradeList; // רשימה בזיכרון שתחזיק רק את העסקאות הפתוחות

    // בנאי ריק (חובה באנדרואיד כדי שהאפליקציה לא תקרוס)
    public DashboardFragment() {
    }

    // --- שלב 1: יצירת המראה (הציור) ---
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // "מנפח" (טוען) את קובץ ה-XML שיצרנו והופך אותו למסך אמיתי
        return inflater.inflate(R.layout.fragment_dashboard, container, false);
    }

    // --- שלב 2: המסך מוכן, מתחילים לעבוד ---
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // א. אתחול הכלים של Firebase
        db = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();

        // ב. חיבור המשתנים שלנו לרכיבים ב-XML לפי ה-ID שלהם
        tvProfitValue = view.findViewById(R.id.tvProfitValue);
        tvWinRateValue = view.findViewById(R.id.tvWinRateValue);
        tvActiveValue = view.findViewById(R.id.tvActiveValue);
        tvTopPatternValue = view.findViewById(R.id.tvTopPatternValue);

        tvProfitSubtitle = view.findViewById(R.id.tvProfitSubtitle);
        tvWinSubtitle = view.findViewById(R.id.tvWinSubtitle);
        tvTopPatternSubtitle = view.findViewById(R.id.tvTopPatternSubtitle);

        recyclerActiveTrades = view.findViewById(R.id.recyclerActiveTrades);

        // ג. הגדרת הרשימה (מכינים אותה לקבלת נתונים)
        setupRecyclerView();

        // ד. הפעלת ההאזנה לענן (ברגע שזה רץ, הנתונים יתחילו לזרום)
        listenToData();
    }

    // --- פונקציה להכנת הרשימה ---
    private void setupRecyclerView() {
        activeTradeList = new ArrayList<>(); // יוצרים רשימה ריקה בזיכרון

        // יוצרים את המתאם ומעבירים לו גם פונקציה שתקרה כשלוחצים "סגור" (closePosition)
        activeTradesAdapter = new DashboardTradesAdapter(activeTradeList, trade -> closePosition(trade));

        // מגדירים שהרשימה תהיה אנכית (אחד מתחת לשני)
        recyclerActiveTrades.setLayoutManager(new LinearLayoutManager(getContext()));
        // מחברים את המתאם לרכיב הויזואלי
        recyclerActiveTrades.setAdapter(activeTradesAdapter);
    }

    // --- פונקציה לסגירת עסקה (כשלוחצים על הכפתור) ---
    private void closePosition(Trade trade) {
        if (auth.getCurrentUser() == null) return; // בדיקת אבטחה: אם אין משתמש, לא עושים כלום

        // סימולציה: כרגע אין לנו מחיר שוק אמיתי, אז אנחנו ממציאים מחיר יציאה עם רווח קטן (5%)
        // בעתיד נחליף את זה בקריאה ל-API
        double dummyExitPrice = trade.getEntryPrice() * 1.05;

        // שולחים פקודת עדכון ל-Firebase
        db.collection("users").document(auth.getCurrentUser().getUid())
                .collection("trades").document(trade.getId()) // הולכים למסמך הספציפי
                .update("status", "CLOSED", "exitPrice", dummyExitPrice) // משנים סטטוס ומחיר יציאה
                .addOnSuccessListener(aVoid -> Toast.makeText(getContext(), "Position Closed!", Toast.LENGTH_SHORT).show()) // הצלחה
                .addOnFailureListener(e -> Toast.makeText(getContext(), "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show()); // כישלון
    }

    // --- פונקציה להאזנה לנתונים בזמן אמת ---
    private void listenToData() {
        if (auth.getCurrentUser() == null) return;
        String userId = auth.getCurrentUser().getUid();

        // שמים "מאזין" על התיקייה של הטריידים ב-Firebase
        db.collection("users").document(userId).collection("trades")
                .addSnapshotListener((value, error) -> {
                    if (error != null) return; // אם יש שגיאה בחיבור, עוצרים

                    if (value != null) {
                        // רשימה זמנית לכל הטריידים (גם פתוחים וגם סגורים) לצורך חישובים
                        List<Trade> allTrades = new ArrayList<>();

                        // מנקים את רשימת הפעילים כדי למלא מחדש
                        activeTradeList.clear();

                        // עוברים על כל המסמכים שהגיעו מהענן
                        for (DocumentSnapshot doc : value.getDocuments()) {
                            // הופכים את המסמך לאובייקט Trade
                            Trade t = doc.toObject(Trade.class);
                            if (t != null) {
                                t.setId(doc.getId()); // שומרים את ה-ID
                                allTrades.add(t); // מוסיפים לרשימה הכללית

                                // אם הסטטוס הוא "OPEN", מוסיפים גם לרשימת הפעילים שמוצגת למטה
                                if ("OPEN".equalsIgnoreCase(t.getStatus())) {
                                    activeTradeList.add(t);
                                }
                            }
                        }
                        // מעדכנים את הרשימה הויזואלית למטה
                        activeTradesAdapter.notifyDataSetChanged();

                        // שולחים את כל הנתונים לחישוב הסטטיסטיקות (לכרטיסים למעלה)
                        calculateAndDisplayData(allTrades);
                    }
                });
    }

    // --- המוח המתמטי: חישוב הנתונים לכרטיסים ---
    private void calculateAndDisplayData(List<Trade> trades) {
        double totalProfit = 0;      // סך כל הרווח
        int closedTradesCount = 0;   // כמה עסקאות סגרנו סה"כ
        int winningTrades = 0;       // כמה מהן היו רווחיות
        int activeTradesCount = 0;   // כמה עסקאות פתוחות כרגע

        // מפות עזר לחישובים מתקדמים
        Map<String, Integer> patternCounts = new HashMap<>(); // איזה תבנית מופיעה הכי הרבה?
        Map<String, Double> patternProfits = new HashMap<>(); // כמה כסף כל תבנית עשתה?

        // לולאה שעוברת על כל הטריידים אחד-אחד ומחשבת
        for (Trade trade : trades) {
            // 1. אם העסקה פתוחה -> מעלים את מונה הפעילים
            if ("OPEN".equalsIgnoreCase(trade.getStatus())) {
                activeTradesCount++;
            }
            // 2. אם העסקה סגורה -> מחשבים רווחים
            else if ("CLOSED".equalsIgnoreCase(trade.getStatus())) {
                closedTradesCount++; // ספרנו עסקה סגורה

                // חישוב הרווח: (מחיר יציאה פחות מחיר כניסה) כפול כמות המניות
                double tradeProfit = (trade.getExitPrice() - trade.getEntryPrice()) * trade.getQuantity();
                totalProfit += tradeProfit; // מוסיפים לסכום הכולל

                // אם הרווח חיובי -> ספרנו ניצחון
                if (tradeProfit > 0) winningTrades++;

                // שומרים נתונים לחישוב התבנית
                String pattern = trade.getPattern();
                if (pattern != null && !pattern.isEmpty()) {
                    // מוסיפים את הרווח של העסקה הזו לסך הרווחים של התבנית הספציפית
                    patternProfits.put(pattern, patternProfits.getOrDefault(pattern, 0.0) + tradeProfit);
                }
            }

            // ספירת שימוש בתבנית (כדי לדעת מי הכי פופולרית)
            String pattern = trade.getPattern();
            if (pattern != null && !pattern.isEmpty()) {
                patternCounts.put(pattern, patternCounts.getOrDefault(pattern, 0) + 1);
            }
        }

        // --- מציאת התבנית המובילה ---
        String topPattern = "N/A"; // ברירת מחדל
        int maxCount = 0;

        // עוברים על כל התבניות ומחפשים למי יש הכי הרבה שימושים
        for (Map.Entry<String, Integer> entry : patternCounts.entrySet()) {
            if (entry.getValue() > maxCount) {
                maxCount = entry.getValue();
                topPattern = entry.getKey();
            }
        }

        // חישוב הרווח הממוצע של התבנית המנצחת
        double topPatternAvgProfit = 0;
        if (!topPattern.equals("N/A") && patternCounts.containsKey(topPattern)) {
            double totalProfitForPattern = patternProfits.getOrDefault(topPattern, 0.0);
            int countForPattern = patternCounts.get(topPattern);
            if (countForPattern > 0) {
                topPatternAvgProfit = totalProfitForPattern / countForPattern;
            }
        }

        // --- חישוב אחוז ההצלחה (Win Rate) ---
        // נוסחה: (מספר הנצחונות חלקי סך העסקאות הסגורות) כפול 100
        int winRate = (closedTradesCount > 0) ? (int) (((double) winningTrades / closedTradesCount) * 100) : 0;

        // --- עדכון המסך (הצגת התוצאות למשתמש) ---

        // 1. Active (מספר עסקאות פתוחות)
        if (tvActiveValue != null) tvActiveValue.setText(String.valueOf(activeTradesCount));

        // 2. Top Pattern (התבנית המובילה + רווח ממוצע)
        if (tvTopPatternValue != null) tvTopPatternValue.setText(topPattern);

        if (tvTopPatternSubtitle != null) {
            if (topPattern.equals("N/A")) {
                tvTopPatternSubtitle.setText("No trades yet");
            } else {
                // מציג את הממוצע בדולרים
                String avgText = String.format(Locale.US, "$%.2f Avg.", topPatternAvgProfit);
                if (topPatternAvgProfit > 0) avgText = "+" + avgText; // מוסיף פלוס אם חיובי
                tvTopPatternSubtitle.setText(avgText);

                // צובע בירוק או אדום
                if (topPatternAvgProfit >= 0) tvTopPatternSubtitle.setTextColor(Color.parseColor("#10B981"));
                else tvTopPatternSubtitle.setTextColor(Color.parseColor("#EF4444"));
            }
        }

        // 3. Win Rate (אחוזים + פירוט)
        if (tvWinRateValue != null) tvWinRateValue.setText(winRate + "%");

        if (tvWinSubtitle != null) {
            // מציג רק את כמות העסקאות הסגורות, כמו שביקשת
            tvWinSubtitle.setText(closedTradesCount + " total trades");
        }

        // 4. Profit (רווח כולל + צבעים)
        if (tvProfitValue != null) {
            if (totalProfit >= 0) {
                tvProfitValue.setTextColor(Color.parseColor("#10B981")); // ירוק
                tvProfitValue.setText("+$" + String.format(Locale.US, "%.2f", totalProfit));
            } else {
                tvProfitValue.setTextColor(Color.parseColor("#EF4444")); // אדום
                tvProfitValue.setText("-$" + String.format(Locale.US, "%.2f", Math.abs(totalProfit)));
            }
        }

        if (tvProfitSubtitle != null) {
            tvProfitSubtitle.setText("Based on " + closedTradesCount + " closed trades");
        }
    }
}