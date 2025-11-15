package com.example.thepatternanalyzer;

import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.annotation.NonNull;
import android.util.Log;
import android.view.View;
import android.widget.Toast;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import java.util.Objects;

public class SignupActivity extends AppCompatActivity {

    private com.google.android.material.textfield.TextInputEditText etSignUpEmail, etSignUpPassword, etConfirmPassword;
    private android.widget.Button btnSignUp;
    private android.widget.TextView tvGoToLogin;
    private android.widget.ImageView imgBack;
    // משתנה עבור Firebase ---
    private FirebaseAuth mAuth;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_signup);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        imgBack = findViewById(R.id.imgBack);
        etSignUpEmail = findViewById(R.id.SignUpEmail);
        etSignUpPassword = findViewById(R.id.etSignUpPassword);
        etConfirmPassword = findViewById(R.id.etConfirmPassword);
        btnSignUp = findViewById(R.id.btnSignUp);
        tvGoToLogin = findViewById(R.id.tvGoToLogin);
        // - אתחול Firebase ---
        // "מפעילים" את השלט רחוק של Firebase
        mAuth = FirebaseAuth.getInstance();


        // --- מאזין 1: חץ חזור ---
        imgBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish(); // סוגר את המסך וחוזר ללוגין
            }
        });

        // --- מאזין 2: כפתור הרשמה  ---
        btnSignUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // 1. קריאת הטקסט מהשדות
                String email = Objects.requireNonNull(etSignUpEmail.getText()).toString();
                String password = Objects.requireNonNull(etSignUpPassword.getText()).toString();
                String confirmPassword = Objects.requireNonNull(etConfirmPassword.getText()).toString();

                // 2. בדיקות
                if (email.isEmpty() || password.isEmpty() || confirmPassword.isEmpty()) {
                    // בדיקה אם כל השדות מלאים
                    Toast.makeText(SignupActivity.this, "נא למלא את כל השדות", Toast.LENGTH_SHORT).show();
                } else if (!password.equals(confirmPassword)) {
                    // בדיקה אם הסיסמאות תואמות
                    Toast.makeText(SignupActivity.this, "הסיסמאות אינן תואמות", Toast.LENGTH_SHORT).show();
                } else {

                    registerUser(email, password);
                }
            }
        });
        // --- מאזין 3: הקישור "חזור ללוגין"  ---
        tvGoToLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // בדיוק כמו החץ חזור, סוגר את המסך וחוזר ללוגין
                finish();
            }
        });
    }

    // הרשמה ל-Firebase ---
    // הפונקציה הזו מדברת עם השרת של גוגל
    private void registerUser(String email, String password) {
        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // הרשמה הצליחה!
                            Log.d("FIREBASE_AUTH", "createUserWithEmail:success");
                            Toast.makeText(SignupActivity.this, "הרשמה הצליחה!", Toast.LENGTH_SHORT).show();
                            // נחזיר את המשתמש למסך הלוגין
                            finish();
                        } else {
                            // הרשמה נכשלה!
                            Log.w("FIREBASE_AUTH", "createUserWithEmail:failure", task.getException());
                            // הצג למשתמש את השגיאה האמיתית
                            Toast.makeText(SignupActivity.this, "הרשמה נכשלה: " + task.getException().getMessage(), Toast.LENGTH_LONG).show();
                        }
                    }
                });
    }
    // -----------------------------------------
}