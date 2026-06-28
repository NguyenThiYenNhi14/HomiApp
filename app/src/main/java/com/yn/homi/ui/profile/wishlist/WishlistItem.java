package com.yn.homi.ui.profile.wishlist;

public class WishlistItem {
    private String name;
    private String color;
    private String price;
    private int imageRes; // drawable resource ID

    public WishlistItem(String name, String color, String price, int imageRes) {
        this.name = name;
        this.color = color;
        this.price = price;
        this.imageRes = imageRes;
    }

    // Getters
    public String getName()  { return name; }
    public String getColor() { return color; }
    public String getPrice() { return price; }
    public int    getImage() { return imageRes; }
}
