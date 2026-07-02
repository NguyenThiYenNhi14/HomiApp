package com.yn.homi.ui.auth;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.InputType;
import android.text.TextUtils;
import android.util.Patterns;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import android.net.Uri;
import android.provider.MediaStore;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;

import com.google.firebase.auth.ActionCodeSettings;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.PhoneAuthProvider;
import com.yn.homi.R;
import com.yn.homi.ui.profile.profile.UserProfile;
import com.yn.homi.utils.OTPManager;

import java.util.HashMap;
import java.util.Map;
import java.util.Calendar;
import java.util.Locale;

public class RegistrationActivity extends AppCompatActivity {

    private EditText edtUsername, edtContact, edtAddress, edtPassword, edtDob;
    private Spinner spinnerGender;
    private FrameLayout layoutAvatar;
    private ImageView imgAvatar, ivEyeRegister;
    private AppCompatButton btnSignUp;
    private ImageButton btnBack;
    private ImageView btnDatePicker;
    private TextView txtLogin;
    private boolean isPasswordVisible = false;
    private OTPManager otpManager;
    private Uri selectedImageUri;

    private final ActivityResultLauncher<Intent> pickImageLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                    selectedImageUri = result.getData().getData();
                    imgAvatar.setImageURI(selectedImageUri);
                }
            }
    );

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registration);

        initViews();
        setupGenderSpinner();
        setupListeners();
        otpManager = new OTPManager(this);
    }

    private void initViews() {
        btnBack = findViewById(R.id.btnBack);
        layoutAvatar = findViewById(R.id.layoutAvatar);
        imgAvatar = findViewById(R.id.imgAvatar);
        edtUsername = findViewById(R.id.edtUsername);
        edtContact = findViewById(R.id.edtContact);
        edtAddress = findViewById(R.id.edtAddress);
        edtPassword = findViewById(R.id.edtPassword);
        spinnerGender = findViewById(R.id.spinnerGender);
        edtDob = findViewById(R.id.edtDob);
        btnDatePicker = findViewById(R.id.btnDatePicker);
        btnSignUp = findViewById(R.id.btnSignUp);
        txtLogin = findViewById(R.id.txtLogin);
        ivEyeRegister = findViewById(R.id.ivEyeRegister);

        setMandatoryHints();
    }

    private void setMandatoryHints() {
        String redStar = " <font color='#FF0000'>*</font>";
        
        // Mandatory fields
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
            edtUsername.setHint(android.text.Html.fromHtml(getString(R.string.str_username) + redStar, android.text.Html.FROM_HTML_MODE_LEGACY));
            edtContact.setHint(android.text.Html.fromHtml("Email or Phone" + redStar, android.text.Html.FROM_HTML_MODE_LEGACY));
            edtPassword.setHint(android.text.Html.fromHtml(getString(R.string.password) + redStar, android.text.Html.FROM_HTML_MODE_LEGACY));
        } else {
            edtUsername.setHint(android.text.Html.fromHtml(getString(R.string.str_username) + redStar));
            edtContact.setHint(android.text.Html.fromHtml("Email or Phone" + redStar));
            edtPassword.setHint(android.text.Html.fromHtml(getString(R.string.password) + redStar));
        }
        
        // Optional fields - Set clear (Optional) hint
        edtAddress.setHint(getString(R.string.str_address_label) + " (Optional)");
        edtDob.setHint(getString(R.string.str_dob_label) + " (Optional)");
    }

    private void setupGenderSpinner() {
        String[] genders = {"Male", "Female", "Other"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, genders);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerGender.setAdapter(adapter);
    }

    private void setupListeners() {
        btnBack.setOnClickListener(v -> finish());
        txtLogin.setOnClickListener(v -> finish());

        layoutAvatar.setOnClickListener(v -> openGallery());

        btnDatePicker.setOnClickListener(v -> showDatePicker());
        // Also allow clicking the EditText if it's not focusable
        edtDob.setOnClickListener(v -> showDatePicker());

        btnSignUp.setOnClickListener(v -> handleSignUp());
        ivEyeRegister.setOnClickListener(v -> togglePasswordVisibility());
    }

    private void openGallery() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        pickImageLauncher.launch(intent);
    }

    private void togglePasswordVisibility() {
        if (isPasswordVisible) {
            edtPassword.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
            ivEyeRegister.setImageResource(R.drawable.ic_eye_off);
        } else {
            edtPassword.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
            ivEyeRegister.setImageResource(R.drawable.ic_eye_on);
        }
        isPasswordVisible = !isPasswordVisible;
        edtPassword.setSelection(edtPassword.getText().length());
    }

    private void showDatePicker() {
        final Calendar c = Calendar.getInstance();
        int year = c.get(Calendar.YEAR);
        int month = c.get(Calendar.MONTH);
        int day = c.get(Calendar.DAY_OF_MONTH);

        DatePickerDialog datePickerDialog = new DatePickerDialog(this,
                (view, year1, monthOfYear, dayOfMonth) -> {
                    String date = String.format(Locale.getDefault(), "%02d/%02d/%d", dayOfMonth, (monthOfYear + 1), year1);
                    edtDob.setText(date);
                }, year, month, day);
        datePickerDialog.show();
    }

    private void handleSignUp() {
        String username = edtUsername.getText().toString().trim();
        String contact = edtContact.getText().toString().trim();
        String address = edtAddress.getText().toString().trim();
        String password = edtPassword.getText().toString().trim();
        String dob = edtDob.getText().toString().trim();
        String gender = spinnerGender.getSelectedItem().toString();

        if (validateInput(username, contact, address, password, dob)) {
            setLoading(true);

            UserProfile userProfile = new UserProfile();
            userProfile.fullName = username;
            userProfile.address = address;
            userProfile.dateOfBirth = dob;
            userProfile.gender = gender;
            if (selectedImageUri != null) {
                String persistentUri = copyUriToInternalStorage(selectedImageUri);
                if (persistentUri != null) {
                    userProfile.avatarUri = persistentUri;
                } else {
                    userProfile.avatarUri = selectedImageUri.toString();
                }
            }

            boolean isEmail = Patterns.EMAIL_ADDRESS.matcher(contact).matches();

            // Save info to SharedPreferences to prevent loss if app is killed
            getSharedPreferences("HomiAuth", MODE_PRIVATE).edit()
                    .putString("pending_contact", contact)
                    .putString("pending_type", isEmail ? "EMAIL" : "PHONE")
                    .putString("pending_flow", "REGISTER")
                    .putString("pending_password", password)
                    .putString("pending_username", username)
                    .putString("pending_address", address)
                    .putString("pending_dob", dob)
                    .putString("pending_gender", gender)
                    .putString("pending_avatar", userProfile.avatarUri)
                    .apply();

            if (isEmail) {
                userProfile.email = contact;
                createUserInFirebase(userProfile, password);
            } else {
                // Assume it's a Phone Number
                userProfile.phone = contact;
                // Note: Firebase Phone Auth requires Billing for real SMS
                otpManager.sendOTPToPhone(contact, new OTPManager.OTPCallback() {
                    @Override
                    public void onCodeSent(String verificationId, com.google.firebase.auth.PhoneAuthProvider.ForceResendingToken token) {
                        setLoading(false);
                        Intent intent = new Intent(RegistrationActivity.this, VerificationActivity.class);
                        intent.putExtra("contact", contact);
                        intent.putExtra("verificationId", verificationId);
                        intent.putExtra("type", "PHONE");
                        intent.putExtra("flow", "REGISTER");
                        intent.putExtra("user_profile", userProfile);
                        intent.putExtra("password", password);
                        startActivity(intent);
                    }

                    @Override
                    public void onVerificationSuccess() {
                        setLoading(false);
                        // Case for automatic verification (Instant Verification)
                        Intent intent = new Intent(RegistrationActivity.this, VerificationActivity.class);
                        intent.putExtra("contact", contact);
                        intent.putExtra("type", "PHONE");
                        intent.putExtra("flow", "REGISTER");
                        intent.putExtra("user_profile", userProfile);
                        intent.putExtra("password", password);
                        intent.putExtra("is_auto_verified", true);
                        startActivity(intent);
                    }

                    @Override
                    public void onFailure(String error) {
                        setLoading(false);
                        Toast.makeText(RegistrationActivity.this, "SMS delivery error: " + error, Toast.LENGTH_LONG).show();
                    }
                });
            }
        }
    }

    private void createUserInFirebase(UserProfile profile, String password) {
        com.google.firebase.auth.FirebaseAuth.getInstance()
                .createUserWithEmailAndPassword(profile.email, password)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        com.google.firebase.auth.FirebaseUser user = task.getResult().getUser();
                        if (user != null) {
                            user.sendEmailVerification(); // Send traditional email verification
                            String uid = user.getUid();
                            saveUserToFirestore(uid, profile);
                        }
                    } else {
                        setLoading(false);
                        Toast.makeText(this, "Registration error: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void saveUserToFirestore(String uid, UserProfile profile) {
        com.google.firebase.firestore.FirebaseFirestore.getInstance()
                .collection("users")
                .document(uid)
                .set(profile)
                .addOnSuccessListener(aVoid -> {
                    grantWelcomeCoupon(uid);
                    setLoading(false);
                    Toast.makeText(this, "Registration successful!", Toast.LENGTH_SHORT).show();
                    
                    // Switch to check email notification screen
                    Intent intent = new Intent(this, VerificationActivity.class);
                    intent.putExtra("contact", profile.email != null ? profile.email : profile.phone);
                    intent.putExtra("type", profile.email != null ? "EMAIL" : "PHONE");
                    intent.putExtra("flow", "REGISTER");
                    startActivity(intent);
                    finish();
                })
                .addOnFailureListener(e -> {
                    setLoading(false);
                    Toast.makeText(this, "Failed to save profile: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    private boolean validateInput(String username, String contact, String address, String password, String dob) {
        if (TextUtils.isEmpty(username)) {
            edtUsername.setError("Username is required");
            edtUsername.requestFocus();
            return false;
        }
        
        boolean isEmail = Patterns.EMAIL_ADDRESS.matcher(contact).matches();
        boolean isPhone = Patterns.PHONE.matcher(contact).matches() && contact.length() >= 10;

        if (!isEmail && !isPhone) {
            edtContact.setError("Enter a valid email or phone number");
            edtContact.requestFocus();
            return false;
        }

        if (TextUtils.isEmpty(password) || password.length() < 6) {
            edtPassword.setError("Password must be at least 6 characters");
            edtPassword.requestFocus();
            return false;
        }

        return true;
    }

    private String copyUriToInternalStorage(Uri uri) {
        try {
            java.io.InputStream inputStream = getContentResolver().openInputStream(uri);
            java.io.File tempFile = new java.io.File(getCacheDir(), "temp_avatar_" + System.currentTimeMillis() + ".jpg");
            java.io.FileOutputStream outputStream = new java.io.FileOutputStream(tempFile);
            byte[] buffer = new byte[1024];
            int read;
            while ((read = inputStream.read(buffer)) != -1) {
                outputStream.write(buffer, 0, read);
            }
            outputStream.close();
            inputStream.close();
            return Uri.fromFile(tempFile).toString();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    private void setLoading(boolean isLoading) {
        btnSignUp.setEnabled(!isLoading);
        btnSignUp.setAlpha(isLoading ? 0.5f : 1.0f);
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

        com.google.firebase.firestore.FirebaseFirestore.getInstance()
                .collection("users").document(uid)
                .collection("coupons")
                .document("welcome_" + uid)
                .set(coupon);

        com.google.firebase.firestore.FirebaseFirestore.getInstance()
                .collection("users").document(uid)
                .update("stats.coupons", com.google.firebase.firestore.FieldValue.increment(1));
    }
}
