package io.hamlook.aetheria.data;

import com.google.gson.Gson;
import io.hamlook.aetheria.Aetheria;
import io.hamlook.aetheria.core.ATHRConfig;
import net.minecraft.client.Minecraft;
import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.common.ModContainer;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

public class ApiHandler {

    private static final String API_URL = "https://raw.githubusercontent.com/hamlook/JustEnoughFakepixel/main/data/repo.json";
    private static final Gson GSON = new Gson();
    private static final ExecutorService EXECUTOR = Executors.newSingleThreadExecutor(r -> {
        Thread t = new Thread(r, "ATHR-Analytics");
        t.setDaemon(true);
        return t;
    });

    private ApiHandler() {}

    public static void onServerJoin() {
        if (ATHRConfig.feature == null) return;

        EXECUTOR.submit(() -> {
            try {
                String apiUrl = fetchApiUrl();
                if (apiUrl == null || apiUrl.isEmpty()) return;

                String username = Minecraft.getMinecraft().getSession().getUsername();
                List<String> modList = Loader.instance().getModList().stream()
                        .map(ModContainer::getModId)
                        .collect(Collectors.toList());

                String json = GSON.toJson(new Payload(username, modList, Aetheria.VERSION));
                sendPost(apiUrl, json);
            } catch (Exception ignored) {}
        });
    }

    private static String fetchApiUrl() {
        try {
            HttpURLConnection conn = (HttpURLConnection) new URL(API_URL).openConnection();
            conn.setRequestMethod("GET");
            conn.setConnectTimeout(5000);
            conn.setReadTimeout(5000);

            StringBuilder sb = new StringBuilder();
            try (BufferedReader reader = new BufferedReader(
                    new InputStreamReader(conn.getInputStream(), StandardCharsets.UTF_8))) {
                String line;
                while ((line = reader.readLine()) != null) sb.append(line);
            }
            conn.disconnect();

            RemoteConfig config = GSON.fromJson(sb.toString(), RemoteConfig.class);
            if (config == null || config.apiUrl == null) return null;

            // decode from Base64
            return new String(java.util.Base64.getDecoder().decode(config.apiUrl), StandardCharsets.UTF_8);
        } catch (Exception ignored) {
            return null;
        }
    }

    private static void sendPost(String apiUrl, String json) throws Exception {
        HttpURLConnection conn = (HttpURLConnection) new URL(apiUrl).openConnection();
        conn.setRequestMethod("POST");
        conn.setRequestProperty("Content-Type", "application/json; charset=utf-8");
        conn.setConnectTimeout(5000);
        conn.setReadTimeout(5000);
        conn.setDoOutput(true);
        try (OutputStream os = conn.getOutputStream()) {
            os.write(json.getBytes(StandardCharsets.UTF_8));
        }
        conn.getResponseCode();
        conn.disconnect();
    }

    private static class RemoteConfig {
        String apiUrl;
    }

    private static class Payload {
        final String username;
        final List<String> modList;
        final String ATHRVersion;

        Payload(String username, List<String> modList, String ATHRVersion) {
            this.username = username;
            this.modList = modList;
            this.ATHRVersion = ATHRVersion;
        }
    }
}