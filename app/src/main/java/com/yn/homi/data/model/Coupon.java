package com.yn.homi.data.model;

import com.google.firebase.firestore.Exclude;
import java.io.Serializable;

public class Coupon implements Serializable {
    @Exclude
    private String id;
    private String type;
    private String code;
    private String discountType; // "fixed" hoặc "percent"
    private double discountValue;
    private boolean isUsed;
    private com.google.firebase.Timestamp createdAt;
    private com.google.firebase.Timestamp expiryDate;

    public Coupon() {}

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getType() { return type; }
    public void setType(String type) { this.type = type; }
    public String getCode() { return code; }
    public void setCode(String code) { this.code = code; }
    public String getDiscountType() { return discountType; }
    public void setDiscountType(String discountType) { this.discountType = discountType; }
    public double getDiscountValue() { return discountValue; }
    public void setDiscountValue(double discountValue) { this.discountValue = discountValue; }
    public boolean isUsed() { return isUsed; }
    public void setUsed(boolean used) { isUsed = used; }
    public com.google.firebase.Timestamp getCreatedAt() { return createdAt; }
    public void setCreatedAt(com.google.firebase.Timestamp createdAt) { this.createdAt = createdAt; }
    public com.google.firebase.Timestamp getExpiryDate() { return expiryDate; }
    public void setExpiryDate(com.google.firebase.Timestamp expiryDate) { this.expiryDate = expiryDate; }

    @Exclude
    public double calculateDiscount(double subtotal) {
        if ("percent".equals(discountType)) {
            return subtotal * (discountValue / 100.0);
        } else {
            return Math.min(discountValue, subtotal); // fixed, không giảm quá subtotal
        }
    }
}