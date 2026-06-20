package com.yn.homi.utils;

import android.content.Context;
import android.content.SharedPreferences;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.yn.homi.models.CartItem;
import com.yn.homi.models.Product;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

public class CartManager {
    private static final String PREF_NAME = "homi_cart";
    private static final String KEY_CART_ITEMS = "cart_items";
    private SharedPreferences sharedPreferences;
    private Gson gson;

    public CartManager(Context context) {
        sharedPreferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        gson = new Gson();
    }

    public List<CartItem> getCartItems() {
        String json = sharedPreferences.getString(KEY_CART_ITEMS, null);
        if (json == null) {
            return new ArrayList<>();
        }
        Type type = new TypeToken<List<CartItem>>() {}.getType();
        return gson.fromJson(json, type);
    }

    public void addToCart(Product product, int quantity) {
        List<CartItem> items = getCartItems();
        boolean exists = false;
        for (CartItem item : items) {
            if (item.getProduct().getId().equals(product.getId())) {
                item.setQuantity(item.getQuantity() + quantity);
                exists = true;
                break;
            }
        }
        if (!exists) {
            items.add(new CartItem(product, quantity));
        }
        saveCartItems(items);
    }

    public void removeFromCart(String productId) {
        List<CartItem> items = getCartItems();
        for (int i = 0; i < items.size(); i++) {
            if (items.get(i).getProduct().getId().equals(productId)) {
                items.remove(i);
                break;
            }
        }
        saveCartItems(items);
    }

    public void updateQuantity(String productId, int quantity) {
        List<CartItem> items = getCartItems();
        for (CartItem item : items) {
            if (item.getProduct().getId().equals(productId)) {
                item.setQuantity(quantity);
                break;
            }
        }
        saveCartItems(items);
    }

    private void saveCartItems(List<CartItem> items) {
        String json = gson.toJson(items);
        sharedPreferences.edit().putString(KEY_CART_ITEMS, json).apply();
    }

    public int getCartCount() {
        int count = 0;
        for (CartItem item : getCartItems()) {
            count += item.getQuantity();
        }
        return count;
    }
}
