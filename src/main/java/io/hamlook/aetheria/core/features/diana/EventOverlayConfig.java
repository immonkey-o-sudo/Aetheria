package io.hamlook.aetheria.core.features.diana;

import com.google.gson.annotations.Expose;
import io.hamlook.aetheria.core.moulconfig.gui.config.ConfigAnnotations.*;
import io.hamlook.aetheria.utils.Position;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class EventOverlayConfig {

    @Expose
    @ConfigOption(name = "Show Event Overlay", desc = "Show the Diana Event HUD")
    @ConfigEditorBoolean
    public boolean showEventOverlay = true;

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
    @ConfigOption(name = "Background Color", desc = "Background color of the Event overlay (alpha controls opacity)")
    @ConfigEditorColour
    public String eventBgColor = "0:136:0:0:0";

    @Expose
    @ConfigOption(name = "Corner Radius", desc = "Roundness of the Event overlay corners")
    @ConfigEditorSliderAnnotation(minValue = 0f, maxValue = 12f, minStep = 1f)
    public int eventCornerRadius = 4;

    @Expose
    @ConfigOption(name = "Scale", desc = "Size of the Event overlay")
    @ConfigEditorSliderAnnotation(minValue = 0.5f, maxValue = 3f, minStep = 0.1f)
    public float eventScale = 1f;

    @Expose
    public Position eventOverlayPos = new Position(4, 200);

    @Expose
    @ConfigOption(name = "Event Overlay Lines", desc = "Choose which lines to show. Drag to reorder, drag to trash to hide.")
    @ConfigEditorDraggableList(exampleText = {
        "Total Mobs",
        "Playtime / Session",
        "Burrows",
        "Inquisitor",
        "Minotaur",
        "Minos Champion",
        "Gaia Construct",
        "Minos Hunter",
        "Siamese Lynx"
    })
    public List<Integer> eventLines = new ArrayList<>(Arrays.asList(0, 1, 2, 3, 4, 5, 6, 7, 8));
}
