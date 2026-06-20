package com.yn.homi.models;

import com.google.firebase.firestore.PropertyName;

public class Banner {
    private String id;
    private String title;
    private String subtitle;
    @PropertyName("imageUrl")
    private String imageUrl;
    @PropertyName("linkUrl")
    private String linkUrl;
    @PropertyName("bgColor")
    private String bgColor;
    @PropertyName("textColor")
    private String textColor;
    private int order;
    @PropertyName("isActive")
    private boolean isActive;

    public Banner() {
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getSubtitle() {
        return subtitle;
    }

    public void setSubtitle(String subtitle) {
        this.subtitle = subtitle;
    }

    @PropertyName("imageUrl")
    public String getImageUrl() {
        return imageUrl;
    }

    @PropertyName("imageUrl")
    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    @PropertyName("linkUrl")
    public String getLinkUrl() {
        return linkUrl;
    }

    @PropertyName("linkUrl")
    public void setLinkUrl(String linkUrl) {
        this.linkUrl = linkUrl;
    }

    @PropertyName("bgColor")
    public String getBgColor() {
        return bgColor;
    }

    @PropertyName("bgColor")
    public void setBgColor(String bgColor) {
        this.bgColor = bgColor;
    }

    @PropertyName("textColor")
    public String getTextColor() {
        return textColor;
    }

    @PropertyName("textColor")
    public void setTextColor(String textColor) {
        this.textColor = textColor;
    }

    public int getOrder() {
        return order;
    }

    public void setOrder(int order) {
        this.order = order;
    }

    @PropertyName("isActive")
    public boolean isActive() {
        return isActive;
    }

    @PropertyName("isActive")
    public void setActive(boolean active) {
        isActive = active;
    }
}
