package com.yn.homi.data.model;

import java.util.ArrayList;
import java.util.List;

public class Wishlist {
    private String name;
    private List<Product> items;

    public Wishlist(String name) {
        this.name = name;
        this.items = new ArrayList<>();
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<Product> getItems() {
        if (items == null) items = new ArrayList<>();
        return items;
    }

    public void setItems(List<Product> items) {
        this.items = items;
    }

    public void addProduct(Product product) {
        if (product == null || product.getId() == null) return;
        if (items == null) items = new ArrayList<>();
        // Avoid duplicates in the same list
        for (Product p : items) {
            if (p != null && p.getId() != null && p.getId().equals(product.getId())) return;
        }
        items.add(product);
    }

    public void removeProduct(String productId) {
        if (items == null || productId == null) return;
        items.removeIf(p -> p != null && p.getId() != null && p.getId().equals(productId));
    }
}
