package io.hamlook.aetheria.features.video;

import io.hamlook.aetheria.core.ATHRConfig;
import net.minecraft.client.Minecraft;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import org.lwjgl.input.Keyboard;

/**
 * Wires up the fullscreen-swap hotkey.
 * <p>
 * Register once with {@code MinecraftForge.EVENT_BUS.register(new VideoOverlayFeature())}
 * from Aetheria's clientInit, alongside the other event handlers.
 * <p>
 * Deliberately uses a raw Keyboard.isKeyDown poll (matching the edge-detection
 * pattern already used in ATHRConfig) rather than a registered KeyBinding, so the
 * hotkey works identically whether the video overlay or the game currently owns
 * the screen/focus.
 */
public class VideoOverlayFeature {

    private boolean keyWasDown = false;

    @SubscribeEvent
    public void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.START) return;

        Minecraft mc = Minecraft.getMinecraft();
        if (mc.thePlayer == null) return; // not in-world yet

        int toggleKey = ATHRConfig.feature.videoOverlay.toggleKeyCode;
        boolean down = toggleKey != Keyboard.KEY_NONE && Keyboard.isKeyDown(toggleKey);

        if (down && !keyWasDown) {
            toggle(mc);
        }
        keyWasDown = down;
    }

    private void toggle(Minecraft mc) {
        if (mc.currentScreen instanceof GuiVideoOverlay) {
            mc.displayGuiScreen(null); // back to fullscreen gameplay
        } else if (mc.currentScreen == null) {
            String url = ATHRConfig.feature.videoOverlay.videoUrl;
            if (url == null || url.trim().isEmpty()) {
                if (mc.thePlayer != null) {
                    mc.thePlayer.addChatMessage(new net.minecraft.util.ChatComponentText(
                            "\u00a7c[Aetheria] Set a Video URL in the Aetheria config \u00a77(Video Overlay category)\u00a7c first."));
                }
                return;
            }
            mc.displayGuiScreen(new GuiVideoOverlay()); // fullscreen video
        }
        // If some other GUI (inventory, chat, pause menu...) is open, ignore the
        // hotkey rather than fighting with it.
    }
}
