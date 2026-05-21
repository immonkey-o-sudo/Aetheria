package io.hamlook.aetheria.core.config.editors;

import io.hamlook.aetheria.core.config.gui.config.ConfigProcessor;
import io.hamlook.aetheria.utils.render.RenderUtils;
import io.hamlook.aetheria.utils.render.TextRenderUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import org.lwjgl.input.Mouse;

import java.lang.reflect.Method;

import static io.hamlook.aetheria.core.config.gui.GuiTextures.button_tex;

public class GuiOptionEditorButton extends GuiOptionEditor {

    private final String runnableId;
    private String buttonText;
    private final Object config;

    public GuiOptionEditorButton(ConfigProcessor.ProcessedOption option, String runnableId, String buttonText, Object config) {
        super(option);
        this.runnableId = runnableId;
        this.config = config;

        this.buttonText = buttonText;
        if (this.buttonText != null && this.buttonText.isEmpty()) this.buttonText = null;
    }

    @Override
    public void render(int x, int y, int width) {
        super.render(x, y, width);

        int height = getHeight();

        GlStateManager.color(1, 1, 1, 1);
        Minecraft.getMinecraft().getTextureManager().bindTexture(button_tex);
        RenderUtils.drawTexturedRect(x + width / 6 - 24, y + height - 7 - 14, 48, 16);

        if (buttonText != null) {
            TextRenderUtils.drawStringCenteredScaledMaxWidth(buttonText, Minecraft.getMinecraft().fontRendererObj, x + width / 6, y + height - 7 - 6, false, 44, 0xFF303030);
        }
    }

    @Override
    public boolean mouseInput(int x, int y, int width, int mouseX, int mouseY) {
        if (Mouse.getEventButtonState()) {
            int height = getHeight();
            if (mouseX > x + width / 6 - 24 && mouseX < x + width / 6 + 24 && mouseY > y + height - 7 - 14 && mouseY < y + height - 7 + 2) {
                invokeRunnable(runnableId);
                return true;
            }
        }

        return false;
    }

    private void invokeRunnable(String id) {
        try {
            Method method = config.getClass().getMethod("executeRunnable", String.class);
            method.invoke(config, id);
        } catch (Exception ignored) {
        }
    }

    @Override
    public boolean keyboardInput() {
        return false;
    }
}
