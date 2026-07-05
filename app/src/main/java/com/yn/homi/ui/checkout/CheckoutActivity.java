package com.yn.homi.ui.checkout;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import com.yn.homi.core.BaseActivity;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.yn.homi.ui.cart.CartAdapter;
import com.yn.homi.R;
import com.yn.homi.ui.checkout.model.PaymentMethod;
import com.yn.homi.data.model.CartItem;
import com.yn.homi.data.model.Product;
import com.yn.homi.data.repository.ProductRepository;
import com.yn.homi.ui.cart.CartManager;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import com.yn.homi.ui.profile.order.Order;
import com.yn.homi.ui.profile.order.OrderItem;
import com.yn.homi.ui.profile.order.OrderManager;
import com.yn.homi.data.local.SharedPrefManager;
import com.yn.homi.utils.FavoritesManager;
import com.yn.homi.data.repository.FirestoreRepository;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

public class CheckoutActivity extends BaseActivity {
    private List<CartItem> cartItems;
    private final double DELIVERY_FEE = 0.0; // FREE shipping
    private PaymentMethod selectedPayment = null;
    private com.yn.homi.data.model.Coupon selectedCoupon = null;
    private boolean isFromWishlist = false;
    private String wishlistName = null;

    private TextView tvItemCost, tvOrderTotal, tvPaymentMethod, tvDeliveryCost, tvVoucher;
    private View layoutDiscountRow;
    private RecyclerView rvCheckoutItems;
    private CartAdapter adapter;
    private FirestoreRepository firestoreRepository;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_checkout);

        View main = findViewById(R.id.main);
        View bottomBar = findViewById(R.id.layoutBottomBar);

        ViewCompat.setOnApplyWindowInsetsListener(main, (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, 0);
            
            int basePaddingBottom = (int) (12 * getResources().getDisplayMetrics().density);
            bottomBar.setPadding(
                    bottomBar.getPaddingLeft(),
                    bottomBar.getPaddingTop(),
                    bottomBar.getPaddingRight(),
                    systemBars.bottom + basePaddingBottom
            );
            return insets;
        });

        bindViews();
        loadData();
        setupListeners();
    }

    private void bindViews() {
        findViewById(R.id.btnBack).setOnClickListener(v -> finish());
        tvItemCost = findViewById(R.id.tvItemCost);
        tvOrderTotal = findViewById(R.id.tvOrderTotal);
        tvPaymentMethod = findViewById(R.id.tvPaymentMethod);
        tvDeliveryCost = findViewById(R.id.tvDeliveryCost);
        tvVoucher = findViewById(R.id.tvVoucher);
        layoutDiscountRow = findViewById(R.id.layoutDiscountRow);
        rvCheckoutItems = findViewById(R.id.rvCheckoutItems);
    }

    private void loadData() {
        firestoreRepository = new FirestoreRepository();
        mAuth = FirebaseAuth.getInstance();

        // 1. Kiểm tra xem có danh sách CartItem được truyền trực tiếp không (từ Product Detail hoặc Wishlist)
        List<CartItem> directCartItems = (List<CartItem>) getIntent().getSerializableExtra("SELECTED_CART_ITEMS");
        List<Product> directProducts = (List<Product>) getIntent().getSerializableExtra("SELECTED_PRODUCTS");
        
        isFromWishlist = getIntent().getBooleanExtra("FROM_WISHLIST", false);
        wishlistName = getIntent().getStringExtra("WISHLIST_NAME");

        if (directCartItems != null && !directCartItems.isEmpty()) {
            cartItems = directCartItems;
        } else if (directProducts != null && !directProducts.isEmpty()) {
            // Chuyển đổi Product sang CartItem để hiển thị trong adapter (Số lượng mặc định 1)
            cartItems = new ArrayList<>();
            for (Product p : directProducts) {
                cartItems.add(new CartItem(p, 1));
            }
        } else {
            // Mặc định lấy toàn bộ items từ giỏ hàng
            // Dùng new ArrayList để tránh lỗi ConcurrentModification
            cartItems = new ArrayList<>(CartManager.getInstance(this).getItems());
        }
        
        if (cartItems.isEmpty()) {
            // Fallback nếu giỏ hàng trống (chỉ để demo/test)
            List<Product> products = ProductRepository.getProducts(this);
            if (products != null && !products.isEmpty()) {
                for (int i = 0; i < Math.min(2, products.size()); i++) {
                    Product p = products.get(i);
                    CartManager.getInstance(this).addItem(new CartItem(p.getId(), p.getName(), p.getPrice(), 1, p.getImageUrl()));
                }
            }
            cartItems = new ArrayList<>(CartManager.getInstance(this).getItems());
        }

        // Setup RecyclerView hiển thị toàn bộ sản phẩm
        adapter = new CartAdapter(this, cartItems, null);
        adapter.setEditable(false); 
        rvCheckoutItems.setLayoutManager(new LinearLayoutManager(this));
        rvCheckoutItems.setAdapter(adapter);
        
        updatePricingUI();
    }

    private void setupListeners() {
        findViewById(R.id.layoutPaymentMethod).setOnClickListener(v -> openPaymentSheet());
        findViewById(R.id.layoutVoucher).setOnClickListener(v -> openVoucherSheet());
        findViewById(R.id.btnCheckout).setOnClickListener(v -> {
            if (selectedPayment == null) {
                openPaymentSheet();
            } else {
                proceedToSuccess();
            }
        });
    }

    private void updatePricingUI() {
        // Load address from Firestore
        TextView tvAddress = findViewById(R.id.tvAddress);
        if (tvAddress != null) {
            if (mAuth.getCurrentUser() != null) {
                firestoreRepository.getUserProfile(mAuth.getCurrentUser().getUid(), new FirestoreRepository.OnUserProfileLoadedListener() {
                    @Override
                    public void onLoaded(com.yn.homi.ui.profile.profile.UserProfile profile) {
                        if (profile != null && profile.address != null && !profile.address.isEmpty()) {
                            tvAddress.setText(profile.address);
                        } else {
                            tvAddress.setText(SharedPrefManager.getInstance(CheckoutActivity.this).getUserAddress());
                        }
                    }

                    @Override
                    public void onError(Exception e) {
                        tvAddress.setText(SharedPrefManager.getInstance(CheckoutActivity.this).getUserAddress());
                    }
                });
            } else {
                tvAddress.setText(SharedPrefManager.getInstance(this).getUserAddress());
            }
        }

        double subtotal = 0;
        int totalQty = 0;
        
        if (cartItems != null) {
            for (CartItem item : cartItems) {
                subtotal += item.getPrice() * item.getQuantity();
                totalQty += item.getQuantity();
            }
        }
        
        double discount = selectedCoupon != null ? selectedCoupon.calculateDiscount(subtotal) : 0;
        double total = subtotal + DELIVERY_FEE - discount;
        if (tvItemCost != null) tvItemCost.setText(getUSDString(subtotal));
        if (tvDeliveryCost != null) tvDeliveryCost.setText("FREE");
        View layoutDiscountRowLocal = findViewById(R.id.layoutDiscountRow);
        TextView tvDiscountAmount = findViewById(R.id.tvDiscountAmount);
        if (selectedCoupon != null && discount > 0) {
            if (layoutDiscountRowLocal != null) layoutDiscountRowLocal.setVisibility(View.VISIBLE);
            if (tvDiscountAmount != null) tvDiscountAmount.setText("-" + getUSDString(discount));
        } else if (layoutDiscountRowLocal != null) {
            layoutDiscountRowLocal.setVisibility(View.GONE);
        }
        if (tvOrderTotal != null) tvOrderTotal.setText(getUSDString(total));
        
        TextView tvItemLabel = findViewById(R.id.tvItemLabel);
        if (tvItemLabel != null) {
            tvItemLabel.setText(getString(R.string.label_subtotal_items, totalQty));
        }
    }

    private String getUSDString(double amount) {
        return String.format(Locale.US, "$%.2f", amount);
    }

    private void openPaymentSheet() {
        PaymentMethodBottomSheet sheet = new PaymentMethodBottomSheet();
        
        if (selectedPayment != null) {
            sheet.setSelectedMethodName(selectedPayment.getName());
        } else {
            sheet.setSelectedMethodName(tvPaymentMethod.getText().toString());
        }

        sheet.setOnPaymentConfirmedListener(method -> {
            selectedPayment = method;
            tvPaymentMethod.setText(method.getName());
            tvPaymentMethod.setTextColor(ContextCompat.getColor(this, android.R.color.black));
        });
        sheet.show(getSupportFragmentManager(), "PaymentSheet");
    }

    private void openVoucherSheet() {
        com.yn.homi.ui.checkout.VoucherBottomSheet sheet = new com.yn.homi.ui.checkout.VoucherBottomSheet();
        sheet.setOnVoucherSelectedListener(coupon -> {
            selectedCoupon = coupon;
            TextView tvVoucherLocal = findViewById(R.id.tvVoucher);
            if (tvVoucherLocal != null) {
                tvVoucherLocal.setText(coupon.getCode());
                tvVoucherLocal.setTextColor(androidx.core.content.ContextCompat.getColor(this, android.R.color.black));
            }
            updatePricingUI();
        });
        sheet.show(getSupportFragmentManager(), "VoucherSheet");
    }

    private void proceedToSuccess() {
        // 1. Tạo đơn hàng mới từ danh sách items đang hiển thị
        if (cartItems == null || cartItems.isEmpty()) return;

        List<OrderItem> orderItems = new ArrayList<>();
        double subtotal = 0;
        for (CartItem ci : cartItems) {
            subtotal += ci.getPrice() * ci.getQuantity();
            orderItems.add(new OrderItem(
                    ci.getId(),
                    ci.getName(),
                    ci.getPrice(),
                    ci.getSelectedColor() != null ? ci.getSelectedColor() : getString(R.string.color_standard),
                    ci.getSelectedSize() != null ? ci.getSelectedSize() : "Default",
                    ci.getQuantity(),
                    ci.getImageUrl(),
                    getString(R.string.status_packing)
            ));
        }

        double discount = selectedCoupon != null ? selectedCoupon.calculateDiscount(subtotal) : 0;
        String orderId = String.valueOf(100000 + new Random().nextInt(900000));
        String date = new SimpleDateFormat("MMM dd, yyyy", Locale.US).format(new Date());
        
        TextView tvAddress = findViewById(R.id.tvAddress);
        String address = tvAddress != null ? tvAddress.getText().toString() : getString(R.string.no_address_provided);

        Order newOrder = new Order(
                orderId,
                Order.Status.PENDING,
                orderItems,
                subtotal,
                DELIVERY_FEE,
                date,
                address,
                "TRK" + orderId,
                selectedPayment != null ? selectedPayment.getName() : "Cash on hand",
                selectedCoupon != null ? selectedCoupon.getCode() : null,
                discount
        );

        // 2. Lưu vào OrderManager
        OrderManager.getInstance(this).addOrder(newOrder);

        if (selectedCoupon != null && mAuth.getCurrentUser() != null) {
            com.google.firebase.firestore.FirebaseFirestore.getInstance()
                    .collection("users").document(mAuth.getCurrentUser().getUid())
                    .collection("coupons").document(selectedCoupon.getId())
                    .update("isUsed", true);
        }

        if (mAuth.getCurrentUser() != null) {
            String uid = mAuth.getCurrentUser().getUid();
            com.google.firebase.firestore.FirebaseFirestore db = com.google.firebase.firestore.FirebaseFirestore.getInstance();
            
            db.collection("users").document(uid)
                    .update("stats.points", com.google.firebase.firestore.FieldValue.increment(1000))
                    .addOnSuccessListener(aVoid -> {
                        Toast.makeText(this, getString(R.string.msg_earn_points, 1000), Toast.LENGTH_SHORT).show();
                    });
            
            checkLoyaltyCoupon(uid);
        }

        // 3. Xoá các items đã mua khỏi giỏ hàng hoặc wishlist
        if (isFromWishlist && wishlistName != null) {
            FavoritesManager favoritesManager = new FavoritesManager(this);
            for (CartItem ci : cartItems) {
                favoritesManager.removeProductFromWishlist(wishlistName, ci.getId());
            }
        }
        
        // Luôn kiểm tra và xóa khỏi giỏ hàng nếu item đó tồn tại (kể cả mua từ Checkout hay Wishlist)
        CartManager cartManager = CartManager.getInstance(this);
        for (CartItem ci : cartItems) {
            cartManager.removeItem(ci.getId(), ci.getSelectedColor(), ci.getSelectedSize());
        }

        // 4. Chuyển sang màn hình thành công
        startActivity(new Intent(this, OrderSuccessActivity.class));
        finish();
    }

    private void checkLoyaltyCoupon(String uid) {
        java.util.Calendar cal = java.util.Calendar.getInstance();
        cal.add(java.util.Calendar.DAY_OF_MONTH, -30);
        com.google.firebase.Timestamp thirtyDaysAgo = new com.google.firebase.Timestamp(cal.getTime());

        FirebaseFirestore.getInstance()
                .collection("users").document(uid).collection("orders")
                .whereGreaterThanOrEqualTo("createdAt", thirtyDaysAgo)
                .get()
                .addOnSuccessListener(snapshot -> {
                    if (snapshot.size() >= 5) {
                        Map<String, Object> coupon = new HashMap<>();
                        coupon.put("type", "loyalty");
                        coupon.put("code", "LOYAL" + System.currentTimeMillis());
                        coupon.put("discountType", "percent");
                        coupon.put("discountValue", 15);
                        coupon.put("isUsed", false);
                        coupon.put("createdAt", com.google.firebase.Timestamp.now());

                        FirebaseFirestore.getInstance()
                                .collection("users").document(uid).collection("coupons")
                                .add(coupon);

                        FirebaseFirestore.getInstance().collection("users").document(uid)
                                .update("stats.coupons", com.google.firebase.firestore.FieldValue.increment(1));
                    }
                });
    }
}
