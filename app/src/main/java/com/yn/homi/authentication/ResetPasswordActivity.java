package com.yn.homi.authentication;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.InputType;
import android.text.TextUtils;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;

import com.yn.homi.R;

public class ResetPasswordActivity extends AppCompatActivity {

    private EditText edtPassword, edtConfirmPassword;
    private AppCompatButton btnResetPassword;
    private ImageView btnBack, ivEyeReset, ivEyeResetConfirm;
    private TextView txtResetLater;
    private boolean isPasswordVisible = false;
    private boolean isConfirmPasswordVisible = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reset_password);

        initViews();
        setupListeners();
    }

    private void initViews() {
        edtPassword = findViewById(R.id.edtPassword);
        edtConfirmPassword = findViewById(R.id.edtConfirmPassword);
        btnResetPassword = findViewById(R.id.btnResetPassword);
        btnBack = findViewById(R.id.btnBack);
        txtResetLater = findViewById(R.id.txtResetLater);
        ivEyeReset = findViewById(R.id.ivEyeReset);
        ivEyeResetConfirm = findViewById(R.id.ivEyeResetConfirm);
    }

    private void setupListeners() {
        btnBack.setOnClickListener(v -> finish());
        txtResetLater.setOnClickListener(v -> {
            Intent intent = new Intent(ResetPasswordActivity.this, LoginActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(intent);
            finish();
        });

        btnResetPassword.setOnClickListener(v -> handleResetPassword());

        ivEyeReset.setOnClickListener(v -> {
            if (isPasswordVisible) {
                edtPassword.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
                ivEyeReset.setImageResource(R.drawable.ic_eye_off);
            } else {
                edtPassword.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
                ivEyeReset.setImageResource(R.drawable.ic_eye_on);
            }
            isPasswordVisible = !isPasswordVisible;
            edtPassword.setSelection(edtPassword.getText().length());
        });

        ivEyeResetConfirm.setOnClickListener(v -> {
            if (isConfirmPasswordVisible) {
                edtConfirmPassword.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
                ivEyeResetConfirm.setImageResource(R.drawable.ic_eye_off);
            } else {
                edtConfirmPassword.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
                ivEyeResetConfirm.setImageResource(R.drawable.ic_eye_on);
            }
            isConfirmPasswordVisible = !isConfirmPasswordVisible;
            edtConfirmPassword.setSelection(edtConfirmPassword.getText().length());
        });
    }

    private void handleResetPassword() {
        String password = edtPassword.getText().toString().trim();
        String confirmPassword = edtConfirmPassword.getText().toString().trim();

        if (!validatePassword(password)) {
            edtPassword.setError("Password must be at least 8 chars with Uppercase, Lowercase, Number and Special char");
            return;
        }

        if (!password.equals(confirmPassword)) {
            edtConfirmPassword.setError("Passwords do not match");
            return;
        }

        setLoading(true);

        // Mock Reset Password API
        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            setLoading(false);
            Toast.makeText(ResetPasswordActivity.this, "Password reset successful!", Toast.LENGTH_LONG).show();
            
            Intent intent = new Intent(ResetPasswordActivity.this, LoginActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
            finish();
        }, 2000);
    }

    private boolean validatePassword(String password) {
        if (password.length() < 8) return false;
        
        boolean hasUpper = false, hasLower = false, hasDigit = false, hasSpecial = false;
        String specialChars = "!@#$%^&*()-_+=<>?";

        for (char c : password.toCharArray()) {
            if (Character.isUpperCase(c)) hasUpper = true;
            else if (Character.isLowerCase(c)) hasLower = true;
            else if (Character.isDigit(c)) hasDigit = true;
            else if (specialChars.contains(String.valueOf(c))) hasSpecial = true;
        }

        return hasUpper && hasLower && hasDigit && hasSpecial;
    }

    private void setLoading(boolean isLoading) {
        btnResetPassword.setEnabled(!isLoading);
        btnResetPassword.setAlpha(isLoading ? 0.5f : 1.0f);
        edtPassword.setEnabled(!isLoading);
        edtConfirmPassword.setEnabled(!isLoading);
    }
}
