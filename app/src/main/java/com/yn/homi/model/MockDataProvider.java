package com.yn.homi.model;

import com.yn.homi.model.Order;
import com.yn.homi.model.OrderItem;
import com.yn.homi.R;
import java.util.Arrays;
import java.util.List;

public class MockDataProvider {

    public static List<Order> getAllOrders() {
        return Arrays.asList(
                // PAID order
                new Order(
                        "860368",
                        Order.Status.PAID,
                        Arrays.asList(
                                new OrderItem("p1", "Nordic Wooden Chair",
                                        160, "Oak Brown", 1, R.drawable.sofa, "Packing"),
                                new OrderItem("p2", "Minimalist Coffee Table",
                                        100, "White", 1, R.drawable.sofa, "Packing")
                        ),
                        820, 30,
                        "12 Dec 2024, 08:06",
                        "22 Baker Street\nHo Chi Minh City",
                        ""
                ),
                // SHIPPED order
                new Order(
                        "660330",
                        Order.Status.SHIPPED,
                        Arrays.asList(
                                new OrderItem("p3", "Velvet Sofa 3-Seat",
                                        280, "Dark Blue", 1, R.drawable.sofa, "In Transit")
                        ),
                        280, 10,
                        "20 Oct 2024, 09:00",
                        "22 Baker Street\nHo Chi Minh City",
                        "VN18880639"
                ),
                // DELIVERED order
                new Order(
                        "330368",
                        Order.Status.DELIVERED,
                        Arrays.asList(
                                new OrderItem("p4", "Oak Bookshelf",
                                        40, "Natural Oak", 1, R.drawable.sofa, "Delivered"),
                                new OrderItem("p5", "Floor Lamp",
                                        80, "Black", 1, R.drawable.lamp, "Delivered")
                        ),
                        160, 10,
                        "20 Oct 2024, 13:08",
                        "22 Baker Street\nHo Chi Minh City",
                        ""
                ),
                // RETURNED order
                new Order(
                        "830368",
                        Order.Status.RETURNED,
                        Arrays.asList(
                                new OrderItem("p6", "Rattan Lounge Chair",
                                        160, "Natural", 1, R.drawable.sofa, "Refunded"),
                                new OrderItem("p7", "Ceramic Vase Set",
                                        680, "White", 1, R.drawable.sofa, "Order Sent")
                        ),
                        840, 20,
                        "01 Nov 2024, 10:00",
                        "22 Baker Street\nHo Chi Minh City",
                        ""
                )
        );
    }

    public static List<Order> getOrdersByStatus(Order.Status status) {
        List<Order> result = new java.util.ArrayList<>();
        for (Order o : getAllOrders()) {
            if (o.getStatus() == status) result.add(o);
        }
        return result;
    }
}