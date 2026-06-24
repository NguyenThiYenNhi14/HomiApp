package com.yn.homi.setting.order;

import java.util.List;

public class Order {
    public enum Status { ALL, PENDING, PROCESSING, PARTIALLY_SHIPPED, SHIPPED, RETURNED, CANCELLED }

    private String orderId;
    private Status status;
    private List<OrderItem> items;
    private double subtotal;
    private double shippingFee;
    private String placedAt;
    private String shippingAddress;
    private String trackingCode;

    public Order(String orderId, Status status, List<OrderItem> items,
                 double subtotal, double shippingFee,
                 String placedAt, String shippingAddress, String trackingCode) {
        this.orderId = orderId;
        this.status = status;
        this.items = items;
        this.subtotal = subtotal;
        this.shippingFee = shippingFee;
        this.placedAt = placedAt;
        this.shippingAddress = shippingAddress;
        this.trackingCode = trackingCode;
    }

    // Getters
    public String getOrderId() { return orderId; }
    public Status getStatus() { return status; }
    public List<OrderItem> getItems() { return items; }
    public double getSubtotal() { return subtotal; }
    public double getShippingFee() { return shippingFee; }
    public double getTotal() { return subtotal + shippingFee; }
    public String getPlacedAt() { return placedAt; }
    public String getShippingAddress() { return shippingAddress; }
    public String getTrackingCode() { return trackingCode; }
}
