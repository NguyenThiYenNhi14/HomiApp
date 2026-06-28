// Address.java
package com.yn.homi.ui.profile.address;

import com.google.firebase.firestore.Exclude;

import java.io.Serializable;

// Model lưu thông tin 1 địa chỉ, tương thích với Firestore
public class Address implements Serializable {
    @Exclude
    private String id;           // Document ID trong Firestore
    private String label;        // "Home", "Work", "Other"
    private String recipientName;
    private String phone;
    private String street;
    private String ward;
    private String district;
    private String city;
    private boolean isDefault;

    // Cần constructor trống cho Firestore
    public Address() {
    }

    public Address(String id, String label, String recipientName, String phone,
                   String street, String ward, String district, String city,
                   boolean isDefault) {
        this.id            = id;
        this.label         = label;
        this.recipientName = recipientName;
        this.phone         = phone;
        this.street        = street;
        this.ward          = ward;
        this.district      = district;
        this.city          = city;
        this.isDefault     = isDefault;
    }

    // Trả về địa chỉ đầy đủ thành 1 chuỗi
    @Exclude
    public String getFullAddress() {
        StringBuilder sb = new StringBuilder();
        if (street != null && !street.isEmpty()) sb.append(street);
        if (ward != null && !ward.isEmpty()) sb.append(", ").append(ward);
        if (district != null && !district.isEmpty()) sb.append(", ").append(district);
        if (city != null && !city.isEmpty()) sb.append(", ").append(city);
        return sb.toString();
    }

    // Getters
    public String  getId()            { return id; }
    public String  getLabel()         { return label; }
    public String  getRecipientName() { return recipientName; }
    public String  getPhone()         { return phone; }
    public String  getStreet()        { return street; }
    public String  getWard()          { return ward; }
    public String  getDistrict()      { return district; }
    public String  getCity()          { return city; }
    public boolean isDefault()        { return isDefault; }

    // Setters
    public void setId(String id)               { this.id = id; }
    public void setDefault(boolean isDefault)  { this.isDefault = isDefault; }
    public void setLabel(String label)          { this.label = label; }
    public void setRecipientName(String name)  { this.recipientName = name; }
    public void setPhone(String phone)         { this.phone = phone; }
    public void setStreet(String street)       { this.street = street; }
    public void setWard(String ward)           { this.ward = ward; }
    public void setDistrict(String district)   { this.district = district; }
    public void setCity(String city)           { this.city = city; }
}
