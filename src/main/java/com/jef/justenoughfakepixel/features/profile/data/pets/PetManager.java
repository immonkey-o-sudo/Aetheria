package com.jef.justenoughfakepixel.features.profile.data.pets;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.jef.justenoughfakepixel.core.JefConfig;
import net.minecraft.client.Minecraft;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.ResourceLocation;
import org.apache.commons.codec.binary.Base64;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PetManager {

    private static final HashMap<String, String> petSkins = new HashMap<>();

    public static class ParsedPet {
        public String type;
        public int level;
        public int rarityScore;
        public String rarityName;
        public boolean active;

        public ParsedPet(Pet pet) {
            this.active = pet.equipped;
            String stripped = net.minecraft.util.StringUtils.stripControlCodes(pet.data.displayName);

            try {
                if (stripped.contains("[Lvl ")) {
                    int start = stripped.indexOf("[Lvl ") + 5;
                    int end = stripped.indexOf("]");
                    this.level = Integer.parseInt(stripped.substring(start, end));
                    this.type = stripped.substring(end + 2).trim();
                } else {
                    this.level = 1;
                    this.type = stripped;
                }
            } catch (Exception e) {
                this.level = 1;
                this.type = "Unknown";
            }

            this.rarityScore = 1;
            this.rarityName = "COMMON";

            String rawName = pet.data.displayName;
            String lastColor = "";

            for (int i = 0; i < rawName.length() - 1; i++) {
                if (rawName.charAt(i) == '§' || rawName.charAt(i) == '&') {
                    char colorChar = Character.toLowerCase(rawName.charAt(i + 1));
                    if ("0123456789abcdef".indexOf(colorChar) != -1) {
                        lastColor = String.valueOf(colorChar);
                    }
                }
            }

            switch (lastColor) {
                case "d": this.rarityScore = 6; this.rarityName = "MYTHIC"; break;
                case "6": this.rarityScore = 5; this.rarityName = "LEGENDARY"; break;
                case "5": this.rarityScore = 4; this.rarityName = "EPIC"; break;
                case "9": this.rarityScore = 3; this.rarityName = "RARE"; break;
                case "a": this.rarityScore = 2; this.rarityName = "UNCOMMON"; break;
                case "f":
                default:
                    this.rarityName = "COMMON"; break;
            }
        }
    }

    public static void initSkins() {
        if (!petSkins.isEmpty()) return;
        try {
            ResourceLocation loc = new ResourceLocation("justenoughfakepixel", "petSkins.json");
            InputStreamReader reader = new InputStreamReader(Minecraft.getMinecraft().getResourceManager().getResource(loc).getInputStream());
            JsonObject json = new Gson().fromJson(reader, JsonObject.class);
            for (Map.Entry<String, com.google.gson.JsonElement> entry : json.entrySet()) {
                petSkins.put(entry.getKey(), entry.getValue().getAsString());
            }
            reader.close();

            new Thread(() -> {
                File cacheDir = new File(JefConfig.configDirectory, "petSkins");
                if (!cacheDir.exists()) cacheDir.mkdirs();

                for (Map.Entry<String, String> entry : petSkins.entrySet()) {
                    File imgFile = new File(cacheDir, entry.getKey() + ".png");
                    if (!imgFile.exists()) {
                        try (InputStream in = new URL(entry.getValue()).openStream();
                             FileOutputStream out = new FileOutputStream(imgFile)) {
                            byte[] buffer = new byte[1024];
                            int bytesRead;
                            while ((bytesRead = in.read(buffer)) != -1) {
                                out.write(buffer, 0, bytesRead);
                            }
                        } catch (Exception ignored) {}
                    }
                }
            }).start();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static ItemStack getPetSkull(String petType) {
        initSkins();
        String key = petType.toUpperCase().replace(" ", "_");
        String url = petSkins.getOrDefault(key, "https://textures.minecraft.net/texture/38ab5ff554985a177638be6511e955abcc5f38545123baf943efdefa783f35ef");

        String jsonSkin = "{\"textures\":{\"SKIN\":{\"url\":\"" + url + "\"}}}";
        String base64 = Base64.encodeBase64String(jsonSkin.getBytes());

        ItemStack skull = new ItemStack(Items.skull, 1, 3);
        NBTTagCompound tag = new NBTTagCompound();
        NBTTagCompound skullOwner = new NBTTagCompound();
        skullOwner.setString("Id", java.util.UUID.randomUUID().toString());
        NBTTagCompound properties = new NBTTagCompound();
        NBTTagList textures = new NBTTagList();
        NBTTagCompound texture = new NBTTagCompound();
        texture.setString("Value", base64);
        textures.appendTag(texture);
        properties.setTag("textures", textures);
        skullOwner.setTag("Properties", properties);
        tag.setTag("SkullOwner", skullOwner);
        skull.setTagCompound(tag);
        return skull;
    }

    public static int calculatePetScore(List<ParsedPet> pets) {
        HashMap<String, ParsedPet> uniquePets = new HashMap<>();
        for (ParsedPet p : pets) {
            if (!uniquePets.containsKey(p.type)) {
                uniquePets.put(p.type, p);
            } else {
                ParsedPet existing = uniquePets.get(p.type);
                if (p.rarityScore > existing.rarityScore || (p.rarityScore == existing.rarityScore && p.level > existing.level)) {
                    uniquePets.put(p.type, p);
                }
            }
        }

        int score = 0;
        for (ParsedPet p : uniquePets.values()) {
            score += p.rarityScore;
            if (p.level >= 100) score += 1;
        }
        return score;
    }
}