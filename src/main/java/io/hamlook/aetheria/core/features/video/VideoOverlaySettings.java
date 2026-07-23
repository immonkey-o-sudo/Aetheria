package io.hamlook.aetheria.core.features.video;

import com.google.gson.annotations.Expose;
import io.hamlook.aetheria.core.moulconfig.gui.config.ConfigAnnotations.ConfigEditorBoolean;
import io.hamlook.aetheria.core.moulconfig.gui.config.ConfigAnnotations.ConfigEditorKeybind;
import io.hamlook.aetheria.core.moulconfig.gui.config.ConfigAnnotations.ConfigEditorSliderAnnotation;
import io.hamlook.aetheria.core.moulconfig.gui.config.ConfigAnnotations.ConfigEditorText;
import io.hamlook.aetheria.core.moulconfig.gui.config.ConfigAnnotations.ConfigOption;
import org.lwjgl.input.Keyboard;

/**
 * In-game settings for the Video Overlay feature, editable from the normal
 * Aetheria config GUI (previously this only lived in a hand-edited JSON at
 * config/Aetheria/video-overlay.json, which never showed up in-game).
 */
public class VideoOverlaySettings {

    @Expose
    @ConfigOption(name = "Video URL", desc = "YouTube, Twitch, or a direct file/stream URL (.mp4, .m3u8, rtmp://, ...)")
    @ConfigEditorText
    public String videoUrl = "";

    @Expose
    @ConfigOption(name = "Toggle Key", desc = "Swaps between fullscreen video and fullscreen gameplay")
    @ConfigEditorKeybind(defaultKey = Keyboard.KEY_F10)
    public int toggleKeyCode = Keyboard.KEY_F10;

    @Expose
    @ConfigOption(name = "Volume", desc = "Video playback volume")
    @ConfigEditorSliderAnnotation(minValue = 0f, maxValue = 100f, minStep = 1f)
    public int volume = 80;

    @Expose
    @ConfigOption(name = "Mute Game While Fullscreen", desc = "Mutes Minecraft's master volume while the video overlay is open")
    @ConfigEditorBoolean
    public boolean muteGameWhileFullscreen = true;

    @Expose
    @ConfigOption(name = "Loop", desc = "Restarts playback automatically when the video ends")
    @ConfigEditorBoolean
    public boolean loop = true;
}
