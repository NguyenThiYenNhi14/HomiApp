package com.yn.homi.authentication;

import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;

import com.yn.homi.R;

public class VerificationActivity extends AppCompatActivity {

    private EditText[] otpInputs = new EditText[6];
    private AppCompatButton btnContinue;
    private TextView txtContactInfo, txtInstruction, txtBackToLogin;
    private ImageView btnBack;
    private String flow, contact, type;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_verification);

        Intent intent = getIntent();
        contact = intent.getStringExtra("contact");
        type = intent.getStringExtra("type");
        flow = intent.getStringExtra("flow");

        initViews();
        setupOTPInputs();
        setupListeners();
        
        displayInfo();
    }

    private void initViews() {
        btnBack = findViewById(R.id.btnBack);
        txtTitle = findViewById(R.id.txtTitle); // Using txtTitle if needed
        txtInstruction = findViewById(R.id.txtInstruction);
        txtContactInfo = findViewById(R.id.txtContactInfo);
        btnContinue = findViewById(R.id.btnContinue);
        txtBackToLogin = findViewById(R.id.txtBackToLogin);

        otpInputs[0] = findViewById(R.id.otp1);
        otpInputs[1] = findViewById(R.id.otp2);
        otpInputs[2] = findViewById(R.id.otp3);
        otpInputs[3] = findViewById(R.id.otp4);
        otpInputs[4] = findViewById(R.id.otp5);
        otpInputs[5] = findViewById(R.id.otp6);
    }
    
    private TextView txtTitle; // Added locally as it was missed in init

    private void displayInfo() {
        if (contact != null) {
            txtContactInfo.setText(contact);
        }
        if ("PHONE".equals(type)) {
            txtInstruction.setText(R.string.str_verify_phone_hint);
        } else {
            txtInstruction.setText(R.string.str_verify_email_hint);
        }
    }

    private void setupListeners() {
        btnBack.setOnClickListener(v -> finish());
        txtBackToLogin.setOnClickListener(v -> {
            Intent intent = new Intent(VerificationActivity.this, LoginActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
            startActivity(intent);
        });

        btnContinue.setOnClickListener(v -> verifyOTP());
    }

    private void setupOTPInputs() {
        for (int i = 0; i < 6; i++) {
            final int index = i;
            otpInputs[i].addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                    if (s.length() == 1 && index < 5) {
                        otpInputs[index + 1].requestFocus();
                    } else if (s.length() > 1 && index == 0) {
                        // Handle paste
                        handlePaste(s.toString());
                    }
                }

                @Override
                public void afterTextChanged(Editable s) {}
            });

            otpInputs[i].setOnKeyListener((v, keyCode, event) -> {
                if (keyCode == KeyEvent.KEYCODE_DEL && event.getAction() == KeyEvent.ACTION_DOWN) {
                    if (otpInputs[index].getText().length() == 0 && index > 0) {
                        otpInputs[index - 1].requestFocus();
                        otpInputs[index - 1].setText("");
                    }
                }
                return false;
            });
            
            // Handle paste detection on first box
            if (i == 0) {
                otpInputs[i].setOnLongClickListener(v -> {
                    ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
                    if (clipboard != null && clipboard.hasPrimaryClip()) {
                        String pasteData = clipboard.getPrimaryClip().getItemAt(0).getText().toString();
                        if (pasteData.length() == 6 && TextUtils.isDigitsOnly(pasteData)) {
                            handlePaste(pasteData);
                            return true;
                        }
                    }
                    return false;
                });
            }
        }
    }

    private void handlePaste(String code) {
        if (code.length() >= 6 && TextUtils.isDigitsOnly(code.substring(0, 6))) {
            for (int i = 0; i < 6; i++) {
                otpInputs[i].setText(String.valueOf(code.charAt(i)));
            }
            otpInputs[5].requestFocus();
        }
    }

    private void verifyOTP() {
        StringBuilder sb = new StringBuilder();
        for (EditText et : otpInputs) {
            sb.append(et.getText().toString());
        }

        String otp = sb.toString();
        if (otp.length() < 6) {
            Toast.makeText(this, "Please enter 6-digit code", Toast.LENGTH_SHORT).show();
            return;
        }

        // Mock verification (Always correct if "123456")
        if (otp.equals("123456")) {
            Toast.makeText(this, "Verification Successful!", Toast.LENGTH_SHORT).show();
            if ("FORGOT_PASSWORD".equals(flow)) {
                startActivity(new Intent(VerificationActivity.this, ResetPasswordActivity.class));
            } else {
                // From Register
                Intent intent = new Intent(VerificationActivity.this, LoginActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent);
            }
            finish();
        } else {
            Toast.makeText(this, "Invalid code. Try 123456", Toast.LENGTH_SHORT).show();
        }
    }
}
