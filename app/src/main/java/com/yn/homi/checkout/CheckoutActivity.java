package com.yn.homi.checkout;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.bumptech.glide.Glide;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.yn.homi.adapter.CartAdapter;
import com.yn.homi.R;
import com.yn.homi.checkout.model.PaymentMethod;
import com.yn.homi.model.CartItem;
import com.yn.homi.models.Product;
import com.yn.homi.data.ProductRepository;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.text.NumberFormat;

import com.yn.homi.setting.order.Order;
import com.yn.homi.setting.order.OrderItem;
import com.yn.homi.setting.order.OrderManager;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Random;

public class CheckoutActivity extends AppCompatActivity {
    private List<CartItem> cartItems;
    private final double DELIVERY_FEE = 5.0;
    private PaymentMethod selectedPayment = null;

    private TextView tvItemCost, tvOrderTotal, tvPaymentMethod, tvDeliveryCost;
    private RecyclerView rvCheckoutItems;
    private CartAdapter adapter;

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
        rvCheckoutItems = findViewById(R.id.rvCheckoutItems);
    }

    private void loadData() {
        // Sử dụng đúng class Product từ package com.yn.homi.models
        List<Product> directProducts = (List<Product>) getIntent().getSerializableExtra("SELECTED_PRODUCTS");

        if (directProducts != null && !directProducts.isEmpty()) {
            // Chuyển đổi Product sang CartItem để hiển thị trong adapter
            cartItems = new ArrayList<>();
            for (Product p : directProducts) {
                cartItems.add(new CartItem(p.getId(), p.getName(), p.getPrice(), 1, p.getImageUrl()));
            }
        } else {
            // Mặc định lấy toàn bộ items từ giỏ hàng
            cartItems = com.yn.homi.cart.CartManager.getInstance(this).getItems();
        }
        
        if (cartItems.isEmpty()) {
            // Fallback nếu giỏ hàng trống
            List<Product> products = ProductRepository.getProducts(this);
            if (products != null && !products.isEmpty()) {
                for (int i = 0; i < Math.min(2, products.size()); i++) {
                    Product p = products.get(i);
                    com.yn.homi.cart.CartManager.getInstance(this).addItem(new CartItem(p.getId(), p.getName(), p.getPrice(), 1, p.getImageUrl()));
                }
            }
            cartItems = com.yn.homi.cart.CartManager.getInstance(this).getItems();
        }

        // Setup RecyclerView hiển thị toàn bộ sản phẩm
        adapter = new CartAdapter(this, cartItems, null);
        adapter.setEditable(false); // Ở màn Checkout thì không cho chỉnh số lượng
        rvCheckoutItems.setLayoutManager(new LinearLayoutManager(this));
        rvCheckoutItems.setAdapter(adapter);
        
        updatePricingUI();
    }

    private void setupListeners() {
        findViewById(R.id.layoutPaymentMethod).setOnClickListener(v -> openPaymentSheet());
        findViewById(R.id.btnCheckout).setOnClickListener(v -> {
            if (selectedPayment == null) openPaymentSheet();
            else proceedToSuccess();
        });
    }

    private void updatePricingUI() {
        double subtotal = 0;
        int totalQty = 0;
        
        if (cartItems != null) {
            for (CartItem item : cartItems) {
                subtotal += item.getPrice() * item.getQuantity();
                totalQty += item.getQuantity();
            }
        }
        
        double total = subtotal + DELIVERY_FEE;

        if (tvItemCost != null) tvItemCost.setText(getDollarString(subtotal));
        if (tvDeliveryCost != null) tvDeliveryCost.setText(getDollarString(DELIVERY_FEE));
        if (tvOrderTotal != null) tvOrderTotal.setText(getDollarString(total));
        
        TextView tvItemLabel = findViewById(R.id.tvItemLabel);
        if (tvItemLabel != null) {
            tvItemLabel.setText(getString(R.string.label_subtotal_items, totalQty));
        }
    }

    private String getDollarString(double amount) {
        NumberFormat currencyUS = NumberFormat.getCurrencyInstance(Locale.US);
        return currencyUS.format(amount);
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
                    "Standard",
                    ci.getQuantity(),
                    ci.getImageUrl(),
                    "Packing"
            ));
        }

        String orderId = String.valueOf(100000 + new Random().nextInt(900000));
        String date = new SimpleDateFormat("MMM dd, yyyy", Locale.US).format(new Date());
        
        TextView tvAddress = findViewById(R.id.tvAddress);
        String address = tvAddress != null ? tvAddress.getText().toString() : "No address provided";

        Order newOrder = new Order(
                orderId,
                Order.Status.PENDING,
                orderItems,
                subtotal,
                DELIVERY_FEE,
                date,
                address,
                ""
        );

        // 2. Lưu vào OrderManager
        OrderManager.getInstance(this).addOrder(newOrder);

        // 3. Xoá các items đã mua khỏi giỏ hàng
        com.yn.homi.cart.CartManager cartManager = com.yn.homi.cart.CartManager.getInstance(this);
        for (CartItem ci : cartItems) {
            cartManager.removeItem(ci.getId());
        }

        // 4. Chuyển sang màn hình thành công
        startActivity(new Intent(this, OrderSuccessActivity.class));
        finish();
    }
}
