// Address.java
package com.yn.homi.setting.address;

// Model lưu thông tin 1 địa chỉ
public class Address {
    private int    id;
    private String label;        // "Home", "Work", "Other"
    private String recipientName;
    private String phone;
    private String street;
    private String ward;
    private String district;
    private String city;
    private boolean isDefault;

    public Address(int id, String label, String recipientName, String phone,
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
    public String getFullAddress() {
        return street + ", " + ward + ", " + district + ", " + city;
    }

    // Getters
    public int     getId()            { return id; }
    public String  getLabel()         { return label; }
    public String  getRecipientName() { return recipientName; }
    public String  getPhone()         { return phone; }
    public String  getStreet()        { return street; }
    public String  getWard()          { return ward; }
    public String  getDistrict()      { return district; }
    public String  getCity()          { return city; }
    public boolean isDefault()        { return isDefault; }

    // Setters
    public void setDefault(boolean isDefault) { this.isDefault = isDefault; }
    public void setLabel(String label)         { this.label = label; }
}