package io.hamlook.aetheria.core.features.misc;

import com.google.gson.annotations.Expose;
import io.hamlook.aetheria.core.config.gui.config.ConfigAnnotations;

public class ItemListConfig {

    @Expose
    @ConfigAnnotations.ConfigOption(name = "Item List UI", desc = "Show the Item List overlay on container screens")
    @ConfigAnnotations.ConfigEditorBoolean
    public boolean enabled = true;

    @Expose
    @ConfigAnnotations.ConfigOption(name = "Use Global SearchBar", desc = "If enabled, the item list search bar is removed, and the global search bar is used")
    @ConfigAnnotations.ConfigEditorBoolean
    public boolean searchItemList = true;

    @Expose
    @ConfigAnnotations.ConfigOption(name = "Item List GUI Scale", desc = "Configure the grid scale of the item list")
    @ConfigAnnotations.ConfigEditorSliderAnnotation(minValue = 0.5f, maxValue = 2f, minStep = 0.1f)
    public float itemListScale = 1f;

    @Expose
    @ConfigAnnotations.ConfigOption(name = "Search Only Item List", desc = "Only show item list when the button to search for item list is enabled in global search bar, requires Use Global SearchBar to work")
    @ConfigAnnotations.ConfigEditorBoolean
    public boolean itemListSOnly = false;

    @Expose
    @ConfigAnnotations.ConfigOption(name = "Wiki Source", desc = "Which wiki opens when you right-click an item")
    @ConfigAnnotations.ConfigEditorDropdown(values = {"hysb.wiki", "Hypixel Wiki (Official)"})
    public int wikiSource = 0;
}