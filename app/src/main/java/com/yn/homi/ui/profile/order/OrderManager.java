package com.yn.homi.ui.profile.order;

import android.content.Context;
import android.content.SharedPreferences;

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

    private OrderManager(Context context) {
        sharedPreferences = context.getApplicationContext().getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        gson = new Gson();
        orders = loadOrders();
    }

    public static synchronized OrderManager getInstance(Context context) {
        if (instance == null) {
            instance = new OrderManager(context);
        }
        return instance;
    }

    public void addOrder(Order order) {
        orders.add(0, order); // Add new orders to the top
        saveOrders();
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

    private void saveOrders() {
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
}
