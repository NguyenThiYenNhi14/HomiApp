package com.yn.homi.ui.home;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Matrix;
import android.graphics.Shader;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.Handler;
import android.transition.ChangeBounds;
import android.transition.TransitionManager;
import android.transition.TransitionSet;
import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.LinearInterpolator;
import android.view.animation.Transformation;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.widget.AppCompatButton;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.constraintlayout.widget.ConstraintSet;

import com.yn.homi.ui.auth.LoginActivity;
import com.yn.homi.R;
import com.yn.homi.utils.LocaleHelper;

public class WelcomeActivity extends com.yn.homi.core.BaseActivity {

    private ConstraintLayout welcomeRoot;
    private ImageView ivLogo;
    private TextView tvLetsGet, tvStarted;
    private View langToggle, layoutGetStarted, tvWelcomeDesc, layoutSignUp, btnLoginWelcome;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        // Tràn viền giống Splash
        getWindow().getDecorView().setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                | View.SYSTEM_UI_FLAG_FULLSCREEN
                | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);

        setContentView(R.layout.activity_welcome);

        initViews();
        setupLanguageToggle();
        
        // Bước 1: Đưa logo vào giữa màn hình ngay lập tức (không hiệu ứng)
        prepareSplashState();

        // Bước 2: Chạy hiệu ứng sau 1.5 giây (thời gian hiển thị Splash)
        new Handler().postDelayed(this::startTransitionToWelcome, 1500);
    }

    private void initViews() {
        welcomeRoot = findViewById(R.id.welcomeRoot);
        ivLogo = findViewById(R.id.ivLogo);
        tvLetsGet = findViewById(R.id.tvLetsGet);
        tvStarted = findViewById(R.id.tvStarted);
        langToggle = findViewById(R.id.langToggle);
        layoutGetStarted = findViewById(R.id.layoutGetStarted);
        tvWelcomeDesc = findViewById(R.id.tvWelcomeDesc);
        layoutSignUp = findViewById(R.id.layoutSignUp);
        btnLoginWelcome = findViewById(R.id.btnLoginWelcome);

        applyTextGradient(tvLetsGet, "#7A6359", "#5A4841", "#2B1B17"); // Premium Wood Brown Palette
        applyTextGradient(tvStarted, "#FFB294", "#E2906D", "#BC5B39"); // Artistic Terracotta Palette

        findViewById(R.id.tvSignUpNow).setOnClickListener(v -> 
            startActivity(new Intent(this, LoginActivity.class)));
        
        btnLoginWelcome.setOnClickListener(v -> 
            startActivity(new Intent(this, LoginActivity.class)));

        layoutGetStarted.setOnClickListener(v -> {
            startActivity(new Intent(this, HomeActivity.class));
            finish();
        });
    }

    private void prepareSplashState() {
        // Logo is already centered in XML with alpha 1
        // We just need to ensure the zoom animation runs
        Animation zoomIn = AnimationUtils.loadAnimation(this, R.anim.zoom_in);
        ivLogo.startAnimation(zoomIn);
    }

    private void startTransitionToWelcome() {
        // Clear text for typewriter effect
        tvLetsGet.setText("");
        tvStarted.setText("");

        // Tạo transition chuyển động bound và fade nội dung
        TransitionSet set = new TransitionSet();
        set.addTransition(new ChangeBounds());
        set.setDuration(1000);
        set.setInterpolator(new AccelerateDecelerateInterpolator());
        
        TransitionManager.beginDelayedTransition(welcomeRoot, set);

        // Move logo to the TOP in a straight vertical line
        ConstraintSet constraintSet = new ConstraintSet();
        constraintSet.clone(welcomeRoot);
        
        // Final Welcome state dimensions (Scaling down from 255x183 to 180x130 for better fit)
        int widthPx = (int) (180 * getResources().getDisplayMetrics().density);
        int heightPx = (int) (130 * getResources().getDisplayMetrics().density);
        constraintSet.constrainWidth(R.id.ivLogo, widthPx);
        constraintSet.constrainHeight(R.id.ivLogo, heightPx);

        // Clear center vertically and set to TOP with margin
        constraintSet.clear(R.id.ivLogo, ConstraintSet.BOTTOM);
        // Position it lower for the Welcome state (adjusted from 40dp to 65dp)
        constraintSet.connect(R.id.ivLogo, ConstraintSet.TOP, ConstraintSet.PARENT_ID, ConstraintSet.TOP, (int) (65 * getResources().getDisplayMetrics().density));

        // Ensure horizontal centering remains
        constraintSet.centerHorizontally(R.id.ivLogo, ConstraintSet.PARENT_ID);

        constraintSet.applyTo(welcomeRoot);

        // Fade in other UI elements
        float targetAlpha = 1.0f;
        langToggle.animate().alpha(targetAlpha).setDuration(800).start();
        
        // Hide tvStarted initially for the typewriter effect
        tvStarted.setVisibility(View.INVISIBLE);
        
        // Hiện thị layout nền trước, sau đó mới bắt đầu đánh chữ
        layoutGetStarted.animate().alpha(targetAlpha).setDuration(500).setStartDelay(300)
                .withEndAction(() -> {
                    String letsGet = getString(R.string.str_welcome_lets_get);
                    String started = getString(R.string.str_welcome_started);
                    typeWriteText(tvLetsGet, letsGet, 50, () -> {
                        tvStarted.setVisibility(View.VISIBLE);
                        typeWriteText(tvStarted, started, 50, () -> {
                            // Tạo hiệu ứng lật đật (Roly-poly) bằng cách nghiêng trục X (Skew)
                            // Giúp giữ cạnh đáy luôn nằm ngang và tạo kiểu "chữ nghiêng" tự nhiên
                            layoutGetStarted.post(() -> {
                                final float pX = layoutGetStarted.getWidth() / 2f;
                                final float pY = layoutGetStarted.getHeight();

                                Animation rolyPolyAnim = new Animation() {
                                    @Override
                                    protected void applyTransformation(float interpolatedTime, Transformation t) {
                                        // Sử dụng hàm Sine để tạo chuyển động lắc lư tuần hoàn mượt mà
                                        // interpolatedTime (0->1) nhân với 2*PI để tạo một chu kỳ Sin hoàn chỉnh
                                        float phase = interpolatedTime * 2 * (float) Math.PI;
                                        // Hệ số skew tương đương độ nghiêng (0.27 ≈ 15 độ)
                                        float skewX = (float) (Math.sin(phase) * -0.27f);
                                        
                                        Matrix matrix = t.getMatrix();
                                        // Áp dụng Skew với tâm đặt tại chính giữa cạnh đáy để giữ điểm tiếp xúc cố định
                                        matrix.setSkew(skewX, 0, pX, pY);
                                    }
                                };
                                rolyPolyAnim.setDuration(2800); // Tốc độ lắc lư chậm rãi, nghệ thuật
                                rolyPolyAnim.setRepeatCount(Animation.INFINITE);
                                rolyPolyAnim.setInterpolator(new LinearInterpolator());
                                layoutGetStarted.startAnimation(rolyPolyAnim);
                            });
                        });
                    });
                }).start();
        tvWelcomeDesc.animate().alpha(targetAlpha).setDuration(800).setStartDelay(500).start();
        layoutSignUp.animate().alpha(targetAlpha).setDuration(800).setStartDelay(700).start();
        btnLoginWelcome.animate().alpha(targetAlpha).setDuration(800).setStartDelay(900).start();
    }

    private void setupLanguageToggle() {
        TextView tvEn = findViewById(R.id.tvEn);
        TextView tvVi = findViewById(R.id.tvVi);
        
        updateLanguageUI(tvEn, tvVi);
        tvEn.setOnClickListener(v -> updateLanguage("en"));
        tvVi.setOnClickListener(v -> updateLanguage("vi"));
    }

    private void updateLanguage(String langCode) {
        if (!LocaleHelper.getLanguage(this).equals(langCode)) {
            LocaleHelper.setLocale(this, langCode);
            recreate();
        }
    }

    private void applyTextGradient(TextView textView, String startColor, String centerColor, String endColor) {
        textView.post(() -> {
            float height = textView.getHeight();
            if (height <= 0) height = textView.getLineHeight();
            Shader textShader = new LinearGradient(0, 0, 0, height,
                    new int[]{Color.parseColor(startColor), Color.parseColor(centerColor), Color.parseColor(endColor)},
                    new float[]{0f, 0.5f, 1f}, Shader.TileMode.CLAMP);
            textView.getPaint().setShader(textShader);
            
            // Add soft shadow to enhance the Playfair Display elegance
            textView.setShadowLayer(12f, 0f, 6f, Color.parseColor("#33000000"));
            textView.invalidate();
        });
    }

    private void typeWriteText(TextView textView, String text, long delayPerChar, Runnable onComplete) {
        ValueAnimator animator = ValueAnimator.ofInt(0, text.length());
        animator.setDuration(delayPerChar * text.length());
        animator.setInterpolator(new LinearInterpolator());
        animator.addUpdateListener(animation -> {
            int count = (int) animation.getAnimatedValue();
            String currentText = text.substring(0, count);
            if (!textView.getText().toString().equals(currentText)) {
                textView.setText(currentText);
            }
        });
        animator.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                if (onComplete != null) onComplete.run();
            }
        });
        animator.start();
    }

    private void updateLanguageUI(TextView tvEn, TextView tvVi) {
        String currentLang = LocaleHelper.getLanguage(this);
        boolean isVi = currentLang.equals("vi");
        tvVi.setTypeface(null, isVi ? Typeface.BOLD : Typeface.NORMAL);
        tvEn.setTypeface(null, isVi ? Typeface.NORMAL : Typeface.BOLD);
    }
}
