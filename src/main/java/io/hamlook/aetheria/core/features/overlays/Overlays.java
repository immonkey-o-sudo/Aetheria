package io.hamlook.aetheria.core.features.overlays;

import com.google.gson.annotations.Expose;
import io.hamlook.aetheria.core.config.gui.config.ConfigAnnotations;

public class Overlays {

    @Expose
    @ConfigAnnotations.Category(name = "Profile Viewer", desc = "Settings for the profile viewer GUI")
    public ProfileViewerConfig profileViewer = new ProfileViewerConfig();

    @Expose
    @ConfigAnnotations.Category(name = "Item List", desc = "Settings for the Item List Overlay")
    public ItemListConfig itemList = new ItemListConfig();

}
