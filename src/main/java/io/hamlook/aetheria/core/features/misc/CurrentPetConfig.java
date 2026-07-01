package io.hamlook.aetheria.core.features.misc;

import com.google.gson.annotations.Expose;
import io.hamlook.aetheria.core.moulconfig.gui.config.ConfigAnnotations.*;
import io.hamlook.aetheria.utils.Position;

public class CurrentPetConfig {

    @Expose
    @ConfigOption(name = "Enable", desc = "Show the current pet overlay")
    @ConfigEditorBoolean
    public boolean showCurrentPet = true;

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
    @ConfigOption(name = "Background Color", desc = "Background color of the pet overlay (alpha controls opacity)")
    @ConfigEditorColour
    public String currentPetBgColor = "0:0:0:0:0";

    @Expose
    @ConfigOption(name = "Corner Radius", desc = "Roundness of the pet overlay corners")
    @ConfigEditorSliderAnnotation(minValue = 0f, maxValue = 12f, minStep = 1f)
    public int currentPetCornerRadius = 4;

    @Expose
    @ConfigOption(name = "Scale", desc = "Size of the pet overlay")
    @ConfigEditorSliderAnnotation(minValue = 0.5f, maxValue = 3f, minStep = 0.1f)
    public float currentPetScale = 1.5f;

    @Expose
    @ConfigOption(name = "Edit Position", desc = "Drag to reposition the pet overlay")
    @ConfigEditorButton(runnableId = "openCurrentPetEditor", buttonText = "Edit")
    public boolean editCurrentPetPosDummy = false;

    @Expose
    public Position currentPetPos = new Position(18, 14);
}
