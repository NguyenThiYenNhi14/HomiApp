package com.yn.homi.setting.profile;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.yn.homi.R;

public class PasswordSecurityActivity extends AppCompatActivity {

    // UI components
    private TextInputLayout tilCurrentPassword, tilNewPassword, tilConfirmPassword;
    private TextInputEditText etCurrentPassword, etNewPassword, etConfirmPassword;
    private Button btnSavePassword;
    private View layoutStrength, bar1, bar2, bar3;
    private TextView tvStrengthLabel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_password_security);

        initViews();
        setupToolbar();
        setupPasswordStrength();
        setupSaveButton();
    }

    // Step 1: Link all views to variables
    private void initViews() {
        tilCurrentPassword  = findViewById(R.id.tilCurrentPassword);
        tilNewPassword      = findViewById(R.id.tilNewPassword);
        tilConfirmPassword  = findViewById(R.id.tilConfirmPassword);
        etCurrentPassword   = findViewById(R.id.etCurrentPassword);
        etNewPassword       = findViewById(R.id.etNewPassword);
        etConfirmPassword   = findViewById(R.id.etConfirmPassword);
        btnSavePassword     = findViewById(R.id.btnSavePassword);
        layoutStrength      = findViewById(R.id.layoutStrength);
        bar1                = findViewById(R.id.bar1);
        bar2                = findViewById(R.id.bar2);
        bar3                = findViewById(R.id.bar3);
        tvStrengthLabel     = findViewById(R.id.tvStrengthLabel);
    }

    // Step 2: Back button
    private void setupToolbar() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
        toolbar.setNavigationOnClickListener(v ->
                getOnBackPressedDispatcher().onBackPressed());
    }

    // Step 3: Show password strength while typing
    private void setupPasswordStrength() {
        etNewPassword.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void afterTextChanged(Editable s) {
                String password = s.toString();

                if (password.isEmpty()) {
                    layoutStrength.setVisibility(View.GONE);
                    return;
                }

                layoutStrength.setVisibility(View.VISIBLE);
                int strength = calculateStrength(password);
                updateStrengthUI(strength);
            }
        });
    }

    // Calculate strength: 1=Weak, 2=Medium, 3=Strong
    private int calculateStrength(String password) {
        int score = 0;
        if (password.length() >= 8) score++;
        if (password.matches(".*[A-Z].*") && password.matches(".*[0-9].*")) score++;
        if (password.matches(".*[!@#$%^&*()].*")) score++;
        return Math.max(score, 1);
    }

    // Update colored bars
    private void updateStrengthUI(int strength) {
        int gray   = 0xFFDDDDDD;
        int red    = 0xFFE53935;
        int orange = 0xFFFF9800;
        int green  = 0xFF43A047;

        switch (strength) {
            case 1: // Weak — only bar1 red
                bar1.setBackgroundColor(red);
                bar2.setBackgroundColor(gray);
                bar3.setBackgroundColor(gray);
                tvStrengthLabel.setText("Weak");
                tvStrengthLabel.setTextColor(red);
                break;
            case 2: // Medium — bar1+bar2 orange
                bar1.setBackgroundColor(orange);
                bar2.setBackgroundColor(orange);
                bar3.setBackgroundColor(gray);
                tvStrengthLabel.setText("Medium");
                tvStrengthLabel.setTextColor(orange);
                break;
            case 3: // Strong — all bars green
                bar1.setBackgroundColor(green);
                bar2.setBackgroundColor(green);
                bar3.setBackgroundColor(green);
                tvStrengthLabel.setText("Strong");
                tvStrengthLabel.setTextColor(green);
                break;
        }
    }

    // Step 4: Validate and save
    private void setupSaveButton() {
        btnSavePassword.setOnClickListener(v -> {

            // Clear old errors
            tilCurrentPassword.setError(null);
            tilNewPassword.setError(null);
            tilConfirmPassword.setError(null);

            String current = etCurrentPassword.getText().toString().trim();
            String newPass  = etNewPassword.getText().toString().trim();
            String confirm  = etConfirmPassword.getText().toString().trim();

            // Validate — check each field
            if (current.isEmpty()) {
                tilCurrentPassword.setError("Please enter current password");
                etCurrentPassword.requestFocus();
                return;
            }

            if (newPass.isEmpty()) {
                tilNewPassword.setError("Please enter new password");
                etNewPassword.requestFocus();
                return;
            }

            if (newPass.length() < 8) {
                tilNewPassword.setError("Password must be at least 8 characters");
                etNewPassword.requestFocus();
                return;
            }

            if (confirm.isEmpty()) {
                tilConfirmPassword.setError("Please confirm your new password");
                etConfirmPassword.requestFocus();
                return;
            }

            if (!newPass.equals(confirm)) {
                tilConfirmPassword.setError("Passwords do not match");
                etConfirmPassword.requestFocus();
                return;
            }

            if (newPass.equals(current)) {
                tilNewPassword.setError("New password must be different from current");
                etNewPassword.requestFocus();
                return;
            }

            // All good — show success
            Toast.makeText(this,
                    "Password changed successfully!", Toast.LENGTH_SHORT).show();
            finish(); // go back to previous screen
        });
    }
}