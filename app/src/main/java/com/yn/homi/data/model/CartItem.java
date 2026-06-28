package com.yn.homi.data.model;

import java.io.Serializable;

public class CartItem implements Serializable {
    private String id;
    private String name;
    private double price;
    private int quantity;
    private String imageUrl;
    private String selectedColor;
    private String selectedSize;
    private boolean isSelected = true; // Mặc định là được chọn khi mới thêm vào giỏ

    public CartItem() {
    }

    public CartItem(String id, String name, double price, int quantity, String imageUrl) {
        this.id = id;
        this.name = name;
        this.price = price;
        this.quantity = quantity;
        this.imageUrl = imageUrl;
    }

    public CartItem(String id, String name, double price, int quantity, String imageUrl, String selectedColor, String selectedSize) {
        this.id = id;
        this.name = name;
        this.price = price;
        this.quantity = quantity;
        this.imageUrl = imageUrl;
        this.selectedColor = selectedColor;
        this.selectedSize = selectedSize;
    }

    /**
     * Constructor chuẩn hóa để tạo CartItem từ Product.
     */
    public CartItem(Product product, int quantity) {
        if (product != null) {
            this.id = product.getId();
            this.name = product.getName();
            this.price = product.getPrice();
            this.quantity = quantity;
            this.imageUrl = product.getImageUrl();
        }
    }

    public CartItem(Product product, int quantity, String selectedColor, String selectedSize) {
        this(product, quantity);
        this.selectedColor = selectedColor;
        this.selectedSize = selectedSize;
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public double getPrice() { return price; }
    public void setPrice(double price) { this.price = price; }

    public int getQuantity() { return quantity; }
    public void setQuantity(int quantity) { this.quantity = quantity; }

    public String getImageUrl() { return imageUrl; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }

    public String getSelectedColor() { return selectedColor; }
    public void setSelectedColor(String selectedColor) { this.selectedColor = selectedColor; }

    public String getSelectedSize() { return selectedSize; }
    public void setSelectedSize(String selectedSize) { this.selectedSize = selectedSize; }

    public boolean isSelected() { return isSelected; }
    public void setSelected(boolean selected) { isSelected = selected; }

    /** Tính tổng tiền của item này (giá * số lượng) */
    public double getItemTotal() {
        return price * quantity;
    }
}
