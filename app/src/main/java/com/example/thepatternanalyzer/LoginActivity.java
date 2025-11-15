package com.example.thepatternanalyzer;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log; // <-- הוספה חדשה (בשביל הלוגים)
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull; // <-- הוספה חדשה (בשביל ה-Task של Firebase)
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

// --- הוספות חדשות (צריך לייבא את הכלים של Firebase) ---
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputEditText; // <-- הוספה חדשה
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
// ----------------------------------------------------

import java.util.Objects;

public class LoginActivity extends AppCompatActivity {

    private com.google.android.material.textfield.TextInputEditText etEmail, etPassword;
    private android.widget.Button btnLogin;
    private android.widget.TextView GoToSignUp;

    //  משתנה עבור Firebase ---
    private FirebaseAuth mAuth;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_login);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        etEmail = findViewById(R.id.etEmail);
        etPassword = findViewById(R.id.etPassword);
        btnLogin = findViewById(R.id.btnLogin);
        GoToSignUp = findViewById(R.id.GoToSignUp);
        // --- הוספה חדשה: אתחול Firebase ---
        // "מפעילים" את השלט רחוק של Firebase
        mAuth = FirebaseAuth.getInstance();
        // ---------------------------------

        btnLogin.setOnClickListener(new android.view.View.OnClickListener() {
            @Override
            public void onClick(android.view.View v) {
                String email = Objects.requireNonNull(etEmail.getText()).toString();
                String password = Objects.requireNonNull(etPassword.getText()).toString();
                if (email.isEmpty() || password.isEmpty()) {
                    android.widget.Toast.makeText(LoginActivity.this, "נא למלא אימייל וסיסמה", android.widget.Toast.LENGTH_SHORT).show();
                } else {
                    // -- קריאה ל-Firebase ---
                    loginUser(email, password);
                }
            }
        });
        GoToSignUp.setOnClickListener(new android.view.View.OnClickListener() {
            @Override
            public void onClick(android.view.View v) {
                android.content.Intent intent = new android.content.Intent(LoginActivity.this, SignupActivity.class); // שיניתי לשם הקלאס הנכון SignupActivity
                startActivity(intent);
            }
        });
    }

    // --- פונקציה חדשה: כניסה עם Firebase ---
    private void loginUser(String email, String password) {
        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // כניסה הצליחה!
                            Log.d("FIREBASE_AUTH", "signInWithEmail:success");
                            Toast.makeText(LoginActivity.this, "כניסה הצליחה!", Toast.LENGTH_SHORT).show();

                            // אחרי שהתחברנו, נפתח את MainActivity
                            Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                            startActivity(intent);
                            // ונסגור את מסך הלוגין (כדי שהמשתמש לא יחזור אליו עם כפתור "חזור")
                            finish();

                        } else {
                            // כניסה נכשלה!
                            Log.w("FIREBASE_AUTH", "signInWithEmail:failure", task.getException());
                            // הצג למשתמש הודעת שגיאה
                            Toast.makeText(LoginActivity.this, "כניסה נכשלה: " + task.getException().getMessage(),
                                    Toast.LENGTH_LONG).show();
                        }
                    }
                });
    }
}