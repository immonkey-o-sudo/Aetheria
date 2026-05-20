package com.jef.justenoughfakepixel.features.itemList;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.jef.justenoughfakepixel.JefMod;
import com.jef.justenoughfakepixel.features.profile.data.ItemData;

import java.io.InputStreamReader;
import java.lang.reflect.Type;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ItemRegistry {

    public static HashMap<String, SkyblockItem> itemRegistry = new HashMap<>();
    public static LinkedHashMap<String, ItemFamily> familyRegistry = new LinkedHashMap<>();

    private static final Gson GSON = new Gson();

    private static final Pattern ROMAN_SUFFIX =
            Pattern.compile("^(.+?)\\s+(I{1,3}|IV|V?I{0,3}|IX|X{1,3})$", Pattern.CASE_INSENSITIVE);
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
                            items.forEach((id, item) -> {
                                item.skyblockID = id;
                                itemRegistry.put(id, item);
                            });
                            JefMod.logger.info("[JEF] Loaded " + itemRegistry.size() + " items.");
                            buildFamilies();
                        }
                    }
                } else {
                    JefMod.logger.severe("[JEF] Failed to load items. HTTP: " + conn.getResponseCode());
                }
            } catch (Exception e) {
                JefMod.logger.severe("[JEF] Exception loading items");
                e.printStackTrace();
            }
        }, "JEF-ItemRegistry-Loader").start();
    }


    private static void buildFamilies() {
        Map<String, ItemFamily> pending = new LinkedHashMap<>();

        for (Map.Entry<String, SkyblockItem> entry : itemRegistry.entrySet()) {
            String id   = entry.getKey();
            SkyblockItem item = entry.getValue();

            Matcher petM = PET_RARITY.matcher(id);
            if (petM.matches()) {
                String base   = petM.group(1);
                int rarityNum = Integer.parseInt(petM.group(2)); // 0=common … 5=mythic
                String famKey = "PET_" + base;
                ItemFamily fam = pending.computeIfAbsent(famKey,
                        k -> new ItemFamily(famKey, stripColor(item.displayName), ItemFamily.FamilyType.PET));
                item.familyId = famKey;
                item.familyMemberLabel = rarityNum < RARITY_NAMES.length
                        ? RARITY_NAMES[rarityNum] : "§f?";
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

            String cleanName = stripColor(item.displayName != null ? item.displayName : id);
            Matcher romM = ROMAN_SUFFIX.matcher(cleanName);
            if (romM.matches()) {
                String baseName = romM.group(1).trim();
                String level    = romM.group(2).trim();
                String famKey = "ENC_" + baseName.toUpperCase().replaceAll("\\s+", "_");
                ItemFamily fam = pending.computeIfAbsent(famKey,
                        k -> new ItemFamily(famKey, colouredBaseName(item.displayName, baseName), ItemFamily.FamilyType.ENCHANTMENT));
                item.familyId = famKey;
                item.familyMemberLabel = level;
                fam.members.add(item);
                fam.members.sort(Comparator.comparingInt(i -> romanToInt(stripColor(i.displayName))));
                continue;
            }

            String famKey = "SOLO_" + id;
            ItemFamily fam = new ItemFamily(famKey,
                    item.displayName != null ? item.displayName : id, ItemFamily.FamilyType.NONE);
            item.familyId = famKey;
            item.familyMemberLabel = null;
            fam.members.add(item);
            pending.put(famKey, fam);
        }

        familyRegistry.clear();
        familyRegistry.putAll(pending);
        JefMod.logger.info("[JEF] Built " + familyRegistry.size() + " item families.");
    }


    private static String stripColor(String s) {
        return s == null ? "" : s.replaceAll("§.", "");
    }

    private static String colouredBaseName(String full, String strippedBase) {
        if (full == null) return strippedBase;
        return full.replaceAll("\\s+(I{1,3}|IV|V?I{0,3}|IX|X{1,3})$", "").trim();
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
        Map<String, Integer> map = new LinkedHashMap<>();
        map.put("X", 10); map.put("IX", 9); map.put("VIII", 8); map.put("VII", 7);
        map.put("VI", 6); map.put("V", 5); map.put("IV", 4); map.put("III", 3);
        map.put("II", 2); map.put("I", 1);
        return map.getOrDefault(r, 99);
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