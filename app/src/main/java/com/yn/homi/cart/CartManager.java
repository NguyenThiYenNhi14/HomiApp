package com.yn.homi.cart;

import android.content.Context;
import android.content.SharedPreferences;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.yn.homi.model.CartItem;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

/**
 * Singleton quản lý danh sách CartItem trong SharedPreferences.
 * Dùng CartManager.getInstance(context) từ bất kỳ đâu để add/remove/update.
 */
public class CartManager {

    private static final String PREF_NAME = "homi_cart_prefs";
    private static final String KEY_CART_ITEMS = "cart_items";

    private static CartManager instance;
    private final List<CartItem> items;
    private final SharedPreferences sharedPreferences;
    private final Gson gson;

    // Listener để CartActivity cập nhật UI khi cart thay đổi từ màn hình khác
    public interface CartChangeListener {
        void onCartChanged();
    }
    private CartChangeListener listener;

    private CartManager(Context context) {
        sharedPreferences = context.getApplicationContext().getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        gson = new Gson();
        items = loadCartItems();
    }

    public static synchronized CartManager getInstance(Context context) {
        if (instance == null) {
            instance = new CartManager(context);
        }
        return instance;
    }

    public void setCartChangeListener(CartChangeListener l) { this.listener = l; }

    /** Thêm sản phẩm. Nếu đã có thì tăng quantity. */
    public void addItem(CartItem newItem) {
        boolean found = false;
        for (CartItem item : items) {
            if (item.getId().equals(newItem.getId())) {
                item.setQuantity(item.getQuantity() + newItem.getQuantity());
                found = true;
                break;
            }
        }
        if (!found) {
            items.add(newItem);
        }
        saveCartItems();
        notifyChanged();
    }

    /** Xoá một item khỏi giỏ. */
    public void removeItem(String itemId) {
        items.removeIf(item -> item.getId().equals(itemId));
        saveCartItems();
        notifyChanged();
    }

    /** Cập nhật quantity; nếu quantity <= 0 thì xoá. */
    public void updateQuantity(String itemId, int newQty) {
        if (newQty <= 0) {
            removeItem(itemId);
            return;
        }
        for (CartItem item : items) {
            if (item.getId().equals(itemId)) {
                item.setQuantity(newQty);
                saveCartItems();
                notifyChanged();
                return;
            }
        }
    }

    public List<CartItem> getItems() { return items; }

    public int getTotalItemCount() {
        int count = 0;
        for (CartItem item : items) count += item.getQuantity();
        return count;
    }

    public double getSubTotal() {
        double total = 0;
        for (CartItem item : items) total += item.getPrice() * item.getQuantity();
        return total;
    }

    public double getShipping() {
        // Shipping is FREE as requested
        return 0.0;
    }

    public double getOrderTotal() {
        return getSubTotal() + getShipping();
    }

    public void clear() {
        items.clear();
        saveCartItems();
        notifyChanged();
    }

    private void saveCartItems() {
        String json = gson.toJson(items);
        sharedPreferences.edit().putString(KEY_CART_ITEMS, json).apply();
    }

    private List<CartItem> loadCartItems() {
        String json = sharedPreferences.getString(KEY_CART_ITEMS, null);
        if (json == null) {
            return new ArrayList<>();
        }
        Type type = new TypeToken<List<CartItem>>() {}.getType();
        return gson.fromJson(json, type);
    }

    private void notifyChanged() {
        if (listener != null) listener.onCartChanged();
    }
}