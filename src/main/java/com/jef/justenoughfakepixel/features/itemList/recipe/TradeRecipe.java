package com.jef.justenoughfakepixel.features.itemList.recipe;

import com.google.gson.JsonObject;
import com.jef.justenoughfakepixel.features.itemList.SkyblockItem;

public class TradeRecipe extends ForgeRecipe {
    public TradeRecipe(SkyblockItem t, JsonObject d) { super(t, d, "trade"); }
}