package com.yn.homi.ui.home;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import androidx.appcompat.app.AppCompatActivity;
import com.yn.homi.R;
import com.yn.homi.ui.auth.LoginActivity;

public class SplashActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        // Làm cho Splash Screen tràn viền hoàn toàn
        getWindow().getDecorView().setSystemUiVisibility(
                android.view.View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                | android.view.View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                | android.view.View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                | android.view.View.SYSTEM_UI_FLAG_HIDE_NAVIGATION // ẩn thanh điều hướng
                | android.view.View.SYSTEM_UI_FLAG_FULLSCREEN // ẩn thanh trạng thái
                | android.view.View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);

        setContentView(R.layout.activity_splash);

        ImageView logoName = findViewById(R.id.logoName);
        Animation zoomIn = AnimationUtils.loadAnimation(this, R.anim.zoom_in);
        
        logoName.startAnimation(zoomIn);

        // Chuyển sang màn hình Welcome sau 3 giây
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                Intent intent = new Intent(SplashActivity.this, WelcomeActivity.class);
                startActivity(intent);
                finish();
            }
        }, 3000); 
    }
}
