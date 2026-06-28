package com.yn.homi.data.model;

import java.util.List;

public class ShopSection {
    private RoomCategory category;
    private List<RoomSubCategory> subCategories;

    public ShopSection(RoomCategory category, List<RoomSubCategory> subCategories) {
        this.category = category;
        this.subCategories = subCategories;
    }

    public RoomCategory getCategory() {
        return category;
    }

    public List<RoomSubCategory> getSubCategories() {
        return subCategories;
    }
}
