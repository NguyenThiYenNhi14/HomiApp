package com.yn.homi.ui.profile.order;

import android.content.Context;
import android.content.SharedPreferences;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

public class OrderManager {
    private static final String PREF_NAME = "homi_order_prefs";
    private static final String KEY_ORDERS = "orders";

    private static OrderManager instance;
    private final SharedPreferences sharedPreferences;
    private final Gson gson;
    private final List<Order> orders;
    private final FirebaseFirestore db;
    private final FirebaseAuth auth;

    public interface OrderChangeListener {
        void onOrdersChanged();
    }
    private final List<OrderChangeListener> listeners = new ArrayList<>();

    private OrderManager(Context context) {
        sharedPreferences = context.getApplicationContext().getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        gson = new Gson();
        db = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();
        orders = loadOrders();
    }

    public static synchronized OrderManager getInstance(Context context) {
        if (instance == null) {
            instance = new OrderManager(context);
        }
        return instance;
    }

    public void addOrderChangeListener(OrderChangeListener listener) {
        if (!listeners.contains(listener)) {
            listeners.add(listener);
        }
    }

    public void removeOrderChangeListener(OrderChangeListener listener) {
        listeners.remove(listener);
    }

    private String getUserId() {
        return auth.getCurrentUser() != null ? auth.getCurrentUser().getUid() : null;
    }

    public void syncFromFirestore() {
        String uid = getUserId();
        if (uid == null) return;

        // Merge local orders to Firestore first if they are not there
        for (Order order : new ArrayList<>(orders)) {
            updateFirestoreOrder(order);
        }

        db.collection("users").document(uid).collection("orders")
                .orderBy("createdAt", Query.Direction.DESCENDING)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    List<Order> remoteOrders = new ArrayList<>();
                    for (DocumentSnapshot doc : queryDocumentSnapshots) {
                        Order order = doc.toObject(Order.class);
                        if (order != null) {
                            remoteOrders.add(order);
                        }
                    }
                    if (!remoteOrders.isEmpty()) {
                        orders.clear();
                        orders.addAll(remoteOrders);
                        saveOrdersLocal();
                        notifyChanged();
                    }
                });
    }

    public void addOrder(Order order) {
        orders.add(0, order); // Add new orders to the top
        saveOrdersLocal();
        updateFirestoreOrder(order);
        notifyChanged();
    }

    private void updateFirestoreOrder(Order order) {
        String uid = getUserId();
        if (uid == null) return;

        db.collection("users").document(uid).collection("orders")
                .document(order.getOrderId())
                .set(order);
    }

    public List<Order> getOrders() {
        return orders;
    }

    public List<Order> getOrdersByStatus(Order.Status status) {
        if (status == Order.Status.ALL) return orders;
        List<Order> filtered = new ArrayList<>();
        for (Order o : orders) {
            if (o.getStatus() == status) filtered.add(o);
        }
        return filtered;
    }

    private void saveOrdersLocal() {
        String json = gson.toJson(orders);
        sharedPreferences.edit().putString(KEY_ORDERS, json).apply();
    }

    private List<Order> loadOrders() {
        String json = sharedPreferences.getString(KEY_ORDERS, null);
        if (json == null) {
            return new ArrayList<>();
        }
        Type type = new TypeToken<List<Order>>() {}.getType();
        return gson.fromJson(json, type);
    }

    private void notifyChanged() {
        for (OrderChangeListener listener : new ArrayList<>(listeners)) {
            listener.onOrdersChanged();
        }
    }
}
