package com.yn.homi;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import android.Manifest;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.provider.MediaStore;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.core.app.ActivityCompat;
import androidx.core.content.FileProvider;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.mlkit.vision.common.InputImage;
import com.google.mlkit.vision.label.ImageLabel;
import com.google.mlkit.vision.label.ImageLabeler;
import com.google.mlkit.vision.label.ImageLabeling;
import com.google.mlkit.vision.label.defaults.ImageLabelerOptions;
import androidx.palette.graphics.Palette;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.widget.Button;
import java.io.InputStream;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager2.widget.ViewPager2;

import com.google.android.material.navigation.NavigationView;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;
import com.yn.homi.adapters.BannerAdapter;
import com.yn.homi.adapters.BestSellerAdapter;
import com.yn.homi.adapters.FlashSaleAdapter;
import com.yn.homi.adapters.IdeaAdapter;
import com.yn.homi.adapters.IdeaCategoryAdapter;
import com.yn.homi.adapters.ProductAdapter;
import com.yn.homi.adapters.QuickTabAdapter;
import com.yn.homi.models.Banner;
import com.yn.homi.models.Idea;
import com.yn.homi.models.Product;
import com.yn.homi.models.RoomCategory;
import com.yn.homi.utils.FirestoreRepository;
import com.yn.homi.utils.GeminiVisionHelper;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import com.google.firebase.auth.FirebaseAuth;

public class HomeActivity extends AppCompatActivity {

    private ProductAdapter productAdapter;
    private IdeaAdapter ideaAdapter;
    private IdeaCategoryAdapter ideaCategoryAdapter;
    private BannerAdapter bannerAdapter;
    private QuickTabAdapter quickTabAdapter;
    private FlashSaleAdapter flashSaleAdapter;
    private com.yn.homi.adapters.BestSellerAdapter bestSellerAdapter;
    private FirestoreRepository firestoreRepository;
    
    private RecyclerView rvProducts;
    private RecyclerView rvQuickTabs;
    private RecyclerView rvFlashSale;
    private RecyclerView rvIdeaCategories;
    private ProgressBar pbLoading;
    private ViewPager2 viewPagerBanner;
    private ViewPager2 vpBestSellers;
    private TabLayout tabIndicator;
    private TextView tvGreeting, tvEmptySearch;
    private ImageView ivCart, ivFilter;
    private EditText etSearch;
    private ImageView ivCameraSearch;
    private View svServices, llBestSellers, llFlashSale, tabLayoutMain, clBannerContainer;
    private LinearLayout llFreeShipping, llReturnPolicy, llDeliveryComp;

    private DrawerLayout drawerLayout;
    private NavigationView navigationView;
    private View drawerContent;

    private List<RoomCategory> roomCategories = new ArrayList<>();
    private List<String> selectedSorts = new ArrayList<>(Collections.singletonList("best_seller"));
    private List<String> selectedCategories = new ArrayList<>(Collections.singletonList("All"));
    private boolean isFilterOnSale = false;
    private boolean isFilterTips = false;
    private boolean isPriceExpanded = false;

    private ActivityResultLauncher<Intent> cameraLauncher;
    private ActivityResultLauncher<String> galleryLauncher;
    private ActivityResultLauncher<String> requestPermissionLauncher;
    private Uri photoUri;

    private final Handler bannerHandler = new Handler(Looper.getMainLooper());
    private Runnable bannerRunnable;
    private Runnable bestSellerRunnable;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        firestoreRepository = new FirestoreRepository();
        
        initLaunchers();
        initViews();
        initFilterDrawer();
        setupViews();
        updateGreeting();

        LinearLayout btnShop = findViewById(R.id.btn_shop);
        if (btnShop != null) {
            btnShop.setOnClickListener(v -> {
                Intent intent = new Intent(HomeActivity.this, ShopActivity.class);
                startActivity(intent);
            });
        }

        // Sign in anonymously and load data once authenticated
        FirebaseAuth.getInstance().signInAnonymously()
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        Log.d("HomeActivity", "signInAnonymously:success");
                    } else {
                        Log.w("HomeActivity", "signInAnonymously:failure", task.getException());
                    }
                    // Load data anyway, rules might be public or auth might have failed but we try
                    refreshData();
                });
    }

    private void refreshData() {
        fetchBanners();
        fetchQuickTabs();
        fetchFlashSaleProducts();
        fetchBestSellers();
        fetchRoomCategories();
        loadTabProducts(0); // Load Recommended by default
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

    private void showImagePickerDialog() {
        BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(this);
        View view = LayoutInflater.from(this).inflate(R.layout.layout_image_picker, null);
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

    private void showServiceGuaranteeDialog() {
        BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(this);
        View view = LayoutInflater.from(this).inflate(R.layout.dialog_service_guarantee, null);
        bottomSheetDialog.setContentView(view);

        ImageView ivClose = view.findViewById(R.id.iv_close);
        if (ivClose != null) {
            ivClose.setOnClickListener(v -> bottomSheetDialog.dismiss());
        }

        bottomSheetDialog.show();
    }

    private void showFilterDialog() {
        if (drawerLayout != null) {
            updateDrawerUI();
            drawerLayout.openDrawer(GravityCompat.START);
        }
    }

    private boolean isFurnitureMenuExpanded = false;
    private boolean isLivingMenuExpanded = false;
    private boolean isKitchenMenuExpanded = false;
    private boolean isBedroomMenuExpanded = false;
    private boolean isOfficeMenuExpanded = false;

    private void initFilterDrawer() {
        drawerLayout = findViewById(R.id.drawer_layout);
        navigationView = findViewById(R.id.nav_view_filter);

        if (navigationView != null) {
            drawerContent = navigationView.findViewById(R.id.drawer_filter_root);

            if (drawerContent != null) {
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

                // Click listeners for Main menu items (Filters)
                drawerContent.findViewById(R.id.tv_menu_new_arrivals).setOnClickListener(v -> openProductList("New Arrivals", "filter"));
                drawerContent.findViewById(R.id.tv_menu_best_seller).setOnClickListener(v -> openProductList("Best Seller", "filter"));
                drawerContent.findViewById(R.id.tv_menu_flash_sale).setOnClickListener(v -> openProductList("Flash Sale", "filter"));
                drawerContent.findViewById(R.id.tv_menu_recommended).setOnClickListener(v -> openProductList("Recommended", "filter"));
                drawerContent.findViewById(R.id.tv_menu_ideas).setOnClickListener(v -> openProductList("Ideas", "filter"));
            }
        }
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
            Log.e("HomeActivity", "Error creating image file", ex);
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

    private boolean isAiSearching = false;

    private void handleImageSearch(Uri uri) {
        showLoading(true);
        isAiSearching = true;

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
                        // Hiển thị kết quả nhận diện để debug / UX
                        String detected = features.category + (!features.colors.isEmpty() ? " • " + features.colors.get(0) : "");
                        Toast.makeText(HomeActivity.this,
                                "Nhận diện: " + detected, Toast.LENGTH_SHORT).show();

                        // Cập nhật thanh search
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
                        Toast.makeText(HomeActivity.this,
                                "Lỗi nhận diện: " + error, Toast.LENGTH_LONG).show();
                        Log.e("IMAGE_SEARCH", "Gemini Error: " + error);
                    });
                }
            });

        } catch (IOException e) {
            showLoading(false);
            isAiSearching = false;
            Log.e("IMAGE_SEARCH", "Load failed: " + e.getMessage());
        }
    }

    private void searchAndRankProducts(GeminiVisionHelper.FurnitureFeatures features) {
        toggleSearchMode(true);
        List<String> tags = features.keywords;
        List<String> limitedTags = tags.size() > 10 ? tags.subList(0, 10) : tags;

        Log.d("GEMINI_SEARCH", "Querying tags: " + limitedTags.toString());

        firestoreRepository.getProductsByMultipleTags(limitedTags, new FirestoreRepository.OnProductsLoadedListener() {
            @Override
            public void onLoaded(List<Product> products) {
                if (products.isEmpty()) {
                    runOnUiThread(() -> {
                        showLoading(false);
                        isAiSearching = false;
                        tvEmptySearch.setVisibility(View.VISIBLE);
                        tvEmptySearch.setText(R.string.no_products_found);
                        productAdapter.updateData(new ArrayList<>());
                    });
                    return;
                }

                // Sắp xếp theo điểm similarity
                Collections.sort(products, (a, b) ->
                        calculateSimilarityScore(b, features) - calculateSimilarityScore(a, features)
                );

                runOnUiThread(() -> {
                    showLoading(false);
                    isAiSearching = false;
                    productAdapter.updateData(products);
                    tvEmptySearch.setVisibility(View.GONE);
                });
            }

            @Override
            public void onError(Exception e) {
                Log.e("GEMINI_SEARCH", "Firestore query failed", e);
                runOnUiThread(() -> {
                    showLoading(false);
                    isAiSearching = false;
                    tvEmptySearch.setVisibility(View.VISIBLE);
                    tvEmptySearch.setText(R.string.search_error);
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
        for (String material : features.materials) {
            String m = material.toLowerCase();
            if (name.contains(m) || desc.contains(m)) {
                score += 20;
                break;
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

    private void initViews() {
        rvProducts = findViewById(R.id.rv_products);
        rvQuickTabs = findViewById(R.id.rv_quick_tabs);
        rvFlashSale = findViewById(R.id.rv_flash_sale);
        rvIdeaCategories = findViewById(R.id.rv_idea_categories);
        pbLoading = findViewById(R.id.pb_loading);
        viewPagerBanner = findViewById(R.id.view_pager_banner);
        vpBestSellers = findViewById(R.id.vp_best_sellers);
        tabIndicator = findViewById(R.id.tab_indicator);
        tvGreeting = findViewById(R.id.tv_greeting);
        tvEmptySearch = findViewById(R.id.tv_empty_search);
        ivCart = findViewById(R.id.iv_cart);
        ivFilter = findViewById(R.id.iv_menu);
        etSearch = findViewById(R.id.et_search);
        ivCameraSearch = findViewById(R.id.iv_camera_search);
        
        svServices = findViewById(R.id.sv_services);
        llFreeShipping = findViewById(R.id.ll_free_shipping);
        llReturnPolicy = findViewById(R.id.ll_return_policy);
        llDeliveryComp = findViewById(R.id.ll_delivery_comp);

        llBestSellers = findViewById(R.id.ll_best_sellers);
        findViewById(R.id.tv_see_all_best_sellers).setOnClickListener(v -> openProductList("Best Seller", "filter"));
        llFlashSale = findViewById(R.id.ll_flash_sale);
        findViewById(R.id.tv_see_all_flash_sale).setOnClickListener(v -> openProductList("Flash Sale", "filter"));
        tabLayoutMain = findViewById(R.id.tab_layout);
        clBannerContainer = findViewById(R.id.cl_banner_container);
        
        ivCart.setOnClickListener(v -> {
            Intent intent = new Intent(HomeActivity.this, CartActivity.class);
            startActivity(intent);
        });

        ivFilter.setOnClickListener(v -> showFilterDialog());

        findViewById(R.id.tv_sales_badge).setOnClickListener(v -> openProductList("Flash Sale", "filter"));

        ivCameraSearch.setOnClickListener(v -> showImagePickerDialog());

        llFreeShipping.setOnClickListener(v -> showServiceGuaranteeDialog());
        llReturnPolicy.setOnClickListener(v -> showServiceGuaranteeDialog());
        llDeliveryComp.setOnClickListener(v -> showServiceGuaranteeDialog());

        etSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (isAiSearching) return; // Nếu là AI đang điền text thì bỏ qua

                String query = s.toString().trim();
                if (query.isEmpty()) {
                    toggleSearchMode(false);
                    // Load lại dữ liệu theo tab đang chọn
                    loadTabProducts(tabLayoutMain instanceof TabLayout ? ((TabLayout)tabLayoutMain).getSelectedTabPosition() : 0);
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
                // Đóng bàn phím khi nhấn Search
                etSearch.clearFocus();
                return true;
            }
            return false;
        });
    }

    private void toggleSearchMode(boolean isSearching) {
        int visibility = isSearching ? View.GONE : View.VISIBLE;
        
        // Ẩn/Hiện các phần không liên quan
        viewPagerBanner.setVisibility(visibility);
        tabIndicator.setVisibility(visibility);
        svServices.setVisibility(visibility);
        rvQuickTabs.setVisibility(visibility);
        llBestSellers.setVisibility(visibility);
        llFlashSale.setVisibility(visibility);
        tabLayoutMain.setVisibility(visibility);

        if (!isSearching) {
            tvEmptySearch.setVisibility(View.GONE);
        }
        
        // Cập nhật background cho header container khi tìm kiếm
        // Header container nằm trong cl_banner_container
        findViewById(R.id.header_container).setBackgroundColor(
            isSearching ? ContextCompat.getColor(this, R.color.black_overlay) : android.graphics.Color.TRANSPARENT
        );
        
        // Điều chỉnh margin/padding nếu cần để rv_products không bị che mất bởi header khi banner ẩn
        // Trong layout hiện tại, rv_products nằm trong NestedScrollView sau các thành phần bị ẩn.
        // Khi các thành phần trên bị GONE, rv_products sẽ tự động đẩy lên trên.
    }

    private void performLiveSearch(String query) {
        showLoading(true);
        tvEmptySearch.setVisibility(View.GONE);
        firestoreRepository.searchProducts(query, new FirestoreRepository.OnProductsLoadedListener() {
            @Override
            public void onLoaded(List<Product> products) {
                showLoading(false);
                // Sử dụng lại Grid của rvProducts để hiển thị kết quả
                rvProducts.setLayoutManager(new GridLayoutManager(HomeActivity.this, 2));
                rvProducts.setAdapter(productAdapter);
                productAdapter.updateData(products);
                
                if (products.isEmpty()) {
                    tvEmptySearch.setVisibility(View.VISIBLE);
                } else {
                    tvEmptySearch.setVisibility(View.GONE);
                }
            }

            @Override
            public void onError(Exception e) {
                showLoading(false);
                Log.e("HomeActivity", "Search error: " + e.getMessage());
                tvEmptySearch.setVisibility(View.VISIBLE);
                tvEmptySearch.setText(R.string.search_error);
            }
        });
    }

    private void fetchBanners() {
        firestoreRepository.getBanners(new FirestoreRepository.OnBannersLoadedListener() {
            @Override
            public void onLoaded(List<Banner> banners) {
                if (banners != null && !banners.isEmpty()) {
                    Log.d("HomeActivity", "Đã tải " + banners.size() + " banners");
                    bannerAdapter.updateData(banners);
                    setupAutoScroll();
                } else {
                    Log.w("HomeActivity", "Không tìm thấy banner nào trong Firestore");
                }
            }

            @Override
            public void onError(Exception e) {
                Log.e("HomeActivity", "Lỗi tải banner: " + e.getMessage(), e);
            }
        });
    }

    private void fetchFlashSaleProducts() {
        firestoreRepository.getOnSaleProducts(new FirestoreRepository.OnProductsLoadedListener() {
            @Override
            public void onLoaded(List<Product> products) {
                if (products != null && !products.isEmpty()) {
                    // Hiển thị tối đa 4 sản phẩm flash sale
                    List<Product> flashSaleProducts = products.size() > 4 ? products.subList(0, 4) : products;
                    flashSaleAdapter.setProducts(flashSaleProducts);
                }
            }

            @Override
            public void onError(Exception e) {
                Log.e("HomeActivity", "Error loading flash sale products", e);
            }
        });
    }

    private void fetchBestSellers() {
        firestoreRepository.getBestSellers(new FirestoreRepository.OnProductsLoadedListener() {
            @Override
            public void onLoaded(List<Product> products) {
                if (products != null && !products.isEmpty()) {
                    bestSellerAdapter.setProducts(products);
                    // Start at a large enough position for smooth infinite scroll
                    int startPos = (Integer.MAX_VALUE / 2) - ((Integer.MAX_VALUE / 2) % products.size());
                    vpBestSellers.setCurrentItem(startPos, false);
                    setupBestSellerAutoScroll();
                }
            }

            @Override
            public void onError(Exception e) {
                Log.e("HomeActivity", "Error loading best sellers", e);
            }
        });
    }

    private void fetchQuickTabs() {
        firestoreRepository.getQuickTabs(new FirestoreRepository.OnQuickTabsLoadedListener() {
            @Override
            public void onLoaded(List<com.yn.homi.models.QuickTab> quickTabs) {
                if (quickTabs != null && !quickTabs.isEmpty()) {
                    quickTabAdapter.setQuickTabs(quickTabs);
                }
            }

            @Override
            public void onError(Exception e) {
                Log.e("HomeActivity", "Error loading quick tabs", e);
            }
        });
    }

    private void setupAutoScroll() {
        if (bannerRunnable != null) {
            bannerHandler.removeCallbacks(bannerRunnable);
        }

        bannerRunnable = new Runnable() {
            @Override
            public void run() {
                if (bannerAdapter != null && bannerAdapter.getItemCount() > 1) {
                    int currentItem = viewPagerBanner.getCurrentItem();
                    int nextItem = (currentItem + 1) % bannerAdapter.getItemCount();
                    viewPagerBanner.setCurrentItem(nextItem, true);
                }
                bannerHandler.postDelayed(this, 5000); // 5 seconds
            }
        };
        bannerHandler.postDelayed(bannerRunnable, 5000);
    }

    private void setupBestSellerAutoScroll() {
        if (bestSellerRunnable != null) {
            bannerHandler.removeCallbacks(bestSellerRunnable);
        }

        bestSellerRunnable = new Runnable() {
            @Override
            public void run() {
                if (bestSellerAdapter != null && bestSellerAdapter.getItemCount() > 0) {
                    int currentItem = vpBestSellers.getCurrentItem();
                    vpBestSellers.setCurrentItem(currentItem + 1, true);
                }
                bannerHandler.postDelayed(this, 2000); // Tăng tốc độ chuyển (2 giây)
            }
        };
        bannerHandler.postDelayed(bestSellerRunnable, 2000);
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (bannerRunnable != null) {
            bannerHandler.removeCallbacks(bannerRunnable);
        }
        if (bestSellerRunnable != null) {
            bannerHandler.removeCallbacks(bestSellerRunnable);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (bannerRunnable != null) {
            bannerHandler.removeCallbacks(bannerRunnable);
            if (bannerAdapter != null && bannerAdapter.getItemCount() > 1) {
                bannerHandler.postDelayed(bannerRunnable, 5000);
            }
        }
        if (bestSellerRunnable != null) {
            bannerHandler.removeCallbacks(bestSellerRunnable);
            if (bestSellerAdapter != null && bestSellerAdapter.getItemCount() > 0) {
                bannerHandler.postDelayed(bestSellerRunnable, 3000);
            }
        }
    }

    private void loadTabProducts(int position) {
        showLoading(true);

        if (position == 1) {
            // Hiển thị Ideas
            rvIdeaCategories.setVisibility(View.VISIBLE);
            rvProducts.setLayoutManager(new androidx.recyclerview.widget.LinearLayoutManager(this));
            rvProducts.setAdapter(ideaAdapter);
            
            // Lấy danh mục đang chọn hiện tại để tải dữ liệu tương ứng
            String currentCategory = ideaCategoryAdapter.getSelectedCategory();
            fetchIdeas(currentCategory);
            return;
        }

        // Mặc định hoặc các tab khác dùng ProductAdapter
        rvIdeaCategories.setVisibility(View.GONE);
        rvProducts.setLayoutManager(new GridLayoutManager(this, 2));
        rvProducts.setAdapter(productAdapter);

        FirestoreRepository.OnProductsLoadedListener listener = new FirestoreRepository.OnProductsLoadedListener() {
            @Override
            public void onLoaded(List<Product> products) {
                List<Product> filteredList = applyFiltersLocally(products);
                showLoading(false);
                productAdapter.updateData(filteredList);
            }

            @Override
            public void onError(Exception e) {
                showLoading(false);
                Log.e("HomeActivity", "Lỗi tải sản phẩm: " + e.getMessage());
                Toast.makeText(HomeActivity.this, "Lỗi tải sản phẩm: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        };

        switch (position) {
            case 2: // New Arrivals
                firestoreRepository.getNewArrivals(listener);
                break;
            case 0: // Recommended
            default:
                firestoreRepository.getProductsByTag("recommended", listener);
                break;
        }
    }

    private List<Product> applyFiltersLocally(List<Product> products) {
        List<Product> filtered = new ArrayList<>();
        
        // 1. Filter by Features and Category
        for (Product p : products) {
            boolean matches = true;
            
            boolean productIsOnSale = p.isOnSale() || 
                    (p.getTags() != null && (p.getTags().contains("flash_sale") || p.getTags().contains("sale")));
            if (isFilterOnSale && !productIsOnSale) matches = false;
            if (isFilterTips) {
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

    private void fetchRoomCategories() {
        firestoreRepository.getRoomCategories(new FirestoreRepository.OnRoomCategoriesLoadedListener() {
            @Override
            public void onLoaded(List<RoomCategory> categories) {
                roomCategories = categories;
            }

            @Override
            public void onError(Exception e) {
                Log.e("HomeActivity", "Error loading categories", e);
            }
        });
    }

    private void fetchIdeas(String category) {
        showLoading(true);
        FirestoreRepository.OnIdeasLoadedListener listener = new FirestoreRepository.OnIdeasLoadedListener() {
            @Override
            public void onLoaded(List<Idea> ideas) {
                showLoading(false);
                if (ideas.isEmpty()) {
                    Log.d("HomeActivity", "Không tìm thấy idea nào cho category: " + category);
                }
                ideaAdapter.updateData(ideas);
            }

            @Override
            public void onError(Exception e) {
                showLoading(false);
                Log.e("HomeActivity", "Lỗi tải ideas: " + e.getMessage());
                Toast.makeText(HomeActivity.this, "Lỗi tải ideas: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        };

        if (category == null || category.equalsIgnoreCase("All")) {
            firestoreRepository.getIdeas(listener);
        } else {
            firestoreRepository.getIdeasByCategory(category, listener);
        }
    }

    private void showLoading(boolean isLoading) {
        if (isLoading) {
            pbLoading.setVisibility(View.VISIBLE);
            rvProducts.setVisibility(View.GONE);
        } else {
            pbLoading.setVisibility(View.GONE);
            rvProducts.setVisibility(View.VISIBLE);
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

    private void setupViews() {
        rvProducts.setLayoutManager(new GridLayoutManager(this, 2));
        productAdapter = new ProductAdapter(new ArrayList<>());
        ideaAdapter = new IdeaAdapter();
        
        // Setup Idea Categories
        List<String> ideaCats = new ArrayList<>();
        ideaCats.add("All");
        ideaCats.add("Living Room");
        ideaCats.add("Bedroom");
        ideaCats.add("Kitchen & Dining");
        ideaCats.add("Office");
        ideaCategoryAdapter = new IdeaCategoryAdapter(ideaCats, (category, position) -> {
            fetchIdeas(category);
            rvIdeaCategories.setVisibility(View.GONE);
        });
        rvIdeaCategories.setAdapter(ideaCategoryAdapter);

        rvProducts.setAdapter(productAdapter);

        quickTabAdapter = new QuickTabAdapter(new ArrayList<>());
        quickTabAdapter.setOnQuickTabClickListener(tab -> {
            Intent intent = new Intent(HomeActivity.this, ProductListActivity.class);
            String tabName = tab.getName();
            
            if ("sales".equals(tab.getSlug()) || "Sale".equalsIgnoreCase(tabName)) {
                intent.putExtra("menu_value", "Flash Sale");
                intent.putExtra("menu_type", "filter");
            } else if ("TV Stands".equalsIgnoreCase(tabName)) {
                intent.putExtra("menu_value", "TV Stands & Media Consoles");
                intent.putExtra("menu_type", "category");
                intent.putExtra("subCategoryName", "TV Stands & Media Consoles");
            } else if ("Sofas & Sectionals".equalsIgnoreCase(tabName)) {
                intent.putExtra("menu_value", "Sofas & Sectionals");
                intent.putExtra("menu_type", "filter");
            } else {
                intent.putExtra("menu_value", tabName);
                intent.putExtra("menu_type", "category");
                intent.putExtra("subCategoryName", tabName);
            }
            startActivity(intent);
        });
        rvQuickTabs.setAdapter(quickTabAdapter);

        flashSaleAdapter = new FlashSaleAdapter();
        rvFlashSale.setAdapter(flashSaleAdapter);

        bestSellerAdapter = new com.yn.homi.adapters.BestSellerAdapter();
        vpBestSellers.setAdapter(bestSellerAdapter);
        vpBestSellers.setOffscreenPageLimit(1); // Chỉ giữ lại 1 card mỗi bên
        vpBestSellers.setClipToPadding(false);
        vpBestSellers.setClipChildren(false);

        // Tăng padding để đẩy các card thứ 4, thứ 5 ra khỏi vùng nhìn thấy
        int viewPagerPadding = (int) (80 * getResources().getDisplayMetrics().density);
        vpBestSellers.setPadding(viewPagerPadding, 0, viewPagerPadding, 0);

        vpBestSellers.setPageTransformer((page, position) -> {
            float absPos = Math.abs(position);
            
            // Khôi phục khoảng cách camera để hiệu ứng 3D sâu hơn
            page.setCameraDistance(12000f);

            if (absPos > 1.0f) {
                page.setAlpha(0f);
            } else {
                // GIỮ NGUYÊN ĐỘ SÁNG (Alpha = 1): Loại bỏ cảm giác "bóng đen" khi thẻ trượt đi
                page.setAlpha(1.0f);

                // Scale: Thẻ giữa to nhất (1.0), hai bên nhỏ dần về 0.85
                float scale = 0.85f + (1.0f - absPos) * 0.15f;
                page.setScaleX(scale);
                page.setScaleY(scale);

                // Khôi phục cấu trúc xoay nghiêng 3D
                page.setRotationY(position * -45f);

                // TranslationX: Kéo các thẻ sát lại gần nhau để tạo bố cục 3D tập trung
                float translationX = -position * (page.getWidth() / 1.8f);
                page.setTranslationX(translationX);
                
                // Đảm bảo thẻ ở chính giữa luôn nằm trên cùng
                page.setZ(1.0f - absPos);
            }

            // Chỉ hiện thông tin sản phẩm (tên/giá) khi ở gần vị trí trung tâm
            View infoContainer = page.findViewById(R.id.llInfo);
            if (infoContainer != null) {
                // Biến mất hoàn toàn nếu cách trung tâm > 0.1 đơn vị vị trí
                if (absPos < 0.2f) {
                    infoContainer.setVisibility(View.VISIBLE);
                    infoContainer.setAlpha(1.0f - absPos * 5.0f);
                } else {
                    infoContainer.setVisibility(View.GONE);
                    infoContainer.setAlpha(0f);
                }
            }
        });

        vpBestSellers.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageScrollStateChanged(int state) {
                super.onPageScrollStateChanged(state);
                if (state == ViewPager2.SCROLL_STATE_DRAGGING) {
                    bannerHandler.removeCallbacks(bestSellerRunnable);
                } else if (state == ViewPager2.SCROLL_STATE_IDLE) {
                    bannerHandler.removeCallbacks(bestSellerRunnable);
                    bannerHandler.postDelayed(bestSellerRunnable, 3000);
                }
            }
        });

        bannerAdapter = new BannerAdapter(new ArrayList<>());
        viewPagerBanner.setAdapter(bannerAdapter);

        // Fix việc vuốt ViewPager2 bị NestedScrollView cản trở
        viewPagerBanner.getChildAt(0).setOnTouchListener((v, event) -> {
            v.getParent().requestDisallowInterceptTouchEvent(true);
            if (event.getAction() == android.view.MotionEvent.ACTION_UP) {
                v.performClick();
            }
            return false;
        });

        new TabLayoutMediator(tabIndicator, viewPagerBanner, (tab, position) -> {
            // Dots indicator
        }).attach();

        // Tạm dừng auto-scroll khi người dùng đang chạm/vuốt banner
        viewPagerBanner.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageScrollStateChanged(int state) {
                super.onPageScrollStateChanged(state);
                if (state == ViewPager2.SCROLL_STATE_DRAGGING) {
                    bannerHandler.removeCallbacks(bannerRunnable);
                } else if (state == ViewPager2.SCROLL_STATE_IDLE) {
                    bannerHandler.removeCallbacks(bannerRunnable);
                    bannerHandler.postDelayed(bannerRunnable, 5000);
                }
            }
        });

        TabLayout tabLayout = findViewById(R.id.tab_layout);
        setupCustomTabs();
        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                updateTabStyle(tab.getPosition());
                loadTabProducts(tab.getPosition());
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {}

            @Override
            public void onTabReselected(TabLayout.Tab tab) {
                if (tab.getPosition() == 1) {
                    boolean isVisible = rvIdeaCategories.getVisibility() == View.VISIBLE;
                    rvIdeaCategories.setVisibility(isVisible ? View.GONE : View.VISIBLE);
                }
            }
        });
    }

    private void setupCustomTabs() {
        TabLayout tabLayout = findViewById(R.id.tab_layout);
        for (int i = 0; i < tabLayout.getTabCount(); i++) {
            TabLayout.Tab tab = tabLayout.getTabAt(i);
            if (tab != null) {
                View customView = LayoutInflater.from(this).inflate(R.layout.layout_custom_tab, tabLayout, false);
                TextView tabText = customView.findViewById(R.id.tab_text);
                ImageView ivFilter = customView.findViewById(R.id.iv_filter);

                String text = "";
                switch (i) {
                    case 0: text = getString(R.string.recommended); break;
                    case 1:
                        text = getString(R.string.ideas);
                        ivFilter.setVisibility(View.VISIBLE);
                        // Filter icon click toggles dropdown
                        ivFilter.setOnClickListener(v -> {
                            if (tabLayout.getSelectedTabPosition() == 1) {
                                boolean isVisible = rvIdeaCategories.getVisibility() == View.VISIBLE;
                                rvIdeaCategories.setVisibility(isVisible ? View.GONE : View.VISIBLE);
                            } else {
                                tabLayout.selectTab(tabLayout.getTabAt(1));
                                rvIdeaCategories.setVisibility(View.VISIBLE);
                            }
                        });
                        break;
                    case 2: text = getString(R.string.new_arrivals); break;
                }
                tabText.setText(text);
                tab.setCustomView(customView);
            }
        }
        updateTabStyle(tabLayout.getSelectedTabPosition());
    }

    private void updateTabStyle(int selectedPosition) {
        TabLayout tabLayout = findViewById(R.id.tab_layout);
        for (int i = 0; i < tabLayout.getTabCount(); i++) {
            TabLayout.Tab tab = tabLayout.getTabAt(i);
            if (tab != null && tab.getCustomView() != null) {
                TextView tabText = tab.getCustomView().findViewById(R.id.tab_text);
                ImageView ivFilter = tab.getCustomView().findViewById(R.id.iv_filter);

                if (i == selectedPosition) {
                    tabText.setTextColor(ContextCompat.getColor(this, R.color.black));
                    if (ivFilter.getVisibility() == View.VISIBLE) {
                        ivFilter.setColorFilter(ContextCompat.getColor(this, R.color.black));
                    }
                } else {
                    tabText.setTextColor(ContextCompat.getColor(this, R.color.gray_text));
                    if (ivFilter.getVisibility() == View.VISIBLE) {
                        ivFilter.setColorFilter(ContextCompat.getColor(this, R.color.gray_text));
                    }
                }
            }
        }
    }
}
