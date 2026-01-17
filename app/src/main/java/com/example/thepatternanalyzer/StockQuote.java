package com.example.thepatternanalyzer;

import com.google.gson.annotations.SerializedName;

// מחלקה שמייצגת את התשובה שנקבל מ-Finnhub API
public class StockQuote {

    // Finnhub משתמשים בשמות קצרים ומוזרים (כמו "c" ו-"dp").
    // אנחנו נשתמש ב-SerializedName כדי למפות אותם לשמות ברורים באפליקציה שלנו.

    // c = Current Price (המחיר הנוכחי)
    @SerializedName("c")
    private double currentPrice;

    // d = Change (השינוי בדולרים מהפתיחה)
    @SerializedName("d")
    private double change;

    // dp = Percent Change (השינוי באחוזים)
    @SerializedName("dp")
    private double percentChange;

    // --- גטרים (כדי שנוכל לקרוא את הנתונים) ---

    public double getCurrentPrice() {
        return currentPrice;
    }

    public double getChange() {
        return change;
    }

    public double getPercentChange() {
        return percentChange;
    }
}