package com.example.thepatternanalyzer;

// ייבוא הספריות של Gemini
import com.google.ai.client.generativeai.GenerativeModel;
import com.google.ai.client.generativeai.java.GenerativeModelFutures;
import com.google.ai.client.generativeai.type.Content;
import com.google.ai.client.generativeai.type.GenerateContentResponse;
import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

// המחלקה שמנהלת את התקשורת עם הבינה המלאכותית (Gemini)
public class GeminiManager {

    private static GeminiManager instance;
    private GenerativeModelFutures model;

    // *** כאן צריך לשים את המפתח של GEMINI (לא של Finnhub!) ***
    private static final String API_KEY = "AIzaSyC9GHq1ZfqeOvbTB4mkpPcfsIV7ViTHROs";

    // בנאי פרטי (Singleton)
    private GeminiManager() {

        GenerativeModel gm = new GenerativeModel("gemini-2.5-flash", API_KEY);
        model = GenerativeModelFutures.from(gm);
    }

    public static synchronized GeminiManager getInstance() {
        if (instance == null) {
            instance = new GeminiManager();
        }
        return instance;
    }

    // ממשק לתשובה (כמו שעשינו ב-NetworkManager)
    public interface AiCallback {
        void onSuccess(List<String> stockSymbols); // מחזיר רשימה של מניות (למשל: AAPL, TSLA)
        void onError(String error);
    }

    // --- הפונקציה הראשית: בקשת המלצות ---
    public void getStockRecommendations(String strategy, AiCallback callback) {

        // 1. הנדסת הפרומפט (השאלה ל-AI)
        // אנחנו מבקשים ממנו להיות מדוייק ולהחזיר רק רשימה נקייה.
        String prompt = "Act as a professional day trader. " +
                "Suggest 5 US stock tickers that are currently relevant for a '" + strategy + "' trading strategy. " +
                "Consider high volume and volatility. " +
                "Return ONLY the ticker symbols separated by commas (e.g. AAPL,TSLA,NVDA). " +
                "Do NOT add any other text, explanation, or markdown.";

        // 2. יצירת התוכן לשליחה
        Content content = new Content.Builder().addText(prompt).build();

        // 3. שליחה ברקע
        ListenableFuture<GenerateContentResponse> response = model.generateContent(content);

        // מנהל תהליכים (Executor) כדי לא לתקוע את המסך
        Executor executor = Executors.newSingleThreadExecutor();

        // 4. קבלת התשובה
        Futures.addCallback(response, new FutureCallback<GenerateContentResponse>() {
            @Override
            public void onSuccess(GenerateContentResponse result) {
                // המרת התשובה לטקסט
                String text = result.getText();

                if (text != null) {
                    // ניקוי הטקסט (מחיקת רווחים מיותרים)
                    text = text.trim().replace("\n", "").replace(" ", "");

                    // המרת המחרוזת "AAPL,TSLA,NVDA" לרשימה אמיתית
                    String[] symbolsArray = text.split(",");
                    List<String> symbolsList = new ArrayList<>(Arrays.asList(symbolsArray));

                    // החזרת הרשימה למסך
                    callback.onSuccess(symbolsList);
                } else {
                    callback.onError("Empty response from AI");
                }
            }

            @Override
            public void onFailure(Throwable t) {
                callback.onError("AI Error: " + t.getMessage());
            }
        }, executor);
    }
}