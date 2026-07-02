package com.yn.homi.ui.profile.order;

import java.util.List;

public class Order {
    public enum Status { ALL, PENDING, PROCESSING, PARTIALLY_SHIPPED, SHIPPED, COMPLETED, RETURNED, CANCELLED }

    private String orderId;
    private Status status;
    private List<OrderItem> items;
    private double subtotal;
    private double shippingFee;
    private String couponCode;
    private double discountAmount;
    private String placedAt;
    private String shippingAddress;
    private String trackingCode;
    private String paymentMethod;
    private com.google.firebase.Timestamp createdAt;

    public Order() {
        this.createdAt = com.google.firebase.Timestamp.now();
    }

    public Order(String orderId, Status status, List<OrderItem> items,
                 double subtotal, double shippingFee,
                 String placedAt, String shippingAddress, String trackingCode,
                 String paymentMethod, String couponCode, double discountAmount) {
        this.orderId = orderId;
        this.status = status;
        this.items = items;
        this.subtotal = subtotal;
        this.shippingFee = shippingFee;
        this.placedAt = placedAt;
        this.shippingAddress = shippingAddress;
        this.trackingCode = trackingCode;
        this.paymentMethod = paymentMethod;
        this.couponCode = couponCode;
        this.discountAmount = discountAmount;
        this.createdAt = com.google.firebase.Timestamp.now();
    }

    // Getters
    public String getOrderId() { return orderId; }
    public Status getStatus() { return status; }
    public List<OrderItem> getItems() { return items; }
    public double getSubtotal() { return subtotal; }
    public double getShippingFee() { return shippingFee; }
    public String getCouponCode() { return couponCode; }
    public double getDiscountAmount() { return discountAmount; }
    public double getTotal() { return subtotal + shippingFee - discountAmount; }
    public String getPlacedAt() { return placedAt; }
    public String getShippingAddress() { return shippingAddress; }
    public String getTrackingCode() { return trackingCode; }
    public String getPaymentMethod() { return paymentMethod; }
    public com.google.firebase.Timestamp getCreatedAt() { return createdAt; }
    public void setCreatedAt(com.google.firebase.Timestamp createdAt) { this.createdAt = createdAt; }
}