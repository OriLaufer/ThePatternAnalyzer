package com.example.thepatternanalyzer;

// --- 1. ייבוא הספריות (Imports) ---
// אלו "ארגזי הכלים" שאנחנו מביאים מאנדרואיד כדי לבנות את המסך.
import android.graphics.Color; // מאפשר לנו להשתמש בצבעים (כמו לבן לטקסט)
import android.os.Bundle; // משמש להעברת מידע בין מסכים ושמירת מצב
import android.view.LayoutInflater; // "מנפח" את קובץ ה-XML והופך אותו לתצוגה חיה
import android.view.View; // מייצג את המסך כולו או רכיב בתוכו
import android.view.ViewGroup; // המיכל שמחזיק את הרכיבים הגרפיים
import android.widget.AdapterView; // מאפשר לדעת מתי המשתמש בחר משהו ברשימה
import android.widget.ArrayAdapter; // מחבר בין רשימת נתונים (List) לרכיב גרפי (Spinner)
import android.widget.Button; // רכיב כפתור לחיץ
import android.widget.EditText; // שדה שבו המשתמש כותב טקסט
import android.widget.Spinner; // רשימה נפתחת לבחירה (Dropdown)
import android.widget.TextView; // רכיב להצגת טקסט פשוט
import android.widget.Toast; // הודעה קופצת קטנה למשתמש ("בועה")

import androidx.annotation.NonNull; // מוודא שמשתנה לא יהיה ריק (Null)
import androidx.annotation.Nullable; // מאפשר למשתנה להיות ריק
import androidx.fragment.app.Fragment; // המחלקה הבסיסית של מסך בתוך אפליקציה
import androidx.recyclerview.widget.LinearLayoutManager; // מסדר את הרשימה אחד מתחת לשני
import androidx.recyclerview.widget.RecyclerView; // הרכיב שמציג רשימה נגללת ויעילה

import java.util.ArrayList; // סוג של רשימה חכמה שאפשר להוסיף לה איברים
import java.util.List; // הממשק הכללי של רשימות

// --- המחלקה הראשית (Class) ---
// זו המחלקה שמנהלת את מסך ה"זרקור" (Spotlight). היא יורשת מ-Fragment.
public class SpotlightFragment extends Fragment {

    // --- 2. משתנים לרכיבי המסך (ה"מגירות" לרכיבים) ---
    // אנחנו מגדירים אותם כאן כדי שנוכל להשתמש בהם בכל הפונקציות במחלקה.
    private EditText etStrategyName; // השדה שבו כותבים את שם האסטרטגיה
    private EditText etMinPrice;     // השדה למחיר מינימום
    private EditText etMaxPrice;     // השדה למחיר מקסימום
    private Spinner spinnerPattern;  // התיבה הנפתחת לבחירת תבנית
    private TextView tvSpinnerHint;  // הטקסט "Select a pattern..." שנעלם כשבוחרים
    private Button btnAiValidate;    // הכפתור השקוף (לאימות AI)
    private Button btnRunScanner;    // הכפתור הסגול (להרצת הסריקה)
    private RecyclerView recyclerResults; // הרשימה הריקה למטה שתתמלא בתוצאות

    // --- 3. משתנים לניהול הנתונים (ה"מוח" של הרשימה) ---
    private SpotlightAdapter adapter; // ה"מנהל" שמחבר בין הנתונים לרשימה הויזואלית
    private List<SpotlightAdapter.StockResult> resultsList; // רשימה בזיכרון שתחזיק את המניות שנמצאו

    // בנאי ריק (Constructor) - חובה באנדרואיד כדי למנוע קריסות
    public SpotlightFragment() {
    }

    // --- 4. פונקציית יצירת המראה (onCreateView) ---
    // פונקציה זו נקראת כשהאנדרואיד צריך "לצייר" את המסך בפעם הראשונה.
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // הפקודה הזו לוקחת את קובץ העיצוב (fragment_spotlight.xml) וטוענת אותו לזיכרון
        return inflater.inflate(R.layout.fragment_spotlight, container, false);
    }

    // --- 5. הפונקציה הראשית (onViewCreated) ---
    // פונקציה זו נקראת מיד אחרי שהמסך צויר. כאן אנחנו מתחילים לעבוד!
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // --- שלב א': חיבור המשתנים לרכיבים ב-XML ---
        // אנחנו משתמשים ב-view.findViewById כדי למצוא את הרכיב לפי ה-ID שנתת לו ב-XML שלך.
        etStrategyName = view.findViewById(R.id.etStrategyName); // חיבור לשם האסטרטגיה
        etMinPrice = view.findViewById(R.id.etMinPrice); // חיבור לשדה מינימום (השם שלך)
        etMaxPrice = view.findViewById(R.id.etMaxPrice); // חיבור לשדה מקסימום (השם שלך)
        spinnerPattern = view.findViewById(R.id.spinnerPattern); // חיבור לתיבה הנפתחת
        tvSpinnerHint = view.findViewById(R.id.tvSpinnerHint); // חיבור לטקסט הרמז

        btnAiValidate = view.findViewById(R.id.btnAiValidate); // חיבור לכפתור ה-AI
        btnRunScanner = view.findViewById(R.id.btnRunScanner); // חיבור לכפתור הסריקה (השם שלך)

        recyclerResults = view.findViewById(R.id.recyclerResults); // חיבור לרשימה הנגללת למטה

        // --- שלב ב': הפעלת הרכיבים ---
        // קריאה לפונקציות עזר שכתבנו למטה כדי לסדר את המסך
        setupSpinner(); // מסדר את התיבה הנפתחת
        setupRecyclerView(); // מכין את הרשימה לקבלת נתונים

        // --- שלב ג': הגדרת הפעולות (Listeners) ---
        // אנחנו אומרים למערכת: "כשלוחצים על הכפתור הזה, תפעיל את הפונקציה הזאת"

        // כשלוחצים על "Run Scanner" -> תפעיל את הלוגיקה של הסריקה
        btnRunScanner.setOnClickListener(v -> runScannerLogic());

        // כשלוחצים על "AI Validate" -> תפעיל את בדיקת ה-AI
        btnAiValidate.setOnClickListener(v -> runAiValidation());
    }

    // --- פונקציה להכנת הרשימה התחתונה (RecyclerView) ---
    private void setupRecyclerView() {
        resultsList = new ArrayList<>(); // יוצרים רשימה ריקה בזיכרון המחשב

        // יצירת המתאם (Adapter). המתאם הוא זה שלוקח את הנתונים והופך אותם לשורות יפות.
        // אנחנו מעבירים לו גם "פעולה" (Lambda) שתקרה כשלוחצים על הפלוס (+)
        adapter = new SpotlightAdapter(resultsList, stock -> {
            // הקוד הזה ירוץ כשלוחצים על הפלוס בשורה של מניה:
            Toast.makeText(getContext(), "Added " + stock.symbol + " to Watchlist", Toast.LENGTH_SHORT).show();
            // הערה: כאן בעתיד אפשר להוסיף שמירה אמיתית ליומן ב-Firebase
        });

        // בדיקת בטיחות: אם הרשימה קיימת ב-XML (כדי למנוע קריסה)
        if (recyclerResults != null) {
            // קובעים שהרשימה תהיה מסודרת בטור (Linear) ולא בטבלה
            recyclerResults.setLayoutManager(new LinearLayoutManager(getContext()));
            // מחברים את המתאם לרשימה שבמסך
            recyclerResults.setAdapter(adapter);
        }
    }

    // --- פונקציה להגדרת התיבה הנפתחת (Spinner) ---
    private void setupSpinner() {
        // יצירת רשימת האפשרויות (String List) לתבניות
        List<String> patterns = new ArrayList<>();
        patterns.add(""); // פריט ראשון ריק - כדי שיראו את הטקסט "Select a pattern..." מאחורה
        patterns.add("Momentum");
        patterns.add("Bull Flag");
        patterns.add("Gap & Go");
        patterns.add("Reversal");

        // יצירת המתאם (Adapter) של ה-Spinner עם עיצוב מותאם אישית
        ArrayAdapter<String> spinnerAdapter = new ArrayAdapter<String>(requireContext(), android.R.layout.simple_spinner_item, patterns) {

            // פונקציה שקובעת איך נראה הפריט כשהתיבה סגורה
            @NonNull
            @Override
            public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
                View view = super.getView(position, convertView, parent); // לוקחים את התצוגה הרגילה
                TextView tv = (TextView) view; // ממירים ל-TextView
                tv.setTextColor(Color.WHITE); // צובעים את הטקסט בלבן (כי הרקע כהה)
                tv.setTextSize(14); // קובעים גודל טקסט נוח
                return view;
            }

            // פונקציה שקובעת איך נראים הפריטים כשהרשימה נפתחת
            @Override
            public View getDropDownView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
                View view = super.getDropDownView(position, convertView, parent);
                TextView tv = (TextView) view;

                // טיפול בפריט הריק הראשון
                if (position == 0) {
                    tv.setHeight(0); // מסתירים אותו (גובה 0)
                } else {
                    tv.setTextColor(Color.BLACK); // שאר הפריטים בשחור (כי הרקע הלבן של אנדרואיד)
                }
                return view;
            }
        };

        // הגדרת המרווחים ברשימה הנפתחת
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        // חיבור המתאם ל-Spinner
        spinnerPattern.setAdapter(spinnerAdapter);

        // הוספת מאזין לבחירה (Listener) - כדי להסתיר/להציג את ה-Hint
        spinnerPattern.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                // אם נבחר הפריט הראשון (0) -> תציג את ה-Hint ("Select a pattern...")
                if (position == 0) {
                    tvSpinnerHint.setVisibility(View.VISIBLE);
                } else {
                    // אם נבחרה תבנית אמיתית -> תסתיר את ה-Hint
                    tvSpinnerHint.setVisibility(View.INVISIBLE);
                }
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                tvSpinnerHint.setVisibility(View.VISIBLE); // ברירת מחדל: הצג את ה-Hint
            }
        });
    }

    // --- הלב של המערכת: פונקציית הסריקה שמחברת AI ו-API ---
    // הפונקציה הזו רצה כשהמשתמש לוחץ על הכפתור "Run Scanner"
    private void runScannerLogic() {
        // 1. קריאת שם האסטרטגיה מהשדה (ומחיקת רווחים מיותרים עם trim)
        String strategy = etStrategyName.getText().toString().trim();

        // 2. בדיקת תקינות: האם השדה ריק?
        if (strategy.isEmpty()) {
            etStrategyName.setError("Name required"); // מציג שגיאה אדומה בשדה
            return; // עוצר את הפונקציה ולא ממשיך
        }

        // 3. הצגת הודעה למשתמש שאנחנו מתחילים לעבוד
        Toast.makeText(getContext(), "Asking AI for " + strategy + " stocks...", Toast.LENGTH_SHORT).show();

        // 4. ניקוי תוצאות קודמות מהרשימה כדי שלא יהיו כפילויות
        resultsList.clear();
        if (adapter != null) adapter.notifyDataSetChanged(); // עדכון המסך שכרגע הוא ריק

        // --- חיבור ל-AI (Gemini) ---
        // 5. אנחנו מבקשים מ-GeminiManager להמליץ על מניות לפי האסטרטגיה
        GeminiManager.getInstance().getStockRecommendations(strategy, new GeminiManager.AiCallback() {

            // פונקציה זו תופעל כשה-AI יחזיר תשובה מוצלחת
            @Override
            public void onSuccess(List<String> stockSymbols) {
                // חזרה לחוט הראשי (Main Thread) כדי להציג הודעות למשתמש
                getActivity().runOnUiThread(() ->
                        Toast.makeText(getContext(), "AI found: " + stockSymbols.toString(), Toast.LENGTH_SHORT).show()
                );

                // --- חיבור ל-API (Finnhub) ---
                // 6. עבור כל מניה שה-AI המליץ עליה, אנחנו שולחים בקשה למחיר
                for (String symbol : stockSymbols) {
                    fetchStockPrice(symbol); // קריאה לפונקציית עזר (ראה למטה)
                }
            }

            // פונקציה זו תופעל אם ה-AI נכשל
            @Override
            public void onError(String error) {
                getActivity().runOnUiThread(() ->
                        Toast.makeText(getContext(), "AI Error: " + error, Toast.LENGTH_SHORT).show()
                );
            }
        });
    }

    // --- פונקציית עזר להבאת מחיר מניה מה-API ---
    // מקבלת סמל של מניה (למשל "AAPL") ומוסיפה אותו לרשימה עם המחיר
    private void fetchStockPrice(String symbol) {
        // קריאה ל-NetworkManager כדי לקבל מחיר עדכני
        NetworkManager.getInstance().getStockPrice(symbol, new NetworkManager.StockCallback() {

            // כשהמחיר מגיע בהצלחה מהאינטרנט:
            @Override
            public void onSuccess(double price, double changePercent) {
                // יצירת אובייקט תוצאה חדש עם השם, המחיר והאחוזים
                SpotlightAdapter.StockResult result = new SpotlightAdapter.StockResult(symbol, price, changePercent);

                // הוספה לרשימה בזיכרון
                resultsList.add(result);

                // עדכון המתאם: "היי, יש נתון חדש, תצייר אותו!"
                if (adapter != null) {
                    adapter.notifyDataSetChanged();
                }
            }

            // אם הייתה שגיאה בהבאת המחיר:
            @Override
            public void onError(String error) {
                // כרגע אנחנו מתעלמים משגיאות בודדות כדי לא להציף את המשתמש בהודעות
                // אפשר להוסיף כאן לוג אם רוצים (Log.e)
            }
        });
    }

    // --- הלוגיקה של כפתור ה-AI Validate ---
    private void runAiValidation() {
        // כרגע רק מציג הודעה שהפיצ'ר יגיע בקרוב (Placeholder)
        Toast.makeText(getContext(), "AI Feature coming soon...", Toast.LENGTH_SHORT).show();
    }
}