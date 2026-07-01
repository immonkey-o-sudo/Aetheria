package io.hamlook.aetheria.core.features.diana;

import com.google.gson.annotations.Expose;
import io.hamlook.aetheria.core.moulconfig.gui.config.ConfigAnnotations.*;
import io.hamlook.aetheria.utils.Position;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class LootOverlayConfig {

    @Expose
    @ConfigOption(name = "Show Loot Overlay", desc = "Show the Diana Loot HUD")
    @ConfigEditorBoolean
    public boolean showLootOverlay = true;

    @Expose
    @ConfigOption(name = "Hide in Chat", desc = "Hide the overlay when the chat GUI is open")
    @ConfigEditorBoolean
    public boolean hideOnChat = true;

    @Expose
    @ConfigOption(name = "Hide on Tab", desc = "Hide the overlay when the tab list is shown")
    @ConfigEditorBoolean
    public boolean hideOnTab = true;

    @Expose
    @ConfigOption(name = "Hide on F3 Debug", desc = "Hide the overlay when the F3 debug screen is open")
    @ConfigEditorBoolean
    public boolean hideOnDebug = true;

    @Expose
    @ConfigOption(name = "Background Color", desc = "Background color of the Loot overlay (alpha controls opacity)")
    @ConfigEditorColour
    public String lootBgColor = "0:136:0:0:0";

    @Expose
    @ConfigOption(name = "Corner Radius", desc = "Roundness of the Loot overlay corners")
    @ConfigEditorSliderAnnotation(minValue = 0f, maxValue = 12f, minStep = 1f)
    public int lootCornerRadius = 4;

    @Expose
    @ConfigOption(name = "Scale", desc = "Size of the Loot overlay")
    @ConfigEditorSliderAnnotation(minValue = 0.5f, maxValue = 3f, minStep = 0.1f)
    public float lootScale = 1f;

    @Expose
    public Position lootOverlayPos = new Position(4, 310);

    @Expose
    @ConfigOption(name = "Loot Overlay Lines", desc = "Choose which lines to show. Drag to reorder, drag to trash to hide.")
    @ConfigEditorDraggableList(exampleText = {
        "Inqs since Chimera",
        "Chimeras",
        "Feathers",
        "Shelmets / Remedies / Plushies",
        "Daedalus Sticks",
        "Minos Relics",
        "Souvenirs / Crowns",
        "Coins",
        "Estimated Profit"
    })
    public List<Integer> lootLines = new ArrayList<>(Arrays.asList(0, 1, 2, 3, 4, 5, 6, 7, 8));
}
