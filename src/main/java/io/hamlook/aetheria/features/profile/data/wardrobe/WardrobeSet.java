package io.hamlook.aetheria.features.profile.data.wardrobe;

import io.hamlook.aetheria.features.profile.data.ItemData;
import lombok.AllArgsConstructor;

@AllArgsConstructor
public class WardrobeSet {

    public ItemData helmet;
    public ItemData chestplate;
    public ItemData leggings;
    public ItemData boots;
    public boolean equipped;
}
