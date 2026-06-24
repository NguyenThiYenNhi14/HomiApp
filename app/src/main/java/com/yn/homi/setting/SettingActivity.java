package com.yn.homi.setting;

import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.appcompat.widget.SwitchCompat;

import com.bumptech.glide.Glide;
import com.yn.homi.R;
import com.yn.homi.authentication.LoginActivity;
import com.yn.homi.setting.about.AboutActivity;
import com.yn.homi.setting.about.HelpActivity;
import com.yn.homi.setting.about.PrivacyPolicyActivity;
import com.yn.homi.setting.about.TermsConditionsActivity;
import com.yn.homi.setting.address.SavedAddressesActivity;
import com.yn.homi.setting.order.MyOrdersActivity;
import com.yn.homi.setting.preferences.LanguageActivity;
import com.yn.homi.setting.preferences.ThemePreference;
import com.yn.homi.setting.profile.PasswordSecurityActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.yn.homi.authentication.LoginActivity;
import com.yn.homi.setting.profile.EditProfileActivity;
import com.yn.homi.setting.profile.UserProfile;
import com.yn.homi.setting.wishlist.WishlistActivity;

public class SettingActivity extends AppCompatActivity {

    LinearLayout layoutProfile, layoutMyOrders, layoutMyWishlist;
    LinearLayout layoutLanguage, layoutAboutApp, layoutTerms, layoutPrivacy, layoutHelp, layoutLogout;
    LinearLayout layoutPasswordSecurity, layoutSavedAddresses;
    SwitchCompat switchNotification, switchDarkMode;

    ImageView imgAvatar;
    TextView tvUserName, tvUserEmail;

    UserProfile currentProfile;
    SharedPrefManager spm;
    FirebaseAuth mAuth;

    private final ActivityResultLauncher<Intent> editProfileLauncher =
            registerForActivityResult(
                    new ActivityResultContracts.StartActivityForResult(),
                    result -> {
                        if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                            UserProfile updated = null;
                            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
                                updated = result.getData().getParcelableExtra("UPDATED_PROFILE", UserProfile.class);
                            } else {
                                updated = result.getData().getParcelableExtra("UPDATED_PROFILE");
                            }

                            if (updated != null) {
                                currentProfile = updated;
                                updateProfileUI();
                                // Lưu vào local storage
                                spm.saveProfile(updated.fullName, updated.email, updated.avatarUri);
                            }
                        }
                    }
            );

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setting);

        spm = SharedPrefManager.getInstance(this);
        mAuth = FirebaseAuth.getInstance();

        initViews();
        checkUserStatus();
        setupClickListeners();
    }

    private void initViews() {
        layoutProfile          = findViewById(R.id.layoutProfile);
        layoutMyOrders         = findViewById(R.id.layoutMyOrders);
        layoutMyWishlist       = findViewById(R.id.layoutMyWishlist);
        layoutLanguage         = findViewById(R.id.layoutLanguage);
        layoutAboutApp         = findViewById(R.id.layoutAboutApp);
        layoutTerms            = findViewById(R.id.layoutTerms);
        layoutPrivacy          = findViewById(R.id.layoutPrivacy);
        layoutHelp             = findViewById(R.id.layoutHelp);
        layoutLogout           = findViewById(R.id.layoutLogout);
        layoutPasswordSecurity = findViewById(R.id.layoutPasswordSecurity);
        layoutSavedAddresses   = findViewById(R.id.layoutSavedAddresses);
        switchNotification     = findViewById(R.id.switchNotification);
        switchDarkMode         = findViewById(R.id.switchDarkMode);
        imgAvatar              = findViewById(R.id.imgAvatar);
        tvUserName             = findViewById(R.id.tvUserName);
        tvUserEmail            = findViewById(R.id.tvUserEmail);
    }

    private void checkUserStatus() {
        FirebaseUser user = mAuth.getCurrentUser();
        currentProfile = new UserProfile();

        if (user != null) {
            // Đã đăng nhập: Lấy data từ SharedPref hoặc Firebase (tạm thời dùng SharedPref)
            currentProfile.fullName = spm.getUserName().isEmpty() ? user.getDisplayName() : spm.getUserName();
            currentProfile.email = user.getEmail();
            currentProfile.avatarUri = spm.getAvatarUri();
            // Có thể lấy thêm phone, address... từ database nếu đã lưu
        } else {
            // Chưa đăng nhập
            currentProfile.fullName = getString(R.string.sign_in_register);
            currentProfile.email = getString(R.string.guest);
        }
        updateProfileUI();
    }

    private void updateProfileUI() {
        tvUserName.setText(currentProfile.fullName != null && !currentProfile.fullName.isEmpty() 
                ? currentProfile.fullName : getString(R.string.sign_in_register));
        tvUserEmail.setText(currentProfile.email != null && !currentProfile.email.isEmpty() 
                ? currentProfile.email : getString(R.string.guest));

        if (currentProfile.avatarUri != null && !currentProfile.avatarUri.isEmpty()) {
            imgAvatar.clearColorFilter();
            Glide.with(this)
                    .load(Uri.parse(currentProfile.avatarUri))
                    .circleCrop()
                    .placeholder(R.drawable.icon_account_circle)
                    .into(imgAvatar);
        } else {
            imgAvatar.setImageResource(R.drawable.icon_account_circle);
            imgAvatar.setColorFilter(Color.parseColor("#333333"));
        }
    }

    private void setupClickListeners() {

        // ===== PROFILE =====
        layoutProfile.setOnClickListener(v -> {
            if (mAuth.getCurrentUser() != null) {
                Intent intent = new Intent(this, EditProfileActivity.class);
                intent.putExtra("USER_PROFILE", currentProfile);
                editProfileLauncher.launch(intent);
            } else {
                startActivity(new Intent(this, LoginActivity.class));
            }
        });

        // ===== ACCOUNT =====
        layoutPasswordSecurity.setOnClickListener(v ->
                startActivity(new Intent(this, PasswordSecurityActivity.class))
        );

        layoutSavedAddresses.setOnClickListener(v ->
                startActivity(new Intent(this, SavedAddressesActivity.class))
        );

        // ===== SHOPPING =====
        layoutMyOrders.setOnClickListener(v ->
                startActivity(new Intent(this, MyOrdersActivity.class))
        );

        layoutMyWishlist.setOnClickListener(v ->
                startActivity(new Intent(this, WishlistActivity.class))
        );

        // ===== PREFERENCES =====
        layoutLanguage.setOnClickListener(v ->
                startActivity(new Intent(this, LanguageActivity.class))
        );

        switchDarkMode.setOnCheckedChangeListener((btn, isChecked) -> {
            ThemePreference.setDarkMode(this, isChecked);
            int mode = isChecked
                    ? AppCompatDelegate.MODE_NIGHT_YES
                    : AppCompatDelegate.MODE_NIGHT_NO;
            AppCompatDelegate.setDefaultNightMode(mode);
        });

        switchNotification.setOnCheckedChangeListener((btn, isChecked) ->
                spm.setNotificationsEnabled(isChecked)
        );

        // ===== ABOUT / LEGAL =====
        layoutAboutApp.setOnClickListener(v ->
                startActivity(new Intent(this, AboutActivity.class))
        );

        layoutTerms.setOnClickListener(v ->
                startActivity(new Intent(this, TermsConditionsActivity.class))
        );

        layoutPrivacy.setOnClickListener(v ->
                startActivity(new Intent(this, PrivacyPolicyActivity.class))
        );

        layoutHelp.setOnClickListener(v ->
                startActivity(new Intent(this, HelpActivity.class))
        );

        // ===== LOGOUT =====
        layoutLogout.setOnClickListener(v -> showLogoutDialog());
    }

    private void showLogoutDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View view = getLayoutInflater().inflate(R.layout.dialog_logout, null);
        builder.setView(view);

        AlertDialog dialog = builder.create();
        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        }

        View btnLogout = view.findViewById(R.id.btnLogout);
        View btnCancel = view.findViewById(R.id.btnCancel);

        btnLogout.setOnClickListener(v -> {
            dialog.dismiss();
            Toast.makeText(this, "Logged out", Toast.LENGTH_SHORT).show();
            
            Intent i = new Intent(this, LoginActivity.class);
            i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(i);
        });

        btnCancel.setOnClickListener(v -> dialog.dismiss());
        dialog.show();
    }
}