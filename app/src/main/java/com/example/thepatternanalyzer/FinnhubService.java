package com.example.thepatternanalyzer;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

// זהו ה"תפריט" של הבקשות שאנחנו יכולים לשלוח ל-API
public interface FinnhubService {

    // אנחנו רוצים לשלוח בקשת GET לכתובת "/quote"
    // הדוגמה של הכתובת המלאה: https://finnhub.io/api/v1/quote?symbol=AAPL&token=YOUR_KEY

    @GET("quote")
    Call<StockQuote> getQuote(
            @Query("symbol") String symbol, // למשל "AAPL"
            @Query("token") String apiKey   // המפתח הסודי שלך
    );
}