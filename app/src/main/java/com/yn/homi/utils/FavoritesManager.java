package com.yn.homi.utils;

import android.content.Context;
import android.content.SharedPreferences;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.yn.homi.data.model.Product;
import com.yn.homi.data.model.Wishlist;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class FavoritesManager {
    private static final String PREF_NAME = "homi_favorites";
    private static final String KEY_WISHLISTS = "wishlists";
    private SharedPreferences sharedPreferences;
    private Gson gson;
    private final FirebaseFirestore db;
    private final FirebaseAuth auth;

    public FavoritesManager(Context context) {
        sharedPreferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        gson = new Gson();
        db = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();
    }

    private String getUserId() {
        return auth.getCurrentUser() != null ? auth.getCurrentUser().getUid() : null;
    }

    public void syncFromFirestore() {
        String uid = getUserId();
        if (uid == null) return;

        // Push local wishlists to Firestore first
        List<Wishlist> localWishlists = getWishlists();
        for (Wishlist w : localWishlists) {
            updateFirestoreWishlist(w);
            for (Product p : w.getItems()) {
                updateFirestoreProduct(w.getName(), p);
            }
        }

        db.collection("users").document(uid).collection("wishlists")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    List<Wishlist> remoteWishlists = new ArrayList<>();
                    int totalWishlists = queryDocumentSnapshots.size();
                    if (totalWishlists == 0) return;

                    final int[] loadedCount = {0};
                    for (DocumentSnapshot wishlistDoc : queryDocumentSnapshots) {
                        Wishlist wishlist = new Wishlist(wishlistDoc.getString("name"));
                        
                        wishlistDoc.getReference().collection("items")
                                .get()
                                .addOnSuccessListener(itemSnapshots -> {
                                    List<Product> items = new ArrayList<>();
                                    for (DocumentSnapshot itemDoc : itemSnapshots) {
                                        Product product = itemDoc.toObject(Product.class);
                                        if (product != null) {
                                            items.add(product);
                                        }
                                    }
                                    wishlist.setItems(items);
                                    remoteWishlists.add(wishlist);
                                    
                                    loadedCount[0]++;
                                    if (loadedCount[0] == totalWishlists) {
                                        saveWishlistsLocal(remoteWishlists);
                                    }
                                });
                    }
                });
    }

    private void updateFirestoreWishlist(Wishlist wishlist) {
        String uid = getUserId();
        if (uid == null) return;

        Map<String, Object> data = new HashMap<>();
        data.put("name", wishlist.getName());

        db.collection("users").document(uid).collection("wishlists")
                .document(wishlist.getName()) // Using name as ID for simplicity
                .set(data);
    }

    private void updateFirestoreProduct(String wishlistName, Product product) {
        String uid = getUserId();
        if (uid == null) return;

        db.collection("users").document(uid).collection("wishlists")
                .document(wishlistName)
                .collection("items")
                .document(product.getId())
                .set(product);
    }

    private void removeFromFirestoreProduct(String wishlistName, String productId) {
        String uid = getUserId();
        if (uid == null) return;

        db.collection("users").document(uid).collection("wishlists")
                .document(wishlistName)
                .collection("items")
                .document(productId)
                .delete();
    }

    public List<Wishlist> getWishlists() {
        String json = sharedPreferences.getString(KEY_WISHLISTS, null);
        if (json == null) {
            return new ArrayList<>();
        }
        Type type = new TypeToken<List<Wishlist>>() {}.getType();
        return gson.fromJson(json, type);
    }

    public void saveWishlists(List<Wishlist> wishlists) {
        saveWishlistsLocal(wishlists);
        // This is called when reordering or renaming. 
        // For simple additions/deletions, we have specific methods.
        for (Wishlist w : wishlists) {
            updateFirestoreWishlist(w);
            for (Product p : w.getItems()) {
                updateFirestoreProduct(w.getName(), p);
            }
        }
    }

    private void saveWishlistsLocal(List<Wishlist> wishlists) {
        String json = gson.toJson(wishlists);
        sharedPreferences.edit().putString(KEY_WISHLISTS, json).apply();
    }

    public void addProductToWishlist(String wishlistName, Product product) {
        List<Wishlist> wishlists = getWishlists();
        for (Wishlist w : wishlists) {
            if (w.getName().equals(wishlistName)) {
                w.addProduct(product);
                saveWishlistsLocal(wishlists);
                updateFirestoreProduct(wishlistName, product);
                return;
            }
        }
    }

    public void createWishlistAndAddProduct(String name, Product product) {
        List<Wishlist> wishlists = getWishlists();
        Wishlist newList = new Wishlist(name);
        if (product != null) {
            newList.addProduct(product);
        }
        wishlists.add(newList);
        saveWishlistsLocal(wishlists);
        
        updateFirestoreWishlist(newList);
        if (product != null) {
            updateFirestoreProduct(name, product);
        }
    }

    public boolean isFavorite(String productId) {
        if (productId == null || productId.isEmpty()) return false;
        List<Wishlist> wishlists = getWishlists();
        if (wishlists == null) return false;
        
        for (Wishlist w : wishlists) {
            if (w == null || w.getItems() == null) continue;
            for (Product p : w.getItems()) {
                if (p != null && p.getId() != null && productId.equals(p.getId())) {
                    return true;
                }
            }
        }
        return false;
    }

    public void removeProductFromWishlist(String wishlistName, String productId) {
        List<Wishlist> wishlists = getWishlists();
        for (Wishlist w : wishlists) {
            if (w.getName().equals(wishlistName)) {
                w.removeProduct(productId);
                saveWishlistsLocal(wishlists);
                removeFromFirestoreProduct(wishlistName, productId);
                return;
            }
        }
    }

    public void removeProductFromAllWishlists(String productId) {
        List<Wishlist> wishlists = getWishlists();
        for (Wishlist w : wishlists) {
            w.removeProduct(productId);
            removeFromFirestoreProduct(w.getName(), productId);
        }
        saveWishlistsLocal(wishlists);
    }

    public void deleteWishlist(String wishlistName) {
        List<Wishlist> wishlists = getWishlists();
        for (int i = 0; i < wishlists.size(); i++) {
            if (wishlists.get(i).getName().equals(wishlistName)) {
                wishlists.remove(i);
                saveWishlistsLocal(wishlists);
                
                String uid = getUserId();
                if (uid != null) {
                    db.collection("users").document(uid).collection("wishlists")
                            .document(wishlistName).delete();
                }
                return;
            }
        }
    }

    public void renameWishlist(String oldName, String newName) {
        List<Wishlist> wishlists = getWishlists();
        Wishlist target = null;
        for (Wishlist w : wishlists) {
            if (w.getName().equals(oldName)) {
                w.setName(newName);
                target = w;
                break;
            }
        }
        if (target != null) {
            saveWishlistsLocal(wishlists);
            
            // In Firestore, renaming a document usually means delete old and create new
            // For simplicity here, we'll just create the new one and the user can decide
            // how to handle item migration.
            updateFirestoreWishlist(target);
            for (Product p : target.getItems()) {
                updateFirestoreProduct(newName, p);
            }
            
            String uid = getUserId();
            if (uid != null) {
                db.collection("users").document(uid).collection("wishlists")
                        .document(oldName).delete();
            }
        }
    }
}
