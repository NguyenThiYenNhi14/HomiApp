package com.yn.homi.utils;

import android.content.Context;
import android.content.SharedPreferences;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.yn.homi.models.Product;
import com.yn.homi.models.Wishlist;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

public class FavoritesManager {
    private static final String PREF_NAME = "homi_favorites";
    private static final String KEY_WISHLISTS = "wishlists";
    private SharedPreferences sharedPreferences;
    private Gson gson;

    public FavoritesManager(Context context) {
        sharedPreferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        gson = new Gson();
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
        String json = gson.toJson(wishlists);
        sharedPreferences.edit().putString(KEY_WISHLISTS, json).apply();
    }

    public void addProductToWishlist(String wishlistName, Product product) {
        List<Wishlist> wishlists = getWishlists();
        for (Wishlist w : wishlists) {
            if (w.getName().equals(wishlistName)) {
                w.addProduct(product);
                saveWishlists(wishlists);
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
        saveWishlists(wishlists);
    }

    public boolean isFavorite(String productId) {
        List<Wishlist> wishlists = getWishlists();
        for (Wishlist w : wishlists) {
            for (Product p : w.getItems()) {
                if (p.getId().equals(productId)) {
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
                saveWishlists(wishlists);
                return;
            }
        }
    }

    public void removeProductFromAllWishlists(String productId) {
        List<Wishlist> wishlists = getWishlists();
        for (Wishlist w : wishlists) {
            w.removeProduct(productId);
        }
        saveWishlists(wishlists);
    }

    public void deleteWishlist(String wishlistName) {
        List<Wishlist> wishlists = getWishlists();
        for (int i = 0; i < wishlists.size(); i++) {
            if (wishlists.get(i).getName().equals(wishlistName)) {
                wishlists.remove(i);
                saveWishlists(wishlists);
                return;
            }
        }
    }

    public void renameWishlist(String oldName, String newName) {
        List<Wishlist> wishlists = getWishlists();
        for (Wishlist w : wishlists) {
            if (w.getName().equals(oldName)) {
                w.setName(newName);
                saveWishlists(wishlists);
                return;
            }
        }
    }
}
