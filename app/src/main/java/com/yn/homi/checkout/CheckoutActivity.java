package com.yn.homi.checkout;

import android.content.Intent;
import android.os.Bundle;
import android.widget.*;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import com.bumptech.glide.Glide;
import com.yn.homi.R;
import com.yn.homi.checkout.model.PaymentMethod;
import com.yn.homi.model.CartItem;

public class CheckoutActivity extends AppCompatActivity {
    private CartItem currentItem;
    private final double DELIVERY_FEE = 5.00;
    private PaymentMethod selectedPayment = null;

    private TextView tvProductName, tvProductPrice, tvQuantity, tvItemCost, tvOrderTotal, tvPaymentMethod;
    private ImageView imgProduct;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_checkout);
        bindViews();
        loadData();
        setupListeners();
    }

    private void bindViews() {
        findViewById(R.id.btnBack).setOnClickListener(v -> finish());
        tvProductName = findViewById(R.id.tvProductName);
        tvProductPrice = findViewById(R.id.tvProductPrice);
        imgProduct = findViewById(R.id.imgProduct);
        tvQuantity = findViewById(R.id.tvQuantity);
        tvItemCost = findViewById(R.id.tvItemCost);
        tvOrderTotal = findViewById(R.id.tvOrderTotal);
        tvPaymentMethod = findViewById(R.id.tvPaymentMethod);
    }

    private void loadData() {
        currentItem = (CartItem) getIntent().getSerializableExtra("EXTRA_CART_ITEM");
        if (currentItem == null) {
            // Fallback để test
            currentItem = new CartItem("ID", "Modern L-Shaped Sofa", 13500.0, 1, "");
        }

        tvProductName.setText(currentItem.getName());
        tvProductPrice.setText(String.format("$%.2f", currentItem.getPrice()));
        Glide.with(this).load(currentItem.getImageUrl()).into(imgProduct);
        updatePricingUI();
    }

    private void setupListeners() {
        findViewById(R.id.btnMinus).setOnClickListener(v -> {
            if (currentItem.getQuantity() > 1) {
                currentItem.setQuantity(currentItem.getQuantity() - 1);
                updatePricingUI();
            }
        });
        findViewById(R.id.btnPlus).setOnClickListener(v -> {
            currentItem.setQuantity(currentItem.getQuantity() + 1);
            updatePricingUI();
        });
        findViewById(R.id.layoutPaymentMethod).setOnClickListener(v -> openPaymentSheet());
        findViewById(R.id.btnCheckout).setOnClickListener(v -> {
            if (selectedPayment == null) openPaymentSheet();
            else proceedToSuccess();
        });
    }

    private void updatePricingUI() {
        tvQuantity.setText(String.valueOf(currentItem.getQuantity()));
        double subtotal = currentItem.getPrice() * currentItem.getQuantity();
        tvItemCost.setText(String.format("$%.2f", subtotal));
        tvOrderTotal.setText(String.format("$%.2f", subtotal + DELIVERY_FEE));
    }

    private void openPaymentSheet() {
        PaymentMethodBottomSheet sheet = new PaymentMethodBottomSheet();
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