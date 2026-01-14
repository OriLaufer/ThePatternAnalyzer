package com.example.thepatternanalyzer;

// הקובץ הזה מגדיר אילו נתונים אנחנו שומרים על כל טרייד במערכת.
public class Trade {

    // 1. המשתנים (השדות) - הנתונים שנשמור
    // ----------------------------------------------------
    private String id;          // מזהה ייחודי של העסקה (ש-Firebase נותן)
    private String userId;      // המזהה של המשתמש שיצר את העסקה (כדי שלא נערבב בין משתמשים)
    private String ticker;      // שם המניה (למשל: AAPL)
    private int quantity;       // כמות המניות (מספר שלם)
    private double entryPrice;// מחיר כניסה (יכול להיות עם נקודה עשרונית)
    private double exitPrice;   // *** מחיר יציאה

    private String pattern;     // שם התבנית (למשל: Momentum)
    private String status;      // הסטטוס: "OPEN" (פתוח) או "CLOSED" (סגור)
    private long timestamp;     // הזמן שבו העסקה נוצרה (בשניות מחשב)

    // 2. בנאים (Constructors) - איך יוצרים עסקה חדשה
    // ----------------------------------------------------

    // בנאי ריק (חובה בשביל Firebase!)
    // Firebase צריך את זה כדי להפוך את המידע מהשרת חזרה לאובייקט בקוד.
    public Trade() {
    }

    // בנאי מלא - נשתמש בו כשניצור עסקה חדשה מתוך האפליקציה
    public Trade(String id, String userId, String ticker, int quantity, double entryPrice, String pattern, long timestamp) {
        this.id = id;
        this.userId = userId;
        this.ticker = ticker;
        this.quantity = quantity;
        this.entryPrice = entryPrice;
        this.pattern = pattern;
        this.timestamp = timestamp;
        this.status = "OPEN"; // כברירת מחדל, כל עסקה חדשה היא "פתוחה"
        this.exitPrice = 0.0; // אתחול ל-0
    }

    // 3. גטרים וסטרים (Getters & Setters)
    // אלו פונקציות שמאפשרות לקרוא (Get) ולשנות (Set) את הנתונים מבחוץ.
    // ----------------------------------------------------

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getTicker() {
        return ticker;
    }

    public void setTicker(String ticker) {
        this.ticker = ticker;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    public double getEntryPrice() {
        return entryPrice;
    }

    public void setEntryPrice(double entryPrice) {
        this.entryPrice = entryPrice;
    }

    public String getPattern() {
        return pattern;
    }

    public void setPattern(String pattern) {
        this.pattern = pattern;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }
    public double getExitPrice()
    { return exitPrice; }

    public void setExitPrice(double exitPrice)
    { this.exitPrice = exitPrice; }
}