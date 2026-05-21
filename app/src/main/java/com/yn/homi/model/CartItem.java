package com.yn.homi.model;

import java.io.Serializable;

public class CartItem implements Serializable {
    private String id;
    private String name;
    private double price;
    private int quantity;
    private String imageUrl;

    public CartItem(String id, String name, double price, int quantity, String imageUrl) {
        this.id = id;
        this.name = name;
        this.price = price;
        this.quantity = quantity;
        this.imageUrl = imageUrl;
    }

    public String getId() { return id; }
    public String getName() { return name; }
    public double getPrice() { return price; }
    public String getImageUrl() { return imageUrl; }

    public int getQuantity() { return quantity; }
    public void setQuantity(int quantity) { this.quantity = quantity; }

    // --- BỔ SUNG HÀM TÍNH TỔNG TIỀN MÓN HÀNG ---
    public double getItemTotal() {
        return price * quantity;
    }
}