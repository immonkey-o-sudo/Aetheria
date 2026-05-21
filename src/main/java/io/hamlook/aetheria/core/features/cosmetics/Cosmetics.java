package io.hamlook.aetheria.core.features.cosmetics;

import com.google.gson.annotations.Expose;
import io.hamlook.aetheria.core.config.gui.config.ConfigAnnotations.*;
public class Cosmetics {

    @Expose
    @Category(name = "Capes", desc = "Settings for the Capes")
    public CapesConfig capes = new CapesConfig();
}