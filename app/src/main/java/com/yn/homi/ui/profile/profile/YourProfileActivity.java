package com.yn.homi.ui.profile.profile;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.yn.homi.R;
import com.yn.homi.ui.auth.LoginActivity;
import com.yn.homi.ui.cart.CartActivity;
import com.yn.homi.ui.cart.CartManager;
import com.yn.homi.ui.shop.ProductAdapter;
import com.yn.homi.utils.FavoritesManager;
import com.yn.homi.utils.RecentlyViewedManager;
import com.yn.homi.data.model.Product;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class YourProfileActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private UserProfile currentUserProfile;
    private FavoritesManager favoritesManager;
    private RecentlyViewedManager recentlyViewedManager;

    private TextView tvAuthTitle;
    private android.widget.ImageView ivAvatar, ivFav;
    private TextView tvCoupons, tvPoints, tvWishlists, tvViews;
    private TextView tvCartBadge;
    private Button btnLogin, btnRedeem;
    private ConstraintLayout llAuthHeader;
    private LinearLayout llRecentlyEmpty;
    private androidx.recyclerview.widget.RecyclerView rvRecentlyViewed;
    private ProductAdapter recentlyViewedAdapter;
    private LinearLayout llUnpaid, llProcessing, llShipped, llReturns;
    private android.widget.ImageView ivSettings;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_your_profile);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        favoritesManager = new FavoritesManager(this);
        recentlyViewedManager = new RecentlyViewedManager(this);
        
        initViews();
        setupListeners();
        checkUserStatus();
    }

    private void initViews() {
        tvAuthTitle = findViewById(R.id.tv_auth_title);
        ivAvatar = findViewById(R.id.iv_avatar);
        ivFav = findViewById(R.id.iv_fav);
        tvCoupons = findViewById(R.id.tv_coupons_count);
        // Link to MyCouponsActivity
        View llCoupons = (View) tvCoupons.getParent();
        if (llCoupons != null) {
            llCoupons.setOnClickListener(v -> {
                startActivity(new Intent(this, com.yn.homi.ui.profile.coupon.MyCouponsActivity.class));
            });
        }

        tvPoints = findViewById(R.id.tv_points_count);
        tvWishlists = findViewById(R.id.tv_wishlists_count);
        // Link to WishlistActivity
        View llWishlists = (View) tvWishlists.getParent();
        if (llWishlists != null) {
            llWishlists.setOnClickListener(v -> {
                startActivity(new Intent(this, com.yn.homi.ui.profile.wishlist.WishlistActivity.class));
            });
        }
        tvViews = findViewById(R.id.tv_views_count);

        btnLogin = findViewById(R.id.btn_login_account);
        llAuthHeader = findViewById(R.id.ll_auth_header);
        llRecentlyEmpty = findViewById(R.id.ll_recently_empty);
        rvRecentlyViewed = findViewById(R.id.rv_recently_viewed);
        setupRecentlyViewed();
        llUnpaid = findViewById(R.id.ll_unpaid);
        llProcessing = findViewById(R.id.ll_processing);
        llShipped = findViewById(R.id.ll_shipped);
        llReturns = findViewById(R.id.ll_returns);
        ivSettings = findViewById(R.id.iv_settings);
        tvCartBadge = findViewById(R.id.tv_cart_badge);
        btnRedeem = findViewById(R.id.btn_redeem_points);

        updateWishlistCount();
        updateCartBadge();
        setupBottomNavigation();
    }

    private void setupRecentlyViewed() {
        recentlyViewedAdapter = new ProductAdapter(new ArrayList<>(), false); 
        rvRecentlyViewed.setLayoutManager(new androidx.recyclerview.widget.GridLayoutManager(this, 2));
        rvRecentlyViewed.setNestedScrollingEnabled(false);
        rvRecentlyViewed.setAdapter(recentlyViewedAdapter);
        updateRecentlyViewedUI();
    }

    private void updateRecentlyViewedUI() {
        List<Product> products = recentlyViewedManager.getRecentlyViewed();
        if (products.isEmpty()) {
            if (mAuth.getCurrentUser() == null) {
                llRecentlyEmpty.setVisibility(View.VISIBLE);
            } else {
                llRecentlyEmpty.setVisibility(View.GONE);
            }
            rvRecentlyViewed.setVisibility(View.GONE);
        } else {
            llRecentlyEmpty.setVisibility(View.GONE);
            rvRecentlyViewed.setVisibility(View.VISIBLE);
            recentlyViewedAdapter.updateData(products);
        }
    }

    private void updateWishlistCount() {
        if (favoritesManager != null) {
            int count = favoritesManager.getWishlists().size();
            tvWishlists.setText(String.valueOf(count));
        }
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
        if (ivFav != null) {
            ivFav.setOnClickListener(v -> {
                startActivity(new Intent(this, com.yn.homi.ui.profile.wishlist.WishlistActivity.class));
            });
        }

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
                Intent intent = new Intent(this, EditProfileActivity.class);
                if (currentUserProfile != null) {
                    intent.putExtra("USER_PROFILE", currentUserProfile);
                }
                startActivity(intent);
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

        if (btnRedeem != null) {
            btnRedeem.setOnClickListener(v -> redeemPointsForCoupon());
        }
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
            updateRecentlyViewedUI();
            // btnRedeem will be handled in updateUIWithProfile based on points

            // Fetch thông tin chi tiết từ Firestore
            db.collection("users").document(user.getUid())
                    .get()
                    .addOnSuccessListener(documentSnapshot -> {
                        if (documentSnapshot.exists()) {
                            currentUserProfile = documentSnapshot.toObject(UserProfile.class);
                            if (currentUserProfile != null) {
                                currentUserProfile.uid = user.getUid();
                                checkBirthdayCoupon(currentUserProfile, user.getUid());
                                updateUIWithProfile(currentUserProfile);
                            }
                        }
                    })
                    .addOnFailureListener(e -> {
                        // Xử lý lỗi nếu cần
                    });
        } else {
            // Guest state
            tvAuthTitle.setText(getString(R.string.sign_in_register) + " >");
            ivAvatar.setImageResource(R.drawable.ic_person);
            updateRecentlyViewedUI();
            if (btnRedeem != null) btnRedeem.setVisibility(View.GONE);
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
            updateWishlistCount();
            tvViews.setText(String.valueOf(profile.stats.views));

            if (btnRedeem != null) {
                btnRedeem.setVisibility(profile.stats.points >= 500 ? View.VISIBLE : View.GONE);
            }
        }
    }

    private void checkBirthdayCoupon(UserProfile profile, String uid) {
        if (profile.dateOfBirth == null || profile.dateOfBirth.isEmpty()) return;
        try {
            String[] parts = profile.dateOfBirth.split("/");
            int birthMonth = Integer.parseInt(parts[1]);
            java.util.Calendar now = java.util.Calendar.getInstance();
            int currentMonth = now.get(java.util.Calendar.MONTH) + 1;
            int currentYear = now.get(java.util.Calendar.YEAR);
            if (birthMonth != currentMonth) return;
            if (profile.stats.lastBirthdayCouponYear == currentYear) return;

            Map<String, Object> coupon = new HashMap<>();
            coupon.put("type", "birthday");
            coupon.put("code", "HBD" + currentYear);
            coupon.put("discountType", "fixed");
            coupon.put("discountValue", 5);
            coupon.put("isUsed", false);
            coupon.put("createdAt", com.google.firebase.Timestamp.now());

            String couponId = "birthday_" + uid + "_" + currentYear;
            db.collection("users").document(uid).collection("coupons")
                    .document(couponId).set(coupon);

            db.collection("users").document(uid)
                    .update("stats.lastBirthdayCouponYear", currentYear,
                            "stats.coupons", com.google.firebase.firestore.FieldValue.increment(1));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void resetStatsUI() {
        tvCoupons.setText("0");
        tvPoints.setText("0");
        tvWishlists.setText("0");
        tvViews.setText("0");
    }

    private void updateCartBadge() {
        int count = CartManager.getInstance(this).getTotalItemCount();
        if (count > 0) {
            tvCartBadge.setText(String.valueOf(count));
            tvCartBadge.setVisibility(View.VISIBLE);
        } else {
            tvCartBadge.setVisibility(View.GONE);
        }
    }

    private void redeemPointsForCoupon() {
        FirebaseUser user = mAuth.getCurrentUser();
        if (user == null) return;
        
        String uid = user.getUid();
        com.google.firebase.firestore.DocumentReference userRef = db.collection("users").document(uid);

        db.runTransaction(transaction -> {
            com.google.firebase.firestore.DocumentSnapshot snapshot = transaction.get(userRef);
            Long currentPoints = snapshot.getLong("stats.points");
            if (currentPoints == null || currentPoints < 500) {
                throw new RuntimeException("NOT_ENOUGH_POINTS");
            }
            transaction.update(userRef, "stats.points", currentPoints - 500);
            transaction.update(userRef, "stats.coupons", com.google.firebase.firestore.FieldValue.increment(1));

            Map<String, Object> coupon = new HashMap<>();
            coupon.put("type", "redeemed");
            coupon.put("code", "REDEEM" + System.currentTimeMillis());
            coupon.put("discountType", "fixed");
            coupon.put("discountValue", 1);
            coupon.put("isUsed", false);
            coupon.put("createdAt", com.google.firebase.Timestamp.now());

            com.google.firebase.firestore.DocumentReference newCouponRef =
                    userRef.collection("coupons").document();
            transaction.set(newCouponRef, coupon);
            return null;
        }).addOnSuccessListener(unused -> {
            Toast.makeText(this, getString(R.string.redeem_success), Toast.LENGTH_SHORT).show();
            checkUserStatus(); // Refresh UI to show new points/coupons
        }).addOnFailureListener(e -> {
            if ("NOT_ENOUGH_POINTS".equals(e.getMessage())) {
                Toast.makeText(this, getString(R.string.not_enough_points), Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, getString(R.string.error_redeem, e.getMessage()), Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Re-check status in case user logged in/out from another screen
        checkUserStatus();
        updateWishlistCount();
        updateCartBadge();
        updateRecentlyViewedUI();
    }
}
