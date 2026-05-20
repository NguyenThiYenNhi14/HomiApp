package com.yn.homi.cart;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.yn.homi.R;
import com.yn.homi.adapter.CartAdapter;
import com.yn.homi.checkout.CheckoutActivity;
import com.yn.homi.model.CartItem;

import java.util.List;

public class CartActivity extends AppCompatActivity
        implements CartAdapter.CartActionListener, CartManager.CartChangeListener {

    private CartAdapter adapter;
    private List<CartItem> cartItems;

    private TextView tvSubTotal, tvShipping, tvOrderTotal;
    private View     layoutSummary;
    private Button   btnContinue;
    private View     layoutEmpty;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_cart);

        if (com.yn.homi.cart.CartManager.getInstance().getItems().isEmpty()) {
            com.yn.homi.cart.CartManager.getInstance().addItem(new com.yn.homi.model.CartItem("1", "Duplo high chair", 172.00, 1, "https://www.ikea.com/sg/en/images/products/lyckan-chair-yellow-birch-veneer__1370467_pe958731_s5.jpg"));
            com.yn.homi.cart.CartManager.getInstance().addItem(new com.yn.homi.model.CartItem("2", "B2 lounge chair", 120.00, 1, "https://www.ikea.com/sg/en/images/products/herrakra-armchair-vissle-grey__1213671_pe911201_s5.jpg"));
        }

        // Edge-to-edge insets
        View main      = findViewById(R.id.main);
        View bottomBar = findViewById(R.id.layoutBottomBar);
        ViewCompat.setOnApplyWindowInsetsListener(main, (v, insets) -> {
            Insets sys = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(sys.left, sys.top, sys.right, 0);
            int base = (int)(12 * getResources().getDisplayMetrics().density);
            bottomBar.setPadding(
                    bottomBar.getPaddingLeft(),
                    bottomBar.getPaddingTop(),
                    bottomBar.getPaddingRight(),
                    sys.bottom + base);
            return insets;
        });

        bindViews();
        setupRecyclerView();
        setupListeners();

        CartManager.getInstance().setCartChangeListener(this);
        updateSummary();
    }

    private void bindViews() {
        tvSubTotal    = findViewById(R.id.tvSubTotal);
        tvShipping    = findViewById(R.id.tvShipping);
        tvOrderTotal  = findViewById(R.id.tvOrderTotal);
        layoutSummary = findViewById(R.id.layoutSummary);
        btnContinue   = findViewById(R.id.btnContinue);
        layoutEmpty   = findViewById(R.id.layoutEmpty);

        // Nút Back
        findViewById(R.id.btnBack).setOnClickListener(v -> finish());

        // Icon giỏ hàng trên toolbar (không cần thiết ở trang cart, ẩn đi)
        View btnCart = findViewById(R.id.btnCartIcon);
        if (btnCart != null) btnCart.setVisibility(View.GONE);
    }

    private void setupRecyclerView() {
        cartItems = CartManager.getInstance().getItems();
        adapter   = new CartAdapter(this, cartItems, this);

        RecyclerView rv = findViewById(R.id.rvCartItems);
        rv.setLayoutManager(new LinearLayoutManager(this));
        rv.setAdapter(adapter);

        // THÊM ĐOẠN NÀY ĐỂ BẬT TÍNH NĂNG VUỐT ĐỂ XOÁ
        new androidx.recyclerview.widget.ItemTouchHelper(new androidx.recyclerview.widget.ItemTouchHelper.SimpleCallback(0, androidx.recyclerview.widget.ItemTouchHelper.LEFT | androidx.recyclerview.widget.ItemTouchHelper.RIGHT) {
            @Override
            public boolean onMove(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, RecyclerView.ViewHolder target) {
                return false;
            }

            @Override
            public void onSwiped(RecyclerView.ViewHolder viewHolder, int direction) {
                int position = viewHolder.getAdapterPosition();
                CartItem item = cartItems.get(position);
                CartManager.getInstance().removeItem(item.getId());
                // Data sẽ tự cập nhật nhờ hàm onCartChanged
            }
        }).attachToRecyclerView(rv);
    }

    private void setupListeners() {
        btnContinue.setOnClickListener(v -> {
            if (cartItems.isEmpty()) return;

            // Lấy item đầu tiên truyền sang CheckoutActivity
            // (nếu sau này checkout nhiều item thì truyền cả list)
            CartItem first = cartItems.get(0);
            Intent intent = new Intent(this, CheckoutActivity.class);
            intent.putExtra("EXTRA_CART_ITEM", first);
            startActivity(intent);
        });
    }

    // ─── CartAdapter.CartActionListener ───────────────
    @Override
    public void onQuantityChanged(CartItem item, int newQty) {
        CartManager.getInstance().updateQuantity(item.getId(), newQty);
        // onCartChanged() sẽ được gọi tự động
    }

    @Override
    public void onRemove(CartItem item) {
        CartManager.getInstance().removeItem(item.getId());
    }

    // ─── CartManager.CartChangeListener ───────────────
    @Override
    public void onCartChanged() {
        runOnUiThread(() -> {
            adapter.notifyDataSetChanged();
            updateSummary();
        });
    }

    // ─── Cập nhật summary bar ─────────────────────────
    private void updateSummary() {
        boolean empty = cartItems.isEmpty();
        layoutEmpty.setVisibility(empty ? View.VISIBLE : View.GONE);
        layoutSummary.setVisibility(empty ? View.GONE : View.VISIBLE);

        if (!empty) {
            double sub = CartManager.getInstance().getSubTotal();
            tvSubTotal.setText(String.format("$%.2f", sub));
            tvShipping.setText("FREE");
            tvOrderTotal.setText(String.format("$%.2f", sub));
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        CartManager.getInstance().setCartChangeListener(null);
    }
}