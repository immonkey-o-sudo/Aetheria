package io.hamlook.aetheria.features.video;

import io.hamlook.aetheria.core.GsonBuilder;
import org.lwjgl.input.Keyboard;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;

/**
 * Standalone config for the Video Overlay feature.
 * <p>
 * Deliberately does NOT hook into ATHRConfig / StorageManager, since those
 * currently depend on GUI-editor and storage classes that aren't part of
 * this codebase snapshot. This keeps the feature compilable on its own.
 * <p>
 * File lives at config/Aetheria/video-overlay.json. Edit it by hand for now
 * (no in-game GUI editor exists for this yet) — the file is recreated with
 * defaults if missing or corrupt.
 */
public class VideoOverlayConfig {

    private static final File DIR = new File("config/Aetheria");
    private static final File FILE = new File(DIR, "video-overlay.json");

    /** The last video URL set. YouTube, Twitch, or a direct file/stream URL. */
    public String videoUrl = "";

    /** LWJGL2 keycode (org.lwjgl.input.Keyboard.KEY_*) that toggles fullscreen swap. Default: F10. */
    public int toggleKeyCode = Keyboard.KEY_F10;

    /** 0-100 VLC volume for the video. */
    public int volume = 80;

    /** If true, Minecraft's master sound volume is set to 0 while the video overlay is open, and restored on close. */
    public boolean muteGameWhileFullscreen = true;

    /** Loop the video when it ends. */
    public boolean loop = true;

    private static VideoOverlayConfig instance;

    public static VideoOverlayConfig get() {
        if (instance == null) instance = load();
        return instance;
    }

    public void save() {
        instance = this;
        try {
            DIR.mkdirs();
            File tmp = new File(DIR, "video-overlay.json.tmp");
            try (Writer w = new BufferedWriter(new OutputStreamWriter(
                    Files.newOutputStream(tmp.toPath()), StandardCharsets.UTF_8))) {
                GsonBuilder.GSON.toJson(this, w);
            }
            Files.move(tmp.toPath(), FILE.toPath(), StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            System.err.println("[ATHR/VideoOverlay] Failed to save config: " + e.getMessage());
        }
    }

    private static VideoOverlayConfig load() {
        if (FILE.exists()) {
            try (Reader r = new BufferedReader(new InputStreamReader(
                    Files.newInputStream(FILE.toPath()), StandardCharsets.UTF_8))) {
                VideoOverlayConfig loaded = GsonBuilder.GSON.fromJson(r, VideoOverlayConfig.class);
                if (loaded != null) return loaded;
            } catch (Exception e) {
                System.err.println("[ATHR/VideoOverlay] Failed to load config, using defaults: " + e.getMessage());
            }
        }
        VideoOverlayConfig fresh = new VideoOverlayConfig();
        fresh.save();
        return fresh;
    }
}
