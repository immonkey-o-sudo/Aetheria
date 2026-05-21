package io.hamlook.aetheria.core.config.gui;

import net.minecraft.client.Minecraft;
import io.hamlook.aetheria.utils.Utils;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.renderer.GlStateManager;
import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.GL11;

import java.util.function.Consumer;


public class GuiElementSlider extends GuiElement {

    public int x, y, width;
    private static final int HEIGHT = 16;

    private final float minValue, maxValue, minStep;
    private float value;
    private final Consumer<Float> setCallback;
    private boolean clicked = false;

    public GuiElementSlider(int x, int y, int width,
                            float minValue, float maxValue, float minStep,
                            float value, Consumer<Float> setCallback) {
        if (minStep < 0) minStep = 0.01f;
        this.x = x; this.y = y; this.width = width;
        this.minValue = minValue; this.maxValue = maxValue; this.minStep = minStep;
        this.value = value;
        this.setCallback = setCallback;
    }

    public void setValue(float value) { this.value = value; }

    @Override
    public void render() {
        ScaledResolution sr = new ScaledResolution(Minecraft.getMinecraft());
        int mouseX = Mouse.getX() * sr.getScaledWidth() / Minecraft.getMinecraft().displayWidth;

        float v = value;
        if (clicked) {
            v = snap((mouseX - x) * (maxValue - minValue) / width + minValue);
        }

        float sliderAmt = Math.max(0, Math.min(1, (v - minValue) / (maxValue - minValue)));
        int   sliderAmtI = (int) (width * sliderAmt);

        GlStateManager.color(1, 1, 1, 1);

        Minecraft.getMinecraft().getTextureManager().bindTexture(GuiTextures.slider_on_cap);
        Utils.drawTexturedRect(x, y, 4, HEIGHT, GL11.GL_NEAREST);

        Minecraft.getMinecraft().getTextureManager().bindTexture(GuiTextures.slider_off_cap);
        Utils.drawTexturedRect(x + width - 4, y, 4, HEIGHT, GL11.GL_NEAREST);

        if (sliderAmtI > 5) {
            Minecraft.getMinecraft().getTextureManager().bindTexture(GuiTextures.slider_on_segment);
            Utils.drawTexturedRect(x + 4, y, sliderAmtI - 4, HEIGHT, GL11.GL_NEAREST);
        }
        if (sliderAmtI < width - 5) {
            Minecraft.getMinecraft().getTextureManager().bindTexture(GuiTextures.slider_off_segment);
            Utils.drawTexturedRect(x + sliderAmtI, y, width - 4 - sliderAmtI, HEIGHT, GL11.GL_NEAREST);
        }

        for (int i = 1; i < 4; i++) {
            int notchX = x + width * i / 4 - 1;
            Minecraft.getMinecraft().getTextureManager().bindTexture(
                    notchX > x + sliderAmtI ? GuiTextures.slider_off_notch : GuiTextures.slider_on_notch);
            Utils.drawTexturedRect(notchX, y + (HEIGHT - 4f) / 2, 2, 4, GL11.GL_NEAREST);
        }

        Minecraft.getMinecraft().getTextureManager().bindTexture(GuiTextures.slider_button_new);
        Utils.drawTexturedRect(x + sliderAmtI - 4, y, 8, HEIGHT, GL11.GL_NEAREST);
    }

    @Override
    public boolean mouseInput(int mouseX, int mouseY) {
        if (!Mouse.isButtonDown(0)) clicked = false;

        if (Mouse.getEventButton() == 0) {
            clicked = Mouse.getEventButtonState()
                    && mouseX > x && mouseX < x + width
                    && mouseY > y && mouseY < y + HEIGHT;
            if (clicked) {
                value = snap((mouseX - x) * (maxValue - minValue) / width + minValue);
                setCallback.accept(value);
                return true;
            }
        }

        if (!Mouse.getEventButtonState() && Mouse.getEventButton() == -1 && clicked) {
            value = snap((mouseX - x) * (maxValue - minValue) / width + minValue);
            setCallback.accept(value);
            return true;
        }
        return false;
    }

    @Override
    public boolean keyboardInput() { return false; }

    private float snap(float raw) {
        raw = Math.max(minValue, Math.min(maxValue, raw));
        return Math.round(raw / minStep) * minStep;
    }
}