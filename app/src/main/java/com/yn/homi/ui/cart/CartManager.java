package com.yn.homi.ui.cart;

import android.content.Context;
import android.content.SharedPreferences;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.yn.homi.data.model.CartItem;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Singleton quản lý danh sách CartItem trong SharedPreferences và Firestore.
 */
public class CartManager {

    private static final String PREF_NAME = "homi_cart_prefs";
    private static final String KEY_CART_ITEMS = "cart_items";

    private static CartManager instance;
    private final List<CartItem> items;
    private final SharedPreferences sharedPreferences;
    private final Gson gson;
    private final FirebaseFirestore db;
    private final FirebaseAuth auth;

    // Listener để CartActivity cập nhật UI khi cart thay đổi từ màn hình khác
    public interface CartChangeListener {
        void onCartChanged();
    }
    private CartChangeListener listener;

    private CartManager(Context context) {
        sharedPreferences = context.getApplicationContext().getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        gson = new Gson();
        db = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();
        items = loadCartItems();
    }

    public static synchronized CartManager getInstance(Context context) {
        if (instance == null) {
            instance = new CartManager(context);
        }
        return instance;
    }

    public void setCartChangeListener(CartChangeListener listener) {
        this.listener = listener;
    }

    private String getUserId() {
        return auth.getCurrentUser() != null ? auth.getCurrentUser().getUid() : null;
    }

    private String getDocId(CartItem item) {
        String color = item.getSelectedColor() != null ? item.getSelectedColor() : "none";
        String size = item.getSelectedSize() != null ? item.getSelectedSize() : "none";
        return item.getId() + "_" + color + "_" + size;
    }

    public void syncFromFirestore() {
        String uid = getUserId();
        if (uid == null) return;

        // 1. Push local items to Firestore (Migration/Sync)
        for (CartItem item : new ArrayList<>(items)) {
            updateFirestore(item);
        }

        // 2. Pull all items from Firestore to get the full picture
        db.collection("users").document(uid).collection("cart")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    items.clear();
                    for (DocumentSnapshot doc : queryDocumentSnapshots) {
                        CartItem item = doc.toObject(CartItem.class);
                        if (item != null) {
                            items.add(item);
                        }
                    }
                    saveCartItemsLocal();
                    notifyChanged();
                });
    }

    private void updateFirestore(CartItem item) {
        String uid = getUserId();
        if (uid == null) return;

        // Không đồng bộ trạng thái isSelected theo yêu cầu
        Map<String, Object> data = new HashMap<>();
        data.put("id", item.getId());
        data.put("name", item.getName());
        data.put("price", item.getPrice());
        data.put("quantity", item.getQuantity());
        data.put("imageUrl", item.getImageUrl());
        data.put("selectedColor", item.getSelectedColor());
        data.put("selectedSize", item.getSelectedSize());

        db.collection("users").document(uid).collection("cart")
                .document(getDocId(item))
                .set(data);
    }

    private void removeFromFirestore(String itemId, String color, String size) {
        String uid = getUserId();
        if (uid == null) return;

        String colorId = color != null ? color : "none";
        String sizeId = size != null ? size : "none";
        String docId = itemId + "_" + colorId + "_" + sizeId;

        db.collection("users").document(uid).collection("cart")
                .document(docId)
                .delete();
    }

    /** Thêm sản phẩm. Nếu đã có cùng ID, cùng màu và cùng size thì tăng quantity. */
    public void addItem(CartItem newItem) {
        CartItem targetItem = null;
        for (CartItem item : items) {
            boolean sameId = item.getId().equals(newItem.getId());
            boolean sameColor = (item.getSelectedColor() == null && newItem.getSelectedColor() == null) ||
                               (item.getSelectedColor() != null && item.getSelectedColor().equals(newItem.getSelectedColor()));
            boolean sameSize = (item.getSelectedSize() == null && newItem.getSelectedSize() == null) ||
                              (item.getSelectedSize() != null && item.getSelectedSize().equals(newItem.getSelectedSize()));

            if (sameId && sameColor && sameSize) {
                item.setQuantity(item.getQuantity() + newItem.getQuantity());
                targetItem = item;
                break;
            }
        }
        if (targetItem == null) {
            items.add(newItem);
            targetItem = newItem;
        }
        saveCartItemsLocal();
        updateFirestore(targetItem);
        notifyChanged();
    }

    /** Xoá một item cụ thể dựa trên ID, màu sắc và size. */
    public void removeItem(String itemId, String color, String size) {
        items.removeIf(item -> item.getId().equals(itemId) &&
                ((item.getSelectedColor() == null && color == null) ||
                 (item.getSelectedColor() != null && item.getSelectedColor().equals(color))) &&
                ((item.getSelectedSize() == null && size == null) ||
                 (item.getSelectedSize() != null && item.getSelectedSize().equals(size))));
        saveCartItemsLocal();
        removeFromFirestore(itemId, color, size);
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
                saveCartItemsLocal();
                updateFirestore(item);
                notifyChanged();
                return;
            }
        }
    }

    /** Cập nhật thông tin variant (màu/size/image) cho một item. */
    public void updateItemVariant(CartItem oldItem, String newColor, String newSize, int newQty, String newImageUrl) {
        // Xoá item cũ khỏi Firestore và Local
        removeItem(oldItem.getId(), oldItem.getSelectedColor(), oldItem.getSelectedSize());
        
        // Thêm item mới (addItem sẽ lo việc update Firestore)
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
        saveCartItemsLocal();
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
                saveCartItemsLocal();
                notifyChanged();
                return;
            }
        }
    }

    public double getShipping() {
        return 0.0;
    }

    public double getOrderTotal() {
        return getSubTotal() + getShipping();
    }

    public void clear() {
        String uid = getUserId();
        if (uid != null) {
            for (CartItem item : items) {
                db.collection("users").document(uid).collection("cart")
                        .document(getDocId(item)).delete();
            }
        }
        items.clear();
        saveCartItemsLocal();
        notifyChanged();
    }

    private void saveCartItemsLocal() {
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
