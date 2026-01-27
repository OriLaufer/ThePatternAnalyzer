package com.example.thepatternanalyzer;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

public interface FinnhubApi {
    // הגדרת הבקשה לשרת: "תביא לי ציטוט (Quote)"
    @GET("quote")
    Call<StockQuote> getQuote(
            @Query("symbol") String symbol, // למשל AAPL
            @Query("token") String apiKey   // המפתח הסודי שלך
    );
}