package com.yn.homi;

import android.graphics.Paint;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager2.widget.ViewPager2;

import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;
import com.yn.homi.adapters.ProductAdapter;
import com.yn.homi.models.Product;
import com.yn.homi.utils.CartManager;
import com.yn.homi.utils.FavoritesManager;
import com.yn.homi.utils.FirestoreRepository;
import com.yn.homi.utils.FirestoreRepository.OnProductLoadedListener;
import com.yn.homi.utils.FirestoreRepository.OnProductsLoadedListener;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class ProductDetailActivity extends AppCompatActivity {

    private ViewPager2 vpProductImages;
    private TabLayout tabIndicator;
    private TextView tvName, tvPrice, tvOriginalPrice, tvDescription, tvQuantity, tvRatingCount, tvSelectedColor, tvImageCount;
    private RatingBar rbRating;
    private ImageButton btnBack, btnFavorite, btnCart, btnIncrease, btnDecrease, btnMore;
    private View btnAddToCart;
    private View llReviewsEmpty, llReviewsLoaded;
    private TextView tvAvgRatingText;
    private RatingBar rbAvgRating;

    private RecyclerView rvRecommendations, rvColors, rvReviews, rvThumbnails;
    private ProductAdapter recommendationAdapter;
    private ProductImageAdapter imageAdapter;
    private ThumbnailAdapter thumbnailAdapter;

    private FirestoreRepository firestoreRepository;
    private CartManager cartManager;
    private FavoritesManager favoritesManager;
    private String productId;
    private Product product;
    private int quantity = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        // Set white status bar with dark icons
        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
        getWindow().setStatusBarColor(android.graphics.Color.WHITE);

        setContentView(R.layout.activity_product_detail);

        productId = getIntent().getStringExtra("productId");
        firestoreRepository = new FirestoreRepository();
        cartManager = new CartManager(this);
        favoritesManager = new FavoritesManager(this);

        initViews();
        loadProductDetails();
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
        btnFavorite = findViewById(R.id.btn_favorite_header);
        btnCart = findViewById(R.id.btn_cart_header);
        btnIncrease = findViewById(R.id.btn_increase);
        btnDecrease = findViewById(R.id.btn_decrease);
        btnAddToCart = findViewById(R.id.btn_add_to_cart);
        llReviewsEmpty = findViewById(R.id.ll_reviews_empty);
        llReviewsLoaded = findViewById(R.id.ll_reviews_loaded);
        tvAvgRatingText = findViewById(R.id.tv_avg_rating_text);
        rbAvgRating = findViewById(R.id.rb_avg_rating);

        btnBack.setOnClickListener(v -> finish());

        btnIncrease.setOnClickListener(v -> {
            quantity++;
            updateQuantityText();
        });

        btnDecrease.setOnClickListener(v -> {
            if (quantity > 1) {
                quantity--;
                updateQuantityText();
            }
        });

        btnAddToCart.setOnClickListener(v -> {
            if (product != null) {
                cartManager.addToCart(product, quantity);
                Toast.makeText(this, "Added " + quantity + " items to cart", Toast.LENGTH_SHORT).show();
            }
        });

        btnFavorite.setOnClickListener(v -> {
            if (product != null) {
                favoritesManager.toggleFavorite(product);
                updateFavoriteIcon();
            }
        });

        btnCart.setOnClickListener(v -> {
            android.content.Intent intent = new android.content.Intent(this, CartActivity.class);
            startActivity(intent);
        });

        btnMore.setOnClickListener(this::showMoreMenu);
    }

    private void showMoreMenu(View v) {
        androidx.appcompat.widget.PopupMenu popup = new androidx.appcompat.widget.PopupMenu(this, v);
        popup.getMenuInflater().inflate(R.menu.menu_product_detail_more, popup.getMenu());
        popup.setOnMenuItemClickListener(item -> {
            int id = item.getItemId();
            if (id == R.id.action_home) {
                finish(); // Assuming going back to home or specifically navigation
                return true;
            } else if (id == R.id.action_share) {
                shareProduct();
                return true;
            }
            return false;
        });
        popup.show();
    }

    private void shareProduct() {
        if (product == null) return;
        android.content.Intent sendIntent = new android.content.Intent();
        sendIntent.setAction(android.content.Intent.ACTION_SEND);
        sendIntent.putExtra(android.content.Intent.EXTRA_TEXT, "Check out this " + product.getName() + " on Homi!");
        sendIntent.setType("text/plain");
        android.content.Intent shareIntent = android.content.Intent.createChooser(sendIntent, null);
        startActivity(shareIntent);
    }

    private void setupRecommendations() {
        if (product == null || product.getTags() == null || product.getTags().isEmpty()) return;

        recommendationAdapter = new ProductAdapter(new ArrayList<>(), true);
        rvRecommendations.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        rvRecommendations.setAdapter(recommendationAdapter);

        firestoreRepository.getProductsByMultipleTags(product.getTags(), new OnProductsLoadedListener() {
            @Override
            public void onLoaded(List<Product> products) {
                List<Product> filtered = new ArrayList<>();
                for (Product p : products) {
                    if (p.getId() != null && !p.getId().equals(productId)) {
                        filtered.add(p);
                    }
                }
                recommendationAdapter.updateData(filtered);
            }

            @Override
            public void onError(Exception e) {
            }
        });
    }

    private void updateFavoriteIcon() {
        if (product != null && product.getId() != null && favoritesManager.isFavorite(product.getId())) {
            btnFavorite.setImageResource(R.drawable.ic_heart_filled);
        } else {
            btnFavorite.setImageResource(R.drawable.ic_heart);
        }
    }

    private void updateQuantityText() {
        tvQuantity.setText(String.valueOf(quantity));
    }

    private void loadProductDetails() {
        if (productId == null) return;

        firestoreRepository.getProductById(productId, new OnProductLoadedListener() {
            @Override
            public void onLoaded(Product p) {
                product = p;
                displayProductInfo();
                updateFavoriteIcon();
                setupRecommendations();
            }

            @Override
            public void onError(Exception e) {
                Toast.makeText(ProductDetailActivity.this, "Error loading product", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void displayProductInfo() {
        if (product == null) return;

        tvName.setText(product.getName());
        tvPrice.setText(String.format(Locale.getDefault(), "$%.2f", product.getPrice()));

        boolean hasSaleEffect = product.getDiscountPercent() > 0 || product.getOriginalPrice() > product.getPrice() ||
                product.isOnSale() || (product.getTags() != null && (product.getTags().contains("flash_sale") || product.getTags().contains("sale")));

        if (hasSaleEffect) {
            tvOriginalPrice.setVisibility(View.VISIBLE);
            double displayOriginalPrice = product.getOriginalPrice() > 0 ? product.getOriginalPrice() : product.getPrice() * 1.2;
            tvOriginalPrice.setText(String.format(Locale.getDefault(), "$%.2f", displayOriginalPrice));
            tvOriginalPrice.setPaintFlags(tvOriginalPrice.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
        } else {
            tvOriginalPrice.setVisibility(View.GONE);
        }

        rbRating.setRating(product.getRating());
        tvRatingCount.setText(String.format(Locale.getDefault(), "(%d)", product.getReviewCount()));
        tvDescription.setText(product.getDescription());

        setupColorVariants();
        setupReviews();

        List<String> images = product.getImageUrls();
        if (images == null || images.isEmpty()) {
            images = new ArrayList<>();
            images.add(product.getThumbnailUrl());
        }

        setupImageSlider(images);
    }

    private void setupColorVariants() {
        if (product.getColorVariants() != null && !product.getColorVariants().isEmpty()) {
            findViewById(R.id.ll_color_section).setVisibility(View.VISIBLE);
            
            if (tvSelectedColor != null) {
                tvSelectedColor.setText(product.getColorVariants().get(0).getName());
            }

            com.yn.homi.adapters.ColorVariantAdapter colorAdapter = new com.yn.homi.adapters.ColorVariantAdapter(
                    product.getColorVariants(),
                    variant -> {
                        if (tvSelectedColor != null) {
                            tvSelectedColor.setText(variant.getName());
                        }
                        if (variant.getImageUrl() != null && !variant.getImageUrl().isEmpty()) {
                            List<String> currentImages = new ArrayList<>();
                            if (product.getImageUrls() != null && !product.getImageUrls().isEmpty()) {
                                currentImages.addAll(product.getImageUrls());
                            } else if (product.getThumbnailUrl() != null) {
                                currentImages.add(product.getThumbnailUrl());
                            }

                            int index = currentImages.indexOf(variant.getImageUrl());
                            if (index != -1) {
                                vpProductImages.setCurrentItem(index, true);
                            } else {
                                currentImages.add(0, variant.getImageUrl());
                                if (imageAdapter != null) {
                                    imageAdapter.updateImages(currentImages);
                                    vpProductImages.setCurrentItem(0, true);
                                }
                            }
                        }
                    });
            rvColors.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
            rvColors.setAdapter(colorAdapter);
        } else {
            findViewById(R.id.ll_color_section).setVisibility(View.GONE);
        }
    }

    private void setupReviews() {
        if (product.getReviewsSample() != null && !product.getReviewsSample().isEmpty()) {
            llReviewsEmpty.setVisibility(View.GONE);
            llReviewsLoaded.setVisibility(View.VISIBLE);

            if (tvAvgRatingText != null) {
                tvAvgRatingText.setText(String.format(Locale.getDefault(), "%.1f", product.getRating()));
            }
            if (rbAvgRating != null) {
                rbAvgRating.setRating(product.getRating());
            }

            com.yn.homi.adapters.ReviewAdapter reviewAdapter = new com.yn.homi.adapters.ReviewAdapter(product.getReviewsSample());
            rvReviews.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
            rvReviews.setAdapter(reviewAdapter);
        } else {
            llReviewsEmpty.setVisibility(View.VISIBLE);
            llReviewsLoaded.setVisibility(View.GONE);
        }
    }

    private void setupImageSlider(List<String> images) {
        imageAdapter = new ProductImageAdapter(images);
        vpProductImages.setAdapter(imageAdapter);
        
        thumbnailAdapter = new ThumbnailAdapter(images, position -> {
            vpProductImages.setCurrentItem(position, true);
        });
        rvThumbnails.setAdapter(thumbnailAdapter);

        vpProductImages.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                super.onPageSelected(position);
                tvImageCount.setText(String.format(Locale.getDefault(), "%d/%d", position + 1, images.size()));
                thumbnailAdapter.setSelectedPosition(position);
                rvThumbnails.smoothScrollToPosition(position);
            }
        });
        
        tvImageCount.setText(String.format(Locale.getDefault(), "1/%d", images.size()));
    }

    private static class ThumbnailAdapter extends RecyclerView.Adapter<ThumbnailAdapter.ViewHolder> {
        private List<String> images;
        private int selectedPosition = 0;
        private OnItemClickListener listener;

        interface OnItemClickListener {
            void onItemClick(int position);
        }

        ThumbnailAdapter(List<String> images, OnItemClickListener listener) {
            this.images = images;
            this.listener = listener;
        }

        void setSelectedPosition(int position) {
            int oldPos = selectedPosition;
            selectedPosition = position;
            notifyItemChanged(oldPos);
            notifyItemChanged(selectedPosition);
        }

        @androidx.annotation.NonNull
        @Override
        public ViewHolder onCreateViewHolder(@androidx.annotation.NonNull android.view.ViewGroup parent, int viewType) {
            View v = android.view.LayoutInflater.from(parent.getContext()).inflate(R.layout.item_product_image_thumbnail, parent, false);
            return new ViewHolder(v);
        }

        @Override
        public void onBindViewHolder(@androidx.annotation.NonNull ViewHolder holder, int position) {
            com.bumptech.glide.Glide.with(holder.itemView.getContext())
                    .load(images.get(position))
                    .into(holder.ivThumbnail);
            
            holder.vSelectionBorder.setVisibility(position == selectedPosition ? View.VISIBLE : View.GONE);
            holder.itemView.setOnClickListener(v -> listener.onItemClick(position));
        }

        @Override
        public int getItemCount() { return images.size(); }

        static class ViewHolder extends RecyclerView.ViewHolder {
            android.widget.ImageView ivThumbnail;
            View vSelectionBorder;
            ViewHolder(View v) {
                super(v);
                ivThumbnail = v.findViewById(R.id.iv_thumbnail);
                vSelectionBorder = v.findViewById(R.id.v_selection_border);
            }
        }
    }

    private static class ProductImageAdapter extends androidx.recyclerview.widget.RecyclerView.Adapter<ProductImageAdapter.ViewHolder> {
        private List<String> images;

        ProductImageAdapter(List<String> images) { this.images = images; }

        public void updateImages(List<String> newImages) {
            this.images = newImages;
            notifyDataSetChanged();
        }

        @androidx.annotation.NonNull
        @Override
        public ViewHolder onCreateViewHolder(@androidx.annotation.NonNull android.view.ViewGroup parent, int viewType) {
            android.widget.ImageView imageView = new android.widget.ImageView(parent.getContext());
            imageView.setLayoutParams(new android.view.ViewGroup.LayoutParams(
                    android.view.ViewGroup.LayoutParams.MATCH_PARENT,
                    android.view.ViewGroup.LayoutParams.MATCH_PARENT));
            imageView.setScaleType(android.widget.ImageView.ScaleType.CENTER_CROP);
            return new ViewHolder(imageView);
        }

        @Override
        public void onBindViewHolder(@androidx.annotation.NonNull ViewHolder holder, int position) {
            com.bumptech.glide.Glide.with(holder.itemView.getContext())
                    .load(images.get(position))
                    .into((android.widget.ImageView) holder.itemView);
        }

        @Override
        public int getItemCount() { return images.size(); }

        static class ViewHolder extends androidx.recyclerview.widget.RecyclerView.ViewHolder {
            ViewHolder(View v) { super(v); }
        }
    }
}
