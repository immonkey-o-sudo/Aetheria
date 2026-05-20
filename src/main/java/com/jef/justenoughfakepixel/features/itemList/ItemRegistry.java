package com.jef.justenoughfakepixel.features.itemList;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.jef.justenoughfakepixel.JefMod;
import com.jef.justenoughfakepixel.features.profile.data.ItemData;
import net.minecraft.item.ItemStack;

import java.io.InputStreamReader;
import java.lang.reflect.Type;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.*;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ItemRegistry {

    public static volatile Map<String, SkyblockItem> itemRegistry = new HashMap<>();
    public static volatile Map<String, ItemFamily> familyRegistry = new LinkedHashMap<>();
    public static volatile boolean isLoaded = false;

    // Background Queue to warm up the texture cache without lagging the main thread
    public static Queue<ItemStack> preloadQueue = new ConcurrentLinkedQueue<>();

    private static final Gson GSON = new Gson();

    // Expanded to catch up to XX just in case (e.g. Cleave XII)
    private static final Pattern LEVEL_SUFFIX = Pattern.compile("^(.+?)\\s+(I|II|III|IV|V|VI|VII|VIII|IX|X|XI|XII|XIII|XIV|XV|XVI|XVII|XVIII|XIX|XX|\\d+)$", Pattern.CASE_INSENSITIVE);
    private static final Pattern PET_RARITY   = Pattern.compile("^(.+);(\\d)$");
    private static final Pattern RUNE_RARITY  = Pattern.compile("^(.+_RUNE);(\\d)$");
    private static final Pattern ACC_TIER     = Pattern.compile("^(.+?)_(TALISMAN|RING|ARTIFACT)$");

    private static final String[] RARITY_NAMES = {
            "§fCommon", "§aUncommon", "§9Rare", "§5Epic", "§6Legendary", "§dMythic", "§bDivine", "§4Special"
    };

    public static void initialise() {
        new Thread(() -> {
            try {
                URL url = new URL("https://raw.githubusercontent.com/GinaFro1/FPItemData/main/items/itemData.json");
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestProperty("User-Agent", "JustEnoughFakepixel");
                conn.setConnectTimeout(10000);
                conn.setReadTimeout(10000);

                if (conn.getResponseCode() == 200) {
                    try (InputStreamReader reader = new InputStreamReader(conn.getInputStream())) {
                        Type type = new TypeToken<Map<String, SkyblockItem>>(){}.getType();
                        Map<String, SkyblockItem> items = GSON.fromJson(reader, type);

                        if (items != null) {
                            JefMod.logger.info("[JEF] Fetched " + items.size() + " items from JSON. Pre-loading stacks...");

                            Map<String, SkyblockItem> tempItemRegistry = new HashMap<>();

                            int count = 0;
                            for (Map.Entry<String, SkyblockItem> entry : items.entrySet()) {
                                String id = entry.getKey();
                                SkyblockItem item = entry.getValue();

                                // Extract actual Enchantment names out of the lore
                                if (item.displayName != null && stripColor(item.displayName).trim().equalsIgnoreCase("Enchanted Book") && item.baseLore != null && !item.baseLore.isEmpty()) {
                                    String firstLore = item.baseLore.get(0);
                                    if (firstLore.trim().length() > 2) {
                                        item.displayName = firstLore.trim();
                                    }
                                }

                                item.skyblockID = id;
                                item.idLower = id.toLowerCase();
                                item.cleanNameLower = item.displayName != null ? stripColor(item.displayName).trim().toLowerCase() : item.idLower;

                                tempItemRegistry.put(id, item);

                                try {
                                    ItemStack stack = item.getStack(); // Pre-load NBT
                                    if (stack != null) preloadQueue.add(stack); // Queue texture preloader
                                    parseLoreMeta(item); // Extract type/rarity filters
                                } catch (Exception ex) {
                                    JefMod.logger.severe("[JEF] Failed to pre-load stack for " + id + ": " + ex.getMessage());
                                }
                                count++;
                                if (count % 1000 == 0) {
                                    JefMod.logger.info("[JEF] Pre-loaded " + count + " items...");
                                }
                            }
                            itemRegistry = tempItemRegistry; // Atomic swap
                            JefMod.logger.info("[JEF] Loaded " + itemRegistry.size() + " items total.");

                            JefMod.logger.info("[JEF] Building item families...");
                            buildFamilies();
                        } else {
                            JefMod.logger.severe("[JEF] itemData.json parsed to null!");
                        }
                    }
                } else {
                    JefMod.logger.severe("[JEF] Failed to load items. HTTP: " + conn.getResponseCode());
                }
            } catch (Exception e) {
                JefMod.logger.severe("[JEF] Exception loading items in background thread:");
                e.printStackTrace();
            }
        }, "JEF-ItemRegistry-Loader").start();
    }

    private static void parseLoreMeta(SkyblockItem item) {
        if (item.baseLore != null && !item.baseLore.isEmpty()) {
            String lastLine = stripColor(item.baseLore.get(item.baseLore.size() - 1)).trim();
            String[] rarities = {"COMMON", "UNCOMMON", "RARE", "EPIC", "LEGENDARY", "MYTHIC", "DIVINE", "SPECIAL", "VERY SPECIAL"};
            for (String r : rarities) {
                if (lastLine.startsWith(r)) {
                    item.itemRarity = r;
                    item.itemType = lastLine.substring(r.length()).trim();
                    return;
                }
            }
            item.itemType = lastLine;
        }
    }

    private static void buildFamilies() {
        Map<String, ItemFamily> pending = new LinkedHashMap<>();

        for (Map.Entry<String, SkyblockItem> entry : itemRegistry.entrySet()) {
            String id   = entry.getKey();
            SkyblockItem item = entry.getValue();

            Matcher petM = PET_RARITY.matcher(id);
            if (petM.matches()) {
                String base   = petM.group(1);
                int rarityNum = Integer.parseInt(petM.group(2));
                String famKey = "PET_" + base;
                ItemFamily fam = pending.computeIfAbsent(famKey,
                        k -> new ItemFamily(famKey, stripColor(item.displayName), ItemFamily.FamilyType.PET));
                item.familyId = famKey;
                item.familyMemberLabel = rarityNum < RARITY_NAMES.length ? RARITY_NAMES[rarityNum] : "§f?";
                fam.members.add(item);
                fam.members.sort(Comparator.comparing(i -> i.skyblockID));
                continue;
            }

            Matcher runeM = RUNE_RARITY.matcher(id);
            if (runeM.matches()) {
                String base   = runeM.group(1);
                String famKey = "RUNE_" + base;
                ItemFamily fam = pending.computeIfAbsent(famKey,
                        k -> new ItemFamily(famKey, stripColor(item.displayName), ItemFamily.FamilyType.ENCHANTMENT));
                item.familyId = famKey;
                item.familyMemberLabel = "Level " + runeM.group(2);
                fam.members.add(item);
                continue;
            }

            Matcher accM = ACC_TIER.matcher(id);
            if (accM.matches()) {
                String base   = accM.group(1);
                String tier   = accM.group(2);
                String famKey = "ACC_" + base;
                String cleanName = cleanAccessoryName(stripColor(item.displayName), tier);
                ItemFamily fam = pending.computeIfAbsent(famKey,
                        k -> new ItemFamily(famKey, cleanName, ItemFamily.FamilyType.ACCESSORY));
                item.familyId = famKey;
                item.familyMemberLabel = capFirst(tier.toLowerCase());
                fam.members.add(item);
                fam.members.sort(Comparator.comparingInt(ItemRegistry::accTierOrder));
                continue;
            }

            String cleanName = stripColor(item.displayName != null ? item.displayName : id).trim();
            Matcher levelM = LEVEL_SUFFIX.matcher(cleanName);
            if (levelM.matches()) {
                String baseName = levelM.group(1).trim();
                String level    = levelM.group(2).trim();
                String famKey = "ENC_" + baseName.toUpperCase().replaceAll("\\s+", "_");
                ItemFamily fam = pending.computeIfAbsent(famKey,
                        k -> new ItemFamily(famKey, "", ItemFamily.FamilyType.ENCHANTMENT));
                item.familyId = famKey;
                item.familyMemberLabel = level;
                fam.members.add(item);
                fam.members.sort(Comparator.comparingInt(i -> romanToInt(stripColor(i.familyMemberLabel))));
                continue;
            }

            String famKey = "SOLO_" + id;
            ItemFamily fam = new ItemFamily(famKey, item.displayName != null ? item.displayName : id, ItemFamily.FamilyType.NONE);
            item.familyId = famKey;
            item.familyMemberLabel = null;
            fam.members.add(item);
            pending.put(famKey, fam);
        }

        // Second pass: Apply highest rarity colors and correct Enchantment names
        for (ItemFamily fam : pending.values()) {
            if (fam.members.isEmpty()) continue;

            SkyblockItem highest = fam.members.get(fam.members.size() - 1);
            String color = "§f";

            if (fam.type == ItemFamily.FamilyType.ENCHANTMENT) {
                String baseName = toTitleCase(highest.skyblockID.replace("ENCHANTMENT_", "").replaceAll("_\\d+$", ""));
                if (highest.displayName != null && highest.displayName.trim().length() >= 2 && highest.displayName.trim().charAt(0) == '§') {
                    color = highest.displayName.trim().substring(0, 2);
                    // Accurately separate trailing roman numerals by splitting words
                    baseName = stripRomanNumeral(stripColor(highest.displayName));
                }
                fam.updateDisplayName(color + baseName);
            } else {
                if (fam.type == ItemFamily.FamilyType.NONE) {
                    fam.members.sort(Comparator.comparing(i -> stripColor(i.displayName)));
                }
                if (highest.displayName != null && highest.displayName.trim().length() >= 2 && highest.displayName.trim().charAt(0) == '§') {
                    color = highest.displayName.trim().substring(0, 2);
                }
                fam.updateDisplayName(color + stripColor(fam.displayName).trim());
            }
        }

        familyRegistry = pending; // Atomic swap
        isLoaded = true; // Signal the UI that loading is finished
        JefMod.logger.info("[JEF] Built " + familyRegistry.size() + " item families. Initialization Complete!");
    }

    /**
     * Splits a string by spaces and explicitly removes the last word if it matches a Roman Numeral or digit.
     */
    private static String stripRomanNumeral(String name) {
        if (name == null || name.trim().isEmpty()) return name;
        String clean = name.trim();
        String[] parts = clean.split("\\s+");

        if (parts.length > 1) {
            String lastWord = parts[parts.length - 1].toUpperCase();
            if (lastWord.matches("\\d+") || lastWord.matches("^(I|II|III|IV|V|VI|VII|VIII|IX|X|XI|XII|XIII|XIV|XV|XVI|XVII|XVIII|XIX|XX)$")) {
                StringBuilder sb = new StringBuilder();
                for (int i = 0; i < parts.length - 1; i++) {
                    sb.append(parts[i]);
                    if (i < parts.length - 2) sb.append(" ");
                }
                return sb.toString();
            }
        }
        return clean;
    }

    private static String toTitleCase(String input) {
        if (input == null || input.isEmpty()) return input;
        StringBuilder sb = new StringBuilder();
        boolean capitalize = true;
        for (char c : input.toCharArray()) {
            if (c == ' ' || c == '_') {
                sb.append(' ');
                capitalize = true;
            } else if (capitalize) {
                sb.append(Character.toUpperCase(c));
                capitalize = false;
            } else {
                sb.append(Character.toLowerCase(c));
            }
        }
        return sb.toString();
    }

    private static String stripColor(String s) {
        return s == null ? "" : s.replaceAll("§.", "");
    }

    private static String cleanAccessoryName(String name, String tier) {
        String t = capFirst(tier.toLowerCase());
        if (name.endsWith(t)) name = name.substring(0, name.length() - t.length()).trim();
        return name;
    }

    private static String capFirst(String s) {
        if (s == null || s.isEmpty()) return s;
        return Character.toUpperCase(s.charAt(0)) + s.substring(1);
    }

    private static int accTierOrder(SkyblockItem i) {
        if (i.skyblockID.endsWith("_TALISMAN"))  return 0;
        if (i.skyblockID.endsWith("_RING"))      return 1;
        if (i.skyblockID.endsWith("_ARTIFACT"))  return 2;
        return 3;
    }

    private static int romanToInt(String name) {
        String[] parts = name.trim().split("\\s+");
        String r = parts[parts.length - 1].toUpperCase();
        try {
            return Integer.parseInt(r);
        } catch (NumberFormatException e) {
            Map<String, Integer> map = new LinkedHashMap<>();
            map.put("X", 10); map.put("IX", 9); map.put("VIII", 8); map.put("VII", 7);
            map.put("VI", 6); map.put("V", 5); map.put("IV", 4); map.put("III", 3);
            map.put("II", 2); map.put("I", 1);
            return map.getOrDefault(r, 99);
        }
    }

    public static SkyblockItem getWithItemData(SkyblockItem base, ItemData data) {
        if (base == null) return null;
        SkyblockItem item = base.clone();
        if (data.lore != null && !data.lore.isEmpty()) item.baseLore = data.lore;
        item.enchanted = data.enchanted;
        if (data.displayName != null && !data.displayName.isEmpty()) item.displayName = data.displayName;
        return item;
    }
}