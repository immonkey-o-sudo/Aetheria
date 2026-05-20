package com.jef.justenoughfakepixel.core.features.overlays;

import com.google.gson.annotations.Expose;
import com.jef.justenoughfakepixel.core.config.gui.config.ConfigAnnotations;

public class Overlays {

    @Expose
    @ConfigAnnotations.Category(name = "Profile Viewer", desc = "Settings for the profile viewer GUI")
    public ProfileViewerConfig profileViewer = new ProfileViewerConfig();

    @Expose
    @ConfigAnnotations.ConfigOption(name = "Item List GUI Scale", desc = "Configure the grid scale of the item list")
    @ConfigAnnotations.ConfigEditorSliderAnnotation(minValue = 0.75f,maxValue = 2f,minStep = 0.1f)
    public float itemListScale = 1f;

}
