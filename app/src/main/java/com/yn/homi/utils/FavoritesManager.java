package com.yn.homi.utils;

import android.content.Context;
import android.content.SharedPreferences;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.yn.homi.models.Product;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

public class FavoritesManager {
    private static final String PREF_NAME = "homi_favorites";
    private static final String KEY_FAVORITES = "favorite_items";
    private SharedPreferences sharedPreferences;
    private Gson gson;

    public FavoritesManager(Context context) {
        sharedPreferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        gson = new Gson();
    }

    public List<Product> getFavorites() {
        String json = sharedPreferences.getString(KEY_FAVORITES, null);
        if (json == null) {
            return new ArrayList<>();
        }
        Type type = new TypeToken<List<Product>>() {}.getType();
        return gson.fromJson(json, type);
    }

    public void toggleFavorite(Product product) {
        List<Product> favorites = getFavorites();
        boolean removed = false;
        for (int i = 0; i < favorites.size(); i++) {
            if (favorites.get(i).getId().equals(product.getId())) {
                favorites.remove(i);
                removed = true;
                break;
            }
        }
        if (!removed) {
            favorites.add(product);
        }
        saveFavorites(favorites);
    }

    public boolean isFavorite(String productId) {
        List<Product> favorites = getFavorites();
        for (Product p : favorites) {
            if (p.getId().equals(productId)) {
                return true;
            }
        }
        return false;
    }

    private void saveFavorites(List<Product> favorites) {
        String json = gson.toJson(favorites);
        sharedPreferences.edit().putString(KEY_FAVORITES, json).apply();
    }
}
