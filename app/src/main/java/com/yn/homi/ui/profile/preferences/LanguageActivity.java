package com.yn.homi.ui.profile.preferences;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.yn.homi.R;
import com.yn.homi.core.BaseActivity;
import com.yn.homi.ui.home.WelcomeActivity;
import com.yn.homi.utils.LocaleHelper;

public class LanguageActivity extends BaseActivity {

    private LinearLayout layoutVietnamese, layoutEnglish;
    private ImageView checkVietnamese, checkEnglish;
    private String selectedLanguageCode;
    private View btnApply;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_language);

        initViews();
        setupListeners();

        // Lấy ngôn ngữ hiện tại
        selectedLanguageCode = LocaleHelper.getLanguage(this);
        updateUI();
    }

    private void initViews() {
        layoutVietnamese = findViewById(R.id.layoutVietnamese);
        layoutEnglish = findViewById(R.id.layoutEnglish);
        checkVietnamese = findViewById(R.id.checkVietnamese);
        checkEnglish = findViewById(R.id.checkEnglish);
        btnApply = findViewById(R.id.btnApply);

        findViewById(R.id.btnBack).setOnClickListener(v -> finish());
    }

    private void setupListeners() {
        layoutVietnamese.setOnClickListener(v -> {
            selectedLanguageCode = "vi";
            updateUI();
        });

        layoutEnglish.setOnClickListener(v -> {
            selectedLanguageCode = "en";
            updateUI();
        });

        btnApply.setOnClickListener(v -> applyLanguage());
    }

    private void updateUI() {
        if (selectedLanguageCode.equals("vi")) {
            checkVietnamese.setVisibility(View.VISIBLE);
            checkEnglish.setVisibility(View.GONE);
            layoutVietnamese.setBackgroundResource(R.drawable.bg_language_item_selected);
            layoutEnglish.setBackgroundResource(R.drawable.bg_language_item);
        } else {
            checkVietnamese.setVisibility(View.GONE);
            checkEnglish.setVisibility(View.VISIBLE);
            layoutVietnamese.setBackgroundResource(R.drawable.bg_language_item);
            layoutEnglish.setBackgroundResource(R.drawable.bg_language_item_selected);
        }
    }

    private void applyLanguage() {
        // 1. Lưu ngôn ngữ mới
        LocaleHelper.setLocale(this, selectedLanguageCode);

        // 2. Thông báo
        String msg = selectedLanguageCode.equals("vi") ? "Đã cập nhật ngôn ngữ" : "Language updated";
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();

        // 3. Khởi động lại ứng dụng từ đầu (WelcomeActivity) nhưng bỏ qua splash
        Intent intent = new Intent(this, WelcomeActivity.class);
        intent.putExtra("IS_LANGUAGE_SWITCH", true);
        // Xóa sạch stack để nạp lại toàn bộ resources mới
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        
        finish();
        
        // Hiệu ứng mượt mà
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
    }
}
