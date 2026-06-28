package com.yn.homi.data.model;

import com.google.firebase.firestore.PropertyName;
import com.google.gson.annotations.SerializedName;
import java.io.Serializable;
import java.util.List;
import java.util.Map;

public class Product implements Serializable {
    @SerializedName("product_id")
    private String id;
    private String name;
    private double price;
    private double originalPrice;
    private int discountPercent;
    private String currency;
    private float rating;
    private int reviewCount;
    private String thumbnailUrl;
    private List<String> imageUrls;
    
    @SerializedName("color")
    private List<String> colors;
    @SerializedName("material")
    private List<String> materials;
    private List<String> features;
    private String style;
    private Map<String, Double> dimensions;
    private boolean isOnSale;
    private boolean isBestSeller;
    private boolean isNew;
    private boolean isQuickShip;
    private List<String> tags;
    private List<String> roomSubCategoryIds;
    private List<String> quickTabIds;
    private String description;
    private String brand;
    private String collection;
    private String stockStatus;
    private int stockQuantity;
    private int lowStockThreshold;
    private String subCategoryId;
    private int estimatedDeliveryDays;
    private String shape;
    private Object seatsUpTo;
    private Object bedSize;
    private Object tvSizeRange;
    private List<Review> reviewsSample;
    private List<ColorVariant> colorVariants;
    private List<SizeVariant> sizeVariants;

    public Product() {
    }

    public Product(String id, String name, double price, float rating, String thumbnailUrl) {
        this.id = id;
        this.name = name;
        this.price = price;
        this.rating = rating;
        this.thumbnailUrl = thumbnailUrl;
    }

    public static class Review implements Serializable {
        private String reviewId;
        private String productId;
        private String userId;
        private String reviewerName;
        private int rating;
        private String date;
        private String title;
        private String body;
        private boolean verifiedBuyer;
        private String imageUrl;

        public Review() {}

        public String getReviewId() { return reviewId; }
        public void setReviewId(String reviewId) { this.reviewId = reviewId; }
        public String getProductId() { return productId; }
        public void setProductId(String productId) { this.productId = productId; }

        @PropertyName("product_id")
        public String getProductIdFs() { return productId; }
        @PropertyName("product_id")
        public void setProductIdFs(String productId) { this.productId = productId; }
        public String getUserId() { return userId; }
        public void setUserId(String userId) { this.userId = userId; }
        
        @PropertyName("reviewerName")
        public String getReviewerName() { return reviewerName; }
        @PropertyName("reviewerName")
        public void setReviewerName(String reviewerName) { this.reviewerName = reviewerName; }
        
        @PropertyName("reviewer_name")
        public String getReviewerNameFs() { return reviewerName; }
        @PropertyName("reviewer_name")
        public void setReviewerNameFs(String reviewerName) { this.reviewerName = reviewerName; }
        
        @PropertyName("user_name")
        public String getUserNameFs() { return reviewerName; }
        @PropertyName("user_name")
        public void setUserNameFs(String reviewerName) { this.reviewerName = reviewerName; }

        public int getRating() { return rating; }
        public void setRating(int rating) { this.rating = rating; }
        
        public String getDate() { return date; }
        public void setDate(String date) { this.date = date; }
        
        @PropertyName("review_date")
        public String getDateFs() { return date; }
        @PropertyName("review_date")
        public void setDateFs(String date) { this.date = date; }

        public String getTitle() { return title; }
        public void setTitle(String title) { this.title = title; }
        
        public String getBody() { return body; }
        public void setBody(String body) { this.body = body; }
        
        @PropertyName("comment")
        public String getCommentFs() { return body; }
        @PropertyName("comment")
        public void setCommentFs(String body) { this.body = body; }
        
        @PropertyName("content")
        public String getContentFs() { return body; }
        @PropertyName("content")
        public void setContentFs(String body) { this.body = body; }
        
        @PropertyName("verifiedBuyer")
        public boolean isVerifiedBuyer() { return verifiedBuyer; }
        @PropertyName("verifiedBuyer")
        public void setVerifiedBuyer(boolean verifiedBuyer) { this.verifiedBuyer = verifiedBuyer; }
        
        @PropertyName("verified_buyer")
        public boolean isVerifiedBuyerFs() { return verifiedBuyer; }
        @PropertyName("verified_buyer")
        public void setVerifiedBuyerFs(boolean verifiedBuyer) { this.verifiedBuyer = verifiedBuyer; }

        @PropertyName("imageUrl")
        public String getImageUrl() { return imageUrl; }
        @PropertyName("imageUrl")
        public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }
        
        @PropertyName("image_url")
        public String getImageUrlFs() { return imageUrl; }
        @PropertyName("image_url")
        public void setImageUrlFs(String imageUrl) { this.imageUrl = imageUrl; }
    }

    public static class ColorVariant implements Serializable {
        private String name;
        private String swatch;
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
        
        @PropertyName("image_url")
        public String getImageUrlFs() { return imageUrl; }
        @PropertyName("image_url")
        public void setImageUrlFs(String imageUrl) { this.imageUrl = imageUrl; }
    }

    public static class SizeVariant implements Serializable {
        private String label;
        private double widthInch;
        private boolean isDefault;

        public SizeVariant() {}

        public String getLabel() { return label; }
        public void setLabel(String label) { this.label = label; }
        
        @PropertyName("widthInch")
        public double getWidthInch() { return widthInch; }
        @PropertyName("widthInch")
        public void setWidthInch(double widthInch) { this.widthInch = widthInch; }
        
        @PropertyName("width_inch")
        public double getWidthInchFs() { return widthInch; }
        @PropertyName("width_inch")
        public void setWidthInchFs(double widthInch) { this.widthInch = widthInch; }

        @PropertyName("isDefault")
        public boolean isDefault() { return isDefault; }
        @PropertyName("isDefault")
        public void setDefault(boolean aDefault) { isDefault = aDefault; }
        
        @PropertyName("is_default")
        public boolean isDefaultFs() { return isDefault; }
        @PropertyName("is_default")
        public void setDefaultFs(boolean aDefault) { isDefault = aDefault; }
    }

    // Getters and Setters with Multi-mapping
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    
    @PropertyName("name")
    public String getName() { return name; }
    @PropertyName("name")
    public void setName(String name) { this.name = name; }
    
    @PropertyName("product_name")
    public String getNameFs() { return name; }
    @PropertyName("product_name")
    public void setNameFs(String name) { this.name = name; }

    @PropertyName("price")
    public double getPrice() { return price; }
    @PropertyName("price")
    public void setPrice(double price) { this.price = price; }
    
    @PropertyName("product_price")
    public double getPriceFs() { return price; }
    @PropertyName("product_price")
    public void setPriceFs(double price) { this.price = price; }

    @PropertyName("originalPrice")
    public double getOriginalPrice() { return originalPrice; }
    @PropertyName("originalPrice")
    public void setOriginalPrice(double originalPrice) { this.originalPrice = originalPrice; }
    
    @PropertyName("original_price")
    public double getOriginalPriceFs() { return originalPrice; }
    @PropertyName("original_price")
    public void setOriginalPriceFs(double originalPrice) { this.originalPrice = originalPrice; }

    @PropertyName("discountPercent")
    public int getDiscountPercent() { return discountPercent; }
    @PropertyName("discountPercent")
    public void setDiscountPercent(int discountPercent) { this.discountPercent = discountPercent; }
    
    @PropertyName("discount_percent")
    public int getDiscountPercentFs() { return discountPercent; }
    @PropertyName("discount_percent")
    public void setDiscountPercentFs(int discountPercent) { this.discountPercent = discountPercent; }

    public String getCurrency() { return currency; }
    public void setCurrency(String currency) { this.currency = currency; }
    public float getRating() { return rating; }
    public void setRating(float rating) { this.rating = rating; }

    @PropertyName("reviewCount")
    public int getReviewCount() { return reviewCount; }
    @PropertyName("reviewCount")
    public void setReviewCount(int reviewCount) { this.reviewCount = reviewCount; }
    
    @PropertyName("review_count")
    public int getReviewCountFs() { return reviewCount; }
    @PropertyName("review_count")
    public void setReviewCountFs(int reviewCount) { this.reviewCount = reviewCount; }

    @PropertyName("thumbnailUrl")
    public String getThumbnailUrl() { return thumbnailUrl; }
    @PropertyName("thumbnailUrl")
    public void setThumbnailUrl(String thumbnailUrl) { this.thumbnailUrl = thumbnailUrl; }
    
    @PropertyName("thumbnail_url")
    public String getThumbnailUrlFs() { return thumbnailUrl; }
    @PropertyName("thumbnail_url")
    public void setThumbnailUrlFs(String thumbnailUrl) { this.thumbnailUrl = thumbnailUrl; }

    @PropertyName("imageUrls")
    public List<String> getImageUrls() { return imageUrls; }
    @PropertyName("imageUrls")
    public void setImageUrls(List<String> imageUrls) { this.imageUrls = imageUrls; }
    
    @PropertyName("image_urls")
    public List<String> getImageUrlsFs() { return imageUrls; }
    @PropertyName("image_urls")
    public void setImageUrlsFs(List<String> imageUrls) { this.imageUrls = imageUrls; }

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
    
    @PropertyName("is_on_sale")
    public boolean isOnSaleFs() { return isOnSale; }
    @PropertyName("is_on_sale")
    public void setOnSaleFs(boolean onSale) { isOnSale = onSale; }

    @PropertyName("isBestSeller")
    public boolean isBestSeller() { return isBestSeller; }
    @PropertyName("isBestSeller")
    public void setBestSeller(boolean bestSeller) { isBestSeller = bestSeller; }
    
    @PropertyName("is_best_seller")
    public boolean isBestSellerFs() { return isBestSeller; }
    @PropertyName("is_best_seller")
    public void setBestSellerFs(boolean bestSeller) { isBestSeller = bestSeller; }

    @PropertyName("isNew")
    public boolean isNew() { return isNew; }
    @PropertyName("isNew")
    public void setNew(boolean aNew) { isNew = aNew; }
    
    @PropertyName("is_new")
    public boolean isNewFs() { return isNew; }
    @PropertyName("is_new")
    public void setNewFs(boolean aNew) { isNew = aNew; }

    @PropertyName("isQuickShip")
    public boolean isQuickShip() { return isQuickShip; }
    @PropertyName("isQuickShip")
    public void setQuickShip(boolean quickShip) { isQuickShip = quickShip; }
    
    @PropertyName("is_quick_ship")
    public boolean isQuickShipFs() { return isQuickShip; }
    @PropertyName("is_quick_ship")
    public void setQuickShipFs(boolean quickShip) { isQuickShip = quickShip; }

    public List<String> getTags() { return tags; }
    public void setTags(List<String> tags) { this.tags = tags; }

    @PropertyName("roomSubCategoryIds")
    public List<String> getRoomSubCategoryIds() { return roomSubCategoryIds; }
    @PropertyName("roomSubCategoryIds")
    public void setRoomSubCategoryIds(List<String> roomSubCategoryIds) { this.roomSubCategoryIds = roomSubCategoryIds; }
    
    @PropertyName("room_sub_category_ids")
    public List<String> getRoomSubCategoryIdsFs() { return roomSubCategoryIds; }
    @PropertyName("room_sub_category_ids")
    public void setRoomSubCategoryIdsFs(List<String> roomSubCategoryIds) { this.roomSubCategoryIds = roomSubCategoryIds; }

    @PropertyName("quickTabIds")
    public List<String> getQuickTabIds() { return quickTabIds; }
    @PropertyName("quickTabIds")
    public void setQuickTabIds(List<String> quickTabIds) { this.quickTabIds = quickTabIds; }
    
    @PropertyName("quick_tab_ids")
    public List<String> getQuickTabIdsFs() { return quickTabIds; }
    @PropertyName("quick_tab_ids")
    public void setQuickTabIdsFs(List<String> quickTabIds) { this.quickTabIds = quickTabIds; }

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
    
    @PropertyName("stock_status")
    public String getStockStatusFs() { return stockStatus; }
    @PropertyName("stock_status")
    public void setStockStatusFs(String stockStatus) { this.stockStatus = stockStatus; }

    @PropertyName("stockQuantity")
    public int getStockQuantity() { return stockQuantity; }
    @PropertyName("stockQuantity")
    public void setStockQuantity(int stockQuantity) { this.stockQuantity = stockQuantity; }

    @PropertyName("stock_quantity")
    public int getStockQuantityFs() { return stockQuantity; }
    @PropertyName("stock_quantity")
    public void setStockQuantityFs(int stockQuantity) { this.stockQuantity = stockQuantity; }

    @PropertyName("lowStockThreshold")
    public int getLowStockThreshold() { return lowStockThreshold; }
    @PropertyName("lowStockThreshold")
    public void setLowStockThreshold(int lowStockThreshold) { this.lowStockThreshold = lowStockThreshold; }

    @PropertyName("low_stock_threshold")
    public int getLowStockThresholdFs() { return lowStockThreshold; }
    @PropertyName("low_stock_threshold")
    public void setLowStockThresholdFs(int lowStockThreshold) { this.lowStockThreshold = lowStockThreshold; }

    @PropertyName("subCategoryId")
    public String getSubCategoryId() { return subCategoryId; }
    @PropertyName("subCategoryId")
    public void setSubCategoryId(String subCategoryId) { this.subCategoryId = subCategoryId; }

    @PropertyName("sub_category_id")
    public String getSubCategoryIdFs() { return subCategoryId; }
    @PropertyName("sub_category_id")
    public void setSubCategoryIdFs(String subCategoryId) { this.subCategoryId = subCategoryId; }

    @PropertyName("estimatedDeliveryDays")
    public int getEstimatedDeliveryDays() { return estimatedDeliveryDays; }
    @PropertyName("estimatedDeliveryDays")
    public void setEstimatedDeliveryDays(int estimatedDeliveryDays) { this.estimatedDeliveryDays = estimatedDeliveryDays; }

    @PropertyName("estimated_delivery_days")
    public int getEstimatedDeliveryDaysFs() { return estimatedDeliveryDays; }
    @PropertyName("estimated_delivery_days")
    public void setEstimatedDeliveryDaysFs(int estimatedDeliveryDays) { this.estimatedDeliveryDays = estimatedDeliveryDays; }

    @PropertyName("shape")
    public String getShape() { return shape; }
    @PropertyName("shape")
    public void setShape(String shape) { this.shape = shape; }

    @PropertyName("seatsUpTo")
    public Object getSeatsUpTo() {
        return seatsUpTo;
    }
    @PropertyName("seatsUpTo")
    public void setSeatsUpTo(Object seatsUpTo) { this.seatsUpTo = seatsUpTo; }

    @PropertyName("seats_up_to")
    public Object getSeatsUpToFs() {
        return seatsUpTo;
    }
    @PropertyName("seats_up_to")
    public void setSeatsUpToFs(Object seatsUpTo) { this.seatsUpTo = seatsUpTo; }

    @PropertyName("bedSize")
    public Object getBedSize() {
        return bedSize;
    }
    @PropertyName("bedSize")
    public void setBedSize(Object bedSize) { this.bedSize = bedSize; }

    @PropertyName("bed_size")
    public Object getBedSizeFs() {
        return bedSize;
    }
    @PropertyName("bed_size")
    public void setBedSizeFs(Object bedSize) { this.bedSize = bedSize; }

    @PropertyName("tvSizeRange")
    public Object getTvSizeRange() {
        return tvSizeRange;
    }
    @PropertyName("tvSizeRange")
    public void setTvSizeRange(Object tvSizeRange) { this.tvSizeRange = tvSizeRange; }

    @PropertyName("tv_size_range")
    public Object getTvSizeRangeFs() {
        return tvSizeRange;
    }
    @PropertyName("tv_size_range")
    public void setTvSizeRangeFs(Object tvSizeRange) { this.tvSizeRange = tvSizeRange; }

    @PropertyName("reviewsSample")
    public List<Review> getReviewsSample() { return reviewsSample; }
    @PropertyName("reviewsSample")
    public void setReviewsSample(List<Review> reviewsSample) { this.reviewsSample = reviewsSample; }

    @PropertyName("colorVariants")
    public List<ColorVariant> getColorVariants() { return colorVariants; }
    @PropertyName("colorVariants")
    public void setColorVariants(List<ColorVariant> colorVariants) { this.colorVariants = colorVariants; }
    
    @PropertyName("color_variants")
    public List<ColorVariant> getColorVariantsFs() { return colorVariants; }
    @PropertyName("color_variants")
    public void setColorVariantsFs(List<ColorVariant> colorVariants) { this.colorVariants = colorVariants; }

    @PropertyName("sizeVariants")
    public List<SizeVariant> getSizeVariants() { return sizeVariants; }
    @PropertyName("sizeVariants")
    public void setSizeVariants(List<SizeVariant> sizeVariants) { this.sizeVariants = sizeVariants; }
    
    @PropertyName("size_variants")
    public List<SizeVariant> getSizeVariantsFs() { return sizeVariants; }
    @PropertyName("size_variants")
    public void setSizeVariantsFs(List<SizeVariant> sizeVariants) { this.sizeVariants = sizeVariants; }

    public String getImageUrl() {
        if (thumbnailUrl != null && !thumbnailUrl.isEmpty()) return thumbnailUrl;
        return (imageUrls != null && !imageUrls.isEmpty()) ? imageUrls.get(0) : "";
    }
}
