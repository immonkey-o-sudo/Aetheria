package com.jef.justenoughfakepixel.features.storage;

import com.jef.justenoughfakepixel.features.storage.data.StorageData;
import com.jef.justenoughfakepixel.features.storage.render.StorageRenderer;
import com.jef.justenoughfakepixel.features.storage.utils.*;
import com.jef.justenoughfakepixel.init.RegisterEvents;
import lombok.Getter;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.inventory.GuiChest;
import net.minecraft.inventory.ContainerChest;

import java.util.LinkedHashMap;


@RegisterEvents
public class StorageManager {

    @Getter
    private static String activeContainerId = null;
    @Getter
    private static StorageOverlay currentGui = null;
    @Getter
    private static StorageRenderer renderer = null;
    @Getter
    private static boolean overlayActive = false;

    public static void setActiveContainer(String containerId) {
        activeContainerId = containerId;
    }

    public static boolean initializeOverlay(ContainerChest parser) {
        if (StorageData.containers.isEmpty()) {
            StorageData.loadContainers();
        }

        LinkedHashMap<String, SContainer> containers = StorageParser.parseOverlay(parser, StorageData.containers);

        if (containers.isEmpty()) {
            return false;
        }

        StorageData.containers = containers;

        renderer = new StorageRenderer(containers);
        overlayActive = true;
        activeContainerId = null;

        return true;
    }

    public static void renderOverlay(int mouseX, int mouseY, float partialTicks) {
        if (renderer == null && !StorageData.containers.isEmpty()) {
            renderer = new StorageRenderer(StorageData.containers);
            overlayActive = true;
        }

        if (renderer != null && overlayActive) {
            renderer.render(mouseX, mouseY, partialTicks);
        }
    }


    public static void handleMouseInput() {
        if (renderer == null) return;

        int dWheel = org.lwjgl.input.Mouse.getEventDWheel();
        if (dWheel != 0) {
            renderer.handleScroll(dWheel);
        }

        if (org.lwjgl.input.Mouse.getEventButtonState()) {
            int mouseButton = org.lwjgl.input.Mouse.getEventButton();
            if (mouseButton == 0) {
                net.minecraft.client.gui.ScaledResolution sr = new net.minecraft.client.gui.ScaledResolution(Minecraft.getMinecraft());
                int mouseX = org.lwjgl.input.Mouse.getX() * sr.getScaledWidth() / Minecraft.getMinecraft().displayWidth;
                int mouseY = sr.getScaledHeight() - org.lwjgl.input.Mouse.getY() * sr.getScaledHeight() / Minecraft.getMinecraft().displayHeight - 1;

                renderer.handleClick(mouseX, mouseY);
            }
        }
    }


    public static boolean handleKeyTyped(char typedChar, int keyCode) {
        if (renderer == null) return false;
        return renderer.handleKeyTyped(typedChar, keyCode);
    }

    public static boolean openOverlay(ContainerChest parser) {
        if (StorageData.containers.isEmpty()) {
            StorageData.loadContainers();
        }

        LinkedHashMap<String, SContainer> containers = StorageParser.parseOverlay(parser, StorageData.containers);

        if (containers.isEmpty()) {
            return false;
        }

        StorageData.containers = containers;

        return true;
    }

    public static StorageOverlay createGui(ContainerChest parser) {
        if (StorageData.containers.isEmpty()) {
            StorageData.loadContainers();
        }

        LinkedHashMap<String, SContainer> containers = StorageParser.parseOverlay(parser, StorageData.containers);

        if (containers.isEmpty()) {
            return null;
        }

        StorageData.containers = containers;

        currentGui = new StorageOverlay(parser, containers);
        activeContainerId = null;

        return currentGui;
    }

    public static void switchToContainer(String containerId) {
        SContainer container = StorageData.containers.get(containerId);
        if (container == null) {
            return;
        }

        StorageListener.setSwitchingContainer(true);

        Minecraft.getMinecraft().thePlayer.closeScreen();

        final String command;
        if (container.type == Type.ECHEST) {
            command = "/echest " + container.page;
        } else {
            command = "/storage " + container.page;
        }

        new Thread(() -> {
            try {
                Thread.sleep(100);
                Minecraft.getMinecraft().addScheduledTask(() -> {
                    Minecraft.getMinecraft().thePlayer.sendChatMessage(command);
                });
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }).start();
    }


    private static int getStorageSlotForContainer(SContainer container) {


        if (container.type == Type.ECHEST) {
            if (container.page >= 1 && container.page <= 5) {
                return 9 + (container.page - 1);
            }
        } else if (container.type == Type.BAG) {
            if (container.page >= 1 && container.page <= 18) {
                return 27 + (container.page - 1);
            }
        }

        return -1;
    }


    public static boolean isClickingPlayerInventory(int mouseX, int mouseY) {
        if (renderer == null) return false;
        return renderer.isClickingPlayerInventory(mouseX, mouseY);
    }

    public static void closeOverlay() {
        StorageData.saveContainers();
        activeContainerId = null;
        currentGui = null;
        renderer = null;
        overlayActive = false;
    }


    public static void overrideIsMouseOverSlot(net.minecraft.inventory.Slot slotIn, int mouseX, int mouseY, org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable<Boolean> cir) {
        if (!isOverlayActive() || renderer == null) return;
        if (!(Minecraft.getMinecraft().currentScreen instanceof GuiChest)) return;

        GuiChest guiChest = (GuiChest) Minecraft.getMinecraft().currentScreen;
        ContainerChest container = (ContainerChest) guiChest.inventorySlots;

        boolean isPlayerSlot = slotIn.inventory == Minecraft.getMinecraft().thePlayer.inventory;


        if (isPlayerSlot) {
            cir.setReturnValue(renderer.isMouseOverPlayerInventorySlot(slotIn, mouseX, mouseY));
        } else {
            cir.setReturnValue(renderer.isMouseOverActiveContainerSlot(slotIn, mouseX, mouseY));
        }
    }
}