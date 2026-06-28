package com.yn.homi.models;

import com.google.firebase.firestore.PropertyName;
import java.io.Serializable;

public class Review implements Serializable {
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

    @PropertyName("productId")
    public String getProductId() { return productId; }
    @PropertyName("productId")
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
