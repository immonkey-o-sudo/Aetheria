package io.hamlook.aetheria.utils.overlay;

import io.hamlook.aetheria.features.storage.StorageManager;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiChat;
import org.lwjgl.input.Keyboard;

public class OverlayUtils {

    private static final Minecraft mc = Minecraft.getMinecraft();

    public static boolean isChatOpen()       { return mc.currentScreen instanceof GuiChat; }
    public static boolean isDebugActive()    { return mc.gameSettings.showDebugInfo; }
    public static boolean isTabHeld()        { return mc.currentScreen == null && Keyboard.isKeyDown(mc.gameSettings.keyBindPlayerList.getKeyCode()); }
    public static boolean isStorageActive()  { return StorageManager.isOverlayActive(); }

    public static boolean shouldHide() {
        return isChatOpen() || isDebugActive() || isTabHeld() || isStorageActive();
    }
}