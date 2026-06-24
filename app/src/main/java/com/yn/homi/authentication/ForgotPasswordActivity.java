package com.yn.homi.authentication;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;

import com.google.firebase.auth.PhoneAuthProvider;
import com.yn.homi.R;
import com.yn.homi.utils.OTPManager;

public class ForgotPasswordActivity extends AppCompatActivity {

    private EditText edtEmail;
    private AppCompatButton btnSendCode;
    private ImageView btnBack;
    private TextView txtBackToLogin;
    private OTPManager otpManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forgot_password);

        initViews();
        setupListeners();
        otpManager = new OTPManager(this);
    }

    private void initViews() {
        edtEmail = findViewById(R.id.edtEmail);
        btnSendCode = findViewById(R.id.btnSendCode);
        btnBack = findViewById(R.id.btnBack);
        txtBackToLogin = findViewById(R.id.txtBackToLogin);
    }

    private void setupListeners() {
        btnBack.setOnClickListener(v -> finish());
        txtBackToLogin.setOnClickListener(v -> finish());

        btnSendCode.setOnClickListener(v -> handleSendCode());
    }

    private void handleSendCode() {
        String contact = edtEmail.getText().toString().trim();

        if (TextUtils.isEmpty(contact)) {
            edtEmail.setError("Email or Phone required");
            return;
        }

        boolean isEmail = android.util.Patterns.EMAIL_ADDRESS.matcher(contact).matches();
        boolean isPhone = TextUtils.isDigitsOnly(contact) && contact.length() >= 10;

        if (!isEmail && !isPhone) {
            edtEmail.setError("Enter a valid email or phone number");
            return;
        }

        setLoading(true);

        OTPManager.OTPCallback callback = new OTPManager.OTPCallback() {
            @Override
            public void onCodeSent(String verificationId, PhoneAuthProvider.ForceResendingToken token) {
                setLoading(false);
                Intent intent = new Intent(ForgotPasswordActivity.this, VerificationActivity.class);
                intent.putExtra("contact", contact);
                intent.putExtra("verificationId", verificationId);
                intent.putExtra("type", isEmail ? "EMAIL" : "PHONE");
                intent.putExtra("flow", "FORGOT_PASSWORD");
                startActivity(intent);
            }

            @Override
            public void onVerificationSuccess() {
                setLoading(false);
                startActivity(new Intent(ForgotPasswordActivity.this, ResetPasswordActivity.class));
                finish();
            }

            @Override
            public void onFailure(String error) {
                setLoading(false);
                Toast.makeText(ForgotPasswordActivity.this, "Lỗi: " + error, Toast.LENGTH_LONG).show();
            }
        };

        if (isPhone) {
            otpManager.sendOTPToPhone(contact, callback);
        } else {
            otpManager.sendOTPToEmail(contact, callback);
        }
    }

    private void setLoading(boolean isLoading) {
        btnSendCode.setEnabled(!isLoading);
        btnSendCode.setAlpha(isLoading ? 0.5f : 1.0f);
        edtEmail.setEnabled(!isLoading);
    }
}
