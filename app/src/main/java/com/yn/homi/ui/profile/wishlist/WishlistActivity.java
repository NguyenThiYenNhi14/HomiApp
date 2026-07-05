package com.yn.homi.ui.profile.wishlist;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageView;
import androidx.annotation.NonNull;
import com.yn.homi.core.BaseActivity;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.yn.homi.R;
import com.yn.homi.ui.profile.preferences.notification.NotificationActivity;

import java.util.ArrayList;
import java.util.List;

import com.yn.homi.data.model.Product;
import com.yn.homi.data.model.Wishlist;
import com.yn.homi.utils.FavoritesManager;
import com.yn.homi.data.repository.FirestoreRepository;
import com.yn.homi.ui.shop.ProductAdapter;

import com.yn.homi.ui.home.HomeActivity;
import com.yn.homi.ui.shop.ShopActivity;
import com.yn.homi.ui.profile.profile.YourProfileActivity;

public class WishlistActivity extends BaseActivity {

    private RecyclerView recyclerWishlist;
    private WishlistGroupAdapter groupAdapter;
    private List<Wishlist> wishlists;
    private FavoritesManager favoritesManager;
    private ImageView btnBack, btnAddList;
    private android.view.View llEmptyState;
    private android.widget.Button btnCreateFirstList;

    private RecyclerView rvRecommendations;
    private ProductAdapter recommendationAdapter;
    private FirestoreRepository firestoreRepository;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_wishlist);

        favoritesManager = new FavoritesManager(this);
        firestoreRepository = new FirestoreRepository();
        recyclerWishlist = findViewById(R.id.recyclerWishlist);
        btnBack = findViewById(R.id.btnBack);
        btnAddList = findViewById(R.id.btn_add_list);
        llEmptyState = findViewById(R.id.ll_empty_state);
        rvRecommendations = findViewById(R.id.rvRecommendations);

        btnBack.setOnClickListener(v -> finish());
        
        btnAddList.setOnClickListener(v -> showCreateWishlistDialog());
        llEmptyState.setOnClickListener(v -> showCreateWishlistDialog());

        // Setup RecyclerView — vertical list of groups
        recyclerWishlist.setLayoutManager(new LinearLayoutManager(this));
        recyclerWishlist.setNestedScrollingEnabled(false);

        // Load data from FavoritesManager
        updateWishlistUI();
        syncFromFirestore();

        setupSwipeToDelete();
        setupRecommendations();
        setupBottomNavigation();
    }

    private void syncFromFirestore() {
        favoritesManager.syncFromFirestore();
        // The sync is async, but we can set up a listener if FavoritesManager supported it.
        // For now, we rely on onResume or manual refresh.
    }

    private void setupSwipeToDelete() {
        androidx.recyclerview.widget.ItemTouchHelper.SimpleCallback swipeCallback = 
            new androidx.recyclerview.widget.ItemTouchHelper.SimpleCallback(0, androidx.recyclerview.widget.ItemTouchHelper.LEFT) {
                @Override
                public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder target) {
                    return false;
                }

                @Override
                public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
                    int position = viewHolder.getAdapterPosition();
                    if (groupAdapter != null) {
                        groupAdapter.deleteItem(position);
                    }
                }

                @Override
                public void onChildDraw(@NonNull android.graphics.Canvas c, @NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, float dX, float dY, int actionState, boolean isCurrentlyActive) {
                    if (actionState == androidx.recyclerview.widget.ItemTouchHelper.ACTION_STATE_SWIPE) {
                        android.view.View itemView = viewHolder.itemView;
                        android.graphics.Paint p = new android.graphics.Paint();
                        
                        if (dX < 0) { // Swiping to the left
                            // Draw dark background
                            p.setColor(android.graphics.Color.parseColor("#333333"));
                            c.drawRect((float) itemView.getRight() + dX, (float) itemView.getTop(), (float) itemView.getRight(), (float) itemView.getBottom(), p);

                            // Draw "Xóa group" text
                            p.setColor(android.graphics.Color.WHITE);
                            p.setTextSize(40);
                            p.setAntiAlias(true);
                            String text = getString(R.string.delete_group);
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
        new androidx.recyclerview.widget.ItemTouchHelper(swipeCallback).attachToRecyclerView(recyclerWishlist);
    }

    public void checkEmptyState() {
        if (wishlists == null || wishlists.isEmpty()) {
            recyclerWishlist.setVisibility(android.view.View.GONE);
            llEmptyState.setVisibility(android.view.View.VISIBLE);
        } else {
            recyclerWishlist.setVisibility(android.view.View.VISIBLE);
            llEmptyState.setVisibility(android.view.View.GONE);
        }
    }

    private void updateWishlistUI() {
        wishlists = favoritesManager.getWishlists();
        checkEmptyState();
        
        if (!wishlists.isEmpty()) {
            if (groupAdapter == null) {
                groupAdapter = new WishlistGroupAdapter(this, wishlists);
                recyclerWishlist.setAdapter(groupAdapter);
            } else {
                groupAdapter.updateData(wishlists);
            }
        }
    }

    private void showCreateWishlistDialog() {
        androidx.appcompat.app.AlertDialog.Builder builder = new androidx.appcompat.app.AlertDialog.Builder(this);
        builder.setTitle(R.string.new_wishlist_title);

        final android.widget.EditText input = new android.widget.EditText(this);
        input.setHint(R.string.hint_enter_list_name);
        int padding = (int) (16 * getResources().getDisplayMetrics().density);
        android.widget.FrameLayout container = new android.widget.FrameLayout(this);
        android.widget.FrameLayout.LayoutParams params = new android.widget.FrameLayout.LayoutParams(
                android.view.ViewGroup.LayoutParams.MATCH_PARENT, 
                android.view.ViewGroup.LayoutParams.WRAP_CONTENT
        );
        params.leftMargin = padding;
        params.rightMargin = padding;
        input.setLayoutParams(params);
        container.addView(input);
        builder.setView(container);

        builder.setPositiveButton(R.string.create, (dialog, which) -> {
            String name = input.getText().toString().trim();
            if (!name.isEmpty()) {
                favoritesManager.createWishlistAndAddProduct(name, null);
                updateWishlistUI();
            }
        });
        builder.setNegativeButton(R.string.cancel, (dialog, which) -> dialog.cancel());

        builder.show();
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Refresh data when returning to this screen
        updateWishlistUI();
    }

    private void setupBottomNavigation() {
        findViewById(R.id.btn_shop).setOnClickListener(v -> {
            startActivity(new Intent(this, ShopActivity.class));
            finish();
        });

        findViewById(R.id.fab_home).setOnClickListener(v -> {
            Intent intent = new Intent(this, HomeActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
            startActivity(intent);
            finish();
        });

        findViewById(R.id.btn_account).setOnClickListener(v -> {
            startActivity(new Intent(this, YourProfileActivity.class));
            finish();
        });

        // Current page is Lists (Wishlist), no need to set click listener or it can just refresh
    }

    private void setupRecommendations() {
        recommendationAdapter = new ProductAdapter(new ArrayList<>(), false);
        rvRecommendations.setLayoutManager(new androidx.recyclerview.widget.GridLayoutManager(this, 2));
        rvRecommendations.setNestedScrollingEnabled(false);
        rvRecommendations.setAdapter(recommendationAdapter);

        firestoreRepository.getBestSellers(new FirestoreRepository.OnProductsLoadedListener() {
            @Override
            public void onLoaded(List<Product> products) {
                recommendationAdapter.updateData(products);
            }

            @Override
            public void onError(Exception e) {
            }
        });
    }
}
