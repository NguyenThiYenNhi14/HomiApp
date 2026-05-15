package com.yn.homi.model;

import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;


import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.appcompat.widget.SwitchCompat;

import com.yn.homi.R;
import com.yn.homi.model.SharedPrefManager;
import com.yn.homi.model.TermsConditionsActivity;
import com.yn.homi.model.ThemePreference;
import com.yn.homi.model.UserProfile;
import com.yn.homi.model.WishlistActivity;
import com.yn.homi.model.YourProfileActivity;
import com.bumptech.glide.Glide;
import com.google.android.material.bottomsheet.BottomSheetDialog;

public class SettingActivity extends AppCompatActivity {

    LinearLayout layoutProfile, layoutMyOrders, layoutMyWishlist;
    LinearLayout layoutLanguage, layoutAboutApp, layoutTerms, layoutPrivacy, layoutHelp, layoutLogout;
    LinearLayout layoutPasswordSecurity, layoutPaymentMethods, layoutSavedAddresses;
    SwitchCompat switchNotification, switchDarkMode;

    ImageView imgAvatar;
    TextView tvUserName, tvUserEmail;

    UserProfile currentProfile;
    SharedPrefManager spm;

    // FIX 1: Dùng ActivityResultLauncher thay startActivityForResult (đã đúng, giữ nguyên)
    private final ActivityResultLauncher<Intent> profileLauncher =
            registerForActivityResult(
                    new ActivityResultContracts.StartActivityForResult(),
                    result -> {
                        if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                            UserProfile updated = result.getData()
                                    .getParcelableExtra("UPDATED_PROFILE");

                            if (updated != null) {
                                currentProfile = updated;
                                tvUserName.setText(updated.fullName);
                                tvUserEmail.setText(updated.email);

                                if (updated.avatarUri != null && !updated.avatarUri.isEmpty()) {
                                    imgAvatar.clearColorFilter();
                                    Glide.with(this)
                                            .load(Uri.parse(updated.avatarUri))
                                            .circleCrop()
                                            .placeholder(R.drawable.icon_account_circle)
                                            .into(imgAvatar);
                                } else {
                                    imgAvatar.setImageResource(R.drawable.icon_account_circle);
                                    imgAvatar.setColorFilter(Color.parseColor("#333333"));
                                }

                                // Lưu profile vào SharedPreferences
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

        currentProfile          = new UserProfile();
        currentProfile.fullName = spm.getUserName();
        currentProfile.email    = spm.getUserEmail();

        // Link views
        layoutProfile           = findViewById(R.id.layoutProfile);
        layoutMyOrders          = findViewById(R.id.layoutMyOrders);
        layoutMyWishlist        = findViewById(R.id.layoutMyWishlist);
        layoutLanguage          = findViewById(R.id.layoutLanguage);
        layoutAboutApp          = findViewById(R.id.layoutAboutApp);
        layoutTerms             = findViewById(R.id.layoutTerms);
        layoutPrivacy           = findViewById(R.id.layoutPrivacy);
        layoutHelp              = findViewById(R.id.layoutHelp);
        layoutLogout            = findViewById(R.id.layoutLogout);
        layoutPasswordSecurity  = findViewById(R.id.layoutPasswordSecurity);
        layoutPaymentMethods    = findViewById(R.id.layoutPaymentMethods);
        layoutSavedAddresses    = findViewById(R.id.layoutSavedAddresses);
        switchNotification      = findViewById(R.id.switchNotification);
        switchDarkMode          = findViewById(R.id.switchDarkMode);
        imgAvatar               = findViewById(R.id.imgAvatar);
        tvUserName              = findViewById(R.id.tvUserName);
        tvUserEmail             = findViewById(R.id.tvUserEmail);

        // Hiển thị tên và email từ profile đã load
        tvUserName.setText(currentProfile.fullName);
        tvUserEmail.setText(currentProfile.email);

        //Load trạng thái dark mode đã lưu, set switch đúng trạng thái
        boolean isDark = ThemePreference.isDarkMode(this);
        switchDarkMode.setChecked(isDark);

        //Load trạng thái notification đã lưu
        boolean notifEnabled = spm.isNotificationsEnabled();
        switchNotification.setChecked(notifEnabled);

        setupClickListeners();

        String savedAvatarUri = spm.getAvatarUri();
        if (!savedAvatarUri.isEmpty()) {
            imgAvatar.clearColorFilter();
            Glide.with(this)
                    .load(Uri.parse(savedAvatarUri))
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
            Intent intent = new Intent(this, YourProfileActivity.class);
            intent.putExtra("USER_PROFILE", currentProfile);
            profileLauncher.launch(intent);
        });

        // ===== ACCOUNT =====
        layoutPasswordSecurity.setOnClickListener(v ->
                startActivity(new Intent(this, PasswordSecurityActivity.class))
        );

        layoutPaymentMethods.setOnClickListener(v ->
                startActivity(new Intent(this, PaymentMethodsActivity.class))
        );

        layoutSavedAddresses.setOnClickListener(v ->
                startActivity(new Intent(this, SavedAddressesActivity.class))
        );

        // ===== SHOPPING =====
        layoutMyOrders.setOnClickListener(v -> startActivity(new Intent(this, MyOrdersActivity.class)) );

        layoutMyWishlist.setOnClickListener(v -> startActivity(new Intent(this, WishlistActivity.class)) );

        // ===== PREFERENCES =====

        layoutLanguage.setOnClickListener(v ->
                startActivity(new Intent(this, LanguageActivity.class))
        );

        // Dark mode — lưu trạng thái vào ThemePreference
        switchDarkMode.setOnCheckedChangeListener((btn, isChecked) -> {
            ThemePreference.setDarkMode(this, isChecked);
            int mode = isChecked
                    ? AppCompatDelegate.MODE_NIGHT_YES
                    : AppCompatDelegate.MODE_NIGHT_NO;
            AppCompatDelegate.setDefaultNightMode(mode);
        });

        //Notification — lưu trạng thái vào SharedPrefManager
        switchNotification.setOnCheckedChangeListener((btn, isChecked) -> {
            spm.setNotificationsEnabled(isChecked);
            // TODO: bật/tắt notification channel thực tế ở đây nếu cần
        });

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
        layoutLogout.setOnClickListener(v -> showLogoutBottomSheet());
    }

    private void showLogoutBottomSheet() {
        BottomSheetDialog dialog = new BottomSheetDialog(this);

        View view = getLayoutInflater().inflate(
                R.layout.bottom_sheet_logout,
                findViewById(android.R.id.content),
                false
        );
        dialog.setContentView(view);

        Button btnYes    = view.findViewById(R.id.btnYes);
        Button btnCancel = view.findViewById(R.id.btnCancel);

        btnYes.setOnClickListener(v -> {
            dialog.dismiss();
            Toast.makeText(this, "Logged out", Toast.LENGTH_SHORT).show();
            // TODO: uncomment khi có LoginActivity
            // Intent i = new Intent(this, LoginActivity.class);
            // i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            // startActivity(i);
        });

        btnCancel.setOnClickListener(v -> dialog.dismiss());
        dialog.show();
    }
}