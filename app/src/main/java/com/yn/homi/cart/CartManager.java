package com.yn.homi.cart;

import com.yn.homi.model.CartItem;

import java.util.ArrayList;
import java.util.List;

/**
 * Singleton quản lý danh sách CartItem trong session.
 * Dùng CartManager.getInstance() từ bất kỳ đâu để add/remove/update.
 */
public class CartManager {

    private static CartManager instance;
    private final List<CartItem> items = new ArrayList<>();

    // Listener để CartActivity cập nhật UI khi cart thay đổi từ màn hình khác
    public interface CartChangeListener {
        void onCartChanged();
    }
    private CartChangeListener listener;

    private CartManager() {}

    public static CartManager getInstance() {
        if (instance == null) instance = new CartManager();
        return instance;
    }

    public void setCartChangeListener(CartChangeListener l) { this.listener = l; }

    /** Thêm sản phẩm. Nếu đã có thì tăng quantity. */
    public void addItem(CartItem newItem) {
        for (CartItem item : items) {
            if (item.getId().equals(newItem.getId())) {
                item.setQuantity(item.getQuantity() + newItem.getQuantity());
                notifyChanged();
                return;
            }
        }
        items.add(newItem);
        notifyChanged();
    }

    /** Xoá một item khỏi giỏ. */
    public void removeItem(String itemId) {
        items.removeIf(item -> item.getId().equals(itemId));
        notifyChanged();
    }

    /** Cập nhật quantity; nếu quantity <= 0 thì xoá. */
    public void updateQuantity(String itemId, int newQty) {
        if (newQty <= 0) { removeItem(itemId); return; }
        for (CartItem item : items) {
            if (item.getId().equals(itemId)) {
                item.setQuantity(newQty);
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

    public void clear() { items.clear(); notifyChanged(); }

    private void notifyChanged() {
        if (listener != null) listener.onCartChanged();
    }
}