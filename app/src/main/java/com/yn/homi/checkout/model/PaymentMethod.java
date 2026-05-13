package com.yn.homi.checkout.model;

public class PaymentMethod {
    private int    iconRes;      // drawable resource id
    private String name;         // "PayPal", "Mastercard"
    private String detail;       // email or masked card number
    private boolean selected;

    public PaymentMethod(int iconRes, String name, String detail, boolean selected) {
        this.iconRes  = iconRes;
        this.name     = name;
        this.detail   = detail;
        this.selected = selected;
    }

    public int     getIconRes()             { return iconRes; }
    public String  getName()                { return name; }
    public String  getDetail()              { return detail; }
    public boolean isSelected()             { return selected; }
    public void    setSelected(boolean s)   { this.selected = s; }
}