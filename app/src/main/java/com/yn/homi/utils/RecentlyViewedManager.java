package com.yn.homi.utils;

import android.content.Context;
import android.content.SharedPreferences;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.yn.homi.data.model.Product;
import com.yn.homi.data.repository.FirestoreRepository;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class RecentlyViewedManager {
    private static final String PREF_NAME = "homi_recently_viewed";
    private static final String KEY_PRODUCTS = "recently_viewed_products";
    private static final int MAX_ITEMS = 20;
    
    private SharedPreferences sharedPreferences;
    private Gson gson;
    private FirebaseFirestore db;
    private FirebaseAuth auth;
    private FirestoreRepository firestoreRepository;

    public RecentlyViewedManager(Context context) {
        sharedPreferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        gson = new Gson();
        db = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();
        firestoreRepository = new FirestoreRepository();
    }

    public void addProduct(Product product) {
        if (product == null) return;
        
        List<Product> products = getRecentlyViewed();
        // Remove if already exists to move to top
        for (int i = 0; i < products.size(); i++) {
            if (products.get(i).getId().equals(product.getId())) {
                products.remove(i);
                break;
            }
        }
        
        // Add to beginning
        products.add(0, product);
        
        // Limit size
        if (products.size() > MAX_ITEMS) {
            products = products.subList(0, MAX_ITEMS);
        }
        
        saveProducts(products);

        String uid = auth.getCurrentUser() != null ? auth.getCurrentUser().getUid() : null;
        if (uid != null) {
            Map<String, Object> data = new HashMap<>();
            data.put("productId", product.getId());
            data.put("name", product.getName());
            data.put("price", product.getPrice());
            data.put("imageUrl", product.getThumbnailUrl());
            data.put("viewedAt", com.google.firebase.Timestamp.now());

            db.collection("users").document(uid)
                    .collection("recentlyBrowsed").document(product.getId())
                    .set(data);
        }
    }

    public List<Product> getRecentlyViewed() {
        String json = sharedPreferences.getString(KEY_PRODUCTS, null);
        if (json == null) {
            return new ArrayList<>();
        }
        Type type = new TypeToken<List<Product>>() {}.getType();
        return gson.fromJson(json, type);
    }

    private void saveProducts(List<Product> products) {
        String json = gson.toJson(products);
        sharedPreferences.edit().putString(KEY_PRODUCTS, json).apply();
    }

    public void clear() {
        sharedPreferences.edit().remove(KEY_PRODUCTS).apply();
    }

    public interface OnSyncListener {
        void onSynced(List<Product> products);
    }

    public void syncFromFirestore(OnSyncListener listener) {
        String uid = auth.getCurrentUser() != null ? auth.getCurrentUser().getUid() : null;
        if (uid == null) {
            if (listener != null) listener.onSynced(getRecentlyViewed());
            return;
        }

        db.collection("users").document(uid).collection("recentlyBrowsed")
                .orderBy("viewedAt", Query.Direction.DESCENDING)
                .limit(MAX_ITEMS)
                .get()
                .addOnSuccessListener(snapshot -> {
                    List<String> productIds = new ArrayList<>();
                    for (com.google.firebase.firestore.DocumentSnapshot doc : snapshot.getDocuments()) {
                        String pid = doc.getString("productId");
                        if (pid != null) productIds.add(pid);
                    }
                    fetchFullProducts(productIds, listener);
                })
                .addOnFailureListener(e -> {
                    if (listener != null) listener.onSynced(getRecentlyViewed());
                });
    }

    private void fetchFullProducts(List<String> productIds, OnSyncListener listener) {
        List<Product> result = new ArrayList<>();
        if (productIds.isEmpty()) {
            saveProducts(result);
            if (listener != null) listener.onSynced(result);
            return;
        }

        int[] remaining = {productIds.size()};
        Product[] resultsArray = new Product[productIds.size()];

        for (int i = 0; i < productIds.size(); i++) {
            final int index = i;
            firestoreRepository.getProductById(productIds.get(i), new FirestoreRepository.OnProductLoadedListener() {
                @Override
                public void onLoaded(Product p) {
                    resultsArray[index] = p;
                    remaining[0]--;
                    if (remaining[0] == 0) finishSync(resultsArray, listener);
                }
                @Override
                public void onError(Exception e) {
                    remaining[0]--;
                    if (remaining[0] == 0) finishSync(resultsArray, listener);
                }
            });
        }
    }

    private void finishSync(Product[] resultsArray, OnSyncListener listener) {
        List<Product> result = new ArrayList<>();
        for (Product p : resultsArray) if (p != null) result.add(p);
        saveProducts(result);
        if (listener != null) listener.onSynced(result);
    }
}
