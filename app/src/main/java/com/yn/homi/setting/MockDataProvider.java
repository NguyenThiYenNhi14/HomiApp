package com.yn.homi.setting;

import android.content.Context;
import com.yn.homi.setting.order.Order;
import com.yn.homi.setting.order.OrderManager;

import java.util.List;

public class MockDataProvider {

    public static List<Order> getAllOrders(Context context) {
        return OrderManager.getInstance(context).getOrders();
    }

    public static List<Order> getOrdersByStatus(Context context, Order.Status status) {
        return OrderManager.getInstance(context).getOrdersByStatus(status);
    }
}
