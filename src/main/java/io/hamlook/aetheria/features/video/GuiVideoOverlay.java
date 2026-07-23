package io.hamlook.aetheria.features.video;

import net.minecraft.client.audio.SoundCategory;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.WorldRenderer;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import org.lwjgl.input.Keyboard;
import org.lwjgl.opengl.GL11;

import java.io.IOException;

/**
 * The "video is fullscreen" side of the swap. Opening this screen (via
 * {@code mc.displayGuiScreen(new GuiVideoOverlay())}) covers the whole window
 * with the live video frame; closing it ({@code mc.displayGuiScreen(null)})
 * hands the fullscreen back to gameplay. Minecraft's own screen-open/close
 * logic already handles mouse grab/release for us.
 * <p>
 * Note: class names for Tessellator/WorldRenderer/GlStateManager/VertexFormats
 * reflect the vanilla 1.8.9 renderer layout used elsewhere in this pack; adjust
 * imports here if your workspace's deobf mappings name them slightly differently.
 */
public class GuiVideoOverlay extends GuiScreen {

    private final VideoFrameTexture texture = new VideoFrameTexture();
    private float previousMasterVolume = 1.0f;
    private boolean startAttempted = false;

    @Override
    public void initGui() {
        super.initGui();
        VideoOverlayConfig cfg = VideoOverlayConfig.get();

        if (cfg.muteGameWhileFullscreen) {
            previousMasterVolume = mc.gameSettings.getSoundLevel(SoundCategory.MASTER);
            mc.gameSettings.setSoundLevel(SoundCategory.MASTER, 0.0f);
        }

        if (!startAttempted) {
            startAttempted = true;
            new Thread(() -> {
                try {
                    VideoPlayer.get().playUrlBlocking(cfg.videoUrl);
                } catch (Exception e) {
                    System.err.println("[ATHR/VideoOverlay] Failed to start playback: " + e.getMessage());
                }
            }, "ATHR-VideoOverlay-Start").start();
        } else {
            VideoPlayer.get().resume();
        }
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        drawDefaultBackground();
        texture.update();

        if (texture.hasFrame()) {
            drawVideoQuad();
        } else {
            String msg = "Loading video...";
            drawCenteredString(fontRendererObj, msg, width / 2, height / 2, 0xFFFFFF);
        }

        String hint = "Press the overlay hotkey (or Esc) to return to the game";
        drawString(fontRendererObj, hint, 6, height - 14, 0x88FFFFFF);
    }

    private void drawVideoQuad() {
        // Fit the video into the window preserving aspect ratio, letterboxed.
        float videoAspect = (float) texture.getWidth() / (float) texture.getHeight();
        float screenAspect = (float) width / (float) height;

        float drawW, drawH;
        if (screenAspect > videoAspect) {
            drawH = height;
            drawW = height * videoAspect;
        } else {
            drawW = width;
            drawH = width / videoAspect;
        }
        float x0 = (width - drawW) / 2f;
        float y0 = (height - drawH) / 2f;

        GlStateManager.enableTexture2D();
        GlStateManager.bindTexture(texture.getTextureId());
        GlStateManager.color(1f, 1f, 1f, 1f);

        Tessellator tessellator = Tessellator.getInstance();
        WorldRenderer wr = tessellator.getWorldRenderer();
        wr.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_TEX);
        wr.pos(x0, y0 + drawH, 0).tex(0, 1).endVertex();
        wr.pos(x0 + drawW, y0 + drawH, 0).tex(1, 1).endVertex();
        wr.pos(x0 + drawW, y0, 0).tex(1, 0).endVertex();
        wr.pos(x0, y0, 0).tex(0, 0).endVertex();
        tessellator.draw();
    }

    @Override
    protected void keyTyped(char typedChar, int keyCode) throws IOException {
        VideoOverlayConfig cfg = VideoOverlayConfig.get();
        if (keyCode == Keyboard.KEY_ESCAPE || keyCode == cfg.toggleKeyCode) {
            mc.displayGuiScreen(null);
            return;
        }
        super.keyTyped(typedChar, keyCode);
    }

    @Override
    public boolean doesGuiPauseGame() {
        // Keep the world/server simulation running behind the video (important on
        // multiplayer/Hypixel so you don't desync while watching).
        return false;
    }

    @Override
    public void onGuiClosed() {
        super.onGuiClosed();
        VideoOverlayConfig cfg = VideoOverlayConfig.get();
        VideoPlayer.get().pause();
        if (cfg.muteGameWhileFullscreen) {
            mc.gameSettings.setSoundLevel(SoundCategory.MASTER, previousMasterVolume);
        }
        texture.delete();
    }
}
