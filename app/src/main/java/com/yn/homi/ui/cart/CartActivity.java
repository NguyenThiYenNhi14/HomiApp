package com.yn.homi.ui.cart;

import android.content.Context;
import android.content.Intent;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import com.yn.homi.core.BaseActivity;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.ItemTouchHelper;

import com.yn.homi.R;
import com.yn.homi.ui.cart.CartAdapter;
import com.yn.homi.data.model.CartItem;
import com.yn.homi.ui.shop.ProductDetailActivity;
import com.yn.homi.ui.checkout.CheckoutActivity;
import com.yn.homi.data.model.Product;
import com.yn.homi.data.repository.FirestoreRepository;
import com.yn.homi.data.local.SharedPrefManager;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class CartActivity extends BaseActivity
        implements CartAdapter.OnCartItemInteractionListener, CartManager.CartChangeListener {

    private List<CartItem> cartItems;
    private CartAdapter adapter;
    private TextView tvSubTotal, tvShipping, tvOrderTotal;
    private View layoutSummary, layoutBottomContainer, layoutSelectAll;
    private android.widget.CheckBox cbSelectAll;
    private android.widget.EditText etSearchCart;
    private Button btnContinue;
    private View layoutEmpty;
    private List<CartItem> filteredItems;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_cart);
        View main = findViewById(R.id.main);
        View bottomContainer = findViewById(R.id.layoutBottomContainer);
        if (main != null && bottomContainer != null) {
            ViewCompat.setOnApplyWindowInsetsListener(main, (v, insets) -> {
                Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
                v.setPadding(systemBars.left, systemBars.top, systemBars.right, 0);

                // Padding dưới cho cả khối container để nền trắng tràn xuống navigation bar
                int basePaddingBottom = (int) (4 * getResources().getDisplayMetrics().density);
                bottomContainer.setPadding(
                        bottomContainer.getPaddingLeft(),
                        bottomContainer.getPaddingTop(),
                        bottomContainer.getPaddingRight(),
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
        tvSubTotal            = findViewById(R.id.tvSubTotal);
        tvShipping            = findViewById(R.id.tvShipping);
        tvOrderTotal          = findViewById(R.id.tvOrderTotal);
        layoutSummary         = findViewById(R.id.layoutSummary);
        layoutBottomContainer = findViewById(R.id.layoutBottomContainer);
        btnContinue           = findViewById(R.id.btnContinue);
        layoutEmpty           = findViewById(R.id.layoutEmpty);
        layoutSelectAll       = findViewById(R.id.layoutSelectAll);
        cbSelectAll           = findViewById(R.id.cbSelectAll);
        etSearchCart          = findViewById(R.id.etSearchCart);
    }

    private void loadData() {
        setupRecyclerView();
        CartManager.getInstance(this).setCartChangeListener(this);
        updateSummary();
        updateSelectAllState();
    }

    private void setupListeners() {
        ImageButton btnBack = findViewById(R.id.btnBack);
        if (btnBack != null) btnBack.setOnClickListener(v -> finish());

        if (btnContinue != null) {
            btnContinue.setOnClickListener(v -> onContinue());
        }

        if (cbSelectAll != null) {
            cbSelectAll.setOnClickListener(v -> {
                boolean isChecked = cbSelectAll.isChecked();
                CartManager.getInstance(this).setAllSelected(isChecked);
            });
        }

        if (etSearchCart != null) {
            etSearchCart.addTextChangedListener(new android.text.TextWatcher() {
                @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
                @Override public void onTextChanged(CharSequence s, int start, int before, int count) {
                    performSearch(s.toString());
                }
                @Override public void afterTextChanged(android.text.Editable s) {}
            });
        }
    }

    private void performSearch(String query) {
        if (query.isEmpty()) {
            filteredItems = new ArrayList<>(cartItems);
        } else {
            filteredItems = new ArrayList<>();
            for (CartItem item : cartItems) {
                if (item.getName().toLowerCase().contains(query.toLowerCase())) {
                    filteredItems.add(item);
                }
            }
        }
        updateAdapter(filteredItems);
    }

    private void updateAdapter(List<CartItem> items) {
        if (adapter != null) {
            adapter.updateData(items);
        }
    }

    private void setupRecyclerView() {
        cartItems = CartManager.getInstance(this).getItems();
        filteredItems = new ArrayList<>(cartItems);
        adapter   = new CartAdapter(this, filteredItems, this);

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
                    CartManager.getInstance(CartActivity.this).removeItem(swipedItem.getId(), swipedItem.getSelectedColor(), swipedItem.getSelectedSize());
                    String msg = getString(R.string.msg_deleted_item, swipedItem.getName());
                    Toast.makeText(CartActivity.this, msg, Toast.LENGTH_SHORT).show();
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

                        // VẼ CHỮ "Xóa" BÊN PHẢI MÀU TRẮNG
                        Paint textPaint = new Paint();
                        textPaint.setColor(Color.WHITE);
                        textPaint.setTextSize(15 * density);
                        textPaint.setAntiAlias(true);
                        textPaint.setFakeBoldText(true);
                        textPaint.setTextAlign(Paint.Align.CENTER);

                        float textMarginRight = 36 * density;
                        float textX = itemView.getRight() - textMarginRight;
                        float textY = itemView.getTop() + (itemView.getHeight() / 2f) - ((textPaint.descent() + textPaint.ascent()) / 2f);

                        String deleteLabel = context.getString(R.string.delete);
                        c.drawText(deleteLabel, textX, textY, textPaint);
                    }
                    super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive);
                }
            }).attachToRecyclerView(rvCartItems);
        }
    }

    @Override
    public void onItemQuantityChanged(CartItem item, int newQuantity) {
        CartManager.getInstance(this).updateQuantity(item.getId(), item.getSelectedColor(), item.getSelectedSize(), newQuantity);
    }

    @Override
    public void onItemVariantClicked(CartItem item) {
        showVariantSelectionDialog(item);
    }

    @Override
    public void onItemSelectionChanged(CartItem item, boolean isSelected) {
        CartManager.getInstance(this).updateItemSelection(item.getId(), item.getSelectedColor(), item.getSelectedSize(), isSelected);
        updateSummary();
        updateSelectAllState();
    }

    private void updateSelectAllState() {
        if (cbSelectAll == null) return;
        boolean allSelected = true;
        if (cartItems.isEmpty()) {
            allSelected = false;
        } else {
            for (CartItem item : cartItems) {
                if (!item.isSelected()) {
                    allSelected = false;
                    break;
                }
            }
        }
        // Use a flag or remove listener temporarily to avoid infinite loop if using setChecked
        // But here we used setOnClickListener for cbSelectAll, so setChecked is fine
        cbSelectAll.setChecked(allSelected);
    }

    @Override
    public void onItemClicked(CartItem item) {
        Intent intent = new Intent(this, ProductDetailActivity.class);
        intent.putExtra("productId", item.getId());
        startActivity(intent);
    }

    private void showVariantSelectionDialog(CartItem cartItem) {
        // Cần lấy thông tin đầy đủ của Product để hiển thị danh sách màu/size
        new FirestoreRepository().getProductById(cartItem.getId(), new FirestoreRepository.OnProductLoadedListener() {
            @Override
            public void onLoaded(Product product) {
                View dialogView = getLayoutInflater().inflate(R.layout.dialog_variant_selection, null);
                com.google.android.material.bottomsheet.BottomSheetDialog dialog = new com.google.android.material.bottomsheet.BottomSheetDialog(CartActivity.this);
                dialog.setContentView(dialogView);

                ImageView imgProduct = dialogView.findViewById(R.id.imgProduct);
                TextView tvPrice = dialogView.findViewById(R.id.tvPrice);
                TextView tvStock = dialogView.findViewById(R.id.tvStock);
                TextView tvQuantity = dialogView.findViewById(R.id.tvQuantity);
                RecyclerView rvColors = dialogView.findViewById(R.id.rvColors);
                RecyclerView rvSizes = dialogView.findViewById(R.id.rvSizes);
                
                com.bumptech.glide.Glide.with(CartActivity.this).load(cartItem.getImageUrl()).into(imgProduct);
                tvPrice.setText(getUSDString(cartItem.getPrice()));
                tvStock.setText(getString(R.string.stock) + ": " + product.getStockStatus());
                
                final int[] qty = {cartItem.getQuantity()};
                tvQuantity.setText(String.valueOf(qty[0]));

                dialogView.findViewById(R.id.btnPlus).setOnClickListener(v -> {
                    qty[0]++;
                    tvQuantity.setText(String.valueOf(qty[0]));
                });

                dialogView.findViewById(R.id.btnMinus).setOnClickListener(v -> {
                    if (qty[0] > 1) {
                        qty[0]--;
                        tvQuantity.setText(String.valueOf(qty[0]));
                    }
                });

                final String[] selectedColor = {cartItem.getSelectedColor()};
                final String[] selectedSize = {cartItem.getSelectedSize()};
                final String[] selectedImageUrl = {cartItem.getImageUrl()};

                // Setup Colors
                if (product.getColorVariants() != null && rvColors != null) {
                    rvColors.setLayoutManager(new androidx.recyclerview.widget.LinearLayoutManager(CartActivity.this, androidx.recyclerview.widget.LinearLayoutManager.HORIZONTAL, false));
                    VariantColorAdapter colorAdapter = new VariantColorAdapter(product.getColorVariants(), selectedColor[0], variant -> {
                        selectedColor[0] = variant.getName();
                        if (variant.getImageUrl() != null && !variant.getImageUrl().isEmpty()) {
                            selectedImageUrl[0] = variant.getImageUrl();
                            com.bumptech.glide.Glide.with(CartActivity.this).load(variant.getImageUrl()).into(imgProduct);
                        }
                    });
                    rvColors.setAdapter(colorAdapter);
                }

                // Setup Sizes
                if (product.getSizeVariants() != null && rvSizes != null) {
                    rvSizes.setLayoutManager(new androidx.recyclerview.widget.LinearLayoutManager(CartActivity.this, androidx.recyclerview.widget.LinearLayoutManager.HORIZONTAL, false));
                    VariantSizeAdapter sizeAdapter = new VariantSizeAdapter(product.getSizeVariants(), selectedSize[0], variant -> {
                        selectedSize[0] = variant.getLabel();
                    });
                    rvSizes.setAdapter(sizeAdapter);
                }

                dialogView.findViewById(R.id.btnConfirm).setOnClickListener(v -> {
                    CartManager.getInstance(CartActivity.this).updateItemVariant(cartItem, selectedColor[0], selectedSize[0], qty[0], selectedImageUrl[0]);
                    dialog.dismiss();
                });

                dialogView.findViewById(R.id.btnClose).setOnClickListener(v -> dialog.dismiss());
                dialog.show();
            }

            @Override
            public void onError(Exception e) {
                Toast.makeText(CartActivity.this, getString(R.string.msg_error_load_product), Toast.LENGTH_SHORT).show();
            }
        });
    }

    // Adapter nội bộ cho popup chọn màu
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

    // Adapter nội bộ cho popup chọn size
    private static class VariantSizeAdapter extends RecyclerView.Adapter<VariantSizeAdapter.ViewHolder> {
        private final List<Product.SizeVariant> variants;
        private String selectedSize;
        private final OnSizeSelectedListener listener;

        interface OnSizeSelectedListener { void onSizeSelected(Product.SizeVariant variant); }

        VariantSizeAdapter(List<Product.SizeVariant> variants, String selectedSize, OnSizeSelectedListener listener) {
            this.variants = variants;
            this.selectedSize = selectedSize;
            this.listener = listener;
        }

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            return new ViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_size_variant_chip, parent, false));
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            Product.SizeVariant v = variants.get(position);
            holder.tvLabel.setText(v.getLabel());
            
            boolean isSelected = v.getLabel().equals(selectedSize);
            holder.card.setCardBackgroundColor(isSelected ? Color.parseColor("#FFF1F0") : Color.parseColor("#F5F5F5"));
            holder.tvLabel.setTextColor(isSelected ? Color.parseColor("#EE4D2D") : Color.parseColor("#333333"));
            holder.card.setStrokeColor(isSelected ? Color.parseColor("#EE4D2D") : Color.TRANSPARENT);
            holder.card.setStrokeWidth(isSelected ? 2 : 0);

            holder.itemView.setOnClickListener(view -> {
                selectedSize = v.getLabel();
                notifyDataSetChanged();
                listener.onSizeSelected(v);
            });
        }

        @Override
        public int getItemCount() { return variants.size(); }

        static class ViewHolder extends RecyclerView.ViewHolder {
            TextView tvLabel; com.google.android.material.card.MaterialCardView card;
            ViewHolder(View v) { super(v); tvLabel = v.findViewById(R.id.tvSizeLabel); card = (com.google.android.material.card.MaterialCardView) v.findViewById(R.id.cardContainer); }
        }
    }


    @Override
    public void onCartChanged() {
        cartItems = CartManager.getInstance(this).getItems();
        String query = etSearchCart != null ? etSearchCart.getText().toString() : "";
        performSearch(query);
        updateSummary();
        updateSelectAllState();
    }

    private void updateSummary() {
        CartManager manager = CartManager.getInstance(this);
        boolean isEmpty = manager.getItems().isEmpty();
        int selectedCount = manager.getSelectedCount();

        if (layoutBottomContainer != null) {
            layoutBottomContainer.setVisibility(isEmpty ? View.GONE : View.VISIBLE);
        }
        if (layoutSelectAll != null) {
            layoutSelectAll.setVisibility(isEmpty ? View.GONE : View.VISIBLE);
        }
        
        if (btnContinue != null) {
            btnContinue.setEnabled(selectedCount > 0);
            if (selectedCount > 0) {
                btnContinue.setText(getString(R.string.btn_continue_count, selectedCount));
            } else {
                btnContinue.setText(getString(R.string.btn_continue));
            }
        }
        
        if (layoutEmpty != null) layoutEmpty.setVisibility(isEmpty ? View.VISIBLE : View.GONE);

        if (!isEmpty) {
            double subTotal = manager.getSubTotal();
            if (tvSubTotal != null)    tvSubTotal.setText(getUSDString(subTotal));
            if (tvShipping != null)    tvShipping.setText(subTotal > 0 ? getString(R.string.free) : getUSDString(0));
            if (tvOrderTotal != null)  tvOrderTotal.setText(getUSDString(subTotal));
        }
    }

    private String getUSDString(double amount) {
        return String.format(Locale.US, "$%.2f", amount);
    }

    private void onContinue() {
        List<CartItem> selectedItems = new ArrayList<>();
        for (CartItem item : cartItems) {
            if (item.isSelected()) selectedItems.add(item);
        }

        if (!selectedItems.isEmpty()) {
            Intent intent = new Intent(this, CheckoutActivity.class);
            intent.putExtra("SELECTED_CART_ITEMS", (java.io.Serializable) selectedItems);
            startActivity(intent);
        }
    }
}
