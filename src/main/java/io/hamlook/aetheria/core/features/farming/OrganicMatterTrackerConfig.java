package io.hamlook.aetheria.core.features.farming;

import com.google.gson.annotations.Expose;
import io.hamlook.aetheria.core.moulconfig.gui.config.ConfigAnnotations.*;
import io.hamlook.aetheria.utils.Position;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class OrganicMatterTrackerConfig {

    @Expose
    @ConfigOption(name = "Enable", desc = "Track Organic Matter and show the organic matter overlay")
    @ConfigEditorBoolean
    public boolean enabled = false;

    @Expose
    @ConfigOption(name = "Require Farming Location", desc = "Only track Organic Matter while in farming locations (Barn, Private Island, Garden)")
    @ConfigEditorBoolean
    public boolean requireFarmingIsland = true;

    @Expose
    @ConfigOption(name = "Keep Tracker Across Sessions", desc = "Persist item counts and Organic Matter across world unloads / game restarts instead of resetting")
    @ConfigEditorBoolean
    public boolean persistAcrossSessions = true;

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
    @ConfigOption(name = "Tracked Crops", desc = "Choose which crops (including Seeds) count toward Organic Matter")
    @ConfigEditorDraggableList(exampleText = {
            "Wheat", "Carrot", "Potato", "Pumpkin", "Melon", "Sugar Cane", "Cocoa Beans",
            "Cactus", "Red Mushroom", "Brown Mushroom", "Nether Wart", "Seeds",
            "Squash", "Cropie", "Fermento"
    })
    public List<Integer> trackedCrops = new ArrayList<>(Arrays.asList(12, 13, 14));

    @Expose
    @ConfigOption(name = "Display Lines", desc = "Choose which lines to show and drag to reorder")
    @ConfigEditorDraggableList(exampleText = {
            "§a§lOrganic Matter Tracker",
            "§7Total Organic Matter: §f4,830,000",
            "§b402,500/h organic matter",
            "§aWheat: §f2,304 §b(2,304/h)",
            "§aCarrot: §f1,120 §b(325/h)",
            "§aPotato: §f980 §b(323/h)",
            "§aPumpkin: §f560 §b(560/h)",
            "§aMelon: §f92 §b(166/h)",
            "§aSugar Cane: §f640 §b(320/h)",
            "§aCocoa Beans: §f300 §b(120/h)",
            "§aCactus: §f150 §b(75/h)",
            "§aRed Mushroom: §f80 §b(80/h)",
            "§aBrown Mushroom: §f75 §b(75/h)",
            "§aNether Wart: §f420 §b(139/h)",
            "§aSeeds: §f4,000 §b(4,000/h)",
            "§aSquash: §f12 §b(120,000/h)",
            "§aCropie: §f30 §b(75,000/h)",
            "§aFermento: §f8 §b(160,000/h)",
            "§7Session: §f42:17",
            "§bTotal: §f108,240 items"
    })
    public List<Integer> organicMatterDisplayLines = new ArrayList<>(Arrays.asList(0, 1, 2, 19, 18, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17));

    @Expose
    @ConfigOption(name = "Edit Position", desc = "Drag to reposition the organic matter tracker overlay")
    @ConfigEditorButton(runnableId = "openOrganicMatterTrackerEditor", buttonText = "Edit")
    public boolean editOrganicMatterTrackerPosDummy = false;

    @Expose
    @ConfigOption(name = "Reset Tracker", desc = "Wipe the current session's tracked Organic Matter")
    @ConfigEditorButton(runnableId = "resetOrganicMatterTracker", buttonText = "Reset")
    public boolean resetOrganicMatterTrackerDummy = false;

    @Expose
    @ConfigOption(name = "Scale", desc = "Size of the organic matter tracker overlay")
    @ConfigEditorSliderAnnotation(minValue = 0.5f, maxValue = 3f, minStep = 0.1f)
    public float organicMatterTrackerScale = 1f;

    @Expose
    @ConfigOption(name = "Background Color", desc = "Background color of the organic matter tracker overlay")
    @ConfigEditorColour
    public int organicMatterTrackerBgColor = 0x80000000;

    @Expose
    @ConfigOption(name = "Corner Radius", desc = "Roundness of the organic matter tracker overlay corners")
    @ConfigEditorSliderAnnotation(minValue = 0f, maxValue = 12f, minStep = 1f)
    public int organicMatterTrackerCornerRadius = 4;

    @Expose
    public Position organicMatterTrackerPosition = new Position(10, 200, false, false);
}

