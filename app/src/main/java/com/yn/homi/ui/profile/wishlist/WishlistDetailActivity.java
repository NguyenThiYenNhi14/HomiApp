package com.yn.homi.ui.profile.wishlist;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.view.LayoutInflater;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.graphics.Color;
import androidx.annotation.NonNull;
import com.yn.homi.core.BaseActivity;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.yn.homi.R;
import com.yn.homi.ui.cart.CartActivity;
import com.yn.homi.ui.cart.CartManager;
import com.yn.homi.ui.checkout.CheckoutActivity;
import com.yn.homi.data.model.CartItem;
import com.yn.homi.data.model.Product;
import com.yn.homi.data.model.Wishlist;
import com.yn.homi.utils.FavoritesManager;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class WishlistDetailActivity extends BaseActivity {

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
                // Lấy màu và size đầu tiên nếu có
                String defaultColor = null;
                String defaultImageUrl = p.getImageUrl();
                if (p.getColorVariants() != null && !p.getColorVariants().isEmpty()) {
                    com.yn.homi.data.model.Product.ColorVariant variant = p.getColorVariants().get(0);
                    defaultColor = variant.getName();
                    if (variant.getImageUrl() != null && !variant.getImageUrl().isEmpty()) {
                        defaultImageUrl = variant.getImageUrl();
                    }
                }
                String defaultSize = (p.getSizeVariants() != null && !p.getSizeVariants().isEmpty())
                        ? p.getSizeVariants().get(0).getLabel() : null;

                // Add to cart
                CartItem cartItem = new CartItem(p.getId(), p.getName(), p.getPrice(), 1, defaultImageUrl, defaultColor, defaultSize);
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
                ArrayList<CartItem> cartItemsToBuy = new ArrayList<>();
                for (Product p : selected) {
                    String defaultColor = null;
                    String defaultImageUrl = p.getImageUrl();
                    if (p.getColorVariants() != null && !p.getColorVariants().isEmpty()) {
                        com.yn.homi.data.model.Product.ColorVariant variant = p.getColorVariants().get(0);
                        defaultColor = variant.getName();
                        if (variant.getImageUrl() != null && !variant.getImageUrl().isEmpty()) {
                            defaultImageUrl = variant.getImageUrl();
                        }
                    }
                    String defaultSize = (p.getSizeVariants() != null && !p.getSizeVariants().isEmpty())
                            ? p.getSizeVariants().get(0).getLabel() : null;
                    cartItemsToBuy.add(new CartItem(p.getId(), p.getName(), p.getPrice(), 1, defaultImageUrl, defaultColor, defaultSize));
                }
                
                Intent intent = new Intent(this, CheckoutActivity.class);
                intent.putExtra("SELECTED_CART_ITEMS", cartItemsToBuy);
                intent.putExtra("FROM_WISHLIST", true);
                intent.putExtra("WISHLIST_NAME", wishlistName);
                startActivity(intent);
                
                adapter.setSelectionMode(false);
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
        adapter.setOnVariantClickListener(this::showVariantSelectionDialog);
        adapter.setOnBuyNowListener(product -> {
            ArrayList<CartItem> cartItemsToBuy = new ArrayList<>();
            String defaultColor = (product.getColorVariants() != null && !product.getColorVariants().isEmpty()) 
                    ? product.getColorVariants().get(0).getName() : null;
            String defaultSize = (product.getSizeVariants() != null && !product.getSizeVariants().isEmpty())
                    ? product.getSizeVariants().get(0).getLabel() : null;
            cartItemsToBuy.add(new CartItem(product, 1, defaultColor, defaultSize));

            Intent intent = new Intent(this, CheckoutActivity.class);
            intent.putExtra("SELECTED_CART_ITEMS", cartItemsToBuy);
            intent.putExtra("FROM_WISHLIST", true);
            intent.putExtra("WISHLIST_NAME", wishlistName);
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

    private void showVariantSelectionDialog(Product product) {
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_variant_selection, null);
        com.google.android.material.bottomsheet.BottomSheetDialog dialog = new com.google.android.material.bottomsheet.BottomSheetDialog(this);
        dialog.setContentView(dialogView);

        ImageView imgProduct = dialogView.findViewById(R.id.imgProduct);
        TextView tvPrice = dialogView.findViewById(R.id.tvPrice);
        TextView tvStock = dialogView.findViewById(R.id.tvStock);
        TextView tvQuantity = dialogView.findViewById(R.id.tvQuantity);
        RecyclerView rvColors = dialogView.findViewById(R.id.rvColors);
        
        com.bumptech.glide.Glide.with(this).load(product.getThumbnailUrl()).into(imgProduct);
        tvPrice.setText(getUSDString(product.getPrice()));
        tvStock.setText(getString(R.string.stock, product.getStockStatus()));
        
        // Wishlist usually doesn't store quantity, default to 1
        tvQuantity.setText("1");
        dialogView.findViewById(R.id.layoutQuantitySelector).setVisibility(View.GONE);

        final String[] selectedColor = {null};
        if (product.getColorVariants() != null && !product.getColorVariants().isEmpty()) {
            selectedColor[0] = product.getColorVariants().get(0).getName();
        }

        // Setup Colors
        if (product.getColorVariants() != null) {
            rvColors.setLayoutManager(new androidx.recyclerview.widget.LinearLayoutManager(this, androidx.recyclerview.widget.LinearLayoutManager.HORIZONTAL, false));
            VariantColorAdapter colorAdapter = new VariantColorAdapter(product.getColorVariants(), selectedColor[0], variant -> {
                selectedColor[0] = variant.getName();
                if (variant.getImageUrl() != null && !variant.getImageUrl().isEmpty()) {
                    com.bumptech.glide.Glide.with(this).load(variant.getImageUrl()).into(imgProduct);
                }
            });
            rvColors.setAdapter(colorAdapter);
        }

        dialogView.findViewById(R.id.btnConfirm).setOnClickListener(v -> {
            // Cập nhật UI ngay lập tức
            for (Product p : items) {
                if (p.getId().equals(product.getId())) {
                    // Cập nhật list color variants để giữ lại lựa chọn (giả lập vì model Product k lưu selectedColor)
                    if (product.getColorVariants() != null) {
                        List<Product.ColorVariant> newVariants = new ArrayList<>();
                        for (Product.ColorVariant cv : product.getColorVariants()) {
                            if (cv.getName().equals(selectedColor[0])) {
                                newVariants.add(0, cv); // Đưa màu đã chọn lên đầu
                            } else {
                                newVariants.add(cv);
                            }
                        }
                        p.setColorVariants(newVariants);
                    }
                    break;
                }
            }
            
            // Lưu vào SharedPreferences qua FavoritesManager
            List<Wishlist> wishlists = favoritesManager.getWishlists();
            for (Wishlist w : wishlists) {
                if (w.getName().equals(wishlistName)) {
                    for (Product p : w.getItems()) {
                        if (p.getId().equals(product.getId())) {
                            if (product.getColorVariants() != null) {
                                List<Product.ColorVariant> newVariants = new ArrayList<>();
                                for (Product.ColorVariant cv : product.getColorVariants()) {
                                    if (cv.getName().equals(selectedColor[0])) {
                                        newVariants.add(0, cv);
                                    } else {
                                        newVariants.add(cv);
                                    }
                                }
                                p.setColorVariants(newVariants);
                            }
                            break;
                        }
                    }
                    break;
                }
            }
            favoritesManager.saveWishlists(wishlists);
            
            adapter.notifyDataSetChanged();
            dialog.dismiss();
            Toast.makeText(this, R.string.msg_variant_updated, Toast.LENGTH_SHORT).show();
        });

        dialogView.findViewById(R.id.btnClose).setOnClickListener(v -> dialog.dismiss());
        dialog.show();
    }

    private String getUSDString(double amount) {
        return String.format(Locale.US, "$%.2f", amount);
    }

    private static class VariantColorAdapter extends RecyclerView.Adapter<VariantColorAdapter.ViewHolder> {
        private final List<Product.ColorVariant> variants;
        private String selectedColor;
        private final OnColorSelectedListener listener;

        interface OnColorSelectedListener { void onColorSelected(Product.ColorVariant variant); }

        VariantColorAdapter(List<Product.ColorVariant> variants, String selectedColor, OnColorSelectedListener listener) {
            this.variants = variants;
            this.selectedColor = selectedColor;
            this.listener = listener;
        }

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            return new ViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_color_variant_chip, parent, false));
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            Product.ColorVariant v = variants.get(position);
            holder.tvName.setText(v.getName());
            com.bumptech.glide.Glide.with(holder.itemView.getContext()).load(v.getImageUrl()).into(holder.ivImage);
            
            boolean isSelected = v.getName().equals(selectedColor);
            holder.card.setCardBackgroundColor(isSelected ? Color.parseColor("#FFF1F0") : Color.parseColor("#F5F5F5"));
            holder.tvName.setTextColor(isSelected ? Color.parseColor("#EE4D2D") : Color.parseColor("#333333"));
            holder.card.setStrokeColor(isSelected ? Color.parseColor("#EE4D2D") : Color.TRANSPARENT);
            holder.card.setStrokeWidth(isSelected ? 2 : 0);

            holder.itemView.setOnClickListener(view -> {
                selectedColor = v.getName();
                notifyDataSetChanged();
                listener.onColorSelected(v);
            });
        }

        @Override
        public int getItemCount() { return variants.size(); }

        static class ViewHolder extends RecyclerView.ViewHolder {
            ImageView ivImage; TextView tvName; com.google.android.material.card.MaterialCardView card;
            ViewHolder(View v) { super(v); ivImage = v.findViewById(R.id.ivVariantImage); tvName = v.findViewById(R.id.tvVariantName); card = (com.google.android.material.card.MaterialCardView) v.findViewById(R.id.cardContainer); }
        }
    }
}
