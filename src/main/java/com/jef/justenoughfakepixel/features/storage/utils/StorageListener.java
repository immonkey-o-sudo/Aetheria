package com.jef.justenoughfakepixel.features.storage.utils;

import com.jef.justenoughfakepixel.core.JefConfig;
import com.jef.justenoughfakepixel.features.storage.StorageManager;
import com.jef.justenoughfakepixel.features.storage.data.StorageData;
import com.jef.justenoughfakepixel.features.storage.render.StorageRenderer;
import com.jef.justenoughfakepixel.init.RegisterEvents;
import lombok.Setter;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.gui.inventory.GuiChest;
import net.minecraft.inventory.ContainerChest;
import net.minecraftforge.client.event.ClientChatReceivedEvent;
import net.minecraftforge.client.event.GuiOpenEvent;
import net.minecraftforge.client.event.GuiScreenEvent;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import org.lwjgl.input.Mouse;

@RegisterEvents
public class StorageListener {

    @Setter
    private static boolean switchingContainer = false;
    private boolean shouldRenderOverlay = false;
    private boolean overlayInitialized = false;

    public static boolean shouldRenderStorageOverlay() {
        return Minecraft.getMinecraft().currentScreen instanceof GuiChest && StorageManager.isOverlayActive();
    }

    @SubscribeEvent
    public void onChatMessage(ClientChatReceivedEvent event) {
        if (!JefConfig.feature.storage.enabled) return;
        if (!shouldRenderOverlay || !overlayInitialized) return;

        String message = event.message.getUnformattedText();
        if (message.contains("Slow down!") || message.contains("executing commands too fast")) {
            shouldRenderOverlay = false;
            overlayInitialized = false;
            StorageManager.closeOverlay();
        }
    }

    @SubscribeEvent
    public void onGuiOpen(GuiOpenEvent event) {
        if (!JefConfig.feature.storage.enabled) return;

        if (event.gui == null) {
            if (!switchingContainer) {
                shouldRenderOverlay = false;
                overlayInitialized = false;
                StorageManager.closeOverlay();
            }
            return;
        }

        if (!(event.gui instanceof GuiChest)) {
            if (!switchingContainer) {
                shouldRenderOverlay = false;
                overlayInitialized = false;
            }
            return;
        }

        GuiChest guiChest = (GuiChest) event.gui;
        if (!(guiChest.inventorySlots instanceof ContainerChest)) return;

        ContainerChest chest = (ContainerChest) guiChest.inventorySlots;
        String title = chest.getLowerChestInventory().getDisplayName().getUnformattedText();

        if (title != null && title.equals("Storage")) {
            shouldRenderOverlay = true;
            overlayInitialized = false;
            switchingContainer = false;
        } else if (title != null && StorageParser.isStorageContainer(title)) {
            if (!StorageData.containers.isEmpty() || shouldRenderOverlay) {
                shouldRenderOverlay = true;
                overlayInitialized = true;
                switchingContainer = false;
            }
        } else {
            if (!switchingContainer) {
                shouldRenderOverlay = false;
                overlayInitialized = false;
            }
        }
    }

    @SubscribeEvent
    public void onBackgroundDrawn(GuiScreenEvent.BackgroundDrawnEvent event) {
        if (!shouldRenderOverlay) return;
        if (!JefConfig.feature.storage.enabled) return;

        if (!(event.gui instanceof GuiChest)) return;

        GuiChest guiChest = (GuiChest) event.gui;
        ContainerChest chest = (ContainerChest) guiChest.inventorySlots;
        String title = chest.getLowerChestInventory().getDisplayName().getUnformattedText();

        if (title == null || !title.equals("Storage")) return;

        if (!overlayInitialized) {
            boolean success = StorageManager.initializeOverlay(chest);
            if (success) {
                overlayInitialized = true;
            }
        }
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public void onDrawScreenPre(GuiScreenEvent.DrawScreenEvent.Pre event) {
        if (!shouldRenderOverlay || !overlayInitialized) return;
        if (!JefConfig.feature.storage.enabled) {
        }
    }

    @SubscribeEvent
    public void onMouseInput(GuiScreenEvent.MouseInputEvent.Pre event) {
        if (!shouldRenderOverlay || !overlayInitialized) return;
        if (!JefConfig.feature.storage.enabled) return;
        if (!(event.gui instanceof GuiChest)) return;

        GuiChest guiChest = (GuiChest) event.gui;
        int mouseX = Mouse.getX() * guiChest.width / Minecraft.getMinecraft().displayWidth;
        int mouseY = guiChest.height - Mouse.getY() * guiChest.height / Minecraft.getMinecraft().displayHeight - 1;

        int dWheel = Mouse.getEventDWheel();
        if (dWheel != 0) {
            StorageManager.handleMouseInput();
            event.setCanceled(true);
            return;
        }

        int button = Mouse.getEventButton();
        if (button != 0 && button != 1) return;

        if (isClickingPlayerInventory(mouseX, mouseY)) return;
        if (isClickingActiveContainerSlots(mouseX, mouseY, guiChest)) return;

        StorageManager.handleMouseInput();
        event.setCanceled(true);
    }

    private boolean isClickingPlayerInventory(int mouseX, int mouseY) {
        return StorageManager.isClickingPlayerInventory(mouseX, mouseY);
    }

    private boolean isClickingActiveContainerSlots(int mouseX, int mouseY, GuiChest guiChest) {
        StorageRenderer r = StorageManager.getRenderer();
        if (r == null) return false;
        for (net.minecraft.inventory.Slot slot : guiChest.inventorySlots.inventorySlots) {
            if (slot == null) continue;
            if (slot.inventory == Minecraft.getMinecraft().thePlayer.inventory) continue;
            if (r.isMouseOverActiveContainerSlot(slot, mouseX, mouseY)) return true;
        }
        return false;
    }

    @SubscribeEvent
    public void onKeyboardInput(GuiScreenEvent.KeyboardInputEvent.Pre event) {
        if (!shouldRenderOverlay || !overlayInitialized) return;
        if (!JefConfig.feature.storage.enabled) return;
        if (!(event.gui instanceof GuiChest)) return;

        int keyCode = org.lwjgl.input.Keyboard.getEventKey();
        if (keyCode == org.lwjgl.input.Keyboard.KEY_ESCAPE) return;
        if (!org.lwjgl.input.Keyboard.getEventKeyState()) return;

        char typedChar = org.lwjgl.input.Keyboard.getEventCharacter();

        StorageManager.handleKeyTyped(typedChar, keyCode);
    }

    @SubscribeEvent
    public void onDrawScreen(GuiScreenEvent.DrawScreenEvent.Post event) {
        if (!shouldRenderOverlay || !overlayInitialized) return;
        if (!JefConfig.feature.storage.enabled) return;
        if (!(event.gui instanceof GuiChest)) return;

        StorageManager.renderOverlay(event.mouseX, event.mouseY, event.renderPartialTicks);
        com.jef.justenoughfakepixel.utils.render.ItemRenderUtils.renderHeldCursorItem();
    }

    @SubscribeEvent
    public void onRenderGameOverlay(RenderGameOverlayEvent.Post event) {
        if (event.type != RenderGameOverlayEvent.ElementType.ALL) return;
        if (!JefConfig.feature.storage.enabled) return;
        if (!switchingContainer || !overlayInitialized || !StorageManager.isOverlayActive()) return;
        if (Minecraft.getMinecraft().currentScreen != null) return;

        ScaledResolution sr = new ScaledResolution(Minecraft.getMinecraft());
        int mouseX = Mouse.getX() * sr.getScaledWidth() / Minecraft.getMinecraft().displayWidth;
        int mouseY = sr.getScaledHeight() - Mouse.getY() * sr.getScaledHeight() / Minecraft.getMinecraft().displayHeight - 1;
        StorageManager.renderOverlay(mouseX, mouseY, event.partialTicks);
    }
}
