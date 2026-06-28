package com.yn.homi.data.repository;

import android.util.Log;

import com.google.firebase.firestore.Filter;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.yn.homi.data.model.Banner;
import com.yn.homi.data.model.Idea;
import com.yn.homi.data.model.Product;
import com.yn.homi.data.model.QuickTab;
import com.yn.homi.data.model.RoomCategory;
import com.yn.homi.data.model.RoomSubCategory;
import com.yn.homi.models.Review;
import com.yn.homi.ui.profile.profile.UserProfile;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class FirestoreRepository {
    private static final String TAG = "FirestoreRepo";
    private final FirebaseFirestore db;

    public FirestoreRepository() {
        this.db = FirebaseFirestore.getInstance();
    }

    public void getProductsByTag(String tag, OnProductsLoadedListener listener) {
        db.collection("products")
                .whereArrayContains("tags", tag)
                .limit(40)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    List<Product> products = new ArrayList<>();
                    for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                        Product p = document.toObject(Product.class);
                        p.setId(document.getId());
                        products.add(p);
                    }
                    listener.onLoaded(products);
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error getting products by tag: " + tag, e);
                    listener.onError(e);
                });
    }

    public void getProductsByMultipleTags(List<String> tags, OnProductsLoadedListener listener) {
        if (tags == null || tags.isEmpty()) {
            listener.onLoaded(new ArrayList<>());
            return;
        }
        List<String> limitedTags = tags.size() > 10 ? tags.subList(0, 10) : tags;
        db.collection("products")
            .whereArrayContainsAny("tags", limitedTags)
            .limit(50)
            .get()
            .addOnSuccessListener(snapshots -> {
                Map<String, Product> productMap = new HashMap<>();
                for (QueryDocumentSnapshot doc : snapshots) {
                    Product p = doc.toObject(Product.class);
                    p.setId(doc.getId());
                    productMap.put(doc.getId(), p);
                }
                listener.onLoaded(new ArrayList<>(productMap.values()));
            })
            .addOnFailureListener(e -> {
                Log.e("FIRESTORE", "Query failed: " + e.getMessage());
                listener.onLoaded(new ArrayList<>());
            });
    }

    public void getProductsBySubCategory(String subCategoryId, OnProductsLoadedListener listener) {
        db.collection("products")
                .whereArrayContains("roomSubCategoryIds", subCategoryId)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    List<Product> products = new ArrayList<>();
                    for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                        Product p = document.toObject(Product.class);
                        p.setId(document.getId());
                        products.add(p);
                    }
                    listener.onLoaded(products);
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error getting products by subcategory: " + subCategoryId, e);
                    listener.onError(e);
                });
    }

    public void getProductsByMultipleSubCategories(List<String> subCategoryIds, OnProductsLoadedListener listener) {
        if (subCategoryIds == null || subCategoryIds.isEmpty()) {
            listener.onLoaded(new ArrayList<>());
            return;
        }
        List<String> limitedIds = subCategoryIds.size() > 10 ? subCategoryIds.subList(0, 10) : subCategoryIds;
        db.collection("products")
                .whereArrayContainsAny("roomSubCategoryIds", limitedIds)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    List<Product> products = new ArrayList<>();
                    for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                        Product p = document.toObject(Product.class);
                        p.setId(document.getId());
                        products.add(p);
                    }
                    listener.onLoaded(products);
                })
                .addOnFailureListener(listener::onError);
    }

    public void getProductById(String id, OnProductLoadedListener listener) {
        Log.d(TAG, "===> FETCHING ID: " + id);
        db.collection("products").document(id).get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        // DÒNG NÀY RẤT QUAN TRỌNG: Nó sẽ in ra mọi thứ đang có trên Firebase
                        Log.d(TAG, "===> RAW DATA FROM FIREBASE: " + documentSnapshot.getData());
                        
                        Product product = documentSnapshot.toObject(Product.class);
                        if (product != null) {
                            product.setId(documentSnapshot.getId());
                            Log.d(TAG, "===> MAPPED SUCCESS: " + product.getName());
                        } else {
                            Log.e(TAG, "===> MAPPING FAILED: Product object is null after toObject()");
                        }
                        listener.onLoaded(product);
                    } else {
                        Log.e(TAG, "===> DOCUMENT NOT FOUND for ID: " + id);
                        listener.onError(new Exception("Product not found"));
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "===> FIREBASE ERROR: " + e.getMessage(), e);
                    listener.onError(e);
                });
    }

    public void findCategoryIdByName(String name, OnIdFoundListener listener) {
        db.collection("roomCategories")
                .whereEqualTo("name", name)
                .limit(1)
                .get()
                .addOnSuccessListener(snapshots -> {
                    if (!snapshots.isEmpty()) {
                        listener.onIdFound(snapshots.getDocuments().get(0).getId());
                    } else {
                        listener.onIdFound(null);
                    }
                })
                .addOnFailureListener(e -> listener.onIdFound(null));
    }

    public void findSubCategoryIdByName(String name, OnIdFoundListener listener) {
        db.collection("roomSubCategories")
                .whereEqualTo("name", name)
                .limit(1)
                .get()
                .addOnSuccessListener(snapshots -> {
                    if (!snapshots.isEmpty()) {
                        listener.onIdFound(snapshots.getDocuments().get(0).getId());
                    } else {
                        listener.onIdFound(null);
                    }
                })
                .addOnFailureListener(e -> listener.onIdFound(null));
    }

    public void getProductsByResourceName(String name, OnProductsLoadedListener listener) {
        findSubCategoryIdByName(name, subId -> {
            if (subId != null) {
                getProductsBySubCategory(subId, listener);
            } else {
                findCategoryIdByName(name, catId -> {
                    if (catId != null) {
                        getRoomSubCategories(catId, new OnRoomSubCategoriesLoadedListener() {
                            @Override
                            public void onLoaded(List<RoomSubCategory> subCategories) {
                                if (subCategories != null && !subCategories.isEmpty()) {
                                    List<String> subIds = new ArrayList<>();
                                    for (RoomSubCategory sc : subCategories) subIds.add(sc.getId());
                                    getProductsByMultipleSubCategories(subIds, listener);
                                } else {
                                    searchProducts(name, listener);
                                }
                            }
                            @Override public void onError(Exception e) { searchProducts(name, listener); }
                        });
                    } else {
                        searchProducts(name, listener);
                    }
                });
            }
        });
    }

    public void getProductsByMultipleResourceNames(List<String> names, OnProductsLoadedListener listener) {
        List<String> allSubIds = Collections.synchronizedList(new ArrayList<>());
        java.util.concurrent.atomic.AtomicInteger pending = new java.util.concurrent.atomic.AtomicInteger(names.size());

        for (String name : names) {
            findSubCategoryIdByName(name, id -> {
                if (id != null) allSubIds.add(id);
                if (pending.decrementAndGet() == 0) {
                    if (allSubIds.isEmpty()) {
                        listener.onLoaded(new ArrayList<>());
                    } else {
                        getProductsByMultipleSubCategories(allSubIds, listener);
                    }
                }
            });
        }
    }

    public interface OnIdFoundListener {
        void onIdFound(String id);
    }

    public void getNewArrivals(OnProductsLoadedListener listener) {
        db.collection("products").whereEqualTo("isNew", true).limit(20).get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    List<Product> products = new ArrayList<>();
                    for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                        Product p = document.toObject(Product.class);
                        p.setId(document.getId());
                        products.add(p);
                    }
                    listener.onLoaded(products);
                })
                .addOnFailureListener(listener::onError);
    }

    public void getBestSellers(OnProductsLoadedListener listener) {
        db.collection("products").whereEqualTo("isBestSeller", true).limit(20).get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    List<Product> products = new ArrayList<>();
                    for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                        Product p = document.toObject(Product.class);
                        p.setId(document.getId());
                        products.add(p);
                    }
                    listener.onLoaded(products);
                })
                .addOnFailureListener(listener::onError);
    }

    public void getOnSaleProducts(OnProductsLoadedListener listener) {
        db.collection("products")
                .where(Filter.or(
                        Filter.equalTo("isOnSale", true),
                        Filter.arrayContains("tags", "flash_sale"),
                        Filter.arrayContains("tags", "sale")
                ))
                .limit(50)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    List<Product> products = new ArrayList<>();
                    for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                        Product p = document.toObject(Product.class);
                        p.setId(document.getId());
                        products.add(p);
                    }
                    listener.onLoaded(products);
                })
                .addOnFailureListener(listener::onError);
    }

    public void searchProducts(String query, OnProductsLoadedListener listener) {
        db.collection("products")
                .orderBy("name")
                .startAt(query)
                .endAt(query + "\uf8ff")
                .limit(20)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    List<Product> products = new ArrayList<>();
                    for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                        Product p = document.toObject(Product.class);
                        p.setId(document.getId());
                        products.add(p);
                    }
                    listener.onLoaded(products);
                })
                .addOnFailureListener(listener::onError);
    }

    public void getBanners(OnBannersLoadedListener listener) {
        db.collection("banners").get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    List<Banner> banners = new ArrayList<>();
                    for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                        Banner b = document.toObject(Banner.class);
                        if (document.contains("isActive")) {
                            Object active = document.get("isActive");
                            if (active instanceof Boolean && (Boolean) active) {
                                banners.add(b);
                            } else if (active instanceof String && ((String) active).equalsIgnoreCase("true")) {
                                banners.add(b);
                            }
                        } else {
                            banners.add(b);
                        }
                    }
                    Collections.sort(banners, (b1, b2) -> Integer.compare(b1.getOrder(), b2.getOrder()));
                    listener.onLoaded(banners);
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Lỗi lấy banner: " + e.getMessage());
                    listener.onError(e);
                });
    }

    public void getQuickTabs(OnQuickTabsLoadedListener listener) {
        db.collection("quickTabs").get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    List<QuickTab> quickTabs = new ArrayList<>();
                    for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                        quickTabs.add(document.toObject(QuickTab.class));
                    }
                    // Sắp xếp thủ công theo order nếu có, nếu không thì giữ nguyên
                    Collections.sort(quickTabs, (q1, q2) -> Integer.compare(q1.getOrder(), q2.getOrder()));
                    listener.onLoaded(quickTabs);
                })
                .addOnFailureListener(listener::onError);
    }

    public void getIdeas(OnIdeasLoadedListener listener) {
        db.collection("ideas").whereEqualTo("isActive", true).get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    List<Idea> ideas = new ArrayList<>();
                    for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                        Idea idea = document.toObject(Idea.class);
                        idea.setId(document.getId());
                        ideas.add(idea);
                    }
                    Collections.sort(ideas, (i1, i2) -> Integer.compare(i1.getOrder(), i2.getOrder()));
                    listener.onLoaded(ideas);
                })
                .addOnFailureListener(listener::onError);
    }

    public void getIdeasByCategory(String category, OnIdeasLoadedListener listener) {
        db.collection("ideas")
                .whereEqualTo("isActive", true)
                .whereEqualTo("groupLabel", category)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    List<Idea> ideas = new ArrayList<>();
                    for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                        Idea idea = document.toObject(Idea.class);
                        idea.setId(document.getId());
                        ideas.add(idea);
                    }
                    Collections.sort(ideas, (i1, i2) -> Integer.compare(i1.getOrder(), i2.getOrder()));
                    listener.onLoaded(ideas);
                })
                .addOnFailureListener(listener::onError);
    }

    public void getRoomCategories(OnRoomCategoriesLoadedListener listener) {
        db.collection("roomCategories").orderBy("order").get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    List<RoomCategory> categories = new ArrayList<>();
                    for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                        RoomCategory cat = document.toObject(RoomCategory.class);
                        cat.setId(document.getId()); 
                        categories.add(cat);
                    }
                    listener.onLoaded(categories);
                })
                .addOnFailureListener(listener::onError);
    }

    public void getRoomSubCategories(String roomCategoryId, OnRoomSubCategoriesLoadedListener listener) {
        db.collection("roomSubCategories")
                .whereEqualTo("roomCategoryId", roomCategoryId)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    List<RoomCategory> categories = new ArrayList<>();
                    List<RoomSubCategory> subCategories = new ArrayList<>();
                    for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                        RoomSubCategory subCat = document.toObject(RoomSubCategory.class);
                        subCat.setId(document.getId());
                        subCategories.add(subCat);
                    }
                    listener.onLoaded(subCategories);
                })
                .addOnFailureListener(listener::onError);
    }

    public void getUserProfile(String userId, OnUserProfileLoadedListener listener) {
        db.collection("users").document(userId).get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        UserProfile profile = documentSnapshot.toObject(UserProfile.class);
                        listener.onLoaded(profile);
                    } else {
                        listener.onLoaded(null);
                    }
                })
                .addOnFailureListener(listener::onError);
    }

    public void getReviewsByProductId(String productId, OnReviewsLoadedListener listener) {
        if (productId == null || productId.isEmpty()) {
            listener.onLoaded(new ArrayList<>());
            return;
        }
        
        String cleanId = productId.trim();
        Log.d(TAG, "===> FETCHING REVIEWS for product ID: [" + cleanId + "]");
        
        // Sử dụng collectionGroup để quét qua tất cả các sub-collection "reviews" của mọi user
        db.collectionGroup("reviews")
                .whereEqualTo("productId", cleanId)
                .get()
                .addOnSuccessListener(snapshots -> {
                    Log.d(TAG, "===> REVIEWS FETCH SUCCESS, count: " + snapshots.size());
                    processReviews(snapshots, listener);
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "===> REVIEWS FETCH ERROR: " + e.getMessage());
                    // Nếu lỗi do thiếu Index, Firebase sẽ trả về một link trong Logcat, hãy click vào đó
                    listener.onError(e);
                });
    }

    private void processReviews(com.google.firebase.firestore.QuerySnapshot snapshots, OnReviewsLoadedListener listener) {
        List<Review> reviews = new ArrayList<>();
        for (QueryDocumentSnapshot document : snapshots) {
            Log.d(TAG, "===> REVIEW DATA FOUND: " + document.getData());
            Review review = document.toObject(Review.class);
            reviews.add(review);
        }
        listener.onLoaded(reviews);
    }

    public void saveUserProfile(String userId, UserProfile profile, OnProfileSavedListener listener) {
        db.collection("users").document(userId).set(profile)
                .addOnSuccessListener(aVoid -> listener.onSuccess())
                .addOnFailureListener(listener::onError);
    }

    public interface OnProductsLoadedListener {
        void onLoaded(List<Product> products);
        void onError(Exception e);
    }

    public interface OnBannersLoadedListener {
        void onLoaded(List<Banner> banners);
        void onError(Exception e);
    }

    public interface OnQuickTabsLoadedListener {
        void onLoaded(List<QuickTab> quickTabs);
        void onError(Exception e);
    }

    public interface OnRoomCategoriesLoadedListener {
        void onLoaded(List<RoomCategory> categories);
        void onError(Exception e);
    }

    public interface OnRoomSubCategoriesLoadedListener {
        void onLoaded(List<RoomSubCategory> subCategories);
        void onError(Exception e);
    }

    public interface OnIdeasLoadedListener {
        void onLoaded(List<Idea> ideas);
        void onError(Exception e);
    }

    public interface OnProductLoadedListener {
        void onLoaded(Product product);
        void onError(Exception e);
    }

    public interface OnUserProfileLoadedListener {
        void onLoaded(UserProfile profile);
        void onError(Exception e);
    }

    public interface OnProfileSavedListener {
        void onSuccess();
        void onError(Exception e);
    }

    public interface OnReviewsLoadedListener {
        void onLoaded(List<Review> reviews);
        void onError(Exception e);
    }
}
