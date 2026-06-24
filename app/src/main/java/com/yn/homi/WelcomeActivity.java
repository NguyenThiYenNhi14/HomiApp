package com.yn.homi;

import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.widget.AppCompatButton;
import androidx.core.view.ViewCompat;

import com.yn.homi.authentication.LoginActivity;
import com.yn.homi.utils.LocaleHelper;

public class WelcomeActivity extends BaseActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_welcome);
        
        // Trải dài background toàn màn hình
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> insets);

        // Nút Get Started -> Chuyển đến HomeActivity
        AppCompatButton buttonGetStarted = findViewById(R.id.buttonGetStarted);
        buttonGetStarted.setOnClickListener(v -> {
            startActivity(new Intent(WelcomeActivity.this, HomeActivity.class));
            finish();
        });

        // Nút Sign In -> Chuyển đến LoginActivity
        TextView buttonLogin = findViewById(R.id.buttonLogin);
        buttonLogin.setOnClickListener(v -> startActivity(new Intent(WelcomeActivity.this, LoginActivity.class)));

        // Chuyển ngôn ngữ sang Tiếng Anh
        TextView tvEn = findViewById(R.id.tvEn);
        tvEn.setOnClickListener(v -> updateLanguage("en"));

        // Chuyển ngôn ngữ sang Tiếng Việt
        TextView tvVi = findViewById(R.id.tvVi);
        tvVi.setOnClickListener(v -> updateLanguage("vi"));
        
        // Highlight ngôn ngữ đang chọn
        String currentLang = LocaleHelper.getLanguage(this);
        if (currentLang.equals("vi")) {
            tvVi.setTypeface(null, Typeface.BOLD);
            tvEn.setTypeface(null, Typeface.NORMAL);
        } else {
            tvEn.setTypeface(null, Typeface.BOLD);
            tvVi.setTypeface(null, Typeface.NORMAL);
        }
    }

    private void updateLanguage(String langCode) {
        if (!LocaleHelper.getLanguage(this).equals(langCode)) {
            LocaleHelper.setLocale(this, langCode);
            // Recreate để áp dụng ngôn ngữ mới ngay lập tức
            recreate();
        }
    }
}
