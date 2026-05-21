package io.hamlook.aetheria.features.itemList.recipe;

import com.google.gson.JsonObject;
import io.hamlook.aetheria.features.itemList.SkyblockItem;

public class TradeRecipe extends ForgeRecipe {
    public TradeRecipe(SkyblockItem t, JsonObject d) { super(t, d, "trade"); }
}