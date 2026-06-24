package com.yn.homi;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;

import androidx.activity.EdgeToEdge;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class MainActivity extends BaseActivity {

    private Handler handler;
    private Runnable runnable;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Chế độ tràn viền đã được thiết lập trong theme hoặc EdgeToEdge
        setContentView(R.layout.activity_main);

        // Chuyển sang WelcomeActivity sau 3 giây
        handler = new Handler(Looper.getMainLooper());
        runnable = () -> {
            startActivity(new Intent(MainActivity.this, WelcomeActivity.class));
            // Hiệu ứng chuyển cảnh fade
            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
            finish();
        };
        handler.postDelayed(runnable, 1000);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // Tránh memory leak khi đóng activity trước khi handler chạy xong
        if (handler != null && runnable != null) {
            handler.removeCallbacks(runnable);
        }
    }
}
