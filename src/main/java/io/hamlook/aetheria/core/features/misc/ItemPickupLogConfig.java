package io.hamlook.aetheria.core.features.misc;

import com.google.gson.annotations.Expose;
import io.hamlook.aetheria.core.moulconfig.gui.config.ConfigAnnotations.*;
import io.hamlook.aetheria.utils.Position;

public class ItemPickupLogConfig {

    @Expose
    @ConfigOption(name = "Enable", desc = "Show a HUD list of recently picked-up or lost items")
    @ConfigEditorBoolean
    public boolean itemPickupLog = true;

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
    @ConfigOption(name = "Background Color", desc = "Background color of the log (alpha controls opacity)")
    @ConfigEditorColour
    public String itemPickupLogBgColor = "160:0:0:0:0";

    @Expose
    @ConfigOption(name = "Corner Radius", desc = "Roundness of the log corners")
    @ConfigEditorSliderAnnotation(minValue = 0f, maxValue = 12f, minStep = 1f)
    public int itemPickupLogCornerRadius = 4;

    @Expose
    @ConfigOption(name = "Scale", desc = "Size of the item pickup log")
    @ConfigEditorSliderAnnotation(minValue = 0.5f, maxValue = 3f, minStep = 0.1f)
    public float itemPickupLogScale = 1f;

    @Expose
    @ConfigOption(name = "Edit Position", desc = "Drag to reposition the item pickup log")
    @ConfigEditorButton(runnableId = "openItemPickupLogEditor", buttonText = "Edit")
    public boolean editItemPickupLogPosDummy = false;

    @Expose
    public Position itemPickupLogPos = new Position(2, 60);
}
