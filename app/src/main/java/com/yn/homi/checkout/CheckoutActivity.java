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
import com.yn.homi.model.Product;
import com.yn.homi.data.ProductRepository;

import java.util.List;
import java.util.Locale;
import java.text.NumberFormat;

public class CheckoutActivity extends AppCompatActivity {
    private List<CartItem> cartItems;
    private final double DELIVERY_FEE_VND = 30000.0;
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
        cartItems = com.yn.homi.cart.CartManager.getInstance().getItems();
        
        if (cartItems.isEmpty()) {
            // Fallback nếu giỏ hàng trống (lẽ ra không xảy ra)
            List<Product> products = ProductRepository.getProducts(this);
            if (products != null && !products.isEmpty()) {
                for (int i = 0; i < Math.min(2, products.size()); i++) {
                    Product p = products.get(i);
                    com.yn.homi.cart.CartManager.getInstance().addItem(new CartItem(p.getProductId(), p.getName(), p.getPrice(), 1, p.getFirstImage()));
                }
            }
        }

        // Setup RecyclerView hiển thị toàn bộ sản phẩm trong giỏ
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
        com.yn.homi.cart.CartManager manager = com.yn.homi.cart.CartManager.getInstance();
        int totalQty = manager.getTotalItemCount();
        double subtotal = manager.getSubTotal();
        double total = subtotal + DELIVERY_FEE_VND;

        if (tvItemCost != null) tvItemCost.setText(getVNDString(subtotal));
        if (tvDeliveryCost != null) tvDeliveryCost.setText(getVNDString(DELIVERY_FEE_VND));
        if (tvOrderTotal != null) tvOrderTotal.setText(getVNDString(total));
        
        TextView tvItemLabel = findViewById(R.id.tvItemLabel);
        if (tvItemLabel != null) {
            tvItemLabel.setText("Subtotal (" + totalQty + " items):");
        }
    }

    private String getVNDString(double amount) {
        NumberFormat currencyVN = NumberFormat.getCurrencyInstance(new Locale("vi", "VN"));
        return currencyVN.format(amount).replace("₫", "").trim() + " VND";
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
        startActivity(new Intent(this, OrderSuccessActivity.class));
        finish();
    }
}
