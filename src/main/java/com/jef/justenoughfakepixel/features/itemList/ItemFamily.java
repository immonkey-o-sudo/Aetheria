package com.jef.justenoughfakepixel.features.itemList;

import java.util.ArrayList;
import java.util.List;

public class ItemFamily {

    public final String familyId;
    public String displayName;
    public String cleanDisplayName;
    public String cleanDisplayNameLower;
    public final FamilyType type;
    public final List<SkyblockItem> members = new ArrayList<>();

    public enum FamilyType { ENCHANTMENT, PET, ACCESSORY, NONE }

    public ItemFamily(String familyId, String displayName, FamilyType type) {
        this.familyId    = familyId;
        this.type        = type;
        updateDisplayName(displayName);
    }

    public void updateDisplayName(String newName) {
        this.displayName = newName;
        this.cleanDisplayName = newName != null ? newName.replaceAll("§.", "") : "";
        this.cleanDisplayNameLower = this.cleanDisplayName.toLowerCase();
    }

    public SkyblockItem representative() {
        return members.isEmpty() ? null : members.get(0);
    }

    public boolean hasDropdown() {
        return members.size() > 1;
    }
}