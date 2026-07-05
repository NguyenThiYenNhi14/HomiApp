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
import android.net.Uri;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import java.util.UUID;

import com.yn.homi.core.BaseActivity;
import androidx.appcompat.widget.AppCompatButton;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthProvider;
import com.google.firebase.firestore.FirebaseFirestore;
import com.yn.homi.R;
import com.yn.homi.ui.profile.profile.UserProfile;

import java.util.HashMap;
import java.util.Map;

public class VerificationActivity extends BaseActivity {

    private EditText[] otpInputs = new EditText[6];
    private AppCompatButton btnContinue;
    private TextView txtContactInfo, txtInstruction, txtBackToLogin, txtTitle;
    private ImageView btnBack;
    private String flow, contact, type, verificationId, password;
    private UserProfile userProfile;
    private FirebaseAuth mAuth;
    private boolean isAutoVerified = false;

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
        isAutoVerified = intent.getBooleanExtra("is_auto_verified", false);
        mAuth = FirebaseAuth.getInstance();

        initViews();
        setupOTPInputs();
        setupListeners();
        
        displayInfo();

        if (isAutoVerified) {
            handleSuccess();
        }
    }


    private void initViews() {
        btnBack = findViewById(R.id.btnBack);
        txtTitle = findViewById(R.id.txtTitle);
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

    private void displayInfo() {
        if (contact != null) {
            txtContactInfo.setText(contact);
        }
        if ("EMAIL".equals(type)) {
            txtInstruction.setText(R.string.msg_email_sent_instruction);
            findViewById(R.id.otpLayout).setVisibility(View.GONE);
            btnContinue.setVisibility(View.GONE);
        } else {
            txtInstruction.setText(R.string.str_verify_phone_hint);
            findViewById(R.id.otpLayout).setVisibility(View.VISIBLE);
            btnContinue.setVisibility(View.VISIBLE);
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
            Toast.makeText(this, getString(R.string.msg_enter_6_digits), Toast.LENGTH_SHORT).show();
            return;
        }

        if ("PHONE".equals(type)) {
            verifyPhoneOTP(otp);
        } else {
            Toast.makeText(this, getString(R.string.msg_click_email_link), Toast.LENGTH_LONG).show();
        }
    }

    private void verifyPhoneOTP(String code) {
        if (verificationId == null) {
            Toast.makeText(this, getString(R.string.err_auth_id_not_found), Toast.LENGTH_SHORT).show();
            return;
        }

        setLoading(true);
        PhoneAuthCredential credential = PhoneAuthProvider.getCredential(verificationId, code);
        
        mAuth.signInWithCredential(credential).addOnCompleteListener(task -> {
            setLoading(false);
            if (task.isSuccessful()) {
                handleSuccess();
            } else {
                Toast.makeText(this, getString(R.string.err_invalid_otp), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void handleSuccess() {
        if ("FORGOT_PASSWORD".equals(flow)) {
            Toast.makeText(this, getString(R.string.msg_verification_success), Toast.LENGTH_SHORT).show();
            Intent intent = new Intent(VerificationActivity.this, ResetPasswordActivity.class);
            intent.putExtra("email", contact); 
            startActivity(intent);
            finish();
        } else {
            if ("PHONE".equals(type)) {
                if (userProfile == null || password == null) {
                    recoverUserData();
                }

                if (userProfile != null && password != null) {
                    setLoading(true);
                    mAuth.createUserWithEmailAndPassword(userProfile.email != null ? userProfile.email : (contact + "@homi.com"), password)
                            .addOnCompleteListener(task -> {
                                if (task.isSuccessful()) {
                                    String uid = task.getResult().getUser().getUid();
                                    userProfile.uid = uid;
                                    saveUserToFirestore(uid, userProfile);
                                } else {
                                    setLoading(false);
                                    String errorMsg = task.getException() != null ? task.getException().getMessage() : "Unknown";
                                    Toast.makeText(this, getString(R.string.msg_account_creation_error, errorMsg), Toast.LENGTH_LONG).show();
                                }
                            });
                } else {
                    Toast.makeText(this, getString(R.string.msg_registration_incomplete), Toast.LENGTH_SHORT).show();
                    finish();
                }
            } else {
                Toast.makeText(this, getString(R.string.msg_verification_success), Toast.LENGTH_SHORT).show();
                Intent intent = new Intent(VerificationActivity.this, LoginActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent);
                finish();
            }
        }
    }

    private void recoverUserData() {
        android.content.SharedPreferences prefs = getSharedPreferences("HomiAuth", MODE_PRIVATE);
        contact = prefs.getString("pending_contact", contact);
        type = prefs.getString("pending_type", type);
        flow = prefs.getString("pending_flow", flow);
        password = prefs.getString("pending_password", null);
        
        userProfile = new UserProfile();
        userProfile.fullName = prefs.getString("pending_username", "");
        userProfile.address = prefs.getString("pending_address", "");
        userProfile.dateOfBirth = prefs.getString("pending_dob", "");
        userProfile.gender = prefs.getString("pending_gender", "Other");
        userProfile.avatarUri = prefs.getString("pending_avatar", null);
        
        if ("EMAIL".equals(type)) {
            userProfile.email = contact;
        } else {
            userProfile.phone = contact;
        }
    }

    private void clearPendingData() {
        getSharedPreferences("HomiAuth", MODE_PRIVATE).edit().clear().apply();
    }

    private void saveUserToFirestore(String uid, UserProfile profile) {
        if (profile.avatarUri != null && (profile.avatarUri.startsWith("content://") || profile.avatarUri.startsWith("file://"))) {
            uploadAvatarAndSaveProfile(uid, profile);
        } else {
            submitProfileToFirestore(uid, profile);
        }
    }

    private void uploadAvatarAndSaveProfile(String uid, UserProfile profile) {
        FirebaseStorage storage = FirebaseStorage.getInstance();
        performUpload(storage, uid, profile, false);
    }

    private void performUpload(FirebaseStorage storage, String uid, UserProfile profile, boolean isRetry) {
        StorageReference storageRef = storage.getReference()
                .child("avatars/" + uid + "/" + UUID.randomUUID().toString() + ".jpg");

        Uri fileUri = Uri.parse(profile.avatarUri);

        storageRef.putFile(fileUri)
                .addOnSuccessListener(taskSnapshot -> {
                    storageRef.getDownloadUrl().addOnSuccessListener(uri -> {
                        profile.avatarUri = uri.toString();
                        submitProfileToFirestore(uid, profile);
                    });
                })
                .addOnFailureListener(e -> {
                    String msg = e.getMessage();
                    if (!isRetry && msg != null && (msg.contains("Object does not exist") || msg.contains("Bucket does not exist") || msg.contains("404"))) {
                        String currentBucket = storageRef.getBucket();
                        String nextBucket = currentBucket.contains("appspot.com") ? "homi-47e58.firebasestorage.app" : "homi-47e58.appspot.com";
                        try {
                            FirebaseStorage retryStorage = FirebaseStorage.getInstance("gs://" + nextBucket);
                            performUpload(retryStorage, uid, profile, true);
                        } catch (Exception ex) {
                            handleUploadFailure(e, profile);
                        }
                    } else {
                        handleUploadFailure(e, profile);
                    }
                });
    }

    private void handleUploadFailure(Exception e, UserProfile profile) {
        String msg = e.getMessage();
        Toast.makeText(this, msg, Toast.LENGTH_LONG).show();
        profile.avatarUri = null;
        submitProfileToFirestore(profile.uid, profile);
    }

    private void submitProfileToFirestore(String uid, UserProfile profile) {
        FirebaseFirestore.getInstance().collection("users")
                .document(uid)
                .set(profile)
                .addOnSuccessListener(aVoid -> {
                    grantWelcomeCoupon(uid);
                    setLoading(false);
                    clearPendingData();
                    Toast.makeText(this, getString(R.string.msg_registration_success), Toast.LENGTH_SHORT).show();
                    Intent intent = new Intent(VerificationActivity.this, LoginActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    startActivity(intent);
                    finish();
                })
                .addOnFailureListener(e -> {
                    setLoading(false);
                    Toast.makeText(this, getString(R.string.msg_failed_save_profile, e.getMessage()), Toast.LENGTH_SHORT).show();
                });
    }

    private void setLoading(boolean isLoading) {
        btnContinue.setEnabled(!isLoading);
        btnContinue.setAlpha(isLoading ? 0.5f : 1.0f);
    }

    private void grantWelcomeCoupon(String uid) {
        Map<String, Object> coupon = new HashMap<>();
        coupon.put("type", "welcome");
        coupon.put("code", "WELCOME10");
        coupon.put("discountType", "percent");
        coupon.put("discountValue", 10);
        coupon.put("isUsed", false);
        coupon.put("createdAt", com.google.firebase.Timestamp.now());
        java.util.Calendar cal = java.util.Calendar.getInstance();
        cal.add(java.util.Calendar.DAY_OF_MONTH, 30);
        coupon.put("expiryDate", new com.google.firebase.Timestamp(cal.getTime()));

        FirebaseFirestore.getInstance()
                .collection("users").document(uid)
                .collection("coupons")
                .document("welcome_" + uid)
                .set(coupon);

        FirebaseFirestore.getInstance()
                .collection("users").document(uid)
                .update("stats.coupons", com.google.firebase.firestore.FieldValue.increment(1));
    }
}
