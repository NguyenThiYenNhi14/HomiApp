package com.yn.homi.model;

public class PaymentCard {

    public enum PaymentType { MOMO, ZALOPAY, VISA, MASTERCARD, BANK }

    private String id;
    private PaymentType type;
    private String displayName;
    private String accountNumber;
    private String subInfo;
    private boolean isDefault;
    private int iconColor;

    public PaymentCard(String id, PaymentType type, String displayName,
                       String accountNumber, String subInfo,
                       boolean isDefault, int iconColor) {
        this.id = id;
        this.type = type;
        this.displayName = displayName;
        this.accountNumber = accountNumber;
        this.subInfo = subInfo;
        this.isDefault = isDefault;
        this.iconColor = iconColor;
    }

    public String getId() { return id; }
    public PaymentType getType() { return type; }
    public String getDisplayName() { return displayName; }
    public String getAccountNumber() { return accountNumber; }
    public String getSubInfo() { return subInfo; }
    public boolean isDefault() { return isDefault; }
    public int getIconColor() { return iconColor; }
    public void setDefault(boolean d) { this.isDefault = d; }
}