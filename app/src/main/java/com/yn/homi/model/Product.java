package com.yn.homi.model;

import com.google.gson.annotations.SerializedName;
import java.io.Serializable;
import java.util.List;

public class Product implements Serializable {
    @SerializedName("product_id")
    private String productId;
    private String name;
    private double price;
    private String description;
    @SerializedName("image_url")
    private List<String> imageUrls;

    public String getProductId() { return productId; }
    public String getName() { return name; }
    public double getPrice() { return price; }
    public String getDescription() { return description; }
    public String getFirstImage() {
        return (imageUrls != null && !imageUrls.isEmpty()) ? imageUrls.get(0) : "";
    }
}