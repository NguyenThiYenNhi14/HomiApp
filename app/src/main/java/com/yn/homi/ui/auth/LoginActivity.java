package com.yn.homi.ui.auth;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
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

import java.util.HashMap;
import java.util.Map;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.firestore.FirebaseFirestore;
import com.yn.homi.ui.home.HomeActivity;
import com.yn.homi.R;
import com.yn.homi.ui.profile.profile.UserProfile;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;

public class LoginActivity extends com.yn.homi.core.BaseActivity {

    private EditText edtEmail, edtpsw;
    private CheckBox chkRemember;
    private Button btnLogin, btnforgot;
    private TextView btncreateacc;
    private View btnGoogle;
    private ImageView ivEyeLogin, ivLogo;
    private SharedPreferences sharedPreferences;
    private boolean isPasswordVisible = false;
    private FirebaseAuth mAuth;
    private GoogleSignInClient mGoogleSignInClient;
    private ActivityResultLauncher<Intent> googleSignInLauncher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        mAuth = FirebaseAuth.getInstance();
        setupGoogleSignIn();
        initViews();
        setupListeners();
        loadSavedCredentials();
    }

    private void setupGoogleSignIn() {
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();

        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);

        googleSignInLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == RESULT_OK) {
                        Intent data = result.getData();
                        Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
                        try {
                            GoogleSignInAccount account = task.getResult(ApiException.class);
                            firebaseAuthWithGoogle(account.getIdToken());
                        } catch (ApiException e) {
                            Toast.makeText(this, "Google sign in failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    }
                }
        );
    }

    private void firebaseAuthWithGoogle(String idToken) {
        setLoading(true);
        AuthCredential credential = GoogleAuthProvider.getCredential(idToken, null);
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        FirebaseUser user = mAuth.getCurrentUser();
                        checkUserInFirestore(user);
                    } else {
                        setLoading(false);
                        Toast.makeText(this, "Authentication Failed.", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void checkUserInFirestore(FirebaseUser user) {
        FirebaseFirestore.getInstance().collection("users")
                .document(user.getUid())
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (!documentSnapshot.exists()) {
                        // Nếu user mới đăng nhập Google lần đầu, tạo profile cho họ
                        UserProfile profile = new UserProfile();
                        profile.uid = user.getUid();
                        profile.fullName = user.getDisplayName();
                        profile.email = user.getEmail();
                        profile.phone = user.getPhoneNumber();
                        profile.avatarUri = user.getPhotoUrl() != null ? user.getPhotoUrl().toString() : "";
                        
                        FirebaseFirestore.getInstance().collection("users")
                                .document(user.getUid())
                                .set(profile)
                                .addOnCompleteListener(t -> {
                                    syncManagers();
                                    grantWelcomeCoupon(user.getUid());
                                    navigateToHome("USER");
                                });
                    } else {
                        String role = documentSnapshot.getString("role");
                        syncManagers();
                        navigateToHome(role != null ? role : "USER");
                    }
                })
                .addOnFailureListener(e -> navigateToHome("USER"));
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

        FirebaseFirestore.getInstance().collection("users").document(uid)
                .collection("coupons").document("welcome_" + uid).set(coupon);
        
        FirebaseFirestore.getInstance().collection("users").document(uid)
                .update("stats.coupons", com.google.firebase.firestore.FieldValue.increment(1));
    }

    private void navigateToHome(String role) {
        setLoading(false);
        String roleMessage = role.equals("ADMIN") ? "Login successful as Admin!" : "Login successful!";
        Toast.makeText(LoginActivity.this, roleMessage, Toast.LENGTH_SHORT).show();

        Intent intent = new Intent(LoginActivity.this, HomeActivity.class);
        intent.putExtra("USER_ROLE", role);
        startActivity(intent);
        finish();
    }


    private void initViews() {
        edtEmail = findViewById(R.id.edtEmail);
        edtpsw = findViewById(R.id.edtpsw);
        chkRemember = findViewById(R.id.chkRemember);
        btnLogin = findViewById(R.id.btnLogin);
        btnforgot = findViewById(R.id.btnforgot);
        btncreateacc = findViewById(R.id.btncreateacc);
        
        // Social buttons (IDs from activity_login.xml)
        btnGoogle = findViewById(R.id.btnGoogle);
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

        btnGoogle.setOnClickListener(v -> {
            Intent signInIntent = mGoogleSignInClient.getSignInIntent();
            googleSignInLauncher.launch(signInIntent);
        });

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

        setLoading(true);

        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, task -> {
                    setLoading(false);
                    if (task.isSuccessful()) {
                        FirebaseUser user = mAuth.getCurrentUser();
                        if (user != null) {
                            if (chkRemember.isChecked()) {
                                saveCredentials(email, password);
                            } else {
                                clearCredentials();
                            }

                            // Kiểm tra role từ Firestore (giả sử có field role trong users collection)
                            FirebaseFirestore.getInstance().collection("users")
                                    .document(user.getUid())
                                    .get()
                                    .addOnSuccessListener(documentSnapshot -> {
                                        String role = documentSnapshot.getString("role");
                                        if (role == null) role = "USER";
                                        
                                        String roleMessage = role.equals("ADMIN") ? "Login successful as Admin!" : "Login successful!";
                                        Toast.makeText(LoginActivity.this, roleMessage, Toast.LENGTH_SHORT).show();

                                        syncManagers();
                                        Intent intent = new Intent(LoginActivity.this, HomeActivity.class);
                                        intent.putExtra("USER_ROLE", role);
                                        startActivity(intent);
                                        finish();
                                    })
                                    .addOnFailureListener(e -> {
                                        // Mặc định là USER nếu không query được role
                                        Toast.makeText(LoginActivity.this, "Login successful!", Toast.LENGTH_SHORT).show();
                                        syncManagers();
                                        Intent intent = new Intent(LoginActivity.this, HomeActivity.class);
                                        intent.putExtra("USER_ROLE", "USER");
                                        startActivity(intent);
                                        finish();
                                    });
                        }
                    } else {
                        Toast.makeText(LoginActivity.this, "Authentication failed: " + task.getException().getMessage(),
                                Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void syncManagers() {
        com.yn.homi.ui.cart.CartManager.getInstance(this).syncFromFirestore();
        new com.yn.homi.utils.FavoritesManager(this).syncFromFirestore();
        com.yn.homi.ui.profile.order.OrderManager.getInstance(this).syncFromFirestore();
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
