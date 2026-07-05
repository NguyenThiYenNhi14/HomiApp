package com.yn.homi.ui.shop;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.yn.homi.core.BaseActivity;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.chip.ChipGroup;
import com.yn.homi.R;
import com.yn.homi.ui.shop.ProductAdapter;
import com.yn.homi.data.model.Product;
import com.yn.homi.data.repository.FirestoreRepository;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ProductListActivity extends BaseActivity {

    private RecyclerView rvProducts;
    private ProgressBar pbLoading;
    private TextView tvEmpty, tvTitle, tvItemCount;
    private android.widget.EditText etSearch;
    private ProductAdapter productAdapter;
    private FirestoreRepository firestoreRepository;
    private DrawerLayout drawerFilter;
    private View btnSort, btnFilter;
    private TextView tvSortLabel, tvFilterLabel;
    private ImageView ivSortArrow, ivFilterIcon;
    private String subCategoryId;
    private String subCategoryName;
    private String searchQuery;
    private String menuValue;
    private String menuType;
    private List<Product> originalProducts = new ArrayList<>();

    // Filter states
    private String selectedSort = "best_seller";
    private String selectedCategoryName = "All";
    private boolean isFilterOnSale = false;
    private boolean isFilterQuickShip = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_product_list);

        subCategoryId = getIntent().getStringExtra("subCategoryId");
        subCategoryName = getIntent().getStringExtra("subCategoryName");
        searchQuery = getIntent().getStringExtra("searchQuery");
        menuValue = getIntent().getStringExtra("menu_value");
        menuType = getIntent().getStringExtra("menu_type");

        if ("Flash Sale".equals(menuValue)) {
            isFilterOnSale = true;
        }

        firestoreRepository = new FirestoreRepository();
        initViews();
        loadProducts();
    }

    private void initViews() {
        tvTitle = findViewById(R.id.tv_title);
        tvItemCount = findViewById(R.id.tv_item_count);
        etSearch = findViewById(R.id.et_search_product);
        drawerFilter = findViewById(R.id.drawer_layout_filter);

        btnSort = findViewById(R.id.btn_sort);
        btnFilter = findViewById(R.id.btn_filter);
        tvSortLabel = findViewById(R.id.tv_sort_label);
        tvFilterLabel = findViewById(R.id.tv_filter_label);
        ivSortArrow = findViewById(R.id.iv_sort_arrow);
        ivFilterIcon = findViewById(R.id.iv_filter_icon);

        if (subCategoryName != null) {
            tvTitle.setText(subCategoryName);
        } else if (menuValue != null) {
            tvTitle.setText(menuValue);
        } else if (searchQuery != null) {
            tvTitle.setText("Search: " + searchQuery);
            etSearch.setText(searchQuery);
        }

        findViewById(R.id.iv_back).setOnClickListener(v -> finish());
        btnSort.setOnClickListener(v -> showSortDialog());
        btnFilter.setOnClickListener(v -> openFilterDrawer());

        rvProducts = findViewById(R.id.rv_products);
        pbLoading = findViewById(R.id.pb_loading);
        tvEmpty = findViewById(R.id.tv_empty);

        rvProducts.setLayoutManager(new GridLayoutManager(this, 2));
        productAdapter = new ProductAdapter(new ArrayList<>());
        rvProducts.setAdapter(productAdapter);

        setupSearch();
        setupFilterDrawer();
    }

    private void updateSortButtonUI(boolean active) {
        if (active) {
            btnSort.setBackgroundResource(R.drawable.bg_filter_button_outline);
            tvSortLabel.setTextColor(ContextCompat.getColor(this, R.color.orange));
            ivSortArrow.setColorFilter(ContextCompat.getColor(this, R.color.orange));
        } else {
            btnSort.setBackgroundResource(R.drawable.bg_filter_button_outline_black);
            tvSortLabel.setTextColor(ContextCompat.getColor(this, R.color.black));
            ivSortArrow.setColorFilter(ContextCompat.getColor(this, R.color.black));
        }
    }

    private void updateFilterButtonUI(boolean active) {
        if (active) {
            btnFilter.setBackgroundResource(R.drawable.bg_filter_button_outline);
            tvFilterLabel.setTextColor(ContextCompat.getColor(this, R.color.orange));
            ivFilterIcon.setColorFilter(ContextCompat.getColor(this, R.color.orange));
        } else {
            btnFilter.setBackgroundResource(R.drawable.bg_filter_button_outline_black);
            tvFilterLabel.setTextColor(ContextCompat.getColor(this, R.color.black));
            ivFilterIcon.setColorFilter(ContextCompat.getColor(this, R.color.black));
        }
    }

    private void openFilterDrawer() {
        if (drawerFilter != null) {
            drawerFilter.openDrawer(GravityCompat.END);
            updateFilterButtonUI(true);
        }
    }

    private void setupFilterDrawer() {
        if (drawerFilter != null) {
            drawerFilter.addDrawerListener(new DrawerLayout.SimpleDrawerListener() {
                @Override
                public void onDrawerClosed(View drawerView) {
                    super.onDrawerClosed(drawerView);
                    updateFilterButtonUI(false);
                }
            });
        }
        
        View filterView = findViewById(R.id.nav_view_filter);
        if (filterView == null) return;

        ImageView ivClose = filterView.findViewById(R.id.iv_close_filter);
        Button btnViewResults = filterView.findViewById(R.id.btn_view_results);
        Button btnResetSidebar = filterView.findViewById(R.id.btn_reset_sidebar);

        ivClose.setOnClickListener(v -> drawerFilter.closeDrawer(GravityCompat.END));
        
        btnResetSidebar.setOnClickListener(v -> {
            selectedSort = "best_seller";
            selectedCategoryName = "All";
            isFilterOnSale = false;
            isFilterQuickShip = false;
            applyCurrentFilters();
            drawerFilter.closeDrawer(GravityCompat.END);
        });

        btnViewResults.setOnClickListener(v -> {
            // Here you would normally collect values from Category, Price, etc.
            // For now, we'll just close and maybe apply existing simple filters
            applyCurrentFilters();
            drawerFilter.closeDrawer(GravityCompat.END);
        });

        // Click listeners for filter sections
        filterView.findViewById(R.id.ll_filter_category).setOnClickListener(v -> {
            Toast.makeText(this, "Select Category", Toast.LENGTH_SHORT).show();
        });
        filterView.findViewById(R.id.ll_filter_price).setOnClickListener(v -> {
            Toast.makeText(this, "Select Price Range", Toast.LENGTH_SHORT).show();
        });
        filterView.findViewById(R.id.ll_filter_color).setOnClickListener(v -> {
            Toast.makeText(this, "Select Color", Toast.LENGTH_SHORT).show();
        });
        filterView.findViewById(R.id.ll_filter_material).setOnClickListener(v -> {
            Toast.makeText(this, "Select Material", Toast.LENGTH_SHORT).show();
        });
    }

    private void setupSearch() {
        etSearch.addTextChangedListener(new android.text.TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                String query = s.toString().trim().toLowerCase();
                filterLocalProducts(query);
            }

            @Override
            public void afterTextChanged(android.text.Editable s) {}
        });
    }

    private void filterLocalProducts(String query) {
        if (query.isEmpty()) {
            applyCurrentFilters();
            return;
        }

        List<Product> filtered = new ArrayList<>();
        for (Product p : originalProducts) {
            if (p.getName() != null && p.getName().toLowerCase().contains(query)) {
                filtered.add(p);
            }
        }
        updateRecyclerView(filtered);
    }

    private void applyCurrentFilters() {
        List<Product> filtered = new ArrayList<>();

        // 1. Filter
        for (Product p : originalProducts) {
            boolean matches = true;

            // Kiểm tra trạng thái Sale: Đồng bộ với logic Filter.or ở Repository
            boolean productIsOnSale = p.isOnSale() || 
                    (p.getTags() != null && (p.getTags().contains("flash_sale") || p.getTags().contains("sale")));

            if (isFilterOnSale && !productIsOnSale) matches = false;
            if (isFilterQuickShip && !p.isQuickShip()) matches = false;

            if (!selectedCategoryName.equals("All")) {
                String catLower = selectedCategoryName.toLowerCase();
                boolean catMatch = false;
                if (p.getName() != null && p.getName().toLowerCase().contains(catLower)) catMatch = true;
                if (p.getTags() != null) {
                    for (String t : p.getTags()) {
                        if (t.toLowerCase().contains(catLower)) {
                            catMatch = true;
                            break;
                        }
                    }
                }
                if (!catMatch) matches = false;
            }

            if (matches) filtered.add(p);
        }

        // 2. Sort
        Collections.sort(filtered, (p1, p2) -> {
            switch (selectedSort) {
                case "price_low":
                    return Double.compare(p1.getPrice(), p2.getPrice());
                case "price_high":
                    return Double.compare(p2.getPrice(), p1.getPrice());
                case "newest":
                    if (p1.isNew() && !p2.isNew()) return -1;
                    if (!p1.isNew() && p2.isNew()) return 1;
                    return 0;
                case "rating":
                    return Float.compare(p2.getRating(), p1.getRating());
                case "best_seller":
                default:
                    if (p1.isBestSeller() && !p2.isBestSeller()) return -1;
                    if (!p1.isBestSeller() && p2.isBestSeller()) return 1;
                    return Float.compare(p2.getRating(), p1.getRating());
            }
        });

        updateRecyclerView(filtered);
    }

    private void updateRecyclerView(List<Product> filtered) {
        if (filtered.isEmpty()) {
            tvEmpty.setVisibility(View.VISIBLE);
            rvProducts.setVisibility(View.GONE);
            tvItemCount.setText("0 items");
        } else {
            tvEmpty.setVisibility(View.GONE);
            rvProducts.setVisibility(View.VISIBLE);
            productAdapter.updateData(filtered);
            tvItemCount.setText(filtered.size() + " items");
        }
    }

    private void showSortDialog() {
        updateSortButtonUI(true);
        BottomSheetDialog dialog = new BottomSheetDialog(this);
        View view = LayoutInflater.from(this).inflate(R.layout.dialog_sort_by, null);
        dialog.setContentView(view);

        dialog.setOnDismissListener(d -> updateSortButtonUI(false));

        android.widget.RadioGroup rgSort = view.findViewById(R.id.rg_sort);
        ImageView ivClose = view.findViewById(R.id.iv_close);

        // Pre-select current sort
        switch (selectedSort) {
            case "best_seller": rgSort.check(R.id.rb_recommended); break;
            case "rating": rgSort.check(R.id.rb_rating); break;
            case "newest": rgSort.check(R.id.rb_new_arrival); break;
            case "price_high": rgSort.check(R.id.rb_price_high_low); break;
            case "price_low": rgSort.check(R.id.rb_price_low_high); break;
        }

        rgSort.setOnCheckedChangeListener((group, checkedId) -> {
            if (checkedId == R.id.rb_recommended) selectedSort = "best_seller";
            else if (checkedId == R.id.rb_rating) selectedSort = "rating";
            else if (checkedId == R.id.rb_new_arrival) selectedSort = "newest";
            else if (checkedId == R.id.rb_price_high_low) selectedSort = "price_high";
            else if (checkedId == R.id.rb_price_low_high) selectedSort = "price_low";

            applyCurrentFilters();
            dialog.dismiss();
        });

        ivClose.setOnClickListener(v -> dialog.dismiss());
        dialog.show();
    }

    private void showFilterDialog() {
        BottomSheetDialog dialog = new BottomSheetDialog(this);
        View view = LayoutInflater.from(this).inflate(R.layout.dialog_filter_home, null);
        dialog.setContentView(view);

        ChipGroup cgSort = view.findViewById(R.id.cg_sort);
        ChipGroup cgCategory = view.findViewById(R.id.cg_category);
        ChipGroup cgFeatures = view.findViewById(R.id.cg_features);
        Button btnApply = view.findViewById(R.id.btn_apply);
        TextView tvReset = view.findViewById(R.id.tv_reset);

        // Update Reset text color and padding to match sidebar style if needed
        tvReset.setTextColor(ContextCompat.getColor(this, R.color.orange));

        // Pre-select current values
        if (selectedSort.equals("best_seller")) cgSort.check(R.id.chip_popular);
        else if (selectedSort.equals("newest")) cgSort.check(R.id.chip_newest);
        else if (selectedSort.equals("price_low")) cgSort.check(R.id.chip_price_low);
        else if (selectedSort.equals("price_high")) cgSort.check(R.id.chip_price_high);

        if (selectedCategoryName.equals("All")) cgCategory.check(R.id.chip_cat_all);
        else if (selectedCategoryName.equals("Living Room")) cgCategory.check(R.id.chip_cat_living);
        else if (selectedCategoryName.equals("Bedroom")) cgCategory.check(R.id.chip_cat_bedroom);
        else if (selectedCategoryName.equals("Kitchen")) cgCategory.check(R.id.chip_cat_kitchen);

        if (isFilterOnSale) cgFeatures.check(R.id.chip_on_sale);
        if (isFilterQuickShip) cgFeatures.check(R.id.chip_quick_ship);

        tvReset.setOnClickListener(v -> {
            selectedSort = "best_seller";
            selectedCategoryName = "All";
            isFilterOnSale = false;
            isFilterQuickShip = false;
            dialog.dismiss();
            applyCurrentFilters();
        });

        btnApply.setOnClickListener(v -> {
            int sortId = cgSort.getCheckedChipId();
            if (sortId == R.id.chip_popular) selectedSort = "best_seller";
            else if (sortId == R.id.chip_newest) selectedSort = "newest";
            else if (sortId == R.id.chip_price_low) selectedSort = "price_low";
            else if (sortId == R.id.chip_price_high) selectedSort = "price_high";

            int catId = cgCategory.getCheckedChipId();
            if (catId == R.id.chip_cat_all) selectedCategoryName = "All";
            else if (catId == R.id.chip_cat_living) selectedCategoryName = "Living Room";
            else if (catId == R.id.chip_cat_bedroom) selectedCategoryName = "Bedroom";
            else if (catId == R.id.chip_cat_kitchen) selectedCategoryName = "Kitchen";

            isFilterOnSale = false;
            isFilterQuickShip = false;
            for (int id : cgFeatures.getCheckedChipIds()) {
                if (id == R.id.chip_on_sale) isFilterOnSale = true;
                if (id == R.id.chip_quick_ship) isFilterQuickShip = true;
            }

            dialog.dismiss();
            applyCurrentFilters();
        });

        dialog.show();
    }
    private void loadProducts() {
        pbLoading.setVisibility(View.VISIBLE);
        rvProducts.setVisibility(View.GONE);
        tvEmpty.setVisibility(View.GONE);

        FirestoreRepository.OnProductsLoadedListener listener = new FirestoreRepository.OnProductsLoadedListener() {
            @Override
            public void onLoaded(List<Product> products) {
                pbLoading.setVisibility(View.GONE);
                originalProducts = products != null ? products : new ArrayList<>();
                
                // Nếu đang mở từ menu Flash Sale, tự động bật filter isOnSale cho UI local đồng bộ
                if ("Flash Sale".equals(menuValue)) {
                    isFilterOnSale = true;
                }

                applyCurrentFilters();
            }

            @Override
            public void onError(Exception e) {
                pbLoading.setVisibility(View.GONE);
                Toast.makeText(ProductListActivity.this, "Error loading products", Toast.LENGTH_SHORT).show();
            }
        };

        if (subCategoryId != null) {
            firestoreRepository.getProductsBySubCategory(subCategoryId, listener);
        } else if (menuValue != null) {
            if ("filter".equals(menuType)) {
                handleFilterMenu(listener);
            } else {
                // Đã tối giản: Dùng tên phân giải ra ID tự động trong Repo
                firestoreRepository.getProductsByResourceName(menuValue, listener);
            }
        } else if (searchQuery != null) {
            firestoreRepository.searchProducts(searchQuery, listener);
        } else {
            pbLoading.setVisibility(View.GONE);
            tvEmpty.setVisibility(View.VISIBLE);
        }
    }

    private void handleFilterMenu(FirestoreRepository.OnProductsLoadedListener listener) {
        switch (menuValue) {
            case "New Arrivals":
                firestoreRepository.getNewArrivals(listener);
                break;
            case "Best Seller":
                firestoreRepository.getBestSellers(listener);
                break;
            case "Flash Sale":
                firestoreRepository.getOnSaleProducts(listener);
                break;
            case "Recommended":
                firestoreRepository.getProductsByTag("recommended", listener);
                break;
            case "Sofas & Sectionals":
                List<String> categories = new ArrayList<>();
                categories.add("Sofas");
                categories.add("Living Room Sets");
                firestoreRepository.getProductsByMultipleResourceNames(categories, listener);
                break;
            default:
                firestoreRepository.searchProducts(menuValue, listener);
                break;
        }
    }
}
