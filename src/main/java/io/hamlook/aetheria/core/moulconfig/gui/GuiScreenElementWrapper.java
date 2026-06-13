// SPDX-License-Identifier: LGPL-3.0-only
// Derived from MoulConfig (https://github.com/NotEnoughUpdates/MoulConfig)

package io.hamlook.aetheria.core.moulconfig.gui;

import io.hamlook.aetheria.core.ATHRConfig;
import io.hamlook.aetheria.utils.KeybindHelper;
import net.minecraft.client.gui.GuiScreen;

import java.io.IOException;

public class GuiScreenElementWrapper extends GuiScreen {

    public final GuiElement element;

    public GuiScreenElementWrapper(GuiElement element) {
        this.element = element;
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        super.drawScreen(mouseX, mouseY, partialTicks);
        element.render();
    }

    @Override
    public void handleMouseInput() throws IOException {
        super.handleMouseInput();
        int i = KeybindHelper.getScaledEventX(this.width);
        int j = KeybindHelper.getScaledEventY(this.height);
        element.mouseInput(i, j);
    }

    @Override
    public void handleKeyboardInput() throws IOException {
        super.handleKeyboardInput();
        element.keyboardInput();
    }

    @Override
    public void onGuiClosed() {
        ATHRConfig.saveConfig();
    }
}