package io.hamlook.aetheria.utils;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.ScaledResolution;
import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;


public final class KeybindHelper {

    private static final Minecraft MC = Minecraft.getMinecraft();

    private KeybindHelper() {
    }

    // Key constants
    public static final int KEY_ESCAPE = 1;
    public static final int KEY_RETURN = 28;
    public static final int KEY_NUMPADENTER = 156;

    public static String getKeyName(int keyCode) {
        if (keyCode == 0) return "NONE";
        if (keyCode < 0) return "Button " + (keyCode + 101);
        try {
            String name = Keyboard.getKeyName(keyCode);
            if (name == null) return "???";
            if (name.equalsIgnoreCase("LMENU")) return "LALT";
            if (name.equalsIgnoreCase("RMENU")) return "RALT";
            return name;
        } catch (Exception e) {
            return "???";
        }
    }

    public static boolean isKeyValid(int keyCode) {
        return keyCode != 0;
    }

    public static boolean isKeyDown(int keyCode) {
        if (!isKeyValid(keyCode)) return false;
        return keyCode < 0 ? Mouse.isButtonDown(keyCode + 100) : Keyboard.isKeyDown(keyCode);
    }

    public static boolean isKeyPressed(int keyCode) {
        if (!isKeyValid(keyCode)) return false;
        return keyCode < 0 ? Mouse.getEventButtonState() && Mouse.getEventButton() == keyCode + 100 : Keyboard.getEventKeyState() && Keyboard.getEventKey() == keyCode;
    }

    // Keyboard event accessors
    public static char getEventCharacter() {
        return Keyboard.getEventCharacter();
    }

    public static boolean getEventKeyState() {
        return Keyboard.getEventKeyState();
    }

    public static int getEventKeyCode() {
        return Keyboard.getEventKey();
    }

    public static void enableRepeatEvents(boolean repeat) {
        Keyboard.enableRepeatEvents(repeat);
    }

    // Mouse event accessors
    public static boolean getEventButtonState() {
        return Mouse.getEventButtonState();
    }

    public static int getEventButton() {
        return Mouse.getEventButton();
    }

    public static int getEventDWheel() {
        return Mouse.getEventDWheel();
    }

    // Coordinate helpers, poll-based (for draw/render)
    public static int[] getMouseCoords(ScaledResolution sr) {
        return getMouseCoords(sr.getScaledWidth(), sr.getScaledHeight());
    }

    public static int[] getMouseCoords(int guiWidth, int guiHeight) {
        int mouseX = Mouse.getX() * guiWidth / MC.displayWidth;
        int mouseY = guiHeight - Mouse.getY() * guiHeight / MC.displayHeight - 1;
        return new int[]{mouseX, mouseY};
    }

    public static float[] getMouseCoordsFloat(ScaledResolution sr) {
        float mouseX = (float) (Mouse.getX() * sr.getScaledWidth_double() / MC.displayWidth);
        float mouseY = (float) (sr.getScaledHeight_double() - Mouse.getY() * sr.getScaledHeight_double() / MC.displayHeight - 1);
        return new float[]{mouseX, mouseY};
    }

    // Coordinate helpers, event-based (for mouse input events)
    public static int getScaledEventX(int guiWidth) {
        return Mouse.getEventX() * guiWidth / MC.displayWidth;
    }

    public static int getScaledEventY(int guiHeight) {
        return guiHeight - Mouse.getEventY() * guiHeight / MC.displayHeight - 1;
    }
}