package com.yn.homi.cart;

import android.content.Context;
import android.content.Intent;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.ItemTouchHelper;

import com.yn.homi.R;
import com.yn.homi.adapter.CartAdapter;
import com.yn.homi.model.CartItem;

import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;

public class CartActivity extends AppCompatActivity
        implements CartAdapter.OnCartItemInteractionListener, CartManager.CartChangeListener {

    private List<CartItem> cartItems;
    private CartAdapter adapter;
    private TextView tvSubTotal, tvShipping, tvOrderTotal;
    private View layoutSummary;
    private Button btnContinue;
    private View layoutEmpty;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_cart);

        // DATA GIẢ 6 SẢN PHẨM
        if (com.yn.homi.cart.CartManager.getInstance().getItems().isEmpty()) {
            List<com.yn.homi.model.Product> products = com.yn.homi.data.ProductRepository.getProducts(this);
            if (products != null && !products.isEmpty()) {
                for (int i = 0; i < Math.min(6, products.size()); i++) {
                    com.yn.homi.model.Product p = products.get(i);
                    com.yn.homi.cart.CartManager.getInstance().addItem(new com.yn.homi.model.CartItem(
                            p.getProductId(), p.getName(), p.getPrice(), 1, p.getFirstImage()));
                }
            } else {
                com.yn.homi.cart.CartManager manager = com.yn.homi.cart.CartManager.getInstance();
                manager.addItem(new com.yn.homi.model.CartItem("1", "Modern L-Shaped Sofa", 13500000.0, 1, "https://www.ikea.com/sg/en/images/products/ektorp-3-seat-sofa-with-chaise-longue-hakebo-dark-grey__1194857_pe902107_s5.jpg"));
                manager.addItem(new com.yn.homi.model.CartItem("2", "Ergonomic Chair", 4500000.0, 1, "https://v-f-f.com/cdn/shop/files/E3-1-3.jpg?v=1701332821"));
                manager.addItem(new com.yn.homi.model.CartItem("3", "Wooden Dining Table", 8200000.0, 1, "https://m.media-amazon.com/images/I/71Y87G6YjHL._AC_SL1500_.jpg"));
                manager.addItem(new com.yn.homi.model.CartItem("4", "King Size Bed", 21000000.0, 1, "https://m.media-amazon.com/images/I/81I7I-7Xf+L._AC_SL1500_.jpg"));
                manager.addItem(new com.yn.homi.model.CartItem("5", "Bedside Table", 1200000.0, 1, "https://m.media-amazon.com/images/I/71X87G6YjHL._AC_SL1500_.jpg"));
                manager.addItem(new com.yn.homi.model.CartItem("6", "Wardrobe with Mirror", 15500000.0, 1, "https://m.media-amazon.com/images/I/61X87G6YjHL._AC_SL1500_.jpg"));
            }
        }

        View main = findViewById(R.id.main);
        View bottomBar = findViewById(R.id.layoutBottomBar);
        if (main != null && bottomBar != null) {
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
        }

        bindViews();
        loadData();
        setupListeners();
    }

    private void bindViews() {
        tvSubTotal    = findViewById(R.id.tvSubTotal);
        tvShipping    = findViewById(R.id.tvShipping);
        tvOrderTotal  = findViewById(R.id.tvOrderTotal);
        layoutSummary = findViewById(R.id.layoutSummary);
        btnContinue   = findViewById(R.id.btnContinue);
        layoutEmpty   = findViewById(R.id.layoutEmpty);
    }

    private void loadData() {
        setupRecyclerView();
        com.yn.homi.cart.CartManager.getInstance().setCartChangeListener(this);
        updateSummary();
    }

    private void setupListeners() {
        ImageButton btnBack = findViewById(R.id.btnBack);
        if (btnBack != null) btnBack.setOnClickListener(v -> finish());

        if (btnContinue != null) {
            btnContinue.setOnClickListener(v -> onContinue());
        }
    }

    private void setupRecyclerView() {
        cartItems = com.yn.homi.cart.CartManager.getInstance().getItems();
        adapter   = new com.yn.homi.adapter.CartAdapter(this, cartItems, this);

        RecyclerView rvCartItems = findViewById(R.id.rvCartItems);
        if (rvCartItems != null) {
            rvCartItems.setLayoutManager(new LinearLayoutManager(this));
            rvCartItems.setAdapter(adapter);

            new ItemTouchHelper(new ItemTouchHelper.Callback() {
                @Override
                public int getMovementFlags(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder) {
                    // ĐỔI SANG VUỐT TRÁI (LEFT)
                    return makeMovementFlags(0, ItemTouchHelper.LEFT);
                }

                @Override
                public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder target) {
                    return false;
                }

                @Override
                public float getSwipeThreshold(@NonNull RecyclerView.ViewHolder viewHolder) {
                    return 0.6f; // Vuốt qua 60% màn hình mới xóa
                }

                @Override
                public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
                    int position = viewHolder.getAdapterPosition();
                    CartItem swipedItem = cartItems.get(position);
                    com.yn.homi.cart.CartManager.getInstance().removeItem(swipedItem.getId());
                    Toast.makeText(CartActivity.this, "Đã xoá " + swipedItem.getName(), Toast.LENGTH_SHORT).show();
                }

                @Override
                public void onChildDraw(@NonNull Canvas c, @NonNull RecyclerView recyclerView,
                                        @NonNull RecyclerView.ViewHolder viewHolder, float dX, float dY,
                                        int actionState, boolean isCurrentlyActive) {

                    // NẾU VUỐT TRÁI THÌ dX SẼ ÂM (< 0)
                    if (actionState == ItemTouchHelper.ACTION_STATE_SWIPE && dX < 0) {
                        View itemView = viewHolder.itemView;
                        Context context = recyclerView.getContext();
                        float density = context.getResources().getDisplayMetrics().density;
                        float cornerRadius = 16 * density;

                        // VẼ NỀN ĐỎ ĐẬM Ở DƯỚI (Vẫn vẽ full thẻ, phần thò ra bên phải sẽ lộ màu đỏ)
                        Paint paint = new Paint();
                        paint.setColor(Color.parseColor("#B91C1C"));

                        RectF background = new RectF(
                                (float) itemView.getLeft(),
                                (float) itemView.getTop(),
                                (float) itemView.getRight(),
                                (float) itemView.getBottom()
                        );
                        c.drawRoundRect(background, cornerRadius, cornerRadius, paint);

                        // VẼ ICON THÙNG RÁC BÊN PHẢI MÀU TRẮNG
                        Drawable binIcon = ContextCompat.getDrawable(context, R.drawable.ic_bin);
                        if (binIcon != null) {
                            binIcon.setTint(Color.WHITE);

                            int iconSize = (int) (36 * density);
                            int iconMarginRight = (int) (24 * density); // Cách lề phải 24dp

                            // Căn giữa theo chiều cao của thẻ
                            int iconTop = itemView.getTop() + (itemView.getHeight() - iconSize) / 2;
                            int iconBottom = iconTop + iconSize;

                            // Tính toán tọa độ đặt sát bên phải
                            int iconRight = itemView.getRight() - iconMarginRight;
                            int iconLeft = iconRight - iconSize;

                            binIcon.setBounds(iconLeft, iconTop, iconRight, iconBottom);
                            binIcon.draw(c);
                        }
                    }
                    super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive);
                }
            }).attachToRecyclerView(rvCartItems);
        }
    }

    @Override
    public void onItemQuantityChanged(String itemId, int newQuantity) {
        com.yn.homi.cart.CartManager.getInstance().updateQuantity(itemId, newQuantity);
    }

    @Override
    public void onCartChanged() {
        if (adapter != null) adapter.notifyDataSetChanged();
        updateSummary();
    }

    private void updateSummary() {
        com.yn.homi.cart.CartManager manager = com.yn.homi.cart.CartManager.getInstance();
        boolean isEmpty = manager.getItems().isEmpty();

        if (layoutSummary != null) layoutSummary.setVisibility(isEmpty ? View.GONE : View.VISIBLE);
        if (btnContinue != null) btnContinue.setEnabled(!isEmpty);
        if (layoutEmpty != null) layoutEmpty.setVisibility(isEmpty ? View.VISIBLE : View.GONE);

        if (!isEmpty) {
            if (tvSubTotal != null)    tvSubTotal.setText(getVNDString(manager.getSubTotal()));
            if (tvShipping != null)    tvShipping.setText("FREE");
            if (tvOrderTotal != null)  tvOrderTotal.setText(getVNDString(manager.getOrderTotal()));
        }
    }

    private String getVNDString(double amount) {
        Locale localeVN = new Locale("vi", "VN");
        NumberFormat currencyVN = NumberFormat.getCurrencyInstance(localeVN);
        String formatted = currencyVN.format(amount);
        return formatted.replace("₫", "").trim() + " VND";
    }

    private void onContinue() {
        if (!cartItems.isEmpty()) {
            Intent intent = new Intent(this, com.yn.homi.checkout.CheckoutActivity.class);
            startActivity(intent);
        }
    }
}