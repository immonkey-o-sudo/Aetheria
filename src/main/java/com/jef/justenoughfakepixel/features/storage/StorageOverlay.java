package com.jef.justenoughfakepixel.features.storage;

import com.jef.justenoughfakepixel.features.storage.render.StorageRenderer;
import com.jef.justenoughfakepixel.features.storage.utils.SContainer;
import com.jef.justenoughfakepixel.features.storage.data.StorageData;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.inventory.ContainerChest;
import org.lwjgl.input.Mouse;

import java.io.IOException;
import java.util.LinkedHashMap;

public class StorageOverlay extends GuiContainer {

    private final StorageRenderer renderer;

    public StorageOverlay(ContainerChest container, LinkedHashMap<String, SContainer> containers) {
        super(container);
        this.renderer = new StorageRenderer(containers);
    }

    @Override
    protected void drawGuiContainerBackgroundLayer(float partialTicks, int mouseX, int mouseY) {
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        drawDefaultBackground();
        renderer.render(mouseX, mouseY, partialTicks);
    }

    @Override
    public void handleMouseInput() throws IOException {
        super.handleMouseInput();

        int mouseX = Mouse.getEventX() * this.width / this.mc.displayWidth;
        int mouseY = this.height - Mouse.getEventY() * this.height / this.mc.displayHeight - 1;

        int dWheel = Mouse.getEventDWheel();
        if (dWheel != 0) {
            renderer.handleScroll(dWheel);
        }
    }

    @Override
    protected void mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException {
        if (mouseButton == 0 || mouseButton == 1) {
            if (isClickingActiveContainerSlot(mouseX, mouseY)) {
                super.mouseClicked(mouseX, mouseY, mouseButton);
                return;
            }

            if (isClickingPlayerInventory(mouseX, mouseY)) {
                super.mouseClicked(mouseX, mouseY, mouseButton);
                return;
            }

            if (renderer.isMouseOverOverlay(mouseX, mouseY)) {
                renderer.handleClick(mouseX, mouseY);
            }
        }
    }

    private boolean isClickingActiveContainerSlot(int mouseX, int mouseY) {
        String activeId = StorageManager.getActiveContainerId();
        if (activeId == null) return false;

        SContainer activeContainer = StorageData.containers.get(activeId);
        if (activeContainer == null) return false;

        return false;
    }

    private boolean isClickingPlayerInventory(int mouseX, int mouseY) {
        int inventoryX = (this.width - 162) / 2;
        int inventoryY = this.height - 94 - 10;

        return mouseX >= inventoryX && mouseX < inventoryX + 162 && mouseY >= inventoryY && mouseY < inventoryY + 94;
    }

    @Override
    public void onGuiClosed() {
        StorageData.saveContainers();
        super.onGuiClosed();
    }

    @Override
    public boolean doesGuiPauseGame() {
        return false;
    }
}
