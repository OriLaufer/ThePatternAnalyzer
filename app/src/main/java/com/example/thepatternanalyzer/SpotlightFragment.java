package com.example.thepatternanalyzer;

// --- 1. ייבוא הספריות (Imports) ---
// אלו הכלים שאנחנו מביאים מאנדרואיד כדי לבנות את המסך
import android.graphics.Color; // מאפשר שימוש בצבעים (כמו ירוק לרווח)
import android.os.Bundle; // משמש להעברת מידע בין מסכים
import android.view.LayoutInflater; // הופך את קובץ ה-XML לתצוגה חיה
import android.view.View; // מייצג את רכיבי המסך
import android.view.ViewGroup; // המיכל שמחזיק את הרכיבים
import android.widget.AdapterView; // מזהה לחיצות ברשימות (כמו ב-Spinner)
import android.widget.ArrayAdapter; // מחבר בין רשימת נתונים לתצוגה
import android.widget.Button; // רכיב כפתור
import android.widget.EditText; // שדה קלט טקסט
import android.widget.Spinner; // רכיב רשימה נפתחת
import android.widget.TextView; // רכיב להצגת טקסט
import android.widget.Toast; // הודעה קופצת זמנית למשתמש

import androidx.annotation.NonNull; // מוודא שפרמטר לא יהיה ריק (Null)
import androidx.annotation.Nullable; // מאפשר לפרמטר להיות ריק
import androidx.fragment.app.Fragment; // המחלקה הבסיסית של מסך משני
import androidx.recyclerview.widget.LinearLayoutManager; // מסדר את הרשימה בטור
import androidx.recyclerview.widget.RecyclerView; // הרכיב שמציג רשימה נגללת

import java.util.ArrayList; // סוג של רשימה דינמית
import java.util.List; // הממשק הכללי של רשימות

// --- המחלקה הראשית של מסך ה"זרקור" (Spotlight) ---
// יורשת מ-Fragment כי היא חלק מהאפליקציה הראשית
public class SpotlightFragment extends Fragment {

    // --- 2. משתנים לרכיבי המסך (ה"מגירות" לנתונים) ---
    // כאן אנחנו מכריזים על המשתנים שיחזיקו את האלמנטים מה-XML
    private EditText etStrategyName; // שדה להזנת שם האסטרטגיה
    private EditText etMinPrice;     // שדה להזנת מחיר מינימום
    private EditText etMaxPrice;     // שדה להזנת מחיר מקסימום
    private Spinner spinnerPattern;  // הרשימה הנפתחת לבחירת תבנית
    private TextView tvSpinnerHint;  // הטקסט "Select a pattern..." שנעלם בבחירה
    private Button btnAiValidate;    // כפתור ה-AI (אימות אסטרטגיה)
    private Button btnRunScanner;    // כפתור הסריקה (מפעיל את החיפוש)
    private RecyclerView recyclerResults; // הרשימה התחתונה שתציג את התוצאות

    // --- 3. משתנים לניהול הנתונים ---
    private SpotlightAdapter adapter; // ה"מנהל" שמחבר בין הנתונים לתצוגה ברשימה
    private List<SpotlightAdapter.StockResult> resultsList; // רשימה בזיכרון שתחזיק את המניות שנמצאו

    // בנאי ריק (חובה באנדרואיד כדי למנוע קריסות בשינוי מסך)
    public SpotlightFragment() {
    }

    // --- 4. יצירת המראה (טעינת ה-XML) ---
    // הפונקציה הזו הופכת את קובץ העיצוב XML לאובייקטים של Java
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // טוען את הקובץ fragment_spotlight.xml ומחזיר אותו לתצוגה
        return inflater.inflate(R.layout.fragment_spotlight, container, false);
    }

    // --- 5. הפונקציה הראשית שרצה כשהמסך מוכן ---
    // כאן מתבצע כל האתחול והחיבורים הלוגיים
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // א. חיבור המשתנים לרכיבים ב-XML (לפי ה-ID שנתנו להם)
        // findViewById מחפש את הרכיב בקובץ העיצוב לפי השם שלו
        etStrategyName = view.findViewById(R.id.etStrategyName); // חיבור לשדה השם
        etMinPrice = view.findViewById(R.id.etMinPrice); // שים לב: ב-XML השם הוא etMin
        etMaxPrice = view.findViewById(R.id.etMaxPrice); // שים לב: ב-XML השם הוא etMax
        spinnerPattern = view.findViewById(R.id.spinnerPattern); // חיבור ל-Spinner
        tvSpinnerHint = view.findViewById(R.id.tvSpinnerHint); // חיבור לטקסט הרמז

        btnAiValidate = view.findViewById(R.id.btnAiValidate); // חיבור לכפתור ה-AI
        btnRunScanner = view.findViewById(R.id.btnRunScanner); // שים לב: ב-XML השם הוא btnRun

        // חיבור לרשימה הנגללת (חובה שיהיה לה ID כזה ב-XML)
        recyclerResults = view.findViewById(R.id.recyclerResults);

        // ב. הפעלת הרכיבים השונים
        setupSpinner(); // קריאה לפונקציה שמסדרת את הרשימה הנפתחת
        setupRecyclerView(); // קריאה לפונקציה שמכינה את הרשימה התחתונה

        // ג. הגדרת הפעולות לכפתורים (מה קורה כשלוחצים)
        // כשלוחצים על "Run Scanner", תופעל הפונקציה runScannerLogic
        btnRunScanner.setOnClickListener(v -> runScannerLogic());
        // כשלוחצים על "AI Validate", תופעל הפונקציה runAiValidation
        btnAiValidate.setOnClickListener(v -> runAiValidation());
    }

    // --- פונקציה להכנת הרשימה התחתונה (RecyclerView) ---
    private void setupRecyclerView() {
        resultsList = new ArrayList<>(); // יצירת רשימה חדשה וריקה בזיכרון

        // יצירת המתאם (Adapter) והעברת הרשימה אליו
        // אנחנו מעבירים לו גם "פעולה" (Lambda) שתקרה כשלוחצים על הפלוס (+)
        adapter = new SpotlightAdapter(resultsList, stock -> {
            // הקוד הזה ירוץ כשמשתמש לוחץ על כפתור ההוספה בשורה
            Toast.makeText(getContext(), "Added " + stock.symbol + " to Watchlist", Toast.LENGTH_SHORT).show();
        });

        // בדיקה שהרשימה קיימת (למניעת קריסה אם ה-ID לא נמצא)
        if (recyclerResults != null) {
            // מגדירים שהרשימה תהיה אנכית (Linear)
            recyclerResults.setLayoutManager(new LinearLayoutManager(getContext()));
            // מחברים את המתאם לרכיב הויזואלי
            recyclerResults.setAdapter(adapter);
        }
    }

    // --- פונקציה להגדרת התיבה הנפתחת (Spinner) ---
    private void setupSpinner() {
        // יצירת רשימת האפשרויות לתבניות
        List<String> patterns = new ArrayList<>();
        patterns.add(""); // פריט ראשון ריק (כדי שיראו את ה-Hint מאחורה)
        patterns.add("Momentum");
        patterns.add("Bull Flag");
        patterns.add("Gap & Go");
        patterns.add("Reversal");

        // יצירת מתאם מיוחד ל-Spinner כדי לשלוט בצבעי הטקסט
        ArrayAdapter<String> spinnerAdapter = new ArrayAdapter<String>(requireContext(), android.R.layout.simple_spinner_item, patterns) {

            // פונקציה הקובעת איך נראה הפריט כשהתיבה סגורה
            @NonNull
            @Override
            public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
                View view = super.getView(position, convertView, parent);
                TextView tv = (TextView) view;
                tv.setTextColor(Color.WHITE); // טקסט לבן (כי הרקע כהה)
                tv.setTextSize(14); // גודל טקסט
                return view;
            }

            // פונקציה הקובעת איך נראית הרשימה כשהיא נפתחת
            @Override
            public View getDropDownView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
                View view = super.getDropDownView(position, convertView, parent);
                TextView tv = (TextView) view;
                if (position == 0) {
                    tv.setHeight(0); // מסתירים את הפריט הריק הראשון
                } else {
                    tv.setTextColor(Color.BLACK); // טקסט שחור (כי הרקע לבן בברירת מחדל)
                }
                return view;
            }
        };

        // הגדרת המרווחים ברשימה
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        // חיבור המתאם ל-Spinner
        spinnerPattern.setAdapter(spinnerAdapter);

        // הוספת מאזין לבחירה - כדי לטפל בטקסט הרמז (Hint)
        spinnerPattern.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                // אם נבחר הפריט הראשון (הריק), נציג את ה-Hint
                if (position == 0) {
                    tvSpinnerHint.setVisibility(View.VISIBLE);
                } else {
                    // אחרת, נסתיר את ה-Hint
                    tvSpinnerHint.setVisibility(View.INVISIBLE);
                }
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                tvSpinnerHint.setVisibility(View.VISIBLE);
            }
        });
    }

    // --- הלוגיקה של כפתור "Run Scanner" ---
    // פונקציה זו מופעלת כשהמשתמש לוחץ על הכפתור הסגול לסריקה
    private void runScannerLogic() {
        // 1. קריאת הטקסט שהמשתמש הזין בשדה האסטרטגיה
        String strategy = etStrategyName.getText().toString().trim();

        // 2. בדיקת תקינות: האם השדה ריק?
        if (strategy.isEmpty()) {
            etStrategyName.setError("Name required"); // הצגת שגיאה בשדה
            return; // עצירת הפונקציה
        }

        // 3. הצגת הודעה למשתמש שהתהליך התחיל
        Toast.makeText(getContext(), "Searching market for: " + strategy, Toast.LENGTH_SHORT).show();

        // 4. ניקוי רשימת התוצאות הקודמת ורענון המסך
        resultsList.clear();
        if (adapter != null) adapter.notifyDataSetChanged();

        // 5. רשימת מניות לחיפוש (כרגע קבועה, בעתיד תגיע מה-AI)
        String[] stocksToSearch = {"NVDA", "AMD", "TSLA", "PLTR", "SOFI"};

        // 6. לולאה שעוברת על כל מניה ושולחת בקשה ל-API
        for (String symbol : stocksToSearch) {
            // קריאה למנהל הרשת (NetworkManager) לבקשת מחיר
            NetworkManager.getInstance().getStockPrice(symbol, new NetworkManager.StockCallback() {

                // מה קורה כשהנתונים חוזרים בהצלחה?
                @Override
                public void onSuccess(double price, double changePercent) {
                    // מוסיפים את המניה עם המחיר האמיתי לרשימה
                    resultsList.add(new SpotlightAdapter.StockResult(symbol, price, changePercent));

                    // מעדכנים את המתאם כדי שיצייר את השורה החדשה במסך
                    if (adapter != null) adapter.notifyDataSetChanged();
                }

                // מה קורה אם הייתה שגיאה?
                @Override
                public void onError(String error) {
                    // כרגע רק מדפיסים ללוג, לא מפריעים למשתמש
                    // Log.e("API_ERROR", "Failed to fetch " + symbol + ": " + error);
                }
            });
        }
    }

    // --- הלוגיקה של כפתור ה-AI ---
    // פונקציה זו מופעלת כשהמשתמש לוחץ על הכפתור השקוף
    private void runAiValidation() {
        // כרגע מציגים הודעה זמנית
        Toast.makeText(getContext(), "AI Feature coming soon...", Toast.LENGTH_SHORT).show();
    }
}