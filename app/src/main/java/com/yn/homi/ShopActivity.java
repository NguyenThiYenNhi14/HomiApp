package com.yn.homi;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Paint;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.HorizontalScrollView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;
import androidx.core.view.GravityCompat;
import androidx.core.widget.NestedScrollView;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.navigation.NavigationView;
import com.yn.homi.adapters.ProductAdapter;
import com.yn.homi.adapters.ShopSectionAdapter;
import com.yn.homi.models.Product;
import com.yn.homi.models.RoomCategory;
import com.yn.homi.models.RoomSubCategory;
import com.yn.homi.models.ShopSection;
import com.yn.homi.utils.FirestoreRepository;
import com.yn.homi.utils.GeminiVisionHelper;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class ShopActivity extends AppCompatActivity {

    private FirestoreRepository firestoreRepository;
    private LinearLayout llRoomTabs;
    private HorizontalScrollView hsvRoomTabs;
    private ProgressBar pbLoading;
    private RecyclerView rvShopSections;
    private NestedScrollView nsvShop;
    private View activeTab = null;
    private List<ShopSection> shopSections = new ArrayList<>();
    private ShopSectionAdapter sectionAdapter;
    private ProductAdapter searchAdapter;
    private DrawerLayout drawerLayout;
    private NavigationView navigationView;
    private View drawerContent;
    
    // Filter states
    private List<String> selectedSorts = new ArrayList<>(Collections.singletonList("best_seller"));
    private List<String> selectedCategories = new ArrayList<>(Collections.singletonList("All"));
    private boolean isFilterOnSale = false;
    private boolean isFilterTips = false;
    private boolean isPriceExpanded = false;

    // Header views
    private EditText etSearch;
    private ImageView ivCameraSearch, ivCart, ivFilter;
    private TextView tvEmptySearch, tvGreeting, tvBannerTitle;
    private RecyclerView rvSearchResults;
    private View hsvRoomTabsContainer, bannerContainer;

    private ActivityResultLauncher<Intent> cameraLauncher;
    private ActivityResultLauncher<String> galleryLauncher;
    private ActivityResultLauncher<String> requestPermissionLauncher;
    private Uri photoUri;
    private boolean isAiSearching = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_shop);

        firestoreRepository = new FirestoreRepository();

        initLaunchers();
        initViews();
        updateGreeting();
        loadAllData();

        FloatingActionButton fabHome = findViewById(R.id.fab_home);
        fabHome.setOnClickListener(v -> {
            Intent intent = new Intent(ShopActivity.this, HomeActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
            startActivity(intent);
            finish();
        });

        setupBottomNavigation();
    }

    private void setupBottomNavigation() {
        findViewById(R.id.btn_shop).setOnClickListener(v -> {
            // Already here
        });

        findViewById(R.id.btn_lists).setOnClickListener(v -> {
            startActivity(new Intent(this, com.yn.homi.setting.wishlist.WishlistActivity.class));
            finish();
        });

        findViewById(R.id.btn_account).setOnClickListener(v -> {
            startActivity(new Intent(this, com.yn.homi.setting.profile.YourProfileActivity.class));
            finish();
        });
    }

    private void initLaunchers() {
        cameraLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == RESULT_OK) {
                        handleImageSearch(photoUri);
                    }
                }
        );

        galleryLauncher = registerForActivityResult(
                new ActivityResultContracts.GetContent(),
                uri -> {
                    if (uri != null) {
                        handleImageSearch(uri);
                    }
                }
        );

        requestPermissionLauncher = registerForActivityResult(
                new ActivityResultContracts.RequestPermission(),
                isGranted -> {
                    if (isGranted) {
                        openCamera();
                    } else {
                        Toast.makeText(this, R.string.camera_permission_required, Toast.LENGTH_SHORT).show();
                    }
                }
        );
    }

    private void initViews() {
        llRoomTabs = findViewById(R.id.ll_room_tabs);
        hsvRoomTabs = findViewById(R.id.hsv_room_tabs);
        hsvRoomTabsContainer = hsvRoomTabs;
        bannerContainer = findViewById(R.id.banner_container);
        pbLoading = findViewById(R.id.pb_loading);
        rvShopSections = findViewById(R.id.rv_shop_sections);
        rvSearchResults = findViewById(R.id.rv_search_results);
        tvEmptySearch = findViewById(R.id.tv_empty_search);
        tvGreeting = findViewById(R.id.tv_greeting);
        tvBannerTitle = findViewById(R.id.tv_banner_title);
        nsvShop = findViewById(R.id.nsv_shop);
        
        // Hiệu ứng chữ rỗng (outline) cho banner
        if (tvBannerTitle != null) {
            tvBannerTitle.getPaint().setStyle(Paint.Style.STROKE);
            tvBannerTitle.getPaint().setStrokeWidth(4f); // Độ dày của viền
        }
        
        // Header views from Home style
        etSearch = findViewById(R.id.et_search);
        ivCameraSearch = findViewById(R.id.iv_camera_search);
        ivCart = findViewById(R.id.iv_cart);
        ivFilter = findViewById(R.id.iv_menu);

        ivFilter.setOnClickListener(v -> showFilterDialog());

        findViewById(R.id.tv_sales_badge).setOnClickListener(v -> openProductList("Flash Sale", "filter"));

        initFilterDrawer();
        updateDrawerUI();

        ivCameraSearch.setOnClickListener(v -> showImagePickerDialog());

        etSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (isAiSearching) return;
                String query = s.toString().trim();
                if (query.isEmpty()) {
                    toggleSearchMode(false);
                } else {
                    toggleSearchMode(true);
                    performLiveSearch(query);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        etSearch.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                etSearch.clearFocus();
                return true;
            }
            return false;
        });

        rvShopSections.setLayoutManager(new LinearLayoutManager(this));
        sectionAdapter = new ShopSectionAdapter(shopSections, subCat -> {
            Intent intent = new Intent(ShopActivity.this, ProductListActivity.class);
            if (subCat.getId() != null) {
                intent.putExtra("subCategoryId", subCat.getId());
            }
            intent.putExtra("subCategoryName", subCat.getName());
            // Bổ sung để ProductListActivity có thể xử lý tìm kiếm theo tên nếu ID null
            intent.putExtra("menu_value", subCat.getName());
            intent.putExtra("menu_type", "category");
            startActivity(intent);
        });
        rvShopSections.setAdapter(sectionAdapter);

        rvSearchResults.setLayoutManager(new GridLayoutManager(this, 4));
        searchAdapter = new ProductAdapter(new ArrayList<>());
        rvSearchResults.setAdapter(searchAdapter);

        // Sync tabs with scroll position
        nsvShop.setOnScrollChangeListener((NestedScrollView.OnScrollChangeListener) (v, scrollX, scrollY, oldScrollX, oldScrollY) -> {
            if (rvShopSections.getVisibility() == View.VISIBLE) {
                updateTabsOnScroll(scrollY);
            }
        });
    }

    private boolean isFurnitureMenuExpanded = false;
    private boolean isLivingMenuExpanded = false;
    private boolean isKitchenMenuExpanded = false;
    private boolean isBedroomMenuExpanded = false;
    private boolean isOfficeMenuExpanded = false;

    private void initFilterDrawer() {
        drawerLayout = findViewById(R.id.drawer_layout);
        navigationView = findViewById(R.id.nav_view_filter);
        if (navigationView == null) return;

        drawerContent = navigationView.findViewById(R.id.drawer_filter_root);
        if (drawerContent == null) return;

        // Main Headers Expand/Collapse
        drawerContent.findViewById(R.id.ll_menu_furniture_header).setOnClickListener(v -> {
            isFurnitureMenuExpanded = !isFurnitureMenuExpanded;
            updateMenuUI();
        });

        drawerContent.findViewById(R.id.ll_menu_living_header).setOnClickListener(v -> {
            isLivingMenuExpanded = !isLivingMenuExpanded;
            updateMenuUI();
        });

        drawerContent.findViewById(R.id.ll_menu_kitchen_header).setOnClickListener(v -> {
            isKitchenMenuExpanded = !isKitchenMenuExpanded;
            updateMenuUI();
        });

        drawerContent.findViewById(R.id.ll_menu_bedroom_header).setOnClickListener(v -> {
            isBedroomMenuExpanded = !isBedroomMenuExpanded;
            updateMenuUI();
        });

        drawerContent.findViewById(R.id.ll_menu_office_header).setOnClickListener(v -> {
            isOfficeMenuExpanded = !isOfficeMenuExpanded;
            updateMenuUI();
        });

        // Main Category Clicks
        setupCategoryClick(R.id.tv_menu_living, "Living Room");
        setupCategoryClick(R.id.tv_menu_kitchen, "Kitchen & Dining");
        setupCategoryClick(R.id.tv_menu_bedroom, "Bedroom");
        setupCategoryClick(R.id.tv_menu_office, "Office");

        // Living Room Sub-items
        setupCategoryClick(R.id.tv_menu_living_tv_stands, "TV Stands & Media Consoles");
        setupCategoryClick(R.id.tv_menu_living_coffee_tables, "Coffee Tables");
        setupCategoryClick(R.id.tv_menu_living_sleeper_sofas, "Sleeper Sofas & Futons");
        setupCategoryClick(R.id.tv_menu_living_sofa, "Sofas");
        setupCategoryClick(R.id.tv_menu_living_sets, "Living Room Sets");
        setupCategoryClick(R.id.tv_menu_living_accent_chairs, "Accent Chairs & Recliners");
        setupCategoryClick(R.id.tv_menu_living_cabinets, "Cabinets");
        setupCategoryClick(R.id.tv_menu_living_ottomans, "Ottomans & Benches");

        // Kitchen & Dining Sub-items
        setupCategoryClick(R.id.tv_menu_kitchen_tables, "Dining Tables");
        setupCategoryClick(R.id.tv_menu_kitchen_chairs, "Dining Chairs");
        setupCategoryClick(R.id.tv_menu_kitchen_stools, "Bar Stools");
        setupCategoryClick(R.id.tv_menu_kitchen_islands, "Kitchen Islands & Carts");
        setupCategoryClick(R.id.tv_menu_kitchen_sideboards, "Sideboards & Buffets");

        // Bedroom Sub-items
        setupCategoryClick(R.id.tv_menu_bedroom_beds, "Beds");
        setupCategoryClick(R.id.tv_menu_bedroom_dressers, "Dressers & Chests");
        setupCategoryClick(R.id.tv_menu_bedroom_nightstands, "Nightstands");
        setupCategoryClick(R.id.tv_menu_bedroom_armoires, "Armoires & Wardrobes");
        setupCategoryClick(R.id.tv_menu_bedroom_mattresses, "Mattresses");

        // Office Sub-items
        setupCategoryClick(R.id.tv_menu_office_desks, "Desks");
        setupCategoryClick(R.id.tv_menu_office_chairs, "Office Chairs");
        setupCategoryClick(R.id.tv_menu_office_bookcases, "Bookcases");
        setupCategoryClick(R.id.tv_menu_office_files, "File Cabinets");

        // Click listeners for Main menu items
        drawerContent.findViewById(R.id.tv_menu_new_arrivals).setOnClickListener(v -> openProductList("New Arrivals", "filter"));
        drawerContent.findViewById(R.id.tv_menu_best_seller).setOnClickListener(v -> openProductList("Best Seller", "filter"));
        drawerContent.findViewById(R.id.tv_menu_flash_sale).setOnClickListener(v -> openProductList("Flash Sale", "filter"));
        drawerContent.findViewById(R.id.tv_menu_recommended).setOnClickListener(v -> openProductList("Recommended", "filter"));
        drawerContent.findViewById(R.id.tv_menu_ideas).setOnClickListener(v -> openProductList("Ideas", "filter"));
    }

    private void setupCategoryClick(int viewId, String categoryName) {
        View view = drawerContent.findViewById(viewId);
        if (view != null) {
            view.setOnClickListener(v -> {
                drawerLayout.closeDrawers();
                Intent intent = new Intent(this, ProductListActivity.class);
                intent.putExtra("menu_value", categoryName);
                intent.putExtra("menu_type", "category");
                intent.putExtra("subCategoryName", categoryName);
                startActivity(intent);
            });
        }
    }

    private void updateMenuUI() {
        if (drawerContent == null) return;

        // Furniture
        View subMenu = drawerContent.findViewById(R.id.ll_furniture_sub_menu);
        ImageView arrow = drawerContent.findViewById(R.id.iv_furniture_arrow);
        if (subMenu != null) subMenu.setVisibility(isFurnitureMenuExpanded ? View.VISIBLE : View.GONE);
        if (arrow != null) arrow.setRotation(isFurnitureMenuExpanded ? 180f : 0f);

        // Living Room
        View livingSubMenu = drawerContent.findViewById(R.id.ll_living_sub_menu);
        ImageView livingArrow = drawerContent.findViewById(R.id.iv_living_arrow);
        if (livingSubMenu != null) livingSubMenu.setVisibility(isLivingMenuExpanded ? View.VISIBLE : View.GONE);
        if (livingArrow != null) livingArrow.setRotation(isLivingMenuExpanded ? 180f : 0f);

        // Kitchen
        View kitchenSubMenu = drawerContent.findViewById(R.id.ll_kitchen_sub_menu);
        ImageView kitchenArrow = drawerContent.findViewById(R.id.iv_kitchen_arrow);
        if (kitchenSubMenu != null) kitchenSubMenu.setVisibility(isKitchenMenuExpanded ? View.VISIBLE : View.GONE);
        if (kitchenArrow != null) kitchenArrow.setRotation(isKitchenMenuExpanded ? 180f : 0f);

        // Bedroom
        View bedroomSubMenu = drawerContent.findViewById(R.id.ll_bedroom_sub_menu);
        ImageView bedroomArrow = drawerContent.findViewById(R.id.iv_bedroom_arrow);
        if (bedroomSubMenu != null) bedroomSubMenu.setVisibility(isBedroomMenuExpanded ? View.VISIBLE : View.GONE);
        if (bedroomArrow != null) bedroomArrow.setRotation(isBedroomMenuExpanded ? 180f : 0f);

        // Office
        View officeSubMenu = drawerContent.findViewById(R.id.ll_office_sub_menu);
        ImageView officeArrow = drawerContent.findViewById(R.id.iv_office_arrow);
        if (officeSubMenu != null) officeSubMenu.setVisibility(isOfficeMenuExpanded ? View.VISIBLE : View.GONE);
        if (officeArrow != null) officeArrow.setRotation(isOfficeMenuExpanded ? 180f : 0f);
    }

    private void openProductList(String value, String type) {
        drawerLayout.closeDrawers();
        Intent intent = new Intent(this, ProductListActivity.class);
        intent.putExtra("menu_value", value);
        intent.putExtra("menu_type", type);
        startActivity(intent);
    }

    private void updateDrawerUI() {
        updateMenuUI();
    }

    private void updateTextStyle(TextView textView, boolean isActive, int activeColor, int inactiveColor) {
        if (textView == null) return;
        textView.setTextColor(isActive ? activeColor : inactiveColor);
        if (isActive) {
            textView.setPaintFlags(textView.getPaintFlags() | android.graphics.Paint.UNDERLINE_TEXT_FLAG);
        } else {
            textView.setPaintFlags(textView.getPaintFlags() & (~android.graphics.Paint.UNDERLINE_TEXT_FLAG));
        }
    }

    private void applyAndRefresh() {
        if (rvSearchResults.getVisibility() == View.VISIBLE) {
            performLiveSearch(etSearch.getText().toString().trim());
        }
    }

    private void showFilterDialog() {
        if (drawerLayout != null) {
            updateDrawerUI();
            drawerLayout.openDrawer(GravityCompat.START);
        }
    }

    private List<Product> applyFiltersLocally(List<Product> products) {
        List<Product> filtered = new ArrayList<>();
        
        // 1. Filter by Features and Category
        for (Product p : products) {
            boolean matches = true;
            
            if (isFilterOnSale && !p.isOnSale()) matches = false;
            if (isFilterTips) {
                // Giả sử Tips & Ideas liên quan đến sản phẩm có tag 'ideas' hoặc 'tips'
                boolean hasIdea = false;
                if (p.getTags() != null) {
                    for (String t : p.getTags()) {
                        if (t.toLowerCase().contains("idea") || t.toLowerCase().contains("tip")) {
                            hasIdea = true;
                            break;
                        }
                    }
                }
                if (!hasIdea) matches = false;
            }
            
            if (!selectedCategories.contains("All")) {
                boolean catMatch = false;
                for (String selectedCat : selectedCategories) {
                    String catLower = selectedCat.toLowerCase();
                    if (p.getName() != null && p.getName().toLowerCase().contains(catLower)) {
                        catMatch = true;
                        break;
                    }
                    if (p.getTags() != null) {
                        for (String t : p.getTags()) {
                            if (t.toLowerCase().contains(catLower)) {
                                catMatch = true;
                                break;
                            }
                        }
                    }
                }
                if (!catMatch) matches = false;
            }
            
            if (matches) filtered.add(p);
        }
        
        // 2. Sort (Multi-criteria)
        if (!selectedSorts.isEmpty()) {
            Collections.sort(filtered, (p1, p2) -> {
                for (String sortCriteria : selectedSorts) {
                    int result = 0;
                    switch (sortCriteria) {
                        case "price_low":
                            result = Double.compare(p1.getPrice(), p2.getPrice());
                            break;
                        case "price_high":
                            result = Double.compare(p2.getPrice(), p1.getPrice());
                            break;
                        case "best_seller":
                            if (p1.isBestSeller() != p2.isBestSeller()) {
                                result = p1.isBestSeller() ? -1 : 1;
                            }
                            break;
                        case "newest":
                            if (p1.isNew() != p2.isNew()) {
                                result = p1.isNew() ? -1 : 1;
                            }
                            break;
                    }
                    if (result != 0) return result;
                }
                // Tie-breaker: Popularity/Rating
                return Float.compare(p2.getRating(), p1.getRating());
            });
        }
        
        return filtered;
    }

    private void showImagePickerDialog() {
        BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(this);
        View view = getLayoutInflater().inflate(R.layout.layout_image_picker, null);
        bottomSheetDialog.setContentView(view);

        view.findViewById(R.id.btn_camera).setOnClickListener(v -> {
            checkCameraPermissionAndOpen();
            bottomSheetDialog.dismiss();
        });

        view.findViewById(R.id.btn_gallery).setOnClickListener(v -> {
            galleryLauncher.launch("image/*");
            bottomSheetDialog.dismiss();
        });

        bottomSheetDialog.show();
    }

    private void checkCameraPermissionAndOpen() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
            openCamera();
        } else {
            requestPermissionLauncher.launch(Manifest.permission.CAMERA);
        }
    }

    private void openCamera() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        File photoFile = null;
        try {
            photoFile = createImageFile();
        } catch (IOException ex) {
            Log.e("ShopActivity", "Error creating image file", ex);
        }
        if (photoFile != null) {
            photoUri = FileProvider.getUriForFile(this,
                    getApplicationContext().getPackageName() + ".fileprovider",
                    photoFile);
            takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoUri);
            cameraLauncher.launch(takePictureIntent);
        }
    }

    private File createImageFile() throws IOException {
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        File storageDir = getExternalFilesDir(null);
        return File.createTempFile(imageFileName, ".jpg", storageDir);
    }

    private void handleImageSearch(Uri uri) {
        showLoading(true);
        isAiSearching = true;
        toggleSearchMode(true);

        try {
            InputStream inputStream = getContentResolver().openInputStream(uri);
            Bitmap bitmap = BitmapFactory.decodeStream(inputStream);
            if (bitmap == null) {
                showLoading(false);
                isAiSearching = false;
                return;
            }

            GeminiVisionHelper.analyzeImage(bitmap, new GeminiVisionHelper.OnAnalysisComplete() {
                @Override
                public void onSuccess(GeminiVisionHelper.FurnitureFeatures features) {
                    bitmap.recycle();
                    runOnUiThread(() -> {
                        String detected = features.category + (!features.colors.isEmpty() ? " • " + features.colors.get(0) : "");
                        Toast.makeText(ShopActivity.this, "Nhận diện: " + detected, Toast.LENGTH_SHORT).show();

                        etSearch.setText(features.category);
                        searchAndRankProducts(features);
                    });
                }

                @Override
                public void onError(String error) {
                    if (!bitmap.isRecycled()) bitmap.recycle();
                    runOnUiThread(() -> {
                        showLoading(false);
                        isAiSearching = false;
                        Toast.makeText(ShopActivity.this, "Lỗi nhận diện: " + error, Toast.LENGTH_LONG).show();
                    });
                }
            });
        } catch (IOException e) {
            showLoading(false);
            isAiSearching = false;
        }
    }

    private void searchAndRankProducts(GeminiVisionHelper.FurnitureFeatures features) {
        List<String> tags = features.keywords;
        firestoreRepository.getProductsByMultipleTags(tags, new FirestoreRepository.OnProductsLoadedListener() {
            @Override
            public void onLoaded(List<Product> products) {
                List<Product> filtered = applyFiltersLocally(products);
                
                if (filtered.isEmpty()) {
                    runOnUiThread(() -> {
                        showLoading(false);
                        isAiSearching = false;
                        tvEmptySearch.setVisibility(View.VISIBLE);
                        searchAdapter.updateData(new ArrayList<>());
                    });
                    return;
                }

                Collections.sort(filtered, (a, b) ->
                        calculateSimilarityScore(b, features) - calculateSimilarityScore(a, features)
                );

                runOnUiThread(() -> {
                    showLoading(false);
                    isAiSearching = false;
                    searchAdapter.updateData(filtered);
                    tvEmptySearch.setVisibility(View.GONE);
                });
            }

            @Override
            public void onError(Exception e) {
                runOnUiThread(() -> {
                    showLoading(false);
                    isAiSearching = false;
                    tvEmptySearch.setVisibility(View.VISIBLE);
                });
            }
        });
    }

    private int calculateSimilarityScore(Product product, GeminiVisionHelper.FurnitureFeatures features) {
        int score = 0;
        String name = product.getName() != null ? product.getName().toLowerCase() : "";
        String desc = product.getDescription() != null ? product.getDescription().toLowerCase() : "";

        // Khớp category — quan trọng nhất
        String category = features.category.toLowerCase();
        if (name.contains(category)) score += 60;
        if (product.getTags() != null) {
            for (String tag : product.getTags()) {
                if (tag.equalsIgnoreCase(category)) {
                    score += 60;
                    break;
                }
            }
        }

        // Khớp màu sắc
        if (product.getColors() != null && !features.colors.isEmpty()) {
            for (String color : features.colors) {
                String targetColor = color.toLowerCase();
                for (String c : product.getColors()) {
                    String cl = c.toLowerCase();
                    if (cl.equals(targetColor)) {
                        score += 50;
                        break;
                    }
                    if (cl.contains(targetColor) || targetColor.contains(cl)) {
                        score += 25;
                    }
                }
            }
        }

        // Khớp chất liệu
        if (features.materials != null) {
            for (String material : features.materials) {
                String m = material.toLowerCase();
                if (name.contains(m) || desc.contains(m)) {
                    score += 20;
                    break;
                }
            }
        }

        // Khớp tags bổ sung
        if (product.getTags() != null) {
            for (String tag : product.getTags()) {
                if (features.keywords.contains(tag.toLowerCase())) score += 10;
            }
        }

        return score;
    }

    private void toggleSearchMode(boolean isSearching) {
        rvShopSections.setVisibility(isSearching ? View.GONE : View.VISIBLE);
        hsvRoomTabsContainer.setVisibility(isSearching ? View.GONE : View.VISIBLE);
        if (bannerContainer != null) {
            bannerContainer.setVisibility(isSearching ? View.GONE : View.VISIBLE);
        }
        rvSearchResults.setVisibility(isSearching ? View.VISIBLE : View.GONE);
        if (isSearching) {
            nsvShop.smoothScrollTo(0, 0);
        } else {
            tvEmptySearch.setVisibility(View.GONE);
            isAiSearching = false;
        }
    }

    private void performLiveSearch(String query) {
        showLoading(true);
        tvEmptySearch.setVisibility(View.GONE);
        firestoreRepository.searchProducts(query, new FirestoreRepository.OnProductsLoadedListener() {
            @Override
            public void onLoaded(List<Product> products) {
                List<Product> filtered = applyFiltersLocally(products);
                showLoading(false);
                searchAdapter.updateData(filtered);
                tvEmptySearch.setVisibility(filtered.isEmpty() ? View.VISIBLE : View.GONE);
            }

            @Override
            public void onError(Exception e) {
                showLoading(false);
                tvEmptySearch.setVisibility(View.VISIBLE);
            }
        });
    }

    private void updateTabsOnScroll(int scrollY) {
        for (int i = 0; i < shopSections.size(); i++) {
            View sectionView = rvShopSections.getLayoutManager().findViewByPosition(i);
            if (sectionView != null) {
                int sectionTop = sectionView.getTop() + rvShopSections.getTop();
                int sectionBottom = sectionTop + sectionView.getHeight();

                if (scrollY >= sectionTop - dpToPx(20) && scrollY < sectionBottom) {
                    View tab = llRoomTabs.getChildAt(i);
                    if (tab != null && activeTab != tab) {
                        updateActiveTab(tab);
                        hsvRoomTabs.smoothScrollTo(tab.getLeft() - dpToPx(16), 0);
                    }
                    break;
                }
            }
        }
    }

    private void updateGreeting() {
        Calendar calendar = Calendar.getInstance();
        int hour = calendar.get(Calendar.HOUR_OF_DAY);

        String greeting;
        if (hour < 12) {
            greeting = getString(R.string.good_morning);
        } else if (hour < 17) {
            greeting = getString(R.string.good_afternoon);
        } else {
            greeting = getString(R.string.good_evening);
        }
        if (tvGreeting != null) {
            tvGreeting.setText(greeting);
        }
    }

    private void loadAllData() {
        showLoading(true);
        firestoreRepository.getRoomCategories(new FirestoreRepository.OnRoomCategoriesLoadedListener() {
            @Override
            public void onLoaded(List<RoomCategory> categories) {
                displayTabs(categories);
                fetchSubcategoriesForAll(categories);
            }

            @Override
            public void onError(Exception e) {
                showLoading(false);
                Toast.makeText(ShopActivity.this, "Error loading categories", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void displayTabs(List<RoomCategory> categories) {
        llRoomTabs.removeAllViews();
        for (int i = 0; i < categories.size(); i++) {
            RoomCategory category = categories.get(i);
            TextView tab = new TextView(this);
            tab.setText(category.getName());
            tab.setPadding(dpToPx(16), dpToPx(8), dpToPx(16), dpToPx(8));
            tab.setTextSize(14);
            tab.setTextColor(ContextCompat.getColor(this, R.color.black));
            tab.setBackground(null);

            final int index = i;
            tab.setOnClickListener(v -> {
                updateActiveTab(v);
                scrollToSection(index);
            });

            llRoomTabs.addView(tab);

            if (i == 0) {
                updateActiveTab(tab);
            }
        }
    }

    private void fetchSubcategoriesForAll(List<RoomCategory> categories) {
        shopSections.clear();
        int total = categories.size();
        final int[] count = {0};

        for (RoomCategory category : categories) {
            firestoreRepository.getRoomSubCategories(category.getId(), new FirestoreRepository.OnRoomSubCategoriesLoadedListener() {
                @Override
                public void onLoaded(List<RoomSubCategory> subCats) {
                    List<RoomSubCategory> finalSubCats = new ArrayList<>(subCats);
                    
                    // Bổ sung dữ liệu cho các mục con nếu là tab tương ứng
                    if (category.getName() != null) {
                        if (category.getName().equals("Living Room")) {
                            addManualSubCategories(finalSubCats, category.getId(), "living");
                        } else if (category.getName().contains("Kitchen")) {
                            addManualSubCategories(finalSubCats, category.getId(), "kitchen");
                        } else if (category.getName().equals("Bedroom")) {
                            addManualSubCategories(finalSubCats, category.getId(), "bedroom");
                        } else if (category.getName().equals("Office")) {
                            addManualSubCategories(finalSubCats, category.getId(), "office");
                        }
                    }
                    
                    shopSections.add(new ShopSection(category, finalSubCats));
                    count[0]++;
                    if (count[0] == total) {
                        // Sort sections according to original categories order
                        Collections.sort(shopSections, (s1, s2) -> {
                            int i1 = 0, i2 = 0;
                            for (int i = 0; i < categories.size(); i++) {
                                if (categories.get(i).getId().equals(s1.getCategory().getId())) i1 = i;
                                if (categories.get(i).getId().equals(s2.getCategory().getId())) i2 = i;
                            }
                            return i1 - i2;
                        });
                        onAllDataLoaded();
                    }
                }

                @Override
                public void onError(Exception e) {
                    count[0]++;
                    if (count[0] == total) {
                        onAllDataLoaded();
                    }
                }
            });
        }
    }

    private void addManualSubCategories(List<RoomSubCategory> subCats, String categoryId, String roomType) {
        String[] manualNames;
        switch (roomType) {
            case "kitchen":
                manualNames = new String[]{"Dining Tables", "Dining Chairs", "Bar Stools", "Kitchen Islands & Carts", "Sideboards & Buffets"};
                break;
            case "bedroom":
                manualNames = new String[]{"Beds", "Dressers & Chests", "Nightstands", "Armoires & Wardrobes", "Mattresses"};
                break;
            case "office":
                manualNames = new String[]{"Desks", "Office Chairs", "Bookcases", "File Cabinets"};
                break;
            case "living":
            default:
                manualNames = new String[]{"TV Stands & Media Consoles", "Coffee Tables", "Sleeper Sofas & Futons", "Sofas", "Living Room Sets", "Accent Chairs & Recliners", "Cabinets", "Ottomans & Benches"};
                break;
        }
        
        // Tránh trùng lặp nếu Database đã có
        for (String name : manualNames) {
            boolean exists = false;
            for (RoomSubCategory sc : subCats) {
                if (sc.getName() != null && sc.getName().equalsIgnoreCase(name)) {
                    exists = true;
                    break;
                }
            }
            if (!exists) {
                RoomSubCategory newSub = new RoomSubCategory();
                newSub.setName(name);
                newSub.setRoomCategoryId(categoryId);
                subCats.add(newSub);
            }
        }
    }

    private void onAllDataLoaded() {
        runOnUiThread(() -> {
            showLoading(false);
            sectionAdapter.notifyDataSetChanged();
        });
    }

    private void updateActiveTab(View tabView) {
        if (activeTab != null) {
            ((TextView) activeTab).setTextColor(ContextCompat.getColor(this, R.color.black));
            activeTab.setBackground(null);
        }

        activeTab = tabView;
        ((TextView) tabView).setTextColor(ContextCompat.getColor(this, R.color.orange));
        tabView.setBackgroundResource(R.drawable.bg_chip_selected);
    }

    private void scrollToSection(int index) {
        View sectionView = rvShopSections.getLayoutManager().findViewByPosition(index);
        if (sectionView != null) {
            int scrollTo = sectionView.getTop() + rvShopSections.getTop() - dpToPx(8);
            nsvShop.smoothScrollTo(0, scrollTo);
        }
    }

    private void showLoading(boolean show) {
        pbLoading.setVisibility(show ? View.VISIBLE : View.GONE);
    }

    private int dpToPx(int dp) {
        float density = getResources().getDisplayMetrics().density;
        return Math.round(dp * density);
    }
}
