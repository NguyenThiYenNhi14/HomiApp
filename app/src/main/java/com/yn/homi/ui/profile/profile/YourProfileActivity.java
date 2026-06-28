package com.yn.homi.ui.profile.profile;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.yn.homi.R;
import com.yn.homi.ui.auth.LoginActivity;
import com.yn.homi.ui.cart.CartActivity;

public class YourProfileActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private UserProfile currentUserProfile;

    private TextView tvAuthTitle;
    private android.widget.ImageView ivAvatar;
    private TextView tvCoupons, tvPoints, tvWishlists, tvViews;
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
        db = FirebaseFirestore.getInstance();
        
        initViews();
        setupListeners();
        checkUserStatus();
    }

    private void initViews() {
        tvAuthTitle = findViewById(R.id.tv_auth_title);
        ivAvatar = findViewById(R.id.iv_avatar);
        tvCoupons = findViewById(R.id.tv_coupons_count);
        tvPoints = findViewById(R.id.tv_points_count);
        tvWishlists = findViewById(R.id.tv_wishlists_count);
        tvViews = findViewById(R.id.tv_views_count);

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
            startActivity(new Intent(this, com.yn.homi.ui.shop.ShopActivity.class));
            finish();
        });

        findViewById(R.id.btn_lists).setOnClickListener(v -> {
            startActivity(new Intent(this, com.yn.homi.ui.profile.wishlist.WishlistActivity.class));
            finish();
        });

        findViewById(R.id.btn_account).setOnClickListener(v -> {
            // Already here
        });

        findViewById(R.id.fab_home).setOnClickListener(v -> {
            Intent intent = new Intent(this, com.yn.homi.ui.home.HomeActivity.class);
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
                startActivity(new Intent(this, com.yn.homi.ui.profile.SettingActivity.class));
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
        Intent intent = new Intent(this, com.yn.homi.ui.profile.order.MyOrdersActivity.class);
        intent.putExtra("TARGET_TAB", tabIndex);
        startActivity(intent);
    }

    private void checkUserStatus() {
        FirebaseUser user = mAuth.getCurrentUser();
        if (user != null) {
            // Hiển thị tạm Email/Name từ Auth trước khi load Firestore
            String name = user.getDisplayName();
            if (name == null || name.isEmpty()) {
                name = user.getEmail();
            }
            tvAuthTitle.setText(name + " >");
            if (llRecentlyEmpty != null) llRecentlyEmpty.setVisibility(View.GONE);

            // Fetch thông tin chi tiết từ Firestore
            db.collection("users").document(user.getUid())
                    .get()
                    .addOnSuccessListener(documentSnapshot -> {
                        if (documentSnapshot.exists()) {
                            currentUserProfile = documentSnapshot.toObject(UserProfile.class);
                            if (currentUserProfile != null) {
                                currentUserProfile.uid = user.getUid();
                                updateUIWithProfile(currentUserProfile);
                            }
                        }
                    })
                    .addOnFailureListener(e -> {
                        // Xử lý lỗi nếu cần
                    });
        } else {
            // Guest state
            tvAuthTitle.setText("Sign In / Register >");
            ivAvatar.setImageResource(R.drawable.ic_person);
            if (llRecentlyEmpty != null) llRecentlyEmpty.setVisibility(View.VISIBLE);
            resetStatsUI();
        }
    }

    private void updateUIWithProfile(UserProfile profile) {
        if (profile.fullName != null && !profile.fullName.isEmpty()) {
            tvAuthTitle.setText(profile.fullName + " >");
        }

        if (profile.avatarUri != null && !profile.avatarUri.isEmpty()) {
            Glide.with(this)
                    .load(profile.avatarUri)
                    .placeholder(R.drawable.ic_person)
                    .error(R.drawable.ic_person)
                    .into(ivAvatar);
        } else {
            ivAvatar.setImageResource(R.drawable.ic_person);
        }

        if (profile.stats != null) {
            tvCoupons.setText(String.valueOf(profile.stats.coupons));
            tvPoints.setText(String.valueOf(profile.stats.points));
            tvWishlists.setText(String.valueOf(profile.stats.wishlists));
            tvViews.setText(String.valueOf(profile.stats.views));
        }
    }

    private void resetStatsUI() {
        tvCoupons.setText("0");
        tvPoints.setText("0");
        tvWishlists.setText("0");
        tvViews.setText("0");
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Re-check status in case user logged in/out from another screen
        checkUserStatus();
    }
}
