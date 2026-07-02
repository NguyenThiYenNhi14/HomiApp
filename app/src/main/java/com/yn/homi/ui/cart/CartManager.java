package com.yn.homi.ui.cart;

import android.content.Context;
import android.content.SharedPreferences;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.yn.homi.data.model.CartItem;

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

    /** Thêm sản phẩm. Nếu đã có cùng ID, cùng màu và cùng size thì tăng quantity. */
    public void addItem(CartItem newItem) {
        addItemInternal(newItem);
        saveCartItems();
        notifyChanged();
    }

    public void addItems(List<CartItem> newItems) {
        for (CartItem item : newItems) {
            addItemInternal(item);
        }
        saveCartItems();
        notifyChanged();
    }

    private void addItemInternal(CartItem newItem) {
        boolean found = false;
        for (CartItem item : items) {
            // Kiểm tra cả ID, Màu sắc và Kích thước
            boolean sameId = item.getId().equals(newItem.getId());
            boolean sameColor = (item.getSelectedColor() == null && newItem.getSelectedColor() == null) ||
                               (item.getSelectedColor() != null && item.getSelectedColor().equals(newItem.getSelectedColor()));
            boolean sameSize = (item.getSelectedSize() == null && newItem.getSelectedSize() == null) ||
                              (item.getSelectedSize() != null && item.getSelectedSize().equals(newItem.getSelectedSize()));

            if (sameId && sameColor && sameSize) {
                item.setQuantity(item.getQuantity() + newItem.getQuantity());
                found = true;
                break;
            }
        }
        if (!found) {
            items.add(newItem);
        }
    }

    /** Xoá một item cụ thể dựa trên ID, màu sắc và size. */
    public void removeItem(String itemId, String color, String size) {
        items.removeIf(item -> item.getId().equals(itemId) &&
                ((item.getSelectedColor() == null && color == null) ||
                 (item.getSelectedColor() != null && item.getSelectedColor().equals(color))) &&
                ((item.getSelectedSize() == null && size == null) ||
                 (item.getSelectedSize() != null && item.getSelectedSize().equals(size))));
        saveCartItems();
        notifyChanged();
    }

    /** Cập nhật quantity cho một item cụ thể. */
    public void updateQuantity(String itemId, String color, String size, int newQty) {
        if (newQty <= 0) {
            removeItem(itemId, color, size);
            return;
        }
        for (CartItem item : items) {
            boolean sameId = item.getId().equals(itemId);
            boolean sameColor = (item.getSelectedColor() == null && color == null) ||
                               (item.getSelectedColor() != null && item.getSelectedColor().equals(color));
            boolean sameSize = (item.getSelectedSize() == null && size == null) ||
                              (item.getSelectedSize() != null && item.getSelectedSize().equals(size));
            
            if (sameId && sameColor && sameSize) {
                item.setQuantity(newQty);
                saveCartItems();
                notifyChanged();
                return;
            }
        }
    }

    /** Cập nhật thông tin variant (màu/size/image) cho một item. */
    public void updateItemVariant(CartItem oldItem, String newColor, String newSize, int newQty, String newImageUrl) {
        // Tìm item cũ và xoá đi (vì khi đổi variant nó có thể gộp vào item khác đã có variant đó)
        removeItem(oldItem.getId(), oldItem.getSelectedColor(), oldItem.getSelectedSize());
        
        // Thêm item mới với variant và ảnh mới
        CartItem updatedItem = new CartItem(oldItem.getId(), oldItem.getName(), oldItem.getPrice(), newQty, newImageUrl, newColor, newSize);
        addItem(updatedItem);
    }

    public List<CartItem> getItems() { return items; }

    public int getTotalItemCount() {
        int count = 0;
        for (CartItem item : items) count += item.getQuantity();
        return count;
    }

    public double getSubTotal() {
        double total = 0;
        for (CartItem item : items) {
            if (item.isSelected()) {
                total += item.getPrice() * item.getQuantity();
            }
        }
        return total;
    }

    public int getSelectedCount() {
        int count = 0;
        for (CartItem item : items) {
            if (item.isSelected()) count += item.getQuantity();
        }
        return count;
    }

    public void setAllSelected(boolean selected) {
        for (CartItem item : items) {
            item.setSelected(selected);
        }
        saveCartItems();
        notifyChanged();
    }

    public void updateItemSelection(String itemId, String color, String size, boolean isSelected) {
        for (CartItem item : items) {
            boolean sameId = item.getId().equals(itemId);
            boolean sameColor = (item.getSelectedColor() == null && color == null) ||
                    (item.getSelectedColor() != null && item.getSelectedColor().equals(color));
            boolean sameSize = (item.getSelectedSize() == null && size == null) ||
                    (item.getSelectedSize() != null && item.getSelectedSize().equals(size));

            if (sameId && sameColor && sameSize) {
                item.setSelected(isSelected);
                saveCartItems();
                // Chúng ta không nhất thiết phải notifyChanged ở đây nếu adapter đã tự update UI,
                // nhưng gọi để đảm bảo các thành phần khác (nếu có) cũng đồng bộ.
                // Tuy nhiên, CartActivity đang lắng nghe và sẽ update summary.
                notifyChanged();
                return;
            }
        }
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
