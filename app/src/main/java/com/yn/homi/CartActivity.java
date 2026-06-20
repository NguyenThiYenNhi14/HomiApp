package com.yn.homi;

import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.yn.homi.adapters.CartAdapter;
import com.yn.homi.models.CartItem;
import com.yn.homi.utils.CartManager;
import java.util.List;
import java.util.Locale;

public class CartActivity extends AppCompatActivity implements CartAdapter.OnCartChangeListener {

    private RecyclerView rvCartItems;
    private CartAdapter adapter;
    private CartManager cartManager;
    private TextView tvTotalPrice;
    private View emptyCartContainer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cart);

        cartManager = new CartManager(this);
        initViews();
        loadCart();
    }

    private void initViews() {
        rvCartItems = findViewById(R.id.rv_cart_items);
        tvTotalPrice = findViewById(R.id.tv_total_price);
        emptyCartContainer = findViewById(R.id.empty_cart_container);
        findViewById(R.id.iv_back).setOnClickListener(v -> finish());
        
        rvCartItems.setLayoutManager(new LinearLayoutManager(this));
    }

    private void loadCart() {
        List<CartItem> items = cartManager.getCartItems();
        if (items.isEmpty()) {
            emptyCartContainer.setVisibility(View.VISIBLE);
            rvCartItems.setVisibility(View.GONE);
            tvTotalPrice.setText("$0.00");
        } else {
            emptyCartContainer.setVisibility(View.GONE);
            rvCartItems.setVisibility(View.VISIBLE);
            adapter = new CartAdapter(items, this);
            rvCartItems.setAdapter(adapter);
            calculateTotal(items);
        }
    }

    private void calculateTotal(List<CartItem> items) {
        double total = 0;
        for (CartItem item : items) {
            total += item.getProduct().getPrice() * item.getQuantity();
        }
        tvTotalPrice.setText(String.format(Locale.US, "$%.2f", total));
    }

    @Override
    public void onQuantityChanged(String productId, int newQuantity) {
        cartManager.updateQuantity(productId, newQuantity);
        loadCart();
    }

    @Override
    public void onRemoveItem(String productId) {
        cartManager.removeFromCart(productId);
        loadCart();
    }
}
