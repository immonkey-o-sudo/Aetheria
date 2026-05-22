package io.hamlook.aetheria.features.profile.data;

import lombok.AllArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@AllArgsConstructor
public class ItemData {

    public String displayName;
    public List<String> lore;
    public String skyblockID;
    public boolean enchanted;

    public ItemData(){
        this.skyblockID = "";
        this.displayName = "";
        this.lore = new ArrayList<>();
        this.enchanted = false;
    }
}
