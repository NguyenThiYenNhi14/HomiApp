package com.yn.homi.setting.preferences;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.yn.homi.R;

import java.util.Locale;

public class LanguageActivity extends AppCompatActivity {

    // ── Danh sách ngôn ngữ: { mã ngôn ngữ, tên tiếng Anh, tên bản ngữ, tên drawable cờ } ──
    private static final String[][] LANGUAGES = {
            { "en", "English",    "English",    "america_flag" },
            { "vi", "Vietnamese", "Tiếng Việt", "vietnam_flag" },
    };

    private static final String PREF_NAME     = "homi_prefs";
    private static final String PREF_LANG_KEY = "selected_language";

    private String selectedLanguageCode;
    private View[] languageRowViews;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_language);

        // ── 1. Setup Toolbar ──
        Toolbar toolbar = findViewById(R.id.toolbar_language);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayShowTitleEnabled(false);
            getSupportActionBar().setDisplayHomeAsUpEnabled(false); // tắt vì dùng custom button
        }

        // ── FIX: Gán listener cho nút Back custom ──
        findViewById(R.id.btnBack).setOnClickListener(v -> finish());

        // ── 2. Đọc ngôn ngữ đang lưu ──
        SharedPreferences prefs = getSharedPreferences(PREF_NAME, MODE_PRIVATE);
        selectedLanguageCode = prefs.getString(PREF_LANG_KEY, "en");

        // ── 3. Inflate các row ngôn ngữ ──
        LinearLayout container = findViewById(R.id.language_list_container);
        LayoutInflater inflater = LayoutInflater.from(this);
        languageRowViews = new View[LANGUAGES.length];

        for (int i = 0; i < LANGUAGES.length; i++) {
            final String code    = LANGUAGES[i][0];
            final String name    = LANGUAGES[i][1];
            final String native_ = LANGUAGES[i][2];
            final String flagRes = LANGUAGES[i][3]; // tên drawable

            View row = inflater.inflate(R.layout.item_language, container, false);
            languageRowViews[i] = row;

            // ── FIX: Dùng ImageView thay vì TextView để hiển thị cờ ──
            ImageView flagView = row.findViewById(R.id.iv_language_flag);
            int resId = getResources().getIdentifier(flagRes, "drawable", getPackageName());
            flagView.setImageResource(resId);

            ((TextView) row.findViewById(R.id.tv_language_name)).setText(name);
            ((TextView) row.findViewById(R.id.tv_language_native)).setText(native_);

            updateRowUI(row, code.equals(selectedLanguageCode));

            row.setOnClickListener(v -> {
                selectedLanguageCode = code;
                refreshAllRows();
            });

            container.addView(row);
        }

        // ── 4. Nút Apply ──
        findViewById(R.id.btn_apply_language).setOnClickListener(v -> applyLanguage());
    }

    private void updateRowUI(View row, boolean isSelected) {
        ImageView checkIcon = row.findViewById(R.id.iv_language_check);
        TextView nameText   = row.findViewById(R.id.tv_language_name);

        if (isSelected) {
            row.setBackground(getResources().getDrawable(R.drawable.bg_language_item_selected, null));
            checkIcon.setVisibility(View.VISIBLE);
            nameText.setTextColor(getResources().getColor(R.color.pp_black, null));
        } else {
            row.setBackground(getResources().getDrawable(R.drawable.bg_language_item, null));
            checkIcon.setVisibility(View.GONE);
            nameText.setTextColor(getResources().getColor(R.color.pp_black, null));
        }
    }

    private void refreshAllRows() {
        for (int i = 0; i < LANGUAGES.length; i++) {
            boolean isSelected = LANGUAGES[i][0].equals(selectedLanguageCode);
            updateRowUI(languageRowViews[i], isSelected);
        }
    }

    private void applyLanguage() {
        SharedPreferences prefs = getSharedPreferences(PREF_NAME, MODE_PRIVATE);
        prefs.edit().putString(PREF_LANG_KEY, selectedLanguageCode).apply();

        String langName = selectedLanguageCode;
        for (String[] lang : LANGUAGES) {
            if (lang[0].equals(selectedLanguageCode)) {
                langName = lang[1];
                break;
            }
        }

        Locale locale = new Locale(selectedLanguageCode);
        Locale.setDefault(locale);
        android.content.res.Configuration config = new android.content.res.Configuration();
        config.setLocale(locale);
        getBaseContext().getResources().updateConfiguration(
                config, getBaseContext().getResources().getDisplayMetrics());

        Toast.makeText(this, "Language set to " + langName, Toast.LENGTH_SHORT).show();
        finish();
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }
}