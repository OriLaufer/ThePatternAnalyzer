package com.example.thepatternanalyzer;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

// כלים לניהול המסכים המתחלפים (Fragments)
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

// כלים לתפריט התחתון ו-Firebase
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationBarView;
import com.google.firebase.auth.FirebaseAuth;

public class MainActivity extends AppCompatActivity {

    // 1. משתנים לרכיבים שעל המסך
    private BottomNavigationView bottomNavigationView; // התפריט למטה
    private ImageView btnLogout; // כפתור היציאה למעלה

    // 2. משתנים ל"דפים" (Fragments) שלנו
    private DashboardFragment dashboardFragment;
    private SpotlightFragment spotlightFragment;
    private JournalFragment journalFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // --- 3. חיבור המשתנים לרכיבים במסך (XML) ---
        bottomNavigationView = findViewById(R.id.bottom_navigation);
        btnLogout = findViewById(R.id.btn_logout); // ודא שה-ID שלך ב-XML הוא btn_logout

        // --- 4. אתחול הפראגמנטים (יוצרים "עותק" שלהם בזיכרון) ---
        dashboardFragment = new DashboardFragment();
        spotlightFragment = new SpotlightFragment();
        journalFragment = new JournalFragment();

        // --- 5. טעינת מסך ברירת המחדל (הדשבורד) ---
        if (savedInstanceState == null) {
            // אנחנו קוראים לפונקציה
            // ונותנים לה את הדף הראשון שאנחנו רוצים להציג
            loadFragment(dashboardFragment);
        }

        // 6. הגדרת המאזין ללחיצות על התפריט התחתון
        bottomNavigationView.setOnItemSelectedListener(new NavigationBarView.OnItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                int itemId = item.getItemId(); // לוקחים את ה-ID של הכפתור שנלחץ

                // בדיקה: על איזה כפתור לחצו? (לפי ה-ID מהקובץ bottom_nav_menu.xml)
                if (itemId == R.id.nav_dashboard) {
                    loadFragment(dashboardFragment); // טען את הדשבורד
                    return true; // החזר "אמת" (טיפלנו בלחיצה)
                } else if (itemId == R.id.nav_spotlight) {
                    loadFragment(spotlightFragment); // טען את זורק המניות
                    return true;
                } else if (itemId == R.id.nav_journal) {
                    loadFragment(journalFragment); // טען את היומן
                    return true;
                }
                return false; // החזר "שקר" (לא טיפלנו בלחיצה)
            }
        });

        // 7. הגדרת כפתור היציאה (Logout)
        btnLogout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // א. התנתקות מ-Firebase
                FirebaseAuth.getInstance().signOut();

                // ב. חזרה למסך הלוגין
                Intent intent = new Intent(MainActivity.this, LoginActivity.class);

                // ג. מחיקת היסטוריית המסכים (כדי שהמשתמש לא יוכל ללחוץ 'חזור' ולחזור לאפליקציה)
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);

                startActivity(intent);
            }
        });
    }

    private void loadFragment(Fragment fragment) {
        // מנהל הפרגמנטים - האחראי על החלפת התוכן
        FragmentManager fragmentManager = getSupportFragmentManager();

        // מתחילים פעולת החלפה (Transaction) - כמו לפתוח דף חדש
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();

        // הפקודה הראשית: קח את מה שיש ב-fragment_container, זרוק אותו, ושים את ה-fragment החדש
        fragmentTransaction.replace(R.id.fragment_container, fragment);

        // בצע את השינוי!
        fragmentTransaction.commit();

    }
}