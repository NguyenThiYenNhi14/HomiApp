package com.yn.homi.models;

import com.google.firebase.firestore.PropertyName;
import java.util.List;
import java.util.Map;

public class Product {
    private String id;
    private String name;
    private double price;
    @PropertyName("originalPrice")
    private double originalPrice;
    @PropertyName("discountPercent")
    private int discountPercent;
    private String currency;
    private float rating;
    @PropertyName("reviewCount")
    private int reviewCount;
    @PropertyName("thumbnailUrl")
    private String thumbnailUrl;
    @PropertyName("imageUrls")
    private List<String> imageUrls;
    private List<String> colors;
    private List<String> materials;
    private List<String> features;
    private String style;
    private Map<String, Double> dimensions;
    @PropertyName("isOnSale")
    private boolean isOnSale;
    @PropertyName("isBestSeller")
    private boolean isBestSeller;
    @PropertyName("isNew")
    private boolean isNew;
    @PropertyName("isQuickShip")
    private boolean isQuickShip;
    private List<String> tags;
    @PropertyName("roomSubCategoryIds")
    private List<String> roomSubCategoryIds;
    @PropertyName("quickTabIds")
    private List<String> quickTabIds;
    private String description;
    private String brand;
    private String collection;
    @PropertyName("stockStatus")
    private String stockStatus;
    @PropertyName("reviewsSample")
    private List<Review> reviewsSample;
    @PropertyName("colorVariants")
    private List<ColorVariant> colorVariants;
    @PropertyName("sizeVariants")
    private List<SizeVariant> sizeVariants;

    public Product() {
    }

    // Constructor for compatibility
    public Product(String id, String name, double price, float rating, String thumbnailUrl) {
        this.id = id;
        this.name = name;
        this.price = price;
        this.rating = rating;
        this.thumbnailUrl = thumbnailUrl;
    }

    public static class Review {
        private String reviewerName;
        private int rating;
        private String date;
        private String title;
        private String body;
        @PropertyName("verifiedBuyer")
        private boolean verifiedBuyer;
        @PropertyName("imageUrl")
        private String imageUrl;

        public Review() {}

        @PropertyName("reviewerName")
        public String getReviewerName() { return reviewerName; }
        @PropertyName("reviewerName")
        public void setReviewerName(String reviewerName) { this.reviewerName = reviewerName; }
        public int getRating() { return rating; }
        public void setRating(int rating) { this.rating = rating; }
        public String getDate() { return date; }
        public void setDate(String date) { this.date = date; }
        public String getTitle() { return title; }
        public void setTitle(String title) { this.title = title; }
        public String getBody() { return body; }
        public void setBody(String body) { this.body = body; }
        @PropertyName("verifiedBuyer")
        public boolean isVerifiedBuyer() { return verifiedBuyer; }
        @PropertyName("verifiedBuyer")
        public void setVerifiedBuyer(boolean verifiedBuyer) { this.verifiedBuyer = verifiedBuyer; }
        @PropertyName("imageUrl")
        public String getImageUrl() { return imageUrl; }
        @PropertyName("imageUrl")
        public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }
    }

    public static class ColorVariant {
        private String name;
        private String swatch;
        @PropertyName("imageUrl")
        private String imageUrl;

        public ColorVariant() {}

        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        public String getSwatch() { return swatch; }
        public void setSwatch(String swatch) { this.swatch = swatch; }
        @PropertyName("imageUrl")
        public String getImageUrl() { return imageUrl; }
        @PropertyName("imageUrl")
        public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }
    }

    public static class SizeVariant {
        private String label;
        @PropertyName("widthInch")
        private double widthInch;
        @PropertyName("isDefault")
        private boolean isDefault;

        public SizeVariant() {}

        public String getLabel() { return label; }
        public void setLabel(String label) { this.label = label; }
        @PropertyName("widthInch")
        public double getWidthInch() { return widthInch; }
        @PropertyName("widthInch")
        public void setWidthInch(double widthInch) { this.widthInch = widthInch; }
        @PropertyName("isDefault")
        public boolean isDefault() { return isDefault; }
        @PropertyName("isDefault")
        public void setDefault(boolean aDefault) { isDefault = aDefault; }
    }

    // Getters and Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public double getPrice() { return price; }
    public void setPrice(double price) { this.price = price; }
    @PropertyName("originalPrice")
    public double getOriginalPrice() { return originalPrice; }
    @PropertyName("originalPrice")
    public void setOriginalPrice(double originalPrice) { this.originalPrice = originalPrice; }
    @PropertyName("discountPercent")
    public int getDiscountPercent() { return discountPercent; }
    @PropertyName("discountPercent")
    public void setDiscountPercent(int discountPercent) { this.discountPercent = discountPercent; }
    public String getCurrency() { return currency; }
    public void setCurrency(String currency) { this.currency = currency; }
    public float getRating() { return rating; }
    public void setRating(float rating) { this.rating = rating; }
    @PropertyName("reviewCount")
    public int getReviewCount() { return reviewCount; }
    @PropertyName("reviewCount")
    public void setReviewCount(int reviewCount) { this.reviewCount = reviewCount; }
    @PropertyName("thumbnailUrl")
    public String getThumbnailUrl() { return thumbnailUrl; }
    @PropertyName("thumbnailUrl")
    public void setThumbnailUrl(String thumbnailUrl) { this.thumbnailUrl = thumbnailUrl; }
    @PropertyName("imageUrls")
    public List<String> getImageUrls() { return imageUrls; }
    @PropertyName("imageUrls")
    public void setImageUrls(List<String> imageUrls) { this.imageUrls = imageUrls; }
    public List<String> getColors() { return colors; }
    public void setColors(List<String> colors) { this.colors = colors; }
    public List<String> getMaterials() { return materials; }
    public void setMaterials(List<String> materials) { this.materials = materials; }
    public List<String> getFeatures() { return features; }
    public void setFeatures(List<String> features) { this.features = features; }
    public String getStyle() { return style; }
    public void setStyle(String style) { this.style = style; }
    public Map<String, Double> getDimensions() { return dimensions; }
    public void setDimensions(Map<String, Double> dimensions) { this.dimensions = dimensions; }
    @PropertyName("isOnSale")
    public boolean isOnSale() { return isOnSale; }
    @PropertyName("isOnSale")
    public void setOnSale(boolean onSale) { isOnSale = onSale; }
    @PropertyName("isBestSeller")
    public boolean isBestSeller() { return isBestSeller; }
    @PropertyName("isBestSeller")
    public void setBestSeller(boolean bestSeller) { isBestSeller = bestSeller; }
    @PropertyName("isNew")
    public boolean isNew() { return isNew; }
    @PropertyName("isNew")
    public void setNew(boolean aNew) { isNew = aNew; }
    @PropertyName("isQuickShip")
    public boolean isQuickShip() { return isQuickShip; }
    @PropertyName("isQuickShip")
    public void setQuickShip(boolean quickShip) { isQuickShip = quickShip; }
    public List<String> getTags() { return tags; }
    public void setTags(List<String> tags) { this.tags = tags; }
    @PropertyName("roomSubCategoryIds")
    public List<String> getRoomSubCategoryIds() { return roomSubCategoryIds; }
    @PropertyName("roomSubCategoryIds")
    public void setRoomSubCategoryIds(List<String> roomSubCategoryIds) { this.roomSubCategoryIds = roomSubCategoryIds; }
    @PropertyName("quickTabIds")
    public List<String> getQuickTabIds() { return quickTabIds; }
    @PropertyName("quickTabIds")
    public void setQuickTabIds(List<String> quickTabIds) { this.quickTabIds = quickTabIds; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public String getBrand() { return brand; }
    public void setBrand(String brand) { this.brand = brand; }
    public String getCollection() { return collection; }
    public void setCollection(String collection) { this.collection = collection; }
    @PropertyName("stockStatus")
    public String getStockStatus() { return stockStatus; }
    @PropertyName("stockStatus")
    public void setStockStatus(String stockStatus) { this.stockStatus = stockStatus; }
    @PropertyName("reviewsSample")
    public List<Review> getReviewsSample() { return reviewsSample; }
    @PropertyName("reviewsSample")
    public void setReviewsSample(List<Review> reviewsSample) { this.reviewsSample = reviewsSample; }
    @PropertyName("colorVariants")
    public List<ColorVariant> getColorVariants() { return colorVariants; }
    @PropertyName("colorVariants")
    public void setColorVariants(List<ColorVariant> colorVariants) { this.colorVariants = colorVariants; }
    @PropertyName("sizeVariants")
    public List<SizeVariant> getSizeVariants() { return sizeVariants; }
    @PropertyName("sizeVariants")
    public void setSizeVariants(List<SizeVariant> sizeVariants) { this.sizeVariants = sizeVariants; }

    // Compatibility getter
    public String getImageUrl() { return thumbnailUrl; }
}
