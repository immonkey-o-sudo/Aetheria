package io.hamlook.aetheria.features.chat.emoji;

import com.google.gson.*;
import com.google.gson.stream.JsonReader;
import io.hamlook.aetheria.Aetheria;
import io.hamlook.aetheria.core.ATHRConfig;
import io.hamlook.aetheria.network.NetworkGuard;
import io.hamlook.aetheria.repo.ATHRRepo;
import io.hamlook.aetheria.utils.ElectionUtils;
import lombok.AllArgsConstructor;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.util.ResourceLocation;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Downloads, caches, and serves the emoji texture set used for :name: chat
 * tokens and the chat-box suggestion popup.
 * <p>
 * Everything is packed into ONE json manifest (name + aliases + a small base64
 * PNG per emoji) hosted alongside Aetheria's other repo data files. On startup
 * we fetch only the small emojis_version.json first, compare it to the version
 * we have cached on disk, and only pull down the full manifest - and only then
 * re-decode textures - when it's actually changed or we have no cache at all.
 * All network access is gated through {@link NetworkGuard}.
 */
public class EmojiManager {

    private static final String VERSION_URL = ATHRRepo.BASE + "data/emojis_version.json";

    private static final File EMOJI_DIR = new File(ATHRConfig.configDirectory, "emojis");

    private static final Gson GSON = new Gson();

    private static final Map<String, Emoji> emojis = new ConcurrentHashMap<>();
    private static final Map<String, String> aliases = new ConcurrentHashMap<>();

    private static final AtomicBoolean loaded = new AtomicBoolean(false);
    private static final ExecutorService executor = Executors.newSingleThreadExecutor();

    public static final String[] EMOJI_THEMES = {EmojiLinks.DISCORD_SHEET,EmojiLinks.GOOGLE_SHEET,EmojiLinks.IOS_SHEET};

    public static void init() {
        executor.execute(EmojiManager::startInitialisation);
    }

    public static void startInitialisation() {
        if(!NetworkGuard.githubAllowed()) return;
        if(!checkIfUpdateNeeded()){
            loadSpritesFromFile();
            registerEmojis();
            return;
        }
        for(String theme : EMOJI_THEMES){
            downloadSheet(theme);
            Aetheria.logger.info("[EMOJI] Downloaded Sheet for " + theme);
        }
        loadSpritesFromFile();
        registerEmojis();
        saveCurrentVersion();
    }

    private static void saveCurrentVersion() {
        try {
            URL url = new URL(VERSION_URL);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestProperty("User-Agent", "Aetheria/" + Aetheria.VERSION);
            conn.setConnectTimeout(10000);
            conn.setReadTimeout(10000);
            if (conn.getResponseCode() == 200) {
                String json = ElectionUtils.readResponse(conn);
                JsonObject obj = JsonParser.parseString(json).getAsJsonObject();
                if (obj != null && obj.has("version")) {
                    VersionFile vf = new VersionFile();
                    vf.version = obj.get("version").getAsInt();
                    File file = new File(EMOJI_DIR, "version.json");
                    file.getParentFile().mkdirs();
                    try (java.io.FileWriter w = new java.io.FileWriter(file)) {
                        GSON.toJson(vf, w);
                    }
                }
            }
        } catch (Exception e) {
            Aetheria.logger.info("[EMOJI] Failed to save version file: " + e.getMessage());
        }
    }

    private static void registerEmojis() {
        emojis.clear();
        aliases.clear();
        try {
            URL url = new URL(EmojiLinks.getEmojiJSON());
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestProperty("User-Agent", "Aetheria");
            connection.setConnectTimeout(10000);
            connection.setReadTimeout(10000);

            int responseCode = connection.getResponseCode();
            Aetheria.logger.info("[EMOJI] Fetching emoji.json — response code: " + responseCode);
            if(responseCode == 200){
                String json = ElectionUtils.readResponse(connection);
                if(json.isEmpty()){
                    Aetheria.logger.info("[EMOJI] emoji.json was empty");
                    return;
                }
                JsonArray obj = JsonParser.parseString(json).getAsJsonArray();
                if(obj == null || obj.isEmpty()){
                    Aetheria.logger.info("[EMOJI] emoji.json parsed to empty array");
                    return;
                }
                for(JsonElement element : obj){
                    JsonObject object = element.getAsJsonObject();
                    if(!object.has("short_name") ||
                    !object.has("sheet_x") || !object.has("sheet_y")) continue;

                    String shortName = object.get("short_name").getAsString();
                    int rawX = object.get("sheet_x").getAsInt();
                    int rawY = object.get("sheet_y").getAsInt();

                    int sheetX = (rawX * (EmojiLinks.SHEET_RESOLUTION + 2)) + 1;
                    int sheetY = (rawY * (EmojiLinks.SHEET_RESOLUTION + 2)) + 1;
                    Emoji emoji = new Emoji(shortName,sheetX,sheetY,false);
                    emojis.put(shortName,emoji);
                    if(object.has("short_names")){
                        JsonArray names = object.get("short_names").getAsJsonArray();
                        for (JsonElement name : names) {
                            aliases.put(name.getAsString(),shortName);
                        }
                    }

                }
            }
            if (!emojis.isEmpty()) loaded.set(true);
            Aetheria.logger.info("[EMOJI] Successfully Loaded " + emojis.size() + " emojis & " + aliases.size() + " aliases.");
        }catch (Exception e){
            Aetheria.logger.info("[EMOJI] Failed to load emojis from github");
            Aetheria.logger.info("[EMOJI] Error: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private static void loadSpritesFromFile() {
        Map<String, BufferedImage> images = new HashMap<>();
        for (String sheet : EMOJI_THEMES) {
            File spriteFile = EmojiLinks.getSpriteFile(sheet);
            if (!spriteFile.exists()) {
                downloadSheet(sheet);
            }
            try {
                BufferedImage sheetImage = ImageIO.read(spriteFile);
                if (sheetImage == null || sheetImage.getWidth() < 32) continue;
                images.put(sheet, sheetImage);
            } catch (IOException e) {
                Aetheria.logger.info("[EMOJI] Error Loading " + sheet + " from file at path: " + spriteFile.getPath());
                Aetheria.logger.info("[EMOJI] Error: " + e.getMessage());
                e.printStackTrace();
            }
        }
        if (!images.isEmpty()) {
            Minecraft.getMinecraft().addScheduledTask(() -> {
                for (Map.Entry<String, BufferedImage> entry : images.entrySet()) {
                    try {
                        EmojiLinks.SHEET_SIZE = entry.getValue().getWidth();
                        DynamicTexture texture = new DynamicTexture(entry.getValue());
                        ResourceLocation location = EmojiLinks.getSpriteResource(entry.getKey());
                        Minecraft.getMinecraft().getTextureManager().loadTexture(location, texture);
                    } catch (Exception e) {
                        Aetheria.logger.info("[EMOJI] Error uploading texture for " + entry.getKey() + ": " + e.getMessage());
                    }
                }
            });
        }
    }

    private static void downloadSheet(String sheet) {
        String urlSuffix = EmojiLinks.sheetToURL(sheet);
        try{
            URL url = new URL(EmojiLinks.getSpriteURL(urlSuffix));
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestProperty("User-Agent", "Aetheria/" + Aetheria.VERSION);
            conn.setConnectTimeout(10000);
            conn.setReadTimeout(10000);

            if(conn.getResponseCode() == 200){
                BufferedImage image = ImageIO.read(conn.getInputStream());
                if(image == null || image.getWidth() < 32) return;
                File path = EmojiLinks.getSpriteFile(sheet);
                EmojiLinks.SHEET_SIZE = image.getWidth();
                ImageIO.write(image, "png", path);
                Aetheria.logger.info("[EMOJI] Successfully downloaded Sheet for " + sheet);
            }else {
                Aetheria.logger.info("[EMOJI](" + conn.getResponseCode() + ") Error Downloading " + sheet + " from url: " + url.getPath());
            }
        }catch (Exception e){
            Aetheria.logger.info("[EMOJI] Error Downloading " + sheet + " from url: " + urlSuffix);
            Aetheria.logger.info("[EMOJI] Error: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private static boolean spritesCorrupted() {
        for (String sheet : EMOJI_THEMES) {
            File file = EmojiLinks.getSpriteFile(sheet);
            if (!file.exists()) return true;
            try {
                BufferedImage img = ImageIO.read(file);
                if (img == null || img.getWidth() < 32) return true;
            } catch (IOException e) {
                return true;
            }
        }
        return false;
    }

    private static boolean checkIfUpdateNeeded() {
        if (spritesCorrupted()) return true;
        VersionFile cachedVersion = loadVersionFile();
        if(cachedVersion == null) return true;
        try{
            URL url = new URL(VERSION_URL);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestProperty("User-Agent", "Aetheria/" + Aetheria.VERSION);
            conn.setConnectTimeout(15000);
            conn.setReadTimeout(15000);

            if(conn.getResponseCode() == 200){
                String json = ElectionUtils.readResponse(conn);
                JsonObject obj = JsonParser.parseString(json).getAsJsonObject();
                if(obj == null) return true;
                if(!obj.has("version")) return  true;
                return cachedVersion.version != obj.get("version").getAsInt();
            }
            return true;
        }catch(Exception e){
            Aetheria.logger.info("Error Loading Version from Github: " + e.getMessage());
            e.printStackTrace();
            return true;
        }
    }

    private static VersionFile loadVersionFile() {
        File file = new File(EMOJI_DIR,"version.json");
        if(!file.exists()) return null;
        try{
            return GSON.fromJson(new JsonReader(new FileReader(file)),VersionFile.class);
        }catch(Exception e){
            Aetheria.logger.info("Error Loading Version from Files: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }

    public static boolean isLoaded() {
        return !emojis.isEmpty() && loaded.get();
    }

    public static boolean exists(String nameOrAlias) {
        return nameOrAlias != null &&
                (emojis.containsKey(nameOrAlias.toLowerCase()) || aliases.containsKey(nameOrAlias.toLowerCase()));
    }
    public static List<String> search(String partial, int limit) {
        String lower = partial.toLowerCase();
        Set<String> seen = new HashSet<>();
        List<String> results = new ArrayList<>();

        for (String name : emojis.keySet()) {
            if (results.size() >= limit) break;
            if (name.toLowerCase().startsWith(lower)) {
                results.add(name);
                seen.add(name);
            }
        }

        if (results.size() < limit) {
            for (Map.Entry<String, String> alias : aliases.entrySet()) {
                if (results.size() >= limit) break;
                if (alias.getKey().toLowerCase().startsWith(lower) && !seen.contains(alias.getValue())) {
                    results.add(alias.getKey());
                }
            }
        }

        return results;
    }

    public static Emoji getEmoji(String nameOrAlias) {
        if(emojis.containsKey(nameOrAlias.toLowerCase())) return emojis.get(nameOrAlias.toLowerCase());
        if(aliases.containsKey(nameOrAlias.toLowerCase())){
            return emojis.get(aliases.get(nameOrAlias.toLowerCase()));
        }
        return null;
    }

    private static class VersionFile {
        int version;
    }

    @AllArgsConstructor
    public static class Emoji {
        public String name;
        public int sheetX,sheetY;
        public boolean custom;
    }
}
