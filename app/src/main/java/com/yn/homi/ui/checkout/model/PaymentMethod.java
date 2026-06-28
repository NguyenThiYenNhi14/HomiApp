package com.yn.homi.ui.checkout.model;

import java.io.Serializable;

public class PaymentMethod implements Serializable {
    private int iconRes;
    private String name;
    private String description;
    private boolean isSelected;

    public PaymentMethod(int iconRes, String name, String description, boolean isSelected) {
        this.iconRes = iconRes;
        this.name = name;
        this.description = description;
        this.isSelected = isSelected;
    }

    public int getIconRes() { return iconRes; }
    public String getName() { return name; }
    public String getDescription() { return description; }
    public boolean isSelected() { return isSelected; }
    public void setSelected(boolean selected) { isSelected = selected; }
}
