package io.hamlook.aetheria.core.features.misc;

import com.google.gson.annotations.Expose;
import io.hamlook.aetheria.core.moulconfig.gui.config.ConfigAnnotations.*;
import io.hamlook.aetheria.utils.Position;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class KillComboConfig {

    @Expose
    @ConfigOption(name = "Enable", desc = "Show the Kill Combo tracker overlay")
    @ConfigEditorBoolean
    public boolean enabled = true;

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
    @ConfigOption(name = "Hide Chat Messages", desc = "Hide kill combo milestone and expire messages from chat")
    @ConfigEditorBoolean
    public boolean hideChatMessages = false;

    @Expose
    @ConfigOption(name = "Background Color", desc = "Background color of the overlay (alpha controls opacity)")
    @ConfigEditorColour
    public String bgColor = "0:0:0:0:0";

    @Expose
    @ConfigOption(name = "Corner Radius", desc = "Roundness of the overlay corners")
    @ConfigEditorSliderAnnotation(minValue = 0f, maxValue = 12f, minStep = 1f)
    public int cornerRadius = 4;

    @Expose
    @ConfigOption(name = "Scale", desc = "Size of the overlay")
    @ConfigEditorSliderAnnotation(minValue = 0.5f, maxValue = 3f, minStep = 0.1f)
    public float scale = 1.0f;

    @Expose
    @ConfigOption(name = "Overlay Lines", desc = "Choose which lines to show. Drag to reorder, drag to trash to hide.")
    @ConfigEditorDraggableList(exampleText = {
        "Current Combo",
        "Highest Combo",
        "✯ Magic Find",
        "Coins Per Kill",
        "☯ Combat Wisdom"
    })
    public List<Integer> killComboLines = new ArrayList<>(Arrays.asList(0, 1, 2, 3, 4));

    @Expose
    @ConfigOption(name = "Edit Position", desc = "Drag to reposition the overlay")
    @ConfigEditorButton(runnableId = "openKillComboEditor", buttonText = "Edit")
    public boolean editPosDummy = false;

    @Expose
    @ConfigOption(name = "Reset Tracker", desc = "Wipe all tracked kill combo data")
    @ConfigEditorButton(runnableId = "resetKillCombo", buttonText = "Reset")
    public boolean resetKillComboDummy = false;

    @Expose
    public Position killComboPos = new Position(4, 200);
}
