package com.yn.homi.ui.auth;

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

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthProvider;
import com.google.firebase.firestore.FirebaseFirestore;
import com.yn.homi.R;
import com.yn.homi.ui.profile.profile.UserProfile;

public class VerificationActivity extends AppCompatActivity {

    private EditText[] otpInputs = new EditText[6];
    private AppCompatButton btnContinue;
    private TextView txtContactInfo, txtInstruction, txtBackToLogin;
    private ImageView btnBack;
    private String flow, contact, type, verificationId, password;
    private UserProfile userProfile;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_verification);

        Intent intent = getIntent();
        contact = intent.getStringExtra("contact");
        type = intent.getStringExtra("type");
        flow = intent.getStringExtra("flow");
        verificationId = intent.getStringExtra("verificationId");
        password = intent.getStringExtra("password");
        userProfile = intent.getParcelableExtra("user_profile");
        mAuth = FirebaseAuth.getInstance();

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

        if ("PHONE".equals(type)) {
            verifyPhoneOTP(otp);
        } else {
            // Email verification logic (usually via Backend API)
            verifyEmailOTP(otp);
        }
    }

    private void verifyPhoneOTP(String code) {
        if (verificationId == null) {
            Toast.makeText(this, "Lỗi: Không tìm thấy ID xác thực", Toast.LENGTH_SHORT).show();
            return;
        }

        setLoading(true);
        PhoneAuthCredential credential = PhoneAuthProvider.getCredential(verificationId, code);
        
        mAuth.signInWithCredential(credential).addOnCompleteListener(task -> {
            setLoading(false);
            if (task.isSuccessful()) {
                handleSuccess();
            } else {
                Toast.makeText(this, "Mã xác thực không hợp lệ", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void verifyEmailOTP(String code) {
        // Giả lập cho luồng Email (Bạn nên thay bằng gọi API Backend)
        if (code.equals("123456")) {
            handleSuccess();
        } else {
            Toast.makeText(this, "Mã xác thực Email sai. (Test: 123456)", Toast.LENGTH_SHORT).show();
        }
    }

    private void handleSuccess() {
        if ("FORGOT_PASSWORD".equals(flow)) {
            Toast.makeText(this, "Xác thực thành công!", Toast.LENGTH_SHORT).show();
            Intent intent = new Intent(VerificationActivity.this, ResetPasswordActivity.class);
            intent.putExtra("email", contact); 
            startActivity(intent);
            finish();
        } else {
            if (userProfile != null && password != null) {
                setLoading(true);
                mAuth.createUserWithEmailAndPassword(userProfile.email, password)
                        .addOnCompleteListener(task -> {
                            if (task.isSuccessful()) {
                                String uid = task.getResult().getUser().getUid();
                                // Đảm bảo gán UID vào profile và dùng trường 'phone'
                                userProfile.uid = uid;
                                saveUserToFirestore(uid, userProfile);
                            } else {
                                setLoading(false);
                                Toast.makeText(this, "Tạo tài khoản thất bại: " + task.getException().getMessage(), Toast.LENGTH_LONG).show();
                            }
                        });
            } else {
                Toast.makeText(this, "Thiếu thông tin đăng ký!", Toast.LENGTH_SHORT).show();
                finish();
            }
        }
    }

    private void saveUserToFirestore(String uid, UserProfile profile) {
        FirebaseFirestore.getInstance().collection("users")
                .document(uid)
                .set(profile)
                .addOnSuccessListener(aVoid -> {
                    setLoading(false);
                    Toast.makeText(this, "Đăng ký thành công!", Toast.LENGTH_SHORT).show();
                    Intent intent = new Intent(VerificationActivity.this, LoginActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    startActivity(intent);
                    finish();
                })
                .addOnFailureListener(e -> {
                    setLoading(false);
                    Toast.makeText(this, "Lưu thông tin thất bại: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    private void setLoading(boolean isLoading) {
        btnContinue.setEnabled(!isLoading);
        btnContinue.setAlpha(isLoading ? 0.5f : 1.0f);
    }
}
