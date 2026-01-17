package com.example.thepatternanalyzer;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

// זהו ה"מנהל" של האינטרנט באפליקציה.
// הוא אחראי ליצור את החיבור פעם אחת ולבצע את הבקשות עבורנו.
public class NetworkManager {

    private static NetworkManager instance; // המופע היחיד של המנהל (Singleton)
    private FinnhubService service;         // השליח שלנו
    private static final String BASE_URL = "https://finnhub.io/api/v1/"; // הכתובת של Finnhub

    // בנאי פרטי: אף אחד לא יכול ליצור מנהל חדש חוץ מעצמו
    private NetworkManager() {
        // כאן אנחנו בונים את ה"מכונה" של Retrofit
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(BASE_URL)
                .addConverterFactory(GsonConverterFactory.create()) // ממיר את ה-JSON של התשובה לאובייקטים
                .build();

        // יצירת השליח לפי ההוראות שכתבנו ב-FinnhubService
        service = retrofit.create(FinnhubService.class);
    }

    // פונקציה לקבלת המנהל (תמיד נחזיר את אותו אחד)
    public static synchronized NetworkManager getInstance() {
        if (instance == null) {
            instance = new NetworkManager();
        }
        return instance;
    }

    // --- הממשק לתשובה (Callback) ---
    // כשאנחנו מבקשים מחיר, זה לוקח זמן. הממשק הזה הוא הדרך של המנהל
    // "להתקשר אלינו חזרה" כשיש לו תשובה.
    public interface StockCallback {
        void onSuccess(double price, double changePercent);
        void onError(String error);
    }

    // --- הפונקציה הראשית: תביא לי מחיר! ---
    public void getStockPrice(String symbol, String apiKey, StockCallback callback) {

        // שליחת השליח לעבודה (באופן אסינכרוני - ברקע)
        service.getQuote(symbol, apiKey).enqueue(new Callback<StockQuote>() {
            @Override
            public void onResponse(Call<StockQuote> call, Response<StockQuote> response) {
                // הבקשה חזרה! בוא נבדוק אם היא הצליחה
                if (response.isSuccessful() && response.body() != null) {
                    double price = response.body().getCurrentPrice();
                    double change = response.body().getPercentChange();

                    // קוראים לפונקציית ההצלחה של מי שביקש
                    callback.onSuccess(price, change);
                } else {
                    // משהו השתבש בצד השרת (למשל סמל לא נכון)
                    callback.onError("Error: " + response.code());
                }
            }

            @Override
            public void onFailure(Call<StockQuote> call, Throwable t) {
                // משהו השתבש ברשת (אין אינטרנט וכו')
                callback.onError("Network Failure: " + t.getMessage());
            }
        });
    }
}