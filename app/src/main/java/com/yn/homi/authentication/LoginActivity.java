package com.yn.homi.authentication;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.InputType;
import android.text.TextUtils;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.yn.homi.BaseActivity;
import com.yn.homi.HomeActivity;
import com.yn.homi.MainActivity;
import com.yn.homi.R;

import org.mindrot.jbcrypt.BCrypt;

import java.util.HashMap;
import java.util.Map;

public class LoginActivity extends BaseActivity {

    private EditText edtEmail, edtpsw;
    private CheckBox chkRemember;
    private Button btnLogin, btnforgot, btncreateacc;
    private ImageView btnGoogle, btnApple, btnFacebook, ivEyeLogin;
    private SharedPreferences sharedPreferences;
    private boolean isPasswordVisible = false;

    // Mock User Database: Email -> {Hashed Password, Role}
    private static final Map<String, UserInfo> MOCK_USER_DB = new HashMap<>();

    static {
        // Admin: Anhngoc@0605
        MOCK_USER_DB.put("nguyenthianhngoc060305@gmail.com", new UserInfo(
                BCrypt.hashpw("Anhngoc@0605", BCrypt.gensalt()), "ADMIN"));

        // Regular User: User@123
        MOCK_USER_DB.put("user@example.com", new UserInfo(
                BCrypt.hashpw("User@123", BCrypt.gensalt()), "USER"));
    }

    private static class UserInfo {
        String hashedPassword;
        String role;

        UserInfo(String hashedPassword, String role) {
            this.hashedPassword = hashedPassword;
            this.role = role;
        }
    }

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
        ivEyeLogin = findViewById(R.id.ivEyeLogin);

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

        ivEyeLogin.setOnClickListener(v -> togglePasswordVisibility(edtpsw, ivEyeLogin));
    }

    private void togglePasswordVisibility(EditText editText, ImageView eyeIcon) {
        if (isPasswordVisible) {
            // Hide password
            editText.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
            eyeIcon.setImageResource(R.drawable.ic_eye_off);
        } else {
            // Show password
            editText.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
            eyeIcon.setImageResource(R.drawable.ic_eye_on);
        }
        isPasswordVisible = !isPasswordVisible;
        // Keep cursor at the end
        editText.setSelection(editText.getText().length());
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
            
            UserInfo userInfo = MOCK_USER_DB.get(email);

            if (userInfo != null && BCrypt.checkpw(password, userInfo.hashedPassword)) {
                if (chkRemember.isChecked()) {
                    saveCredentials(email, password);
                } else {
                    clearCredentials();
                }
                
                String roleMessage = userInfo.role.equals("ADMIN") ? "Login successful as Admin!" : "Login successful!";
                Toast.makeText(LoginActivity.this, roleMessage, Toast.LENGTH_SHORT).show();

                // Navigate to Main Screen
                Intent intent = new Intent(LoginActivity.this, HomeActivity.class);
                intent.putExtra("USER_ROLE", userInfo.role);
                startActivity(intent);
                finish();
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
