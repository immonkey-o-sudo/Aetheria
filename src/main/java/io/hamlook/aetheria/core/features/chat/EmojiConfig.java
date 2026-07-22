package io.hamlook.aetheria.core.features.chat;

import com.google.gson.annotations.Expose;
import io.hamlook.aetheria.core.moulconfig.gui.config.ConfigAnnotations.ConfigEditorBoolean;
import io.hamlook.aetheria.core.moulconfig.gui.config.ConfigAnnotations.ConfigEditorSliderAnnotation;
import io.hamlook.aetheria.core.moulconfig.gui.config.ConfigAnnotations.ConfigOption;

public class EmojiConfig {

    @Expose
    @ConfigOption(name = "Enable", desc = "Render :emoji_name: tokens in chat as emoji textures instead of plain text. Other players without the mod just see the plain text.")
    @ConfigEditorBoolean
    public boolean enabled = true;

    @Expose
    @ConfigOption(name = "Suggestions", desc = "Show a popup with matching emoji while typing :name in the chat box (Tab to complete)")
    @ConfigEditorBoolean
    public boolean suggestionsEnabled = true;

    @Expose
    @ConfigOption(name = "Emoji Scale", desc = "Size of rendered emoji relative to normal chat text")
    @ConfigEditorSliderAnnotation(minValue = 0.5f, maxValue = 2f, minStep = 0.1f)
    public float scale = 1f;
}
