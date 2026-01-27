package com.example.thepatternanalyzer;

// ייבוא הספריות הדרושות לתקשורת רשת
import retrofit2.Call; // מייצג בקשת רשת שאפשר לשלוח
import retrofit2.Callback; // הממשק שמאפשר לנו לקבל תשובה (הצלחה/כישלון)
import retrofit2.Response; // מייצג את התשובה שחזרה מהשרת
import retrofit2.Retrofit; // הספרייה הראשית לניהול תקשורת API
import retrofit2.converter.gson.GsonConverterFactory; // כלי שממיר את המידע הגולמי (JSON) לאובייקטים ב-Java

// המחלקה שמנהלת את כל התקשורת עם האינטרנט באפליקציה
public class NetworkManager {

    // משתנה סטטי שמחזיק את המופע היחיד של המחלקה הזו (Singleton)
    // המטרה: שלא ניצור חיבור חדש לאינטרנט בכל פעם, אלא נשתמש באותו אחד.
    private static NetworkManager instance;

    // הממשק שמגדיר את הפקודות שאפשר לשלוח ל-Finnhub
    private FinnhubApi api;

    // כתובת הבסיס של ה-API (כל הבקשות יתחילו בכתובת הזו)
    private static final String BASE_URL = "https://finnhub.io/api/v1/";

    // המפתח הסודי  לשימוש ב-API
    private static final String API_KEY = "d5k1i21r01qjaedsfnqgd5k1i21r01qjaedsfnr0";

    // בנאי פרטי (Constructor)
    // הוא פרטי כדי שאף אחד מחוץ למחלקה לא יוכל ליצור מופע חדש (new NetworkManager)
    private NetworkManager() {
        // כאן אנחנו בונים את ה"מכונה" של Retrofit
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(BASE_URL) // מגדירים לאן פונים
                .addConverterFactory(GsonConverterFactory.create()) // מגדירים איך מפענחים את התשובה (מ-JSON ל-Java)
                .build(); // יוצרים את האובייקט

        // יוצרים את המימוש בפועל של הממשק FinnhubApi
        api = retrofit.create(FinnhubApi.class);
    }

    // פונקציה סטטית לקבלת המופע היחיד (Singleton)
    // זו הדרך היחידה להשתמש במחלקה הזו מבחוץ
    public static synchronized NetworkManager getInstance() {
        // אם המופע עדיין לא נוצר (פעם ראשונה שקוראים לפונקציה)
        if (instance == null) {
            instance = new NetworkManager(); // יוצרים אותו עכשיו
        }
        // מחזירים את המופע הקיים
        return instance;
    }

    // --- הגדרת הממשק (Interface) לתשובה ---
    // מכיוון שאינטרנט זה דבר איטי, אנחנו לא יכולים להחזיר תשובה מיד.
    // הממשק הזה הוא כמו "כרטיס ביקור" שהמסך נותן לנו, ואנחנו מתקשרים אליו כשמוכנים.
    public interface StockCallback {
        // פונקציה שתופעל אם הצלחנו להביא מחיר
        void onSuccess(double price, double changePercent);

        // פונקציה שתופעל אם הייתה שגיאה
        void onError(String error);
    }

    // --- הפונקציה הראשית: בקשת מחיר מניה ---
    // מקבלת את שם המניה (symbol) ואת ה"כרטיס ביקור" (callback) להחזרת תשובה
    public void getStockPrice(String symbol, StockCallback callback) {

        // שולחים בקשה לשרת בעזרת ה-API שיצרנו
        // enqueue אומר: "תעשה את זה ברקע, אל תתקע את האפליקציה"
        api.getQuote(symbol, API_KEY).enqueue(new Callback<StockQuote>() {

            // הפונקציה הזו מופעלת אוטומטית כשהשרת עונה לנו
            @Override
            public void onResponse(Call<StockQuote> call, Response<StockQuote> response) {
                // בודקים אם התשובה תקינה (קוד 200) ואם יש בה תוכן
                if (response.isSuccessful() && response.body() != null) {
                    // מודיעים להצלחה!
                    // שולחים חזרה למסך את המחיר הנוכחי ואת אחוז השינוי
                    callback.onSuccess(
                            response.body().getCurrentPrice(),
                            response.body().getPercentChange()
                    );
                } else {
                    // אם השרת ענה אבל עם שגיאה (למשל מניה לא קיימת)
                    callback.onError("Error: " + response.code());
                }
            }

            // הפונקציה הזו מופעלת אם התקשורת נכשלה לגמרי (אין אינטרנט למשל)
            @Override
            public void onFailure(Call<StockQuote> call, Throwable t) {
                // מודיעים על כישלון רשת
                callback.onError("Network Failure: " + t.getMessage());
            }
        });
    }
}