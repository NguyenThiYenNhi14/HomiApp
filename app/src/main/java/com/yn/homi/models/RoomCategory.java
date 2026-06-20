package com.yn.homi.models;

import com.google.firebase.firestore.PropertyName;

public class RoomCategory {
    private String id;
    private String name;
    private String slug;
    private int order;
    @PropertyName("imageUrl")
    private String imageUrl;

    public RoomCategory() {
    }

    public RoomCategory(String id, String name, String slug, int order, String imageUrl) {
        this.id = id;
        this.name = name;
        this.slug = slug;
        this.order = order;
        this.imageUrl = imageUrl;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getSlug() {
        return slug;
    }

    public void setSlug(String slug) {
        this.slug = slug;
    }

    public int getOrder() {
        return order;
    }

    public void setOrder(int order) {
        this.order = order;
    }

    @PropertyName("imageUrl")
    public String getImageUrl() {
        return imageUrl;
    }

    @PropertyName("imageUrl")
    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }
}
