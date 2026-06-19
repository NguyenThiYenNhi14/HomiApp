package com.yn.homi.setting.profile;

import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.yn.homi.R;

public class YourProfileActivity extends AppCompatActivity {

    private UserProfile profile;

    private ImageView ivAvatar;
    private TextView tvFullName, tvPhone, tvEmail, tvGender, tvDob;

    // Trạng thái ẩn/hiện — mặc định đều đang ẩn
    private boolean phoneVisible = false;
    private boolean emailVisible = false;

    private final ActivityResultLauncher<Intent> editLauncher =
            registerForActivityResult(
                    new ActivityResultContracts.StartActivityForResult(),
                    result -> {
                        if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                            UserProfile updated = null;
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                                updated = result.getData()
                                        .getParcelableExtra("UPDATED_PROFILE", UserProfile.class);
                            }
                            if (updated != null) {
                                profile = updated;
                                showProfile(profile);
                                // Reset về ẩn khi profile được cập nhật
                                phoneVisible = false;
                                emailVisible = false;
                                updatePhoneDisplay();
                                updateEmailDisplay();

                                Intent resultIntent = new Intent();
                                resultIntent.putExtra("UPDATED_PROFILE", profile);
                                setResult(RESULT_OK, resultIntent);
                            }
                        }
                    }
            );

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_your_profile);

        // Lấy profile
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            profile = getIntent().getParcelableExtra("USER_PROFILE", UserProfile.class);
        }
        if (profile == null) profile = new UserProfile();

        // Ánh xạ view
        ivAvatar   = findViewById(R.id.ivAvatar);
        tvFullName = findViewById(R.id.tvFullName);
        tvPhone    = findViewById(R.id.tvPhone);
        tvEmail    = findViewById(R.id.tvEmail);
        tvGender   = findViewById(R.id.tvGender);
        tvDob      = findViewById(R.id.tvDob);

        Button btnEdit = findViewById(R.id.btnEdit);
        ImageButton btnTogglePhone = findViewById(R.id.btnTogglePhone);
        ImageButton btnToggleEmail = findViewById(R.id.btnToggleEmail);

        showProfile(profile);

        // Nút Back
        findViewById(R.id.btnBack).setOnClickListener(v -> finish());

        // Nút Edit
        btnEdit.setOnClickListener(v -> {
            Intent intent = new Intent(this, EditProfileActivity.class);
            intent.putExtra("USER_PROFILE", profile);
            editLauncher.launch(intent);
        });

        // Toggle Phone
        btnTogglePhone.setOnClickListener(v -> {
            phoneVisible = !phoneVisible;
            updatePhoneDisplay();
            btnTogglePhone.setImageResource(
                    phoneVisible ? R.drawable.ic_eye_on : R.drawable.ic_eye_off
            );
        });

        // Toggle Email
        btnToggleEmail.setOnClickListener(v -> {
            emailVisible = !emailVisible;
            updateEmailDisplay();
            btnToggleEmail.setImageResource(
                    emailVisible ? R.drawable.ic_eye_on : R.drawable.ic_eye_off
            );
        });
    }

    private void showProfile(UserProfile p) {
        tvFullName.setText(p.fullName.isEmpty()   ? "—" : p.fullName);
        tvGender.setText(p.gender.isEmpty()       ? "—" : p.gender);
        tvDob.setText(p.dateOfBirth.isEmpty()     ? "—" : p.dateOfBirth);

        // Phone và Email luôn bắt đầu ở trạng thái ẩn
        updatePhoneDisplay();
        updateEmailDisplay();

        if (p.avatarUri != null && !p.avatarUri.isEmpty()) {
            ivAvatar.clearColorFilter();
            Glide.with(this)
                    .load(Uri.parse(p.avatarUri))
                    .circleCrop()
                    .placeholder(R.drawable.icon_account_circle)
                    .into(ivAvatar);
        }
    }

    /** Hiển thị phone dạng ẩn (••••••••) hoặc thật tuỳ trạng thái */
    private void updatePhoneDisplay() {
        String phone = profile.phoneNumber;
        if (phone == null || phone.isEmpty()) {
            tvPhone.setText("—");
            return;
        }
        tvPhone.setText(phoneVisible ? phone : maskText(phone));
    }

    /** Hiển thị email dạng ẩn hoặc thật tuỳ trạng thái */
    private void updateEmailDisplay() {
        String email = profile.email;
        if (email == null || email.isEmpty()) {
            tvEmail.setText("—");
            return;
        }
        tvEmail.setText(emailVisible ? email : maskEmail(email));
    }

    /**
     * Ẩn toàn bộ ký tự trừ 3 ký tự đầu và 2 ký tự cuối.
     * VD: "0912345678" → "091•••••78"
     */
    private String maskText(String text) {
        if (text.length() <= 5) return "•••••";
        return text.substring(0, 3)
                + "•".repeat(text.length() - 5)
                + text.substring(text.length() - 2);
    }

    /**
     * Ẩn phần local của email, giữ lại domain.
     * VD: "bob.smith@gmail.com" → "bob•••••@gmail.com"
     */
    private String maskEmail(String email) {
        int atIndex = email.indexOf('@');
        if (atIndex <= 0) return maskText(email);
        String local  = email.substring(0, atIndex);
        String domain = email.substring(atIndex);       // "@gmail.com"
        if (local.length() <= 3) return "•••" + domain;
        return local.substring(0, 3) + "•".repeat(local.length() - 3) + domain;
    }
}