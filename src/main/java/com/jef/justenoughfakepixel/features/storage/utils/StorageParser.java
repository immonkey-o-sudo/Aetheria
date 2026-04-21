package com.jef.justenoughfakepixel.features.storage.utils;

import com.jef.justenoughfakepixel.DebugLogger;
import com.jef.justenoughfakepixel.utils.ColorUtils;
import com.jef.justenoughfakepixel.utils.item.ItemUtils;
import net.minecraft.block.Block;
import net.minecraft.block.BlockStainedGlassPane;
import net.minecraft.inventory.ContainerChest;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class StorageParser {

    public static final Pattern BACKPACK_N_N = Pattern.compile("(.+) Backpack (\\d+)/(\\d+)");
    public static final Pattern ECHEST_PAGE_N = Pattern.compile("Ender Chest \\(Page (\\d+)\\)");

    public static boolean isStorageContainer(String title) {
        return ECHEST_PAGE_N.matcher(title).matches() || BACKPACK_N_N.matcher(title).matches();
    }

    public static SContainer parseEchest(ContainerChest chest) {
        String title = chest.getLowerChestInventory().getDisplayName().getUnformattedText();
        DebugLogger.log("Parsing Ender Chest: " + title);

        Matcher matcher = ECHEST_PAGE_N.matcher(title);
        if (!matcher.matches()) {
            DebugLogger.log("Title doesn't match Ender Chest pattern: " + title);
            return null;
        }

        int page;
        try {
            page = Integer.parseInt(matcher.group(1));
        } catch (NumberFormatException e) {
            DebugLogger.log("Error parsing page number from: " + title);
            return null;
        }

        HashMap<Integer, ItemStack> itemList = parseItemsFromInventory(chest.getLowerChestInventory(), 9, chest.getLowerChestInventory().getSizeInventory());
        DebugLogger.log("Parsed " + itemList.size() + " items from Ender Chest page " + page);

        int rows = (chest.getLowerChestInventory().getSizeInventory() / 9) - 1;
        int renderH = calculateRenderHeight(rows);
        boolean empty = isContainerEmpty(chest, itemList);

        SContainer container = new SContainer(itemList, page, Type.ECHEST, renderH, false);
        container.empty = empty;
        return container;
    }

    public static SContainer parseBackpack(ContainerChest chest) {
        String title = chest.getLowerChestInventory().getDisplayName().getUnformattedText();
        DebugLogger.log("Parsing Backpack: " + title);

        Matcher matcher = BACKPACK_N_N.matcher(title);
        if (!matcher.matches()) {
            DebugLogger.log("Title doesn't match Backpack pattern: " + title);
            return null;
        }

        int page;
        try {
            page = Integer.parseInt(matcher.group(2));
        } catch (NumberFormatException e) {
            DebugLogger.log("Error parsing backpack number from: " + title);
            return null;
        }

        HashMap<Integer, ItemStack> itemList = parseItemsFromInventory(chest.getLowerChestInventory(), 9, chest.getLowerChestInventory().getSizeInventory());
        DebugLogger.log("Parsed " + itemList.size() + " items from Backpack slot " + page);

        String sizeType = matcher.group(1);
        int renderH = getBackpackRenderHeight(sizeType);
        boolean empty = itemList.isEmpty();

        SContainer container = new SContainer(itemList, page, Type.BAG, renderH, false);
        container.empty = empty;
        return container;
    }

    private static HashMap<Integer, ItemStack> parseItemsFromInventory(IInventory inventory, int startSlot, int endSlot) {
        HashMap<Integer, ItemStack> itemList = new HashMap<>();
        for (int i = startSlot; i < endSlot; i++) {
            ItemStack stack = inventory.getStackInSlot(i);
            if (stack != null) {
                itemList.put(i - startSlot, stack.copy());
            }
        }
        return itemList;
    }

    private static boolean isContainerEmpty(ContainerChest chest, HashMap<Integer, ItemStack> items) {
        if (!items.isEmpty()) return false;
        ItemStack closeButton = chest.getSlot(0).getStack();
        if (closeButton != null) {
            String displayName = ColorUtils.stripColor(closeButton.getDisplayName());
            return displayName.equalsIgnoreCase("close");
        }
        return false;
    }

    public static int calculateRenderHeight(int rows) {
        switch (rows) {
            case 1:
                return 70;
            case 2:
                return 100;
            case 3:
                return 140;
            case 4:
                return 170;
            case 5:
                return 200;
            case 6:
                return 230;
            default:
                return 200;
        }
    }

    public static int getBackpackRenderHeight(String title) {
        String[] words = title.split(" ");
        if (words.length > 0) {
            String size = words[0].toLowerCase();
            switch (size) {
                case "small":
                    return 70;
                case "medium":
                    return 100;
                case "large":
                    return 140;
                case "greater":
                    return 170;
                case "jumbo":
                    return 200;
                default:
                    return 200;
            }
        }
        return 200;
    }

    private static int getSlotCountFromRenderH(int renderH) {
        switch (renderH) {
            case 70:
                return 9;
            case 100:
                return 18;
            case 140:
                return 27;
            case 170:
                return 36;
            default:
                return 45;
        }
    }

    public static LinkedHashMap<String, SContainer> parseOverlay(ContainerChest chest, LinkedHashMap<String, SContainer> loadedContainers) {
        LinkedHashMap<String, SContainer> detectedContainers = new LinkedHashMap<>();

        for (int j = 0; j < 9; j++) {
            int slot = 9 + j;
            ItemStack stack = chest.getSlot(slot).getStack();
            int page = j + 1;

            if (stack == null) continue;

            String id = Type.ECHEST.prefix + "-" + page;
            String title = ColorUtils.stripColor(stack.getDisplayName());

            if (!title.contains("Ender") || !(Block.getBlockFromItem(stack.getItem()) instanceof BlockStainedGlassPane)) {
                continue;
            }

            boolean locked = title.contains("Locked");

            if (loadedContainers.containsKey(id)) {
                SContainer existing = loadedContainers.get(id);
                existing.locked = locked;
                detectedContainers.put(id, existing);
                DebugLogger.log("Using saved data for " + id + " with " + existing.slots.size() + " items");
            } else {
                int renderH = 200;
                int slotCount = getSlotCountFromRenderH(renderH);
                SContainer container = new SContainer(new HashMap<>(), id, page, Type.ECHEST, locked, 307, renderH, slotCount, false);
                detectedContainers.put(id, container);
                DebugLogger.log("Created new empty container " + id);
            }
        }

        for (int j = 0; j < 18; j++) {
            int slot = 27 + j;
            ItemStack stack = chest.getSlot(slot).getStack();
            int page = j + 1;

            if (stack == null) continue;

            String id = Type.BAG.prefix + "-" + page;
            String title = stack.getDisplayName();

            boolean empty = title.startsWith("Empty");
            int renderH = 200;

            String internalName = ItemUtils.getInternalName(stack);
            if (internalName != null && !internalName.isEmpty()) {
                String[] parts = internalName.split("_");
                if (parts.length > 0) {
                    renderH = getBackpackRenderHeight(parts[0]);
                }
            }

            if (loadedContainers.containsKey(id)) {
                SContainer existing = loadedContainers.get(id);
                existing.empty = empty;
                existing.renderH = renderH;
                existing.slotCount = getSlotCountFromRenderH(renderH);
                detectedContainers.put(id, existing);
                DebugLogger.log("Using saved data for " + id + " with " + existing.slots.size() + " items");
            } else {
                int slotCount = getSlotCountFromRenderH(renderH);
                SContainer container = new SContainer(new HashMap<>(), id, page, Type.BAG, empty, 307, renderH, slotCount, false);
                detectedContainers.put(id, container);
                DebugLogger.log("Created new empty container " + id);
            }
        }

        return detectedContainers;
    }
}
