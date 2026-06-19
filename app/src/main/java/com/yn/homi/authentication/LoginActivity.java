package com.yn.homi.authentication;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.yn.homi.R;

public class LoginActivity extends AppCompatActivity {

    private EditText edtEmail, edtpsw;
    private CheckBox chkRemember;
    private Button btnLogin, btnforgot, btncreateacc;
    private ImageView btnGoogle, btnApple, btnFacebook;
    private SharedPreferences sharedPreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        initViews();
        setupListeners();
        loadSavedCredentials();
    }

    private void initViews() {
        edtEmail = findViewById(R.id.edtEmail);
        edtpsw = findViewById(R.id.edtpsw);
        chkRemember = findViewById(R.id.chkRemember);
        btnLogin = findViewById(R.id.btnLogin);
        btnforgot = findViewById(R.id.btnforgot);
        btncreateacc = findViewById(R.id.btncreateacc);
        
        // Social buttons (IDs from activity_login.xml)
        btnGoogle = findViewById(R.id.imageView);
        btnApple = findViewById(R.id.imageView2);
        btnFacebook = findViewById(R.id.imageView3);

        sharedPreferences = getSharedPreferences("LoginPrefs", MODE_PRIVATE);
    }

    private void setupListeners() {
        btnLogin.setOnClickListener(v -> handleLogin());

        btnforgot.setOnClickListener(v -> {
            Intent intent = new Intent(LoginActivity.this, ForgotPasswordActivity.class);
            startActivity(intent);
        });

        btncreateacc.setOnClickListener(v -> {
            Intent intent = new Intent(LoginActivity.this, RegistrationActivity.class);
            startActivity(intent);
        });

        View.OnClickListener socialClickListener = v -> Toast.makeText(LoginActivity.this, "Social Login Coming Soon!", Toast.LENGTH_SHORT).show();
        btnGoogle.setOnClickListener(socialClickListener);
        btnApple.setOnClickListener(socialClickListener);
        btnFacebook.setOnClickListener(socialClickListener);
    }

    private void handleLogin() {
        String email = edtEmail.getText().toString().trim();
        String password = edtpsw.getText().toString().trim();

        if (TextUtils.isEmpty(email)) {
            edtEmail.setError("Please enter your email");
            return;
        }

        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            edtEmail.setError("Please enter a valid email");
            return;
        }

        if (TextUtils.isEmpty(password)) {
            edtpsw.setError("Please enter your password");
            return;
        }

        // Mock Loading State
        setLoading(true);

        // Simulate API call
        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            setLoading(false);
            if (email.equals("admin@gmail.com") && password.equals("Admin@123")) {
                if (chkRemember.isChecked()) {
                    saveCredentials(email, password);
                } else {
                    clearCredentials();
                }
                Toast.makeText(LoginActivity.this, "Login successful!", Toast.LENGTH_SHORT).show();
                // Navigate to Main Screen (Replace MainActivity with your actual main activity)
                // Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                // startActivity(intent);
                // finish();
            } else {
                Toast.makeText(LoginActivity.this, "Invalid email or password", Toast.LENGTH_SHORT).show();
            }
        }, 1500);
    }

    private void setLoading(boolean isLoading) {
        btnLogin.setEnabled(!isLoading);
        btnLogin.setAlpha(isLoading ? 0.5f : 1.0f);
        edtEmail.setEnabled(!isLoading);
        edtpsw.setEnabled(!isLoading);
    }

    private void saveCredentials(String email, String password) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString("email", email);
        editor.putString("password", password);
        editor.putBoolean("remember", true);
        editor.apply();
    }

    private void clearCredentials() {
        sharedPreferences.edit().clear().apply();
    }

    private void loadSavedCredentials() {
        if (sharedPreferences.getBoolean("remember", false)) {
            edtEmail.setText(sharedPreferences.getString("email", ""));
            edtpsw.setText(sharedPreferences.getString("password", ""));
            chkRemember.setChecked(true);
        }
    }
}
