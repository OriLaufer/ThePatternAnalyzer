package com.example.thepatternanalyzer;

// --- 1. ייבוא הספריות (Imports) ---
// אלו הכלים שאנחנו מביאים מאנדרואיד ומ-Firebase כדי לבנות את המסך ולנהל נתונים.
import android.graphics.Color; // מאפשר שימוש בצבעים (למשל ירוק לרווח, אדום להפסד)
import android.os.Bundle; // משמש להעברת מידע בין מסכים ושמירת מצב
import android.util.Log; // כלי לכתיבת הודעות לוג למפתחים (לצורך בדיקה ודיבוג)
import android.view.LayoutInflater; // "מנפח" את קובץ ה-XML והופך אותו לתצוגה חיה (View)
import android.view.View; // מייצג את המסך כולו או רכיב בתוכו
import android.view.ViewGroup; // המיכל שמחזיק את ה-Views
import android.widget.TextView; // רכיב להצגת טקסט על המסך
import android.widget.Toast; // הודעה קופצת קטנה למשתמש

import androidx.annotation.NonNull; // מוודא שמשתנה לא יהיה ריק (Null)
import androidx.annotation.Nullable; // מאפשר למשתנה להיות ריק
import androidx.fragment.app.Fragment; // המחלקה הבסיסית של מסך משני בתוך האפליקציה
import androidx.recyclerview.widget.LinearLayoutManager; // מסדר את הרשימה אחד מתחת לשני (טור)
import androidx.recyclerview.widget.RecyclerView; // הרכיב שמציג רשימה נגללת ויעילה

import com.google.firebase.auth.FirebaseAuth; // ניהול המשתמשים (מי מחובר?)
import com.google.firebase.firestore.DocumentSnapshot; // מייצג מסמך אחד מתוך המסד נתונים
import com.google.firebase.firestore.FirebaseFirestore; // החיבור הראשי למסד הנתונים בענן

import java.util.ArrayList; // סוג של רשימה חכמה ודינמית
import java.util.Calendar; // כלי לניהול תאריכים (נוסף לצורך חישוב חודשי)
import java.util.HashMap; // מבנה נתונים של מפתח-ערך (לחישוב סטטיסטיקות)
import java.util.List; // הממשק הכללי של רשימות
import java.util.Locale; // מאפשר לפרמט טקסט לפי אזור (למשל הוספת $ ונקודה עשרונית)
import java.util.Map; // הממשק הכללי למבנה נתונים מפתח-ערך

// --- המחלקה הראשית של הדשבורד ---
// מחלקה זו מנהלת את מסך הנתונים הראשי. היא יורשת מ-Fragment.
public class DashboardFragment extends Fragment {

    // --- 1. משתנים לרכיבי המסך (UI) ---

    // כותרות ראשיות (המספרים הגדולים בכרטיסים)
    private TextView tvProfitValue;      // הטקסט שמציג את הרווח הכולל (P&L)
    private TextView tvWinRateValue;     // הטקסט שמציג את אחוז ההצלחה (Win Rate)
    private TextView tvActiveValue;      // הטקסט שמציג את מספר העסקאות הפעילות (Active)
    private TextView tvTopPatternValue;  // הטקסט שמציג את התבנית המובילה (Top Pattern)

    // כותרות משניות (הטקסט הקטן בתחתית הכרטיסים)
    private TextView tvProfitSubtitle;     // טקסט משני לרווח (כעת יציג רווח חודשי)
    private TextView tvWinSubtitle;        // טקסט משני לאחוזי הצלחה (למשל: "X עסקאות סגורות")

    // הערה: משתנה זה הוא אופציונלי, תלוי אם הוספנו אותו ל-XML
    private TextView tvTopPatternSubtitle; // טקסט משני לתבנית (למשל: רווח ממוצע לתבנית)

    // הרשימה הנגללת שמציגה את העסקאות הפעילות ("Active Portfolio")
    private RecyclerView recyclerActiveTrades;

    // --- 2. משתנים לניהול הנתונים והלוגיקה ---

    private FirebaseFirestore db;   // האובייקט שמקשר אותנו למסד הנתונים Firestore
    private FirebaseAuth auth;      // האובייקט שמנהל את האימות והמשתמש הנוכחי
    private DashboardTradesAdapter activeTradesAdapter; // ה"מנהל" שמחבר בין רשימת העסקאות לתצוגה ב-RecyclerView
    private List<Trade> activeTradeList; // רשימה בזיכרון שתחזיק רק את העסקאות הפתוחות (עבור התצוגה למטה)

    // בנאי ריק (Constructor) - חובה באנדרואיד כדי שהאפליקציה תוכל ליצור את המסך מחדש
    public DashboardFragment() {
        // בנאי ריק חובה
    }

    // --- 3. יצירת המראה (onCreateView) ---
    // פונקציה זו נקראת כשהמערכת צריכה לצייר את המסך בפעם הראשונה.
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // אנחנו "מנפחים" (Inflate) את קובץ ה-XML (העיצוב) והופכים אותו לאובייקטי Java
        return inflater.inflate(R.layout.fragment_dashboard, container, false);
    }

    // --- 4. הפונקציה הראשית (onViewCreated) ---
    // פונקציה זו נקראת מיד אחרי שהמסך נוצר ויש לנו גישה לרכיבים הגרפיים.
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // א. אתחול הכלים של Firebase (כדי שנוכל לגשת לנתונים)
        db = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();

        // ב. חיבור המשתנים שלנו לרכיבים ב-XML לפי ה-ID שלהם
        // אנחנו מוצאים כל TextView ו-RecyclerView לפי השם שנתנו לו בקובץ העיצוב
        tvProfitValue = view.findViewById(R.id.tvProfitValue);
        tvWinRateValue = view.findViewById(R.id.tvWinRateValue);
        tvActiveValue = view.findViewById(R.id.tvActiveValue);
        tvTopPatternValue = view.findViewById(R.id.tvTopPatternValue);

        tvProfitSubtitle = view.findViewById(R.id.tvProfitSubtitle);
        tvWinSubtitle = view.findViewById(R.id.tvWinSubtitle);

        // מנסים למצוא את כותרת המשנה לתבנית (אם היא קיימת בעיצוב)
        tvTopPatternSubtitle = view.findViewById(R.id.tvTopPatternSubtitle);

        recyclerActiveTrades = view.findViewById(R.id.recyclerActiveTrades);

        // ג. איפוס התצוגה - מחיקת המספרים המזויפים מהעיצוב ($1250)
        resetUI();

        // ד. הגדרת הרשימה (הכנת ה-Adapter וה-LayoutManager)
        setupRecyclerView();

        // ה. הפעלת ההאזנה לנתונים (ברגע שזה רץ, הנתונים יתחילו לזרום ולהתעדכן)
        listenToData();
    }

    // --- פונקציה לאיפוס התצוגה ההתחלתית ---
    // פונקציה זו דואגת שלא נראה נתונים לא נכונים (מה-XML) בזמן שהאפליקציה טוענת
    private void resetUI() {
        if (tvProfitValue != null) tvProfitValue.setText("$0.00");
        if (tvWinRateValue != null) tvWinRateValue.setText("0%");
        if (tvActiveValue != null) tvActiveValue.setText("0");
        if (tvTopPatternValue != null) tvTopPatternValue.setText("-");

        // איפוס הכותרות הקטנות
        if (tvProfitSubtitle != null) tvProfitSubtitle.setText("Loading data...");
        if (tvWinSubtitle != null) tvWinSubtitle.setText("-");
        if (tvTopPatternSubtitle != null) tvTopPatternSubtitle.setText("");
    }

    // --- פונקציה להכנת רשימת העסקאות הפעילות ---
    private void setupRecyclerView() {
        // יצירת רשימה ריקה בזיכרון שתכיל את הנתונים
        activeTradeList = new ArrayList<>();

        // יצירת המתאם (Adapter). אנחנו מעבירים לו את הרשימה, וגם פונקציה ("Callback")
        // שתקרה כשמישהו ילחץ על כפתור "Close Position" בשורה כלשהי.
        activeTradesAdapter = new DashboardTradesAdapter(activeTradeList, trade -> closePosition(trade), totalOpenPnl -> updateOpenPnlCard(totalOpenPnl));

        // מגדירים איך הרשימה תיראה (רשימה אנכית פשוטה)
        recyclerActiveTrades.setLayoutManager(new LinearLayoutManager(getContext()));

        // מחברים את המתאם לרשימה הוויזואלית במסך
        recyclerActiveTrades.setAdapter(activeTradesAdapter);
    }

    // --- פונקציה לעדכון כרטיס הרווח הפתוח (Open P&L) ---
    private void updateOpenPnlCard(double totalOpenPnl) {
        if (tvProfitValue == null) return;

        // עדכון המספר הגדול עם הרווח הפתוח המחושב
        tvProfitValue.setText(String.format(Locale.US, "$%.2f", totalOpenPnl));

        // צביעה בירוק אם חיובי או אדום אם שלילי
        if (totalOpenPnl >= 0) {
            tvProfitValue.setTextColor(Color.parseColor("#10B981")); // ירוק
            tvProfitValue.setText("+$" + String.format(Locale.US, "%.2f", totalOpenPnl));
        } else {
            tvProfitValue.setTextColor(Color.parseColor("#EF4444")); // אדום
            tvProfitValue.setText("-$" + String.format(Locale.US, "%.2f", Math.abs(totalOpenPnl)));
        }
    }

    // --- עדכון קריטי: סגירת עסקה עם מחיר אמת! ---
    private void closePosition(Trade trade) {
        // בדיקת אבטחה: אם המשתמש לא מחובר, לא עושים כלום
        if (auth.getCurrentUser() == null) return;

        // מודיעים למשתמש שאנחנו בודקים את המחיר בשוק
        Toast.makeText(getContext(), "Fetching market price for " + trade.getTicker() + "...", Toast.LENGTH_SHORT).show();

        // 1. קריאה ל-API (NetworkManager) כדי לקבל את מחיר היציאה האמיתי בזמן אמת
        NetworkManager.getInstance().getStockPrice(trade.getTicker(), new NetworkManager.StockCallback() {

            // מה קורה כשהנתונים חוזרים מהשרת בהצלחה?
            @Override
            public void onSuccess(double currentPrice, double changePercent) {
                // 2. קיבלנו מחיר! עכשיו מעדכנים את העסקה במסד הנתונים (Firebase)
                // אנחנו ניגשים למסמך הספציפי של העסקה הזו
                db.collection("users").document(auth.getCurrentUser().getUid())
                        .collection("trades").document(trade.getId())
                        .update("status", "CLOSED", "exitPrice", currentPrice) // משנים סטטוס לסגור ושומרים את מחיר המכירה
                        .addOnSuccessListener(aVoid -> Toast.makeText(getContext(), "Sold at $" + currentPrice, Toast.LENGTH_SHORT).show()) // הצלחה
                        .addOnFailureListener(e -> Toast.makeText(getContext(), "Error closing: " + e.getMessage(), Toast.LENGTH_SHORT).show()); // כישלון
            }

            // מה קורה אם הייתה שגיאה בקבלת המחיר (למשל אין אינטרנט)?
            @Override
            public void onError(String error) {
                Toast.makeText(getContext(), "Failed to get price. Check internet.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    // --- פונקציה להאזנה לנתונים בזמן אמת מ-Firebase ---
    private void listenToData() {
        // בדיקת אבטחה: האם יש משתמש מחובר?
        if (auth.getCurrentUser() == null) return;
        String userId = auth.getCurrentUser().getUid();

        // אנחנו שמים "מאזין" (Listener) על תיקיית ה-trades של המשתמש.
        // הפונקציה בתוך addSnapshotListener תרוץ בכל פעם שמשהו משתנה בנתונים!
        db.collection("users").document(userId).collection("trades")
                .addSnapshotListener((value, error) -> {
                    if (error != null) return; // אם יש שגיאה בחיבור, יוצאים

                    if (value != null) {
                        // יצירת רשימות זמניות לעבודה
                        List<Trade> allTrades = new ArrayList<>(); // כל העסקאות (לחישובים)
                        activeTradeList.clear(); // מנקים את הרשימה הפעילה לפני מילוי מחדש

                        // לולאה שעוברת על כל המסמכים שהגיעו מהענן
                        for (DocumentSnapshot doc : value.getDocuments()) {
                            // הופכים את המסמך הגולמי לאובייקט Trade שלנו
                            Trade t = doc.toObject(Trade.class);
                            if (t != null) {
                                t.setId(doc.getId()); // שומרים את ה-ID של המסמך (חשוב לעדכון/מחיקה)
                                allTrades.add(t); // מוסיפים לרשימה הכללית

                                // אם הסטטוס הוא "OPEN", מוסיפים גם לרשימה שמוצגת למטה בדשבורד
                                if ("OPEN".equalsIgnoreCase(t.getStatus())) {
                                    activeTradeList.add(t);
                                }
                            }
                        }

                        // מודיעים למתאם שהרשימה השתנתה כדי שיעדכן את התצוגה
                        activeTradesAdapter.notifyDataSetChanged();

                        // שולחים את כל העסקאות לפונקציית החישוב שתעדכן את הכרטיסים למעלה
                        calculateAndDisplayData(allTrades);
                    }
                });
    }

    // --- המוח המתמטי: חישוב הנתונים לכרטיסים ---
    private void calculateAndDisplayData(List<Trade> trades) {
        // משתנים לצבירת נתונים
        double monthlyRealizedProfit = 0; // סך הרווח/הפסד הממומש לחודש הנוכחי
        int closedTradesCount = 0;   // מספר העסקאות הסגורות בלבד
        int winningTrades = 0;       // מספר העסקאות המרוויחות
        int activeTradesCount = 0;   // מספר העסקאות הפעילות כרגע

        // קבלת הזמן הנוכחי כדי לחשב חודש נוכחי
        Calendar currentCal = Calendar.getInstance();
        int currentMonth = currentCal.get(Calendar.MONTH);
        int currentYear = currentCal.get(Calendar.YEAR);

        // מפות עזר לחישוב התבניות
        Map<String, Integer> patternCounts = new HashMap<>(); // כמה פעמים הופיעה כל תבנית
        Map<String, Double> patternProfits = new HashMap<>(); // כמה רווח עשתה כל תבנית

        // לולאה שעוברת על כל הטריידים אחד-אחד ומחשבת
        for (Trade trade : trades) {
            // 1. אם העסקה פתוחה -> מעלים את מונה הפעילים
            if ("OPEN".equalsIgnoreCase(trade.getStatus())) {
                activeTradesCount++;
            }
            // 2. אם העסקה סגורה -> מחשבים סטטיסטיקות
            else if ("CLOSED".equalsIgnoreCase(trade.getStatus())) {
                closedTradesCount++; // ספרנו עסקה סגורה

                // חישוב הרווח לעסקה: (מחיר יציאה - מחיר כניסה) * כמות המניות
                double tradeProfit = (trade.getExitPrice() - trade.getEntryPrice()) * trade.getQuantity();

                // בדיקה אם העסקה התבצעה בחודש הנוכחי (לצורך חישוב רווח חודשי)
                Calendar tradeCal = Calendar.getInstance();
                tradeCal.setTimeInMillis(trade.getTimestamp());
                if (tradeCal.get(Calendar.MONTH) == currentMonth && tradeCal.get(Calendar.YEAR) == currentYear) {
                    monthlyRealizedProfit += tradeProfit; // מוסיפים לסכום החודשי
                }

                // אם הרווח גדול מ-0 -> זו עסקה מנצחת
                if (tradeProfit > 0) winningTrades++;

                // איסוף נתונים על התבנית (לצורך חישוב תבנית מובילה)
                String pattern = trade.getPattern();
                if (pattern != null && !pattern.isEmpty()) {
                    // מוסיפים את הרווח של העסקה הזו לסך הרווחים של התבנית
                    patternProfits.put(pattern, patternProfits.getOrDefault(pattern, 0.0) + tradeProfit);
                }
            }

            // ספירת כמות השימוש בתבנית (גם בפתוחות וגם בסגורות)
            String pattern = trade.getPattern();
            if (pattern != null && !pattern.isEmpty()) {
                patternCounts.put(pattern, patternCounts.getOrDefault(pattern, 0) + 1);
            }
        }

        // --- מציאת התבנית המובילה (זו שהופיעה הכי הרבה פעמים) ---
        String topPattern = "N/A"; // ברירת מחדל
        int maxCount = 0;

        // עוברים על המפה ובודקים למי יש הכי הרבה שימושים
        for (Map.Entry<String, Integer> entry : patternCounts.entrySet()) {
            if (entry.getValue() > maxCount) {
                maxCount = entry.getValue();
                topPattern = entry.getKey();
            }
        }

        // חישוב הרווח הממוצע לתבנית המובילה (רק אם מצאנו כזו)
        double topPatternAvgProfit = 0;
        if (!topPattern.equals("N/A") && patternCounts.containsKey(topPattern)) {
            double totalProfitForPattern = patternProfits.getOrDefault(topPattern, 0.0);
            int countForPattern = patternCounts.get(topPattern);

            // ממוצע = סך הרווח מהתבנית / מספר הפעמים שהשתמשנו בה
            if (countForPattern > 0) {
                topPatternAvgProfit = totalProfitForPattern / countForPattern;
            }
        }

        // --- חישוב אחוז ההצלחה (Win Rate) ---
        // נוסחה: (מספר הנצחונות / סך העסקאות הסגורות) * 100
        int winRate = (closedTradesCount > 0) ? (int) (((double) winningTrades / closedTradesCount) * 100) : 0;

        // ============================
        // עדכון המסך (UI) - הצגת התוצאות למשתמש
        // ============================

        // 1. עדכון כרטיס ACTIVE
        if (tvActiveValue != null) tvActiveValue.setText(String.valueOf(activeTradesCount));

        // 2. עדכון כרטיס TOP PATTERN
        if (tvTopPatternValue != null) tvTopPatternValue.setText(topPattern);

        // עדכון כותרת המשנה של התבנית (רווח ממוצע)
        if (tvTopPatternSubtitle != null) {
            if (topPattern.equals("N/A")) {
                tvTopPatternSubtitle.setText("No trades yet");
            } else {
                String avgText = String.format(Locale.US, "$%.2f Avg.", topPatternAvgProfit);
                // הוספת פלוס אם חיובי
                if (topPatternAvgProfit > 0) avgText = "+" + avgText;
                tvTopPatternSubtitle.setText(avgText);

                // צביעה בירוק או אדום לפי הרווח
                if (topPatternAvgProfit >= 0) tvTopPatternSubtitle.setTextColor(Color.parseColor("#10B981"));
                else tvTopPatternSubtitle.setTextColor(Color.parseColor("#EF4444"));
            }
        }

        // 3. עדכון כרטיס WIN RATE
        if (tvWinRateValue != null) tvWinRateValue.setText(winRate + "%");

        // עדכון כותרת המשנה (כמות עסקאות סגורות)
        if (tvWinSubtitle != null) tvWinSubtitle.setText(closedTradesCount + " total trades");

        // 4. עדכון כרטיס TOTAL P&L (רווח והפסד)
        // הערה: המספר הגדול (Open P&L) מתעדכן בנפרד בפונקציה updateOpenPnlCard בזמן אמת

        // עדכון כותרת המשנה של הרווח - מציג רווח/הפסד חודשי ממומש (Realized)
        if (tvProfitSubtitle != null) {
            String monthlyText = String.format(Locale.US, "$%.2f this month", Math.abs(monthlyRealizedProfit));
            if (monthlyRealizedProfit >= 0) {
                monthlyText = "+ " + monthlyText;
                tvProfitSubtitle.setTextColor(Color.parseColor("#10B981")); // ירוק
            } else {
                monthlyText = "- " + monthlyText;
                tvProfitSubtitle.setTextColor(Color.parseColor("#EF4444")); // אדום
            }
            tvProfitSubtitle.setText(monthlyText);
        }
    }
}