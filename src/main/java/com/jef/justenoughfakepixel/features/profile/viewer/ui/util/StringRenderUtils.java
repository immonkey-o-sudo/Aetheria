package com.jef.justenoughfakepixel.features.profile.viewer.ui.util;

import com.jef.justenoughfakepixel.utils.render.ResolutionUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;

public class StringRenderUtils {

    public static void drawString(String text, float xPos, float yPos, float uiScale, boolean displayScale) {
        GlStateManager.pushMatrix();
        GlStateManager.disableBlend();
        GlStateManager.enableAlpha();

        float scaleDisplay = displayScale ? ResolutionUtils.getXStatic(1) : 1f;
        float finalScale = uiScale * scaleDisplay;
        finalScale = Math.max(0.25f, finalScale);

        GlStateManager.translate(xPos, yPos, 0);
        GlStateManager.scale(finalScale, finalScale, 1.0f);

        Minecraft.getMinecraft().fontRendererObj.drawString(text, 0, 0, -1);
        GlStateManager.popMatrix();
    }

    public static void drawString(String text, float xPos, float yPos, float uiScale) {
        drawString(text, xPos, yPos, uiScale, true);
    }

    public static void drawCenteredString(String text, float xPos, float yPos, float uiScale, boolean displayScale) {
        GlStateManager.pushMatrix();
        GlStateManager.disableBlend();
        GlStateManager.enableAlpha();

        float scaleDisplay = displayScale ? ResolutionUtils.getXStatic(1) : 1f;
        float finalScale = uiScale * scaleDisplay;
        finalScale = Math.max(0.25f, finalScale);

        GlStateManager.translate(xPos, yPos, 0);
        GlStateManager.scale(finalScale, finalScale, 1.0f);

        int textWidth = Minecraft.getMinecraft().fontRendererObj.getStringWidth(text);
        int fontHeight = Minecraft.getMinecraft().fontRendererObj.FONT_HEIGHT;

        Minecraft.getMinecraft().fontRendererObj.drawString(text, -textWidth / 2.0f, -fontHeight / 2.0f, -1, false);

        GlStateManager.popMatrix();
    }

    public static void drawCenteredString(String text, float xPos, float yPos, float uiScale) {
        drawCenteredString(text, xPos, yPos, uiScale, true);
    }
}