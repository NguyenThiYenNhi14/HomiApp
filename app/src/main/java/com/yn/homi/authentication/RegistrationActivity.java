package com.yn.homi.authentication;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
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

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;

import com.yn.homi.R;

import java.util.Calendar;
import java.util.Locale;

public class RegistrationActivity extends AppCompatActivity {

    private EditText edtUsername, edtEmail, edtPhone, edtAddress, edtPassword, edtDob;
    private Spinner spinnerGender;
    private FrameLayout layoutAvatar;
    private ImageView imgAvatar;
    private AppCompatButton btnSignUp;
    private ImageButton btnBack;
    private ImageView btnDatePicker;
    private TextView txtLogin;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registration);

        initViews();
        setupGenderSpinner();
        setupListeners();
    }

    private void initViews() {
        btnBack = findViewById(R.id.btnBack);
        layoutAvatar = findViewById(R.id.layoutAvatar);
        imgAvatar = findViewById(R.id.imgAvatar);
        edtUsername = findViewById(R.id.edtUsername);
        edtEmail = findViewById(R.id.edtEmail);
        edtPhone = findViewById(R.id.edtPhone);
        edtAddress = findViewById(R.id.edtAddress);
        edtPassword = findViewById(R.id.edtPassword);
        spinnerGender = findViewById(R.id.spinnerGender);
        edtDob = findViewById(R.id.edtDob);
        btnDatePicker = findViewById(R.id.btnDatePicker);
        btnSignUp = findViewById(R.id.btnSignUp);
        txtLogin = findViewById(R.id.txtLogin);
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

        layoutAvatar.setOnClickListener(v -> Toast.makeText(RegistrationActivity.this, "Open Gallery Coming Soon!", Toast.LENGTH_SHORT).show());

        btnDatePicker.setOnClickListener(v -> showDatePicker());
        // Also allow clicking the EditText if it's not focusable
        edtDob.setOnClickListener(v -> showDatePicker());

        btnSignUp.setOnClickListener(v -> handleSignUp());
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
        String email = edtEmail.getText().toString().trim();
        String phone = edtPhone.getText().toString().trim();
        String address = edtAddress.getText().toString().trim();
        String password = edtPassword.getText().toString().trim();
        String dob = edtDob.getText().toString().trim();

        if (validateInput(username, email, phone, address, password, dob)) {
            setLoading(true);

            // Mock Registration API call
            new Handler(Looper.getMainLooper()).postDelayed(() -> {
                setLoading(false);
                Toast.makeText(RegistrationActivity.this, "Verification code sent to " + email, Toast.LENGTH_LONG).show();
                
                Intent intent = new Intent(RegistrationActivity.this, VerificationActivity.class);
                intent.putExtra("contact", email);
                intent.putExtra("type", "EMAIL");
                intent.putExtra("flow", "REGISTER");
                startActivity(intent);
            }, 1500);
        }
    }

    private boolean validateInput(String username, String email, String phone, String address, String password, String dob) {
        if (TextUtils.isEmpty(username)) {
            edtUsername.setError("Username required");
            return false;
        }
        if (TextUtils.isEmpty(email) || !Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            edtEmail.setError("Valid email required");
            return false;
        }
        if (TextUtils.isEmpty(phone) || phone.length() < 10) {
            edtPhone.setError("Valid phone number required");
            return false;
        }
        if (TextUtils.isEmpty(address)) {
            edtAddress.setError("Address required");
            return false;
        }
        if (TextUtils.isEmpty(password) || password.length() < 6) {
            edtPassword.setError("Password must be at least 6 characters");
            return false;
        }
        if (TextUtils.isEmpty(dob)) {
            Toast.makeText(this, "Please select Date of Birth", Toast.LENGTH_SHORT).show();
            return false;
        }
        return true;
    }

    private void setLoading(boolean isLoading) {
        btnSignUp.setEnabled(!isLoading);
        btnSignUp.setAlpha(isLoading ? 0.5f : 1.0f);
    }
}
