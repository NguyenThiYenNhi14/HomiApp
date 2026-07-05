package com.yn.homi.ui.shop;

import android.graphics.Paint;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import com.yn.homi.core.BaseActivity;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager2.widget.ViewPager2;

import com.yn.homi.R;
import com.google.android.material.tabs.TabLayout;
import com.yn.homi.data.model.Product;
import com.yn.homi.models.Review;
import com.yn.homi.ui.cart.CartActivity;
import com.yn.homi.ui.cart.CartManager;
import com.yn.homi.utils.FavoritesManager;
import com.yn.homi.utils.RecentlyViewedManager;
import com.yn.homi.data.repository.FirestoreRepository;
import com.yn.homi.data.repository.FirestoreRepository.OnProductLoadedListener;
import com.yn.homi.data.repository.FirestoreRepository.OnProductsLoadedListener;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class ProductDetailActivity extends BaseActivity {

    private ViewPager2 vpProductImages;
    private TabLayout tabIndicator;
    private TextView tvName, tvPrice, tvOriginalPrice, tvDescription, tvQuantity, tvRatingCount, tvSelectedColor, tvImageCount, tvCartBadge;
    private RatingBar rbRating;
    private ImageButton btnBack, btnFavoriteHeader, btnCart, btnIncrease, btnDecrease, btnMore, fabFavorite;
    private View btnAddToCart;
    private View llReviewsEmpty, llReviewsLoaded;
    private TextView tvAvgRatingText;
    private RatingBar rbAvgRating;
    private ProgressBar pbLoading;

    private RecyclerView rvRecommendations, rvColors, rvReviews, rvThumbnails;
    private ProductAdapter recommendationAdapter;
    private com.yn.homi.ui.home.ReviewAdapter reviewAdapter;
    private ProductImageAdapter imageAdapter;
    private ThumbnailAdapter thumbnailAdapter;

    private FirestoreRepository firestoreRepository;
    private CartManager cartManager;
    private FavoritesManager favoritesManager;
    private RecentlyViewedManager recentlyViewedManager;
    private String productId;
    private Product product;
    private int quantity = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
        getWindow().setStatusBarColor(android.graphics.Color.WHITE);
        setContentView(R.layout.activity_product_detail);

        productId = getIntent().getStringExtra("productId");
        if (productId != null) productId = productId.trim();

        firestoreRepository = new FirestoreRepository();
        cartManager = CartManager.getInstance(this);
        favoritesManager = new FavoritesManager(this);
        recentlyViewedManager = new RecentlyViewedManager(this);

        initViews();
        loadProductDetails();

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            FirebaseFirestore.getInstance()
                    .collection("users").document(user.getUid())
                    .update("stats.points", com.google.firebase.firestore.FieldValue.increment(1),
                            "stats.views", com.google.firebase.firestore.FieldValue.increment(1))
                    .addOnSuccessListener(aVoid -> {
                        Toast.makeText(this, getString(R.string.msg_earn_points, 1), Toast.LENGTH_SHORT).show();
                    });
        }
    }

    private void initViews() {
        vpProductImages = findViewById(R.id.vp_product_images);
        tabIndicator = findViewById(R.id.tab_indicator);
        tvName = findViewById(R.id.tv_product_name);
        tvPrice = findViewById(R.id.tv_product_price);
        tvOriginalPrice = findViewById(R.id.tv_original_price);
        rbRating = findViewById(R.id.rb_product_rating);
        tvRatingCount = findViewById(R.id.tv_rating_count);
        tvDescription = findViewById(R.id.tv_description);
        tvQuantity = findViewById(R.id.tv_quantity);
        tvSelectedColor = findViewById(R.id.tv_selected_color);
        tvImageCount = findViewById(R.id.tv_image_count);
        rvRecommendations = findViewById(R.id.rv_recommendations);
        rvColors = findViewById(R.id.rv_colors);
        rvReviews = findViewById(R.id.rv_reviews);
        rvThumbnails = findViewById(R.id.rv_thumbnails);
        btnBack = findViewById(R.id.btn_back);
        btnMore = findViewById(R.id.btn_more);
        btnFavoriteHeader = findViewById(R.id.btn_favorite_header);
        fabFavorite = findViewById(R.id.fab_favorite);
        btnCart = findViewById(R.id.btn_cart_header);
        btnIncrease = findViewById(R.id.btn_increase);
        btnDecrease = findViewById(R.id.btn_decrease);
        btnAddToCart = findViewById(R.id.btn_add_to_cart);
        tvCartBadge = findViewById(R.id.tv_cart_badge);
        llReviewsEmpty = findViewById(R.id.ll_reviews_empty);
        pbLoading = findViewById(R.id.pb_loading);
        llReviewsLoaded = findViewById(R.id.ll_reviews_loaded);
        tvAvgRatingText = findViewById(R.id.tv_avg_rating_text);
        rbAvgRating = findViewById(R.id.rb_avg_rating);

        btnBack.setOnClickListener(v -> finish());
        btnIncrease.setOnClickListener(v -> { quantity++; updateQuantityText(); });
        btnDecrease.setOnClickListener(v -> { if (quantity > 1) { quantity--; updateQuantityText(); } });

        btnAddToCart.setOnClickListener(v -> addToCart());
        btnCart.setOnClickListener(v -> startActivity(new android.content.Intent(this, CartActivity.class)));
        fabFavorite.setOnClickListener(v -> showWishlistSelectionDialog());
        btnMore.setOnClickListener(this::showMoreMenu);
        
        btnFavoriteHeader.setOnClickListener(v -> {
            startActivity(new android.content.Intent(this, com.yn.homi.ui.profile.wishlist.WishlistActivity.class));
        });

        findViewById(R.id.btn_buy_now).setOnClickListener(v -> buyNow());
    }

    private void loadProductDetails() {
        if (productId == null) return;
        if (pbLoading != null) pbLoading.setVisibility(View.VISIBLE);

        firestoreRepository.getProductById(productId, new OnProductLoadedListener() {
            @Override
            public void onLoaded(Product p) {
                if (pbLoading != null) pbLoading.setVisibility(View.GONE);
                product = p;
                if (product != null) {
                    recentlyViewedManager.addProduct(product);
                    displayProductDetails();
                    setupRecommendations();
                    setupReviews();
                    updateFavoriteIcon();
                }
            }
            @Override
            public void onError(Exception e) {
                if (pbLoading != null) pbLoading.setVisibility(View.GONE);
                Log.e("ProductDetail", "Error loading product", e);
            }
        });
    }

    private void displayProductDetails() {
        if (product == null) return;
        tvName.setText(product.getName());
        tvPrice.setText(String.format(Locale.US, "$%.2f", product.getPrice()));
        
        if (product.getOriginalPrice() > product.getPrice()) {
            tvOriginalPrice.setText(String.format(Locale.US, "$%.2f", product.getOriginalPrice()));
            tvOriginalPrice.setPaintFlags(tvOriginalPrice.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
            tvOriginalPrice.setVisibility(View.VISIBLE);
        } else {
            tvOriginalPrice.setVisibility(View.GONE);
        }

        tvDescription.setText(product.getDescription());
        rbRating.setRating((float) product.getRating());
        
        tvRatingCount.setText(getString(R.string.reviews_count_format, 
                product.getReviewCount(), 
                getString(R.string.label_reviews_lowercase)));

        // Thu thập tất cả ảnh bao gồm ảnh chính và ảnh biến thể màu sắc
        List<String> allImages = new ArrayList<>();
        if (product.getImageUrls() != null && !product.getImageUrls().isEmpty()) {
            allImages.addAll(product.getImageUrls());
        } else if (product.getThumbnailUrl() != null && !product.getThumbnailUrl().isEmpty()) {
            allImages.add(product.getThumbnailUrl());
        }

        if (product.getColorVariants() != null) {
            for (Product.ColorVariant variant : product.getColorVariants()) {
                String variantUrl = variant.getImageUrl();
                if (variantUrl != null && !variantUrl.isEmpty() && !allImages.contains(variantUrl)) {
                    allImages.add(variantUrl);
                }
            }
        }

        setupImageSlider(allImages);
        setupColorVariants();
    }

    private void setupReviews() {
        reviewAdapter = new com.yn.homi.ui.home.ReviewAdapter(new ArrayList<>());
        rvReviews.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        rvReviews.setAdapter(reviewAdapter);

        // Update: Allow users to click on a review to see it in full screen or zoom the image
        // (Optional enhancement, for now just ensure it's vertical if there are many reviews or keep it horizontal)

        firestoreRepository.getReviewsByProductId(productId, new FirestoreRepository.OnReviewsLoadedListener() {
            @Override
            public void onLoaded(List<Review> reviews) {
                if (reviews != null && !reviews.isEmpty()) {
                    llReviewsEmpty.setVisibility(View.GONE);
                    llReviewsLoaded.setVisibility(View.VISIBLE);
                    reviewAdapter.updateData(reviews);
                    if (tvAvgRatingText != null) tvAvgRatingText.setText(String.format(Locale.getDefault(), "%.1f", product.getRating()));
                    if (rbAvgRating != null) rbAvgRating.setRating((float) product.getRating());
                } else {
                    llReviewsEmpty.setVisibility(View.VISIBLE);
                    llReviewsLoaded.setVisibility(View.GONE);
                }
            }
            @Override
            public void onError(Exception e) { Log.e("ProductDetail", "Error loading reviews", e); }
        });
    }

    private void setupColorVariants() {
        if (product.getColorVariants() != null && !product.getColorVariants().isEmpty()) {
            findViewById(R.id.ll_color_section).setVisibility(View.VISIBLE);
            if (tvSelectedColor != null) tvSelectedColor.setText(product.getColorVariants().get(0).getName());

            com.yn.homi.ui.home.ColorVariantAdapter colorAdapter = new com.yn.homi.ui.home.ColorVariantAdapter(
                    product.getColorVariants(),
                    variant -> {
                        if (tvSelectedColor != null) tvSelectedColor.setText(variant.getName());
                        if (variant.getImageUrl() != null && !variant.getImageUrl().isEmpty()) {
                            List<String> currentImages = imageAdapter.images;
                            int index = currentImages.indexOf(variant.getImageUrl());
                            if (index != -1) {
                                vpProductImages.setCurrentItem(index, true);
                            }
                        }
                    });
            rvColors.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
            rvColors.setAdapter(colorAdapter);

            // Chuyển slider đến ảnh của biến thể đầu tiên nếu có
            String firstVariantImg = product.getColorVariants().get(0).getImageUrl();
            if (firstVariantImg != null && !firstVariantImg.isEmpty()) {
                int index = imageAdapter.images.indexOf(firstVariantImg);
                if (index != -1) vpProductImages.setCurrentItem(index, false);
            }
        } else {
            if (findViewById(R.id.ll_color_section) != null) findViewById(R.id.ll_color_section).setVisibility(View.GONE);
        }
    }

    private void showWishlistSelectionDialog() {
        List<com.yn.homi.data.model.Wishlist> wishlists = favoritesManager.getWishlists();
        if (wishlists.isEmpty()) { showCreateWishlistDialog(null); return; }

        View dialogView = getLayoutInflater().inflate(R.layout.dialog_select_wishlist, null);
        com.google.android.material.bottomsheet.BottomSheetDialog dialog = new com.google.android.material.bottomsheet.BottomSheetDialog(this);
        dialog.setContentView(dialogView);

        RecyclerView rvWishlists = dialogView.findViewById(R.id.rv_wishlists);
        rvWishlists.setLayoutManager(new LinearLayoutManager(this));
        rvWishlists.setAdapter(new com.yn.homi.ui.home.WishlistSelectionAdapter(wishlists, wishlist -> {
            favoritesManager.addProductToWishlist(wishlist.getName(), product);
            updateFavoriteIcon();
            dialog.dismiss();
            Toast.makeText(this, getString(R.string.add_to_wishlist_success, wishlist.getName()), Toast.LENGTH_SHORT).show();
        }));

        dialogView.findViewById(R.id.btn_create_new_list).setOnClickListener(v -> showCreateWishlistDialog(dialog));
        dialog.show();
    }

    private void showCreateWishlistDialog(com.google.android.material.bottomsheet.BottomSheetDialog parent) {
        androidx.appcompat.app.AlertDialog.Builder builder = new androidx.appcompat.app.AlertDialog.Builder(this);
        builder.setTitle(getString(R.string.new_wishlist_title));
        final android.widget.EditText input = new android.widget.EditText(this);
        input.setHint(getString(R.string.hint_enter_list_name));
        builder.setView(input);
        builder.setPositiveButton(getString(R.string.create), (d, w) -> {
            String name = input.getText().toString().trim();
            if (!name.isEmpty()) {
                favoritesManager.createWishlistAndAddProduct(name, product);
                updateFavoriteIcon();
                if (parent != null) parent.dismiss();
            }
        });
        builder.setNegativeButton(getString(R.string.cancel), null);
        builder.show();
    }

    private void showMoreMenu(View v) {
        androidx.appcompat.widget.PopupMenu popup = new androidx.appcompat.widget.PopupMenu(this, v);
        popup.getMenuInflater().inflate(R.menu.menu_product_detail_more, popup.getMenu());
        popup.setOnMenuItemClickListener(item -> {
            if (item.getItemId() == R.id.action_share) { shareProduct(); return true; }
            return false;
        });
        popup.show();
    }

    private void shareProduct() {
        if (product == null) return;
        android.content.Intent intent = new android.content.Intent(android.content.Intent.ACTION_SEND);
        intent.setType("text/plain");
        String shareMsg = getString(R.string.share_product_msg, product.getName());
        intent.putExtra(android.content.Intent.EXTRA_TEXT, shareMsg);
        startActivity(android.content.Intent.createChooser(intent, getString(R.string.share_via)));
    }

    private void addToCart() {
        if (product == null) return;
        
        String color = null;
        String imageUrl = product.getThumbnailUrl();
        
        if (product.getColorVariants() != null && !product.getColorVariants().isEmpty()) {
            com.yn.homi.data.model.Product.ColorVariant firstVariant = product.getColorVariants().get(0);
            color = firstVariant.getName();
            if (firstVariant.getImageUrl() != null && !firstVariant.getImageUrl().isEmpty()) {
                imageUrl = firstVariant.getImageUrl();
            }
        }
        
        // If user manually selected a color, use that instead
        if (tvSelectedColor != null && !tvSelectedColor.getText().toString().isEmpty()) {
            String selected = tvSelectedColor.getText().toString();
            color = selected;
            // Find corresponding image for selected color
            if (product.getColorVariants() != null) {
                for (com.yn.homi.data.model.Product.ColorVariant v : product.getColorVariants()) {
                    if (v.getName().equals(selected) && v.getImageUrl() != null && !v.getImageUrl().isEmpty()) {
                        imageUrl = v.getImageUrl();
                        break;
                    }
                }
            }
        }

        cartManager.addItem(new com.yn.homi.data.model.CartItem(product.getId(), product.getName(), product.getPrice(), quantity, imageUrl, color, null));
        updateCartBadge();
        Toast.makeText(this, getString(R.string.add_to_cart_success), Toast.LENGTH_SHORT).show();
    }

    private void buyNow() {
        if (product == null) return;
        
        String color = null;
        String imageUrl = product.getThumbnailUrl();
        
        if (product.getColorVariants() != null && !product.getColorVariants().isEmpty()) {
            com.yn.homi.data.model.Product.ColorVariant firstVariant = product.getColorVariants().get(0);
            color = firstVariant.getName();
            if (firstVariant.getImageUrl() != null && !firstVariant.getImageUrl().isEmpty()) {
                imageUrl = firstVariant.getImageUrl();
            }
        }
        
        if (tvSelectedColor != null && !tvSelectedColor.getText().toString().isEmpty()) {
            String selected = tvSelectedColor.getText().toString();
            color = selected;
            if (product.getColorVariants() != null) {
                for (com.yn.homi.data.model.Product.ColorVariant v : product.getColorVariants()) {
                    if (v.getName().equals(selected) && v.getImageUrl() != null && !v.getImageUrl().isEmpty()) {
                        imageUrl = v.getImageUrl();
                        break;
                    }
                }
            }
        }

        ArrayList<com.yn.homi.data.model.CartItem> items = new ArrayList<>();
        items.add(new com.yn.homi.data.model.CartItem(product.getId(), product.getName(), product.getPrice(), quantity, imageUrl, color, null));
        android.content.Intent intent = new android.content.Intent(this, com.yn.homi.ui.checkout.CheckoutActivity.class);
        intent.putExtra("SELECTED_CART_ITEMS", items);
        startActivity(intent);
    }

    private void setupRecommendations() {
        if (product.getTags() == null) return;
        recommendationAdapter = new ProductAdapter(new ArrayList<>(), true);
        rvRecommendations.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        rvRecommendations.setAdapter(recommendationAdapter);
        firestoreRepository.getProductsByMultipleTags(product.getTags(), new OnProductsLoadedListener() {
            @Override
            public void onLoaded(List<Product> products) {
                List<Product> filtered = new ArrayList<>();
                for (Product p : products) if (!p.getId().equals(productId)) filtered.add(p);
                recommendationAdapter.updateData(filtered);
            }
            @Override public void onError(Exception e) {}
        });
    }

    private void setupImageSlider(List<String> images) {
        if (images == null || images.isEmpty()) { images = new ArrayList<>(); images.add(product.getThumbnailUrl()); }
        imageAdapter = new ProductImageAdapter(images);
        vpProductImages.setAdapter(imageAdapter);
        thumbnailAdapter = new ThumbnailAdapter(images, pos -> vpProductImages.setCurrentItem(pos, true));
        rvThumbnails.setAdapter(thumbnailAdapter);
        vpProductImages.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override public void onPageSelected(int p) {
                tvImageCount.setText(String.format(Locale.getDefault(), "%d/%d", p + 1, imageAdapter.getItemCount()));
                thumbnailAdapter.setSelectedPosition(p);
            }
        });
    }

    private void updateFavoriteIcon() {
        if (product != null) {
            boolean isFav = favoritesManager.isFavorite(product.getId());
            fabFavorite.setImageResource(isFav ? R.drawable.ic_heart_filled : R.drawable.ic_heart);
        }
    }

    private void updateCartBadge() {
        int count = cartManager.getTotalItemCount();
        if (tvCartBadge != null) {
            tvCartBadge.setText(String.valueOf(count));
            tvCartBadge.setVisibility(count > 0 ? View.VISIBLE : View.GONE);
        }
    }

    private void updateQuantityText() { tvQuantity.setText(String.valueOf(quantity)); }

    @Override
    protected void onResume() { super.onResume(); updateCartBadge(); }

    private static class ProductImageAdapter extends RecyclerView.Adapter<ProductImageAdapter.ViewHolder> {
        List<String> images;
        ProductImageAdapter(List<String> images) { this.images = images; }
        @Override public ViewHolder onCreateViewHolder(android.view.ViewGroup p, int vt) {
            android.widget.ImageView iv = new android.widget.ImageView(p.getContext());
            iv.setLayoutParams(new android.view.ViewGroup.LayoutParams(-1, -1));
            iv.setScaleType(android.widget.ImageView.ScaleType.CENTER_CROP);
            return new ViewHolder(iv);
        }
        @Override public void onBindViewHolder(ViewHolder h, int pos) {
            com.bumptech.glide.Glide.with(h.itemView).load(images.get(pos)).into((android.widget.ImageView)h.itemView);
        }
        @Override public int getItemCount() { return images.size(); }
        static class ViewHolder extends RecyclerView.ViewHolder { ViewHolder(View v) { super(v); } }
    }

    private static class ThumbnailAdapter extends RecyclerView.Adapter<ThumbnailAdapter.ViewHolder> {
        private List<String> images;
        private int selectedPos = 0;
        private OnItemClickListener listener;
        interface OnItemClickListener { void onItemClick(int pos); }
        ThumbnailAdapter(List<String> images, OnItemClickListener l) { this.images = images; this.listener = l; }
        void setSelectedPosition(int p) { int old = selectedPos; selectedPos = p; notifyItemChanged(old); notifyItemChanged(selectedPos); }
        @Override public ViewHolder onCreateViewHolder(android.view.ViewGroup p, int vt) {
            View v = android.view.LayoutInflater.from(p.getContext()).inflate(R.layout.item_product_image_thumbnail, p, false);
            return new ViewHolder(v);
        }
        @Override public void onBindViewHolder(ViewHolder h, int pos) {
            com.bumptech.glide.Glide.with(h.itemView).load(images.get(pos)).into((android.widget.ImageView) h.itemView.findViewById(R.id.iv_thumbnail));
            h.itemView.setSelected(pos == selectedPos);
            h.itemView.setOnClickListener(v -> { if (listener != null) listener.onItemClick(pos); });
        }
        @Override public int getItemCount() { return images.size(); }
        static class ViewHolder extends RecyclerView.ViewHolder { ViewHolder(View v) { super(v); } }
    }
}
