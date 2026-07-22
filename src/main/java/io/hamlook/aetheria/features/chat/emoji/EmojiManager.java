package io.hamlook.aetheria.features.chat.emoji;

import com.google.gson.Gson;
import io.hamlook.aetheria.core.ATHRConfig;
import io.hamlook.aetheria.network.NetworkGuard;
import io.hamlook.aetheria.repo.ATHRRepo;
import io.hamlook.aetheria.utils.HttpClient;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.util.ResourceLocation;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
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
    private static final String MANIFEST_URL = ATHRRepo.BASE + "data/emojis.json";

    private static final File CACHE_DIR = new File(ATHRConfig.configDirectory, "emojis");
    private static final File MANIFEST_FILE = new File(CACHE_DIR, "emojis.json");
    private static final File VERSION_FILE = new File(CACHE_DIR, "version.txt");

    private static final Gson GSON = new Gson();
    private static final HttpClient HTTP = new HttpClient();

    // name/alias (lowercase) -> entry. Swapped in as one atomic reference once
    // fully built so concurrent renders never see a partially-populated map.
    private static volatile Map<String, EmojiEntry> registry = Collections.emptyMap();

    // canonical name -> bound texture location, filled in lazily the first time
    // a given emoji is actually rendered.
    private static final Map<String, ResourceLocation> textures = new ConcurrentHashMap<>();

    private static final AtomicBoolean started = new AtomicBoolean(false);

    private EmojiManager() {
    }

    public static void init() {
        if (!started.compareAndSet(false, true)) return;
        new Thread(EmojiManager::run, "ATHR-EmojiLoader").start();
    }

    private static void run() {
        try {
            CACHE_DIR.mkdirs();
            if (NetworkGuard.githubAllowed()) {
                checkForUpdate();
            }
        } catch (Exception e) {
            System.err.println("[ATHR] Emoji update check failed: " + e.getMessage());
        }
        loadFromDisk();
    }

    // Fetches the tiny version file first; only pulls the full (much larger)
    // manifest down if the version differs from what's cached on disk, or if
    // we don't have a cached manifest at all yet.
    private static void checkForUpdate() {
        try {
            HttpClient.FetchResult versionRes = HTTP.fetch(VERSION_URL, null);
            if (versionRes.body() == null) return;

            VersionFile remote = GSON.fromJson(versionRes.body(), VersionFile.class);
            if (remote == null) return;

            int storedVersion = readStoredVersion();
            boolean needsDownload = !MANIFEST_FILE.exists() || remote.version != storedVersion;
            if (!needsDownload) return;

            HttpClient.FetchResult manifestRes = HTTP.fetch(MANIFEST_URL, null);
            if (manifestRes.body() == null) return;

            Files.write(MANIFEST_FILE.toPath(), manifestRes.body().getBytes(StandardCharsets.UTF_8));
            Files.write(VERSION_FILE.toPath(), String.valueOf(remote.version).getBytes(StandardCharsets.UTF_8));
        } catch (Exception e) {
            System.err.println("[ATHR] Emoji download failed: " + e.getMessage());
        }
    }

    private static int readStoredVersion() {
        try {
            if (!VERSION_FILE.exists()) return -1;
            String raw = new String(Files.readAllBytes(VERSION_FILE.toPath()), StandardCharsets.UTF_8).trim();
            return Integer.parseInt(raw);
        } catch (Exception e) {
            return -1;
        }
    }

    private static void loadFromDisk() {
        if (!MANIFEST_FILE.exists()) return;
        try {
            String json = new String(Files.readAllBytes(MANIFEST_FILE.toPath()), StandardCharsets.UTF_8);
            Manifest manifest = GSON.fromJson(json, Manifest.class);
            if (manifest == null || manifest.emojis == null) return;

            Map<String, EmojiEntry> built = new HashMap<>();
            for (EmojiEntry entry : manifest.emojis) {
                if (entry.name == null || entry.name.isEmpty()) continue;
                built.put(entry.name.toLowerCase(), entry);
                if (entry.aliases != null) {
                    for (String alias : entry.aliases) {
                        if (alias != null && !alias.isEmpty()) built.put(alias.toLowerCase(), entry);
                    }
                }
            }
            registry = built;
        } catch (Exception e) {
            System.err.println("[ATHR] Emoji manifest parse failed: " + e.getMessage());
        }
    }

    public static boolean isLoaded() {
        return !registry.isEmpty();
    }

    public static boolean exists(String nameOrAlias) {
        return nameOrAlias != null && registry.containsKey(nameOrAlias.toLowerCase());
    }

    public static int getWidth(String nameOrAlias) {
        EmojiEntry entry = lookup(nameOrAlias);
        return entry != null ? entry.w : 0;
    }

    public static int getHeight(String nameOrAlias) {
        EmojiEntry entry = lookup(nameOrAlias);
        return entry != null ? entry.h : 0;
    }

    // Returns the bound GL texture for a :name:/alias token, decoding & registering
    // it lazily the first time it's needed. Must be called from the client thread
    // (i.e. from within rendering code).
    public static ResourceLocation getTexture(String nameOrAlias) {
        EmojiEntry entry = lookup(nameOrAlias);
        if (entry == null) return null;

        ResourceLocation cached = textures.get(entry.name);
        if (cached != null) return cached;

        try {
            byte[] png = Base64.getDecoder().decode(entry.data);
            BufferedImage img = ImageIO.read(new ByteArrayInputStream(png));
            if (img == null) return null;

            DynamicTexture tex = new DynamicTexture(img);
            ResourceLocation loc = Minecraft.getMinecraft().getTextureManager()
                    .getDynamicTextureLocation("emoji_" + entry.name, tex);
            textures.put(entry.name, loc);
            return loc;
        } catch (Exception e) {
            return null;
        }
    }

    // Up to `limit` distinct emoji (by canonical name) whose name or an alias
    // starts with `partial`; falls back to "contains" matches to fill any
    // remaining slots. Alphabetical within each tier for a stable order.
    public static List<String> search(String partial, int limit) {
        List<String> results = new ArrayList<>();
        if (partial == null || partial.isEmpty()) return results;
        String needle = partial.toLowerCase();

        TreeSet<String> prefixNames = new TreeSet<>();
        TreeSet<String> containsNames = new TreeSet<>();

        for (Map.Entry<String, EmojiEntry> e : registry.entrySet()) {
            String key = e.getKey();
            String canonical = e.getValue().name;
            if (key.startsWith(needle)) {
                prefixNames.add(canonical);
            } else if (key.contains(needle)) {
                containsNames.add(canonical);
            }
        }

        for (String name : prefixNames) {
            if (results.size() >= limit) return results;
            results.add(name);
        }
        for (String name : containsNames) {
            if (results.size() >= limit) return results;
            if (!results.contains(name)) results.add(name);
        }
        return results;
    }

    private static EmojiEntry lookup(String nameOrAlias) {
        return nameOrAlias != null ? registry.get(nameOrAlias.toLowerCase()) : null;
    }

    private static class Manifest {
        int version;
        List<EmojiEntry> emojis;
    }

    private static class VersionFile {
        int version;
    }

    public static class EmojiEntry {
        String name;
        List<String> aliases;
        int w, h;
        String data;
    }
}
