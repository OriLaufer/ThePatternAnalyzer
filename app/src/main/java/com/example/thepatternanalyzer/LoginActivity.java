package com.example.thepatternanalyzer;

import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import java.util.Objects;

public class LoginActivity extends AppCompatActivity {

    private com.google.android.material.textfield.TextInputEditText etEmail, etPassword;
    private android.widget.Button btnLogin;
    private android.widget.TextView GoToSignUp;

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

        btnLogin.setOnClickListener(new android.view.View.OnClickListener() {
            @Override
            public void onClick(android.view.View v) {
                String email = Objects.requireNonNull(etEmail.getText()).toString();
                String password = Objects.requireNonNull(etPassword.getText()).toString();
                if (email.isEmpty() || password.isEmpty()) {
                    android.widget.Toast.makeText(LoginActivity.this, "נא למלא אימייל וסיסמה", android.widget.Toast.LENGTH_SHORT).show();
                } else {
                    android.widget.Toast.makeText(LoginActivity.this, "מתחבר עם: " + email, android.widget.Toast.LENGTH_SHORT).show();
                }
            }
        });
        GoToSignUp.setOnClickListener(new android.view.View.OnClickListener() {
            @Override
            public void onClick(android.view.View v) {
                android.content.Intent intent = new android.content.Intent(LoginActivity.this, SignupActivity.class);
                startActivity(intent);
            }
        });
    }
}