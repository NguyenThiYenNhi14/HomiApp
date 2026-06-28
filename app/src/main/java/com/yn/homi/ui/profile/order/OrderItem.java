package com.yn.homi.ui.profile.order;

public class OrderItem {
    private String productId;
    private String name;
    private double price;
    private String color;
    private int quantity;
    private String imageUrl;      // Cập nhật để dùng URL thay vì resource ID
    private String packageStatus; // "Packing", "In Transit", "Delivered"

    public OrderItem(String productId, String name, double price,
                     String color, int quantity, String imageUrl, String packageStatus) {
        this.productId = productId;
        this.name = name;
        this.price = price;
        this.color = color;
        this.quantity = quantity;
        this.imageUrl = imageUrl;
        this.packageStatus = packageStatus;
    }

    // Getters
    public String getProductId() { return productId; }
    public String getName() { return name; }
    public double getPrice() { return price; }
    public String getColor() { return color; }
    public int getQuantity() { return quantity; }
    public String getImageUrl() { return imageUrl; }
    public String getPackageStatus() { return packageStatus; }
}
