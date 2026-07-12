package io.hamlook.aetheria.core.features.farming;

import com.google.gson.annotations.Expose;
import io.hamlook.aetheria.core.moulconfig.gui.config.ConfigAnnotations.*;

public class SensitivityReducerConfig {

    @Expose
    @ConfigOption(name = "Enable Sensitivity Reducer", desc = "Reduces your mouse sensitivity while holding a crop farming tool (Melon Dicer, Pumpkin Dicer, etc.)")
    @ConfigEditorBoolean
    public boolean enabled = false;

    @Expose
    @ConfigOption(name = "Sensitivity", desc = "Percentage of your normal sensitivity to use while holding a farming tool")
    @ConfigEditorSliderAnnotation(minValue = 10, maxValue = 100, minStep = 5)
    public float sensitivityPercent = 50f;

    @Expose
    @ConfigOption(name = "Require Farming Island", desc = "Only reduce sensitivity while on a farming location (Barn, Private Island, Garden)")
    @ConfigEditorBoolean
    public boolean requireFarmingIsland = false;
}
