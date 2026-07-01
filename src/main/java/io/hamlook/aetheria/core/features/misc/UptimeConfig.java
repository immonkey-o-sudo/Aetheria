package io.hamlook.aetheria.core.features.misc;

import com.google.gson.annotations.Expose;
import io.hamlook.aetheria.core.moulconfig.gui.config.ConfigAnnotations.*;
import io.hamlook.aetheria.utils.Position;

public class UptimeConfig {

    @Expose
    @ConfigOption(name = "Enable Timer Overlay", desc = "Show a draggable countdown timer HUD on screen")
    @ConfigEditorBoolean
    public boolean uptimeEnabled = true;

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
    @ConfigOption(name = "Background Color", desc = "Background color of the timer overlay")
    @ConfigEditorColour
    public String uptimeBgColor = "0:136:0:0:0";

    @Expose
    @ConfigOption(name = "Corner Radius", desc = "Roundness of the overlay corners")
    @ConfigEditorSliderAnnotation(minValue = 0f, maxValue = 12f, minStep = 1f)
    public int uptimeCornerRadius = 4;

    @Expose
    @ConfigOption(name = "Scale", desc = "Size of the timer overlay")
    @ConfigEditorSliderAnnotation(minValue = 0.5f, maxValue = 3f, minStep = 0.1f)
    public float uptimeScale = 1f;

    @Expose
    @ConfigOption(name = "Show When Expired", desc = "Keep the overlay visible with a 'Done!' label after the timer finishes")
    @ConfigEditorBoolean
    public boolean uptimeShowWhenExpired = true;

    @Expose
    @ConfigOption(name = "Edit Overlay Position", desc = "Drag the timer overlay to reposition it")
    @ConfigEditorButton(runnableId = "openUptimeEditor", buttonText = "Edit")
    public boolean uptimeEditPosDummy = false;

    @Expose
    public Position uptimePos = new Position(2, 60, false, false);
}