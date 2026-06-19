package com.yn.homi.setting.order;

public class OrderItem {
    private String productId;
    private String name;
    private double price;
    private String color;
    private int quantity;
    private int imageResId;      // dùng ảnh local tạm
    private String packageStatus; // "Packing", "In Transit", "Delivered"

    public OrderItem(String productId, String name, double price,
                     String color, int quantity, int imageResId, String packageStatus) {
        this.productId = productId;
        this.name = name;
        this.price = price;
        this.color = color;
        this.quantity = quantity;
        this.imageResId = imageResId;
        this.packageStatus = packageStatus;
    }

    // Getters
    public String getProductId() { return productId; }
    public String getName() { return name; }
    public double getPrice() { return price; }
    public String getColor() { return color; }
    public int getQuantity() { return quantity; }
    public int getImageResId() { return imageResId; }
    public String getPackageStatus() { return packageStatus; }
}