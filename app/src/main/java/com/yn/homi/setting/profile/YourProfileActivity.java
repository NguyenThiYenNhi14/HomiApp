package com.yn.homi.setting.profile;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.yn.homi.R;
import com.yn.homi.authentication.LoginActivity;
import com.yn.homi.cart.CartActivity;

public class YourProfileActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private TextView tvAuthTitle;
    private Button btnLogin;
    private ConstraintLayout llAuthHeader;
    private LinearLayout llRecentlyEmpty;
    private LinearLayout llUnpaid, llProcessing, llShipped, llReturns;
    private android.widget.ImageView ivSettings;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_your_profile);

        mAuth = FirebaseAuth.getInstance();
        
        initViews();
        setupListeners();
        checkUserStatus();
    }

    private void initViews() {
        tvAuthTitle = findViewById(R.id.tv_auth_title);
        btnLogin = findViewById(R.id.btn_login_account);
        llAuthHeader = findViewById(R.id.ll_auth_header);
        llRecentlyEmpty = findViewById(R.id.ll_recently_empty);
        llUnpaid = findViewById(R.id.ll_unpaid);
        llProcessing = findViewById(R.id.ll_processing);
        llShipped = findViewById(R.id.ll_shipped);
        llReturns = findViewById(R.id.ll_returns);
        ivSettings = findViewById(R.id.iv_settings);
        setupBottomNavigation();
    }

    private void setupBottomNavigation() {
        findViewById(R.id.btn_shop).setOnClickListener(v -> {
            startActivity(new Intent(this, com.yn.homi.ShopActivity.class));
            finish();
        });

        findViewById(R.id.btn_lists).setOnClickListener(v -> {
            startActivity(new Intent(this, com.yn.homi.setting.wishlist.WishlistActivity.class));
            finish();
        });

        findViewById(R.id.btn_account).setOnClickListener(v -> {
            // Already here
        });

        findViewById(R.id.fab_home).setOnClickListener(v -> {
            Intent intent = new Intent(this, com.yn.homi.HomeActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
            startActivity(intent);
            finish();
        });
    }

    private void setupListeners() {
        // Toolbar Icons
        findViewById(R.id.iv_cart).setOnClickListener(v -> {
            startActivity(new Intent(this, CartActivity.class));
        });

        if (ivSettings != null) {
            ivSettings.setOnClickListener(v -> {
                startActivity(new Intent(this, com.yn.homi.setting.SettingActivity.class));
            });
        }

        // Auth listeners
        View.OnClickListener loginAction = v -> {
            if (mAuth.getCurrentUser() == null) {
                startActivity(new Intent(this, LoginActivity.class));
            }
        };

        if (llAuthHeader != null) llAuthHeader.setOnClickListener(loginAction);
        if (btnLogin != null) btnLogin.setOnClickListener(loginAction);

        // Service Items
        findViewById(R.id.ll_account_setting).setOnClickListener(v -> {
            if (mAuth.getCurrentUser() != null) {
                // If logged in, maybe show a detailed profile or settings
                startActivity(new Intent(this, EditProfileActivity.class));
            } else {
                startActivity(new Intent(this, LoginActivity.class));
            }
        });

        // Other items can be linked here as needed (Orders, Wishlist, etc.)
        findViewById(R.id.tv_view_all_orders).setOnClickListener(v -> {
            openOrdersWithTab(0);
        });

        if (llUnpaid != null) llUnpaid.setOnClickListener(v -> openOrdersWithTab(1));
        if (llProcessing != null) llProcessing.setOnClickListener(v -> openOrdersWithTab(2));
        if (llShipped != null) llShipped.setOnClickListener(v -> openOrdersWithTab(4));
        if (llReturns != null) llReturns.setOnClickListener(v -> openOrdersWithTab(5));
    }

    private void openOrdersWithTab(int tabIndex) {
        Intent intent = new Intent(this, com.yn.homi.setting.order.MyOrdersActivity.class);
        intent.putExtra("TARGET_TAB", tabIndex);
        startActivity(intent);
    }

    private void checkUserStatus() {
        FirebaseUser user = mAuth.getCurrentUser();
        if (user != null) {
            // Logged in state
            String name = user.getDisplayName();
            if (name == null || name.isEmpty()) {
                name = user.getEmail();
            }
            tvAuthTitle.setText(name + " >");
            if (llRecentlyEmpty != null) llRecentlyEmpty.setVisibility(View.GONE);
            
            // Here you could update stats (Coupons, Points, etc.) from Firestore
        } else {
            // Guest state
            tvAuthTitle.setText("Sign In / Register >");
            if (llRecentlyEmpty != null) llRecentlyEmpty.setVisibility(View.VISIBLE);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Re-check status in case user logged in/out from another screen
        checkUserStatus();
    }
}
