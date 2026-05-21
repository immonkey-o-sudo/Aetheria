package com.jef.justenoughfakepixel.core.features.overlays;

import com.google.gson.annotations.Expose;
import com.jef.justenoughfakepixel.core.config.gui.config.ConfigAnnotations.*;

public class ItemListConfig {

    @Expose
    @ConfigOption(name = "Use Global SearchBar", desc = "If enabled, the item list search bar is removed, and the global search bar is used")
    @ConfigEditorBoolean
    public boolean searchItemList = true;

    @Expose
    @ConfigOption(name = "Item List GUI Scale", desc = "Configure the grid scale of the item list")
    @ConfigEditorSliderAnnotation(minValue = 0.5f,maxValue = 2f,minStep = 0.1f)
    public float itemListScale = 1f;
}
