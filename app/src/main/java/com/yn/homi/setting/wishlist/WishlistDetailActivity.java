package com.yn.homi.setting.wishlist;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.yn.homi.R;
import com.yn.homi.cart.CartActivity;
import com.yn.homi.cart.CartManager;
import com.yn.homi.checkout.CheckoutActivity;
import com.yn.homi.model.CartItem;
import com.yn.homi.models.Product;
import com.yn.homi.models.Wishlist;
import com.yn.homi.utils.FavoritesManager;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class WishlistDetailActivity extends AppCompatActivity {

    private String wishlistName;
    private RecyclerView recyclerView;
    private WishlistAdapter adapter;
    private List<Product> items;
    private FavoritesManager favoritesManager;
    private TextView tvTitle, tvCartBadge;
    private ImageView btnBack, ivCart;
    private LinearLayout llSelectionOptions;
    private View btnDeleteSelected, btnAddToCartSelected, btnBuyNowSelected;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_wishlist_detail);

        wishlistName = getIntent().getStringExtra("WISHLIST_NAME");
        favoritesManager = new FavoritesManager(this);

        tvTitle = findViewById(R.id.tv_wishlist_title);
        tvCartBadge = findViewById(R.id.tv_cart_badge);
        btnBack = findViewById(R.id.btnBack);
        ivCart = findViewById(R.id.iv_cart);
        recyclerView = findViewById(R.id.recyclerWishlistDetail);
        llSelectionOptions = findViewById(R.id.ll_selection_options);
        btnDeleteSelected = findViewById(R.id.btn_delete_selected);
        btnAddToCartSelected = findViewById(R.id.btn_add_to_cart_selected);
        btnBuyNowSelected = findViewById(R.id.btn_buy_now_selected);

        tvTitle.setText(wishlistName);
        btnBack.setOnClickListener(v -> {
            if (adapter != null && adapter.isSelectionMode()) {
                adapter.setSelectionMode(false);
            } else {
                finish();
            }
        });

        ivCart.setOnClickListener(v -> {
            Intent intent = new Intent(this, CartActivity.class);
            startActivity(intent);
        });

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        
        setupSwipeToDelete();
        loadWishlistItems();
        setupSelectionActions();
        updateCartBadge();
    }

    private void updateCartBadge() {
        if (tvCartBadge == null) return;
        int count = CartManager.getInstance(this).getTotalItemCount();
        if (count > 0) {
            tvCartBadge.setText(String.valueOf(count));
            tvCartBadge.setVisibility(View.VISIBLE);
        } else {
            tvCartBadge.setVisibility(View.GONE);
        }
    }

    private void setupSelectionActions() {
        btnDeleteSelected.setOnClickListener(v -> {
            List<Product> selected = adapter.getSelectedItems();
            for (Product p : selected) {
                favoritesManager.removeProductFromWishlist(wishlistName, p.getId());
            }
            adapter.setSelectionMode(false);
            loadWishlistItems();
            Toast.makeText(this, R.string.delete_item, Toast.LENGTH_SHORT).show();
        });

        btnAddToCartSelected.setOnClickListener(v -> {
            List<Product> selected = adapter.getSelectedItems();
            for (Product p : selected) {
                // Add to cart
                CartItem cartItem = new CartItem(
                        p.getId(),
                        p.getName(),
                        p.getPrice(),
                        1,
                        p.getThumbnailUrl()
                );
                CartManager.getInstance(this).addItem(cartItem);
                
                // Remove from wishlist
                favoritesManager.removeProductFromWishlist(wishlistName, p.getId());
            }
            
            adapter.setSelectionMode(false);
            loadWishlistItems();
            updateCartBadge();
            Toast.makeText(this, R.string.add_to_cart, Toast.LENGTH_SHORT).show();
        });

        btnBuyNowSelected.setOnClickListener(v -> {
            List<Product> selected = adapter.getSelectedItems();
            if (!selected.isEmpty()) {
                // Prepare selected products list
                ArrayList<Product> productsToBuy = new ArrayList<>(selected);
                
                // Remove from wishlist as they are being bought
                for (Product p : selected) {
                    favoritesManager.removeProductFromWishlist(wishlistName, p.getId());
                }

                Intent intent = new Intent(this, CheckoutActivity.class);
                // Pass the specific selected products to checkout
                intent.putExtra("SELECTED_PRODUCTS", productsToBuy);
                startActivity(intent);
                
                adapter.setSelectionMode(false);
                loadWishlistItems();
                updateCartBadge();
            }
        });
    }

    private void setupSwipeToDelete() {
        androidx.recyclerview.widget.ItemTouchHelper.SimpleCallback swipeCallback = 
            new androidx.recyclerview.widget.ItemTouchHelper.SimpleCallback(0, androidx.recyclerview.widget.ItemTouchHelper.LEFT) {
                @Override
                public boolean onMove(@androidx.annotation.NonNull RecyclerView recyclerView, @androidx.annotation.NonNull RecyclerView.ViewHolder viewHolder, @androidx.annotation.NonNull RecyclerView.ViewHolder target) {
                    return false;
                }

                @Override
                public void onSwiped(@androidx.annotation.NonNull RecyclerView.ViewHolder viewHolder, int direction) {
                    int position = viewHolder.getAdapterPosition();
                    if (adapter != null) {
                        adapter.deleteItem(position);
                    }
                }

                @Override
                public void onChildDraw(@androidx.annotation.NonNull android.graphics.Canvas c, @androidx.annotation.NonNull RecyclerView recyclerView, @androidx.annotation.NonNull RecyclerView.ViewHolder viewHolder, float dX, float dY, int actionState, boolean isCurrentlyActive) {
                    if (actionState == androidx.recyclerview.widget.ItemTouchHelper.ACTION_STATE_SWIPE) {
                        android.view.View itemView = viewHolder.itemView;
                        android.graphics.Paint p = new android.graphics.Paint();
                        
                        if (dX < 0) { // Swiping to the left
                            // Draw dark background
                            p.setColor(android.graphics.Color.parseColor("#333333"));
                            c.drawRect((float) itemView.getRight() + dX, (float) itemView.getTop(), (float) itemView.getRight(), (float) itemView.getBottom(), p);

                            // Draw "Delete item" text
                            p.setColor(android.graphics.Color.WHITE);
                            p.setTextSize(40);
                            p.setAntiAlias(true);
                            String text = getString(R.string.delete_item);
                            float textWidth = p.measureText(text);
                            float itemHeight = (float) itemView.getBottom() - (float) itemView.getTop();
                            float x = (float) itemView.getRight() - 50 - textWidth;
                            float y = (float) itemView.getTop() + (itemHeight / 2) + (p.getTextSize() / 2) - 5;
                            c.drawText(text, x, y, p);
                        }
                    }
                    super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive);
                }
            };
        new androidx.recyclerview.widget.ItemTouchHelper(swipeCallback).attachToRecyclerView(recyclerView);
    }

    private void loadWishlistItems() {
        List<Wishlist> wishlists = favoritesManager.getWishlists();
        items = new ArrayList<>();
        for (Wishlist w : wishlists) {
            if (w.getName().equals(wishlistName)) {
                items.addAll(w.getItems());
                break;
            }
        }

        adapter = new WishlistAdapter(this, items, wishlistName);
        adapter.setOnSelectionModeListener(enabled -> {
            llSelectionOptions.setVisibility(enabled ? View.VISIBLE : View.GONE);
        });
        adapter.setOnCartUpdateListener(this::updateCartBadge);
        adapter.setOnBuyNowListener(product -> {
            Intent intent = new Intent(this, CheckoutActivity.class);
            List<Product> selected = new ArrayList<>();
            selected.add(product);
            intent.putExtra("SELECTED_PRODUCTS", (Serializable) selected);
            startActivity(intent);
        });
        recyclerView.setAdapter(adapter);
    }

    @Override
    protected void onResume() {
        super.onResume();
        updateCartBadge();
    }

    @Override
    public void onBackPressed() {
        if (adapter != null && adapter.isSelectionMode()) {
            adapter.setSelectionMode(false);
        } else {
            super.onBackPressed();
        }
    }
}
