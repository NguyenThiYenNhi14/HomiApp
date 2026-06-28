package com.yn.homi.data.model;

import com.google.firebase.firestore.PropertyName;

public class RoomSubCategory {
    private String id;
    @PropertyName("roomCategoryId")
    private String roomCategoryId;
    private String name;
    private String slug;
    private int order;
    @PropertyName("imageUrl")
    private String imageUrl;

    public RoomSubCategory() {
    }

    public RoomSubCategory(String id, String roomCategoryId, String name, String slug, int order, String imageUrl) {
        this.id = id;
        this.roomCategoryId = roomCategoryId;
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

    @PropertyName("roomCategoryId")
    public String getRoomCategoryId() {
        return roomCategoryId;
    }

    @PropertyName("roomCategoryId")
    public void setRoomCategoryId(String roomCategoryId) {
        this.roomCategoryId = roomCategoryId;
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
