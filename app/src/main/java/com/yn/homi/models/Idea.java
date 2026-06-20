package com.yn.homi.models;

import com.google.firebase.firestore.PropertyName;
import java.util.List;

public class Idea {
    private String id;
    private String title;
    private String description;
    private String group;
    @PropertyName("groupLabel")
    private String groupLabel;
    @PropertyName("thumbnailUrl")
    private String thumbnailUrl;
    @PropertyName("imageUrls")
    private List<String> imageUrls;
    @PropertyName("productIds")
    private List<String> productIds;
    private List<String> tags;
    @PropertyName("isActive")
    private boolean isActive;
    private int order;

    public Idea() {
        // Required for Firestore
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getGroup() { return group; }
    public void setGroup(String group) { this.group = group; }

    @PropertyName("groupLabel")
    public String getGroupLabel() { return groupLabel; }
    @PropertyName("groupLabel")
    public void setGroupLabel(String groupLabel) { this.groupLabel = groupLabel; }

    @PropertyName("thumbnailUrl")
    public String getThumbnailUrl() { return thumbnailUrl; }
    @PropertyName("thumbnailUrl")
    public void setThumbnailUrl(String thumbnailUrl) { this.thumbnailUrl = thumbnailUrl; }

    @PropertyName("imageUrls")
    public List<String> getImageUrls() { return imageUrls; }
    @PropertyName("imageUrls")
    public void setImageUrls(List<String> imageUrls) { this.imageUrls = imageUrls; }

    @PropertyName("productIds")
    public List<String> getProductIds() { return productIds; }
    @PropertyName("productIds")
    public void setProductIds(List<String> productIds) { this.productIds = productIds; }

    public List<String> getTags() { return tags; }
    public void setTags(List<String> tags) { this.tags = tags; }

    @PropertyName("isActive")
    public boolean isActive() { return isActive; }
    @PropertyName("isActive")
    public void setActive(boolean active) { isActive = active; }

    public int getOrder() { return order; }
    public void setOrder(int order) { this.order = order; }
}
