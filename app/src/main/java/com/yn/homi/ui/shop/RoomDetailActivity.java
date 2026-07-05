package com.yn.homi.ui.shop;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.yn.homi.R;
import com.yn.homi.core.BaseActivity;
import com.yn.homi.data.model.CartItem;
import com.yn.homi.data.model.Product;
import com.yn.homi.data.repository.FirestoreRepository;
import com.yn.homi.ui.cart.CartActivity;
import com.yn.homi.ui.cart.CartManager;
import com.yn.homi.ui.shop.ProductAdapter;

import java.util.ArrayList;
import java.util.List;

public class RoomDetailActivity extends BaseActivity {

    private ImageView ivRoomImage;
    private TextView tvRoomTitle, tvRoomDescription, tvTotalCost;
    private RecyclerView rvRoomProducts;
    private ProgressBar pbLoading;
    private View btnBuyCompleteRoom;
    private ProductAdapter productAdapter;
    private FirestoreRepository firestoreRepository;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_room_detail);

        firestoreRepository = new FirestoreRepository();

        initViews();
        setupToolbar();
        setupRecyclerView();
        setupListeners();
        loadData();
    }

    private void initViews() {
        ivRoomImage = findViewById(R.id.ivRoomImage);
        tvRoomTitle = findViewById(R.id.tvRoomTitle);
        tvRoomDescription = findViewById(R.id.tvRoomDescription);
        tvTotalCost = findViewById(R.id.tvTotalCost);
        rvRoomProducts = findViewById(R.id.rvRoomProducts);
        pbLoading = findViewById(R.id.pbLoading);
        btnBuyCompleteRoom = findViewById(R.id.btnBuyCompleteRoom);
    }

    private void setupListeners() {
        btnBuyCompleteRoom.setOnClickListener(v -> {
            List<Product> products = productAdapter.getProducts();
            if (products == null || products.isEmpty()) {
                Toast.makeText(this, "No products to add", Toast.LENGTH_SHORT).show();
                return;
            }

            CartManager cartManager = CartManager.getInstance(this);
            for (Product p : products) {
                cartManager.addItem(new CartItem(p, 1));
            }
            
            Toast.makeText(this, "Added all items to cart", Toast.LENGTH_SHORT).show();
            
            // Optionally open the cart
            Intent intent = new Intent(this, CartActivity.class);
            startActivity(intent);
        });
    }

    private void setupToolbar() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowTitleEnabled(false);
        }
        toolbar.setNavigationOnClickListener(v -> onBackPressed());
    }

    private void setupRecyclerView() {
        productAdapter = new ProductAdapter(new ArrayList<>());
        rvRoomProducts.setLayoutManager(new GridLayoutManager(this, 2));
        rvRoomProducts.setAdapter(productAdapter);
    }

    private void loadData() {
        String ideaId = getIntent().getStringExtra("IDEA_ID");
        String title = getIntent().getStringExtra("IDEA_TITLE");
        String image = getIntent().getStringExtra("IDEA_IMAGE");
        String desc = getIntent().getStringExtra("IDEA_DESC");

        if (title != null) tvRoomTitle.setText(title);
        if (desc != null) tvRoomDescription.setText(desc);
        
        Glide.with(this)
                .load(image)
                .centerCrop()
                .into(ivRoomImage);

        if (ideaId != null) {
            pbLoading.setVisibility(View.VISIBLE);
            firestoreRepository.getIdeaById(ideaId, new FirestoreRepository.OnIdeaLoadedListener() {
                @Override
                public void onLoaded(com.yn.homi.data.model.Idea idea) {
                    // Ưu tiên 1: Lấy đúng sản phẩm được chỉ định
                    if (idea.getProductIds() != null && !idea.getProductIds().isEmpty()) {
                        fetchProductsByIds(idea.getProductIds());
                    } 
                    // Ưu tiên 2: Quét sản phẩm tương thích theo Tags của Idea
                    else if (idea.getTags() != null && !idea.getTags().isEmpty()) {
                        fetchRoomProductsByMultipleTags(idea.getTags());
                    }
                    // Ưu tiên 3: Fallback tìm theo ID
                    else {
                        fetchRoomProductsByTag(ideaId);
                    }
                }

                @Override
                public void onError(Exception e) {
                    fetchRoomProductsByTag(ideaId);
                }
            });
        }
    }

    private void fetchRoomProductsByMultipleTags(List<String> tags) {
        firestoreRepository.getProductsByMultipleTags(tags, new FirestoreRepository.OnProductsLoadedListener() {
            @Override
            public void onLoaded(List<Product> products) {
                updateUI(products);
            }

            @Override
            public void onError(Exception e) {
                pbLoading.setVisibility(View.GONE);
                Toast.makeText(RoomDetailActivity.this, "Cannot scan compatible items", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void fetchProductsByIds(List<String> productIds) {
        List<Product> products = new ArrayList<>();
        final int[] remaining = {productIds.size()};

        for (String id : productIds) {
            firestoreRepository.getProductById(id, new FirestoreRepository.OnProductLoadedListener() {
                @Override
                public void onLoaded(Product product) {
                    if (product != null) products.add(product);
                    checkFinished();
                }

                @Override
                public void onError(Exception e) {
                    checkFinished();
                }

                private void checkFinished() {
                    remaining[0]--;
                    if (remaining[0] == 0) {
                        runOnUiThread(() -> updateUI(products));
                    }
                }
            });
        }
    }

    private void fetchRoomProductsByTag(String tag) {
        firestoreRepository.getProductsByTag(tag, new FirestoreRepository.OnProductsLoadedListener() {
            @Override
            public void onLoaded(List<Product> products) {
                updateUI(products);
            }

            @Override
            public void onError(Exception e) {
                pbLoading.setVisibility(View.GONE);
                Toast.makeText(RoomDetailActivity.this, "Error loading products", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void updateUI(List<Product> products) {
        pbLoading.setVisibility(View.GONE);
        productAdapter.updateData(products);
        calculateTotalCost(products);
    }

    private void calculateTotalCost(List<Product> products) {
        double total = 0;
        for (Product p : products) {
            total += p.getPrice();
        }
        tvTotalCost.setText(String.format("$%,.2f", total));
    }
}