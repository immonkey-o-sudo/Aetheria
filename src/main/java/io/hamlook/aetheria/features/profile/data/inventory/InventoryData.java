package io.hamlook.aetheria.features.profile.data.inventory;

import io.hamlook.aetheria.features.profile.data.ItemData;
import io.hamlook.aetheria.features.profile.vars.EquipmentSlot;
import lombok.AllArgsConstructor;

import java.util.HashMap;

@AllArgsConstructor
public class InventoryData {

    public HashMap<EquipmentSlot, ItemData> armorData;
    public HashMap<Integer, ItemData> invData;

}
