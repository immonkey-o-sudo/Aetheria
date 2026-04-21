package com.jef.justenoughfakepixel.features.storage.utils;

import com.jef.justenoughfakepixel.DebugLogger;
import com.jef.justenoughfakepixel.core.JefConfig;
import com.jef.justenoughfakepixel.features.storage.StorageManager;
import com.jef.justenoughfakepixel.features.storage.StorageOverlay;
import com.jef.justenoughfakepixel.features.storage.data.StorageData;
import com.jef.justenoughfakepixel.features.storage.data.StorageSaving;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.inventory.GuiChest;
import net.minecraft.inventory.ContainerChest;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.network.play.client.C0EPacketClickWindow;
import net.minecraft.network.play.server.S2DPacketOpenWindow;
import net.minecraft.network.play.server.S2EPacketCloseWindow;
import net.minecraft.network.play.server.S2FPacketSetSlot;
import net.minecraft.network.play.server.S30PacketWindowItems;

import java.util.regex.Matcher;

public class SPacketHandler {

    private int currentWindowId = -1;
    private String currentContainerId = null;
    private Type currentContainerType = null;
    private int currentPage = -1;
    private boolean onStorageMenu = false;

    public int getCurrentWindowId() {
        if (Minecraft.getMinecraft().currentScreen instanceof StorageOverlay) {
            StorageOverlay overlay = (StorageOverlay) Minecraft.getMinecraft().currentScreen;
            return overlay.inventorySlots.windowId;
        }

        if (!(Minecraft.getMinecraft().currentScreen instanceof GuiChest)) {
            currentWindowId = -1;
            return -1;
        }

        GuiChest chest = (GuiChest) Minecraft.getMinecraft().currentScreen;
        return chest.inventorySlots.windowId;
    }

    public SContainer getCurrentContainer() {
        if (currentContainerId == null) return null;
        return StorageData.containers.get(currentContainerId);
    }

    public void handleOpenWindow(S2DPacketOpenWindow packet) {
        if (!JefConfig.feature.storage.enabled) return;

        String windowTitle = packet.getWindowTitle().getUnformattedText();
        windowTitle = windowTitle.replaceAll("§[0-9a-fk-or]", "");

        DebugLogger.log("Window opened: " + windowTitle);

        currentWindowId = -1;
        currentContainerId = null;
        currentContainerType = null;
        currentPage = -1;
        onStorageMenu = false;

        if (windowTitle.trim().equals("Storage")) {
            onStorageMenu = true;
            DebugLogger.log("Opened Storage menu");
            return;
        }

        Matcher backpackMatcher = StorageParser.BACKPACK_N_N.matcher(windowTitle);
        if (backpackMatcher.matches()) {
            try {
                currentPage = Integer.parseInt(backpackMatcher.group(2));
                currentContainerType = Type.BAG;
                currentContainerId = Type.BAG.prefix + "-" + currentPage;

                String sizeType = backpackMatcher.group(1);
                int renderH = StorageParser.getBackpackRenderHeight(sizeType);
                int storageSlots = getSlotCountFromRenderH(renderH);

                DebugLogger.log("Backpack " + sizeType + " has " + storageSlots + " storage slots");

                if (!StorageData.containers.containsKey(currentContainerId)) {
                    SContainer container = new SContainer(new java.util.HashMap<>(), currentPage, Type.BAG, renderH, false);
                    container.slotCount = storageSlots;
                    StorageData.containers.put(currentContainerId, container);
                } else {
                    SContainer existing = StorageData.containers.get(currentContainerId);
                    existing.slotCount = storageSlots;
                    existing.renderH = renderH;
                }

                if (StorageManager.getCurrentGui() != null || StorageManager.isOverlayActive()) {
                    StorageManager.setActiveContainer(currentContainerId);
                }

                return;
            } catch (NumberFormatException e) {
                DebugLogger.log("Failed to parse backpack slot number: " + windowTitle);
            }
        }

        Matcher echestPageMatcher = StorageParser.ECHEST_PAGE_N.matcher(windowTitle);
        if (echestPageMatcher.matches()) {
            try {
                currentPage = Integer.parseInt(echestPageMatcher.group(1));
                currentContainerType = Type.ECHEST;
                currentContainerId = Type.ECHEST.prefix + "-" + currentPage;

                if (!StorageData.containers.containsKey(currentContainerId)) {
                    SContainer container = new SContainer(new java.util.HashMap<>(), currentPage, Type.ECHEST, 200, false);
                    container.slotCount = 45;
                    StorageData.containers.put(currentContainerId, container);
                    DebugLogger.log("Created new Ender Chest page " + currentPage + " with default 45 slots");
                } else {
                    DebugLogger.log("Using existing Ender Chest page " + currentPage);
                }

                if (StorageManager.getCurrentGui() != null || StorageManager.isOverlayActive()) {
                    StorageManager.setActiveContainer(currentContainerId);
                }

            } catch (NumberFormatException e) {
                DebugLogger.log("Failed to parse ender chest page number: " + windowTitle);
            }
        }
    }

    public void handleCloseWindow(S2EPacketCloseWindow packet) {
        if (currentContainerId != null) {
            DebugLogger.log("Closing container: " + currentContainerId);
        }

        currentWindowId = -1;
        currentContainerId = null;
        currentContainerType = null;
        currentPage = -1;
        onStorageMenu = false;
    }

    public void handleSetSlot(S2FPacketSetSlot packet) {
        if (!JefConfig.feature.storage.enabled) return;
        if (currentContainerId == null) return;

        int windowId = getCurrentWindowId();
        if (windowId == -1 || windowId != packet.func_149175_c()) return;

        SContainer container = getCurrentContainer();
        if (container == null) return;

        int slot = packet.func_149173_d();
        ItemStack stack = packet.func_149174_e();

        if (slot >= 9 && slot < 9 + container.slotCount) {
            int storageSlot = slot - 9;

            if (stack == null) {
                container.slots.remove(storageSlot);
                DebugLogger.log("Removed item from slot " + storageSlot);
            } else {
                container.setStack(storageSlot, stack.copy());
                DebugLogger.log("Updated slot " + storageSlot + ": " + stack.getDisplayName());
            }

            StorageSaving.saveStorageData(StorageData.containers.values());
        }
    }

    public void handleWindowItems(S30PacketWindowItems packet) {
        if (!JefConfig.feature.storage.enabled) return;
        if (currentContainerId == null) return;

        int windowId = getCurrentWindowId();
        if (windowId == -1 || windowId != packet.func_148911_c()) return;

        SContainer container = getCurrentContainer();
        if (container == null) return;

        if (Minecraft.getMinecraft().currentScreen instanceof GuiChest) {
            GuiChest guiChest = (GuiChest) Minecraft.getMinecraft().currentScreen;
            ContainerChest containerChest = (ContainerChest) guiChest.inventorySlots;
            IInventory inv = containerChest.getLowerChestInventory();

            int chestSize = inv.getSizeInventory();
            int rows = chestSize / 9;
            int storageSlots = (rows - 1) * 9;
            int renderH = StorageParser.calculateRenderHeight(rows - 1);

            container.slotCount = storageSlots;
            container.renderH = renderH;

            DebugLogger.log("Chest has " + chestSize + " slots (" + rows + " rows), " + storageSlots + " storage slots");
        }

        ItemStack[] items = packet.getItemStacks();

        DebugLogger.log("Received full inventory update with " + items.length + " items");

        container.slots.clear();

        for (int i = 0; i < container.slotCount; i++) {
            int packetSlot = 9 + i;
            if (packetSlot >= items.length) break;

            ItemStack stack = items[packetSlot];
            if (stack != null) {
                container.setStack(i, stack.copy());
                DebugLogger.log("Updated slot " + i + ": " + stack.getDisplayName());
            }
        }

        DebugLogger.log("Updated " + container.slots.size() + " items in container " + currentContainerId);

        StorageSaving.saveStorageData(StorageData.containers.values());
    }

    public void handleClickWindow(C0EPacketClickWindow packet) {
        if (!JefConfig.feature.storage.enabled) return;
        if (currentContainerId == null) return;
        if (!(Minecraft.getMinecraft().currentScreen instanceof GuiChest)) return;

        int windowId = getCurrentWindowId();
        if (windowId == -1 || windowId != packet.getWindowId()) return;

        SContainer container = getCurrentContainer();
        if (container == null) return;

        GuiChest guiChest = (GuiChest) Minecraft.getMinecraft().currentScreen;
        ContainerChest containerChest = (ContainerChest) guiChest.inventorySlots;
        IInventory inv = containerChest.getLowerChestInventory();

        int maxSlot = Math.min(9 + container.slotCount, inv.getSizeInventory());
        for (int i = 9; i < maxSlot; i++) {
            ItemStack stack = inv.getStackInSlot(i);
            int storageSlot = i - 9;
            container.setStack(storageSlot, stack != null ? stack.copy() : null);
        }

        DebugLogger.log("Synced container after click");

        StorageSaving.saveStorageData(StorageData.containers.values());
    }

    public void reset() {
        currentWindowId = -1;
        currentContainerId = null;
        currentContainerType = null;
        currentPage = -1;
        onStorageMenu = false;
    }

    private int getSlotCountFromRenderH(int renderH) {
        switch (renderH) {
            case 70:
                return 9;
            case 100:
                return 18;
            case 140:
                return 27;
            case 170:
                return 36;
            case 200:
                return 45;
            case 230:
                return 54;
            default:
                return 45;
        }
    }
}
