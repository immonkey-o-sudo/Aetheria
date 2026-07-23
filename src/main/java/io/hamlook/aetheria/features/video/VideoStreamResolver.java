package io.hamlook.aetheria.features.video;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.util.concurrent.TimeUnit;

/**
 * Turns a YouTube/Twitch page URL (or any site yt-dlp supports) into a direct,
 * VLC-playable stream URL. Direct file/stream URLs (mp4, m3u8, rtmp, ...) are
 * passed straight through since VLC can already handle those natively.
 */
public final class VideoStreamResolver {

    private VideoStreamResolver() {}

    private static boolean looksLikeDirectMedia(String url) {
        String lower = url.toLowerCase();
        return lower.matches(".*\\.(mp4|webm|mkv|m3u8|mov|flv|ts)([?#].*)?$")
                || lower.startsWith("rtmp://")
                || lower.startsWith("rtsp://");
    }

    private static boolean needsResolution(String url) {
        String lower = url.toLowerCase();
        return lower.contains("youtube.com") || lower.contains("youtu.be") || lower.contains("twitch.tv");
    }

    /**
     * Resolves {@code url} to a direct stream URL. Blocking — always call from a
     * background thread, never the render/client thread.
     *
     * @return a direct URL VLC can open, or the original URL unchanged if it
     *         already looks like direct media / isn't a site yt-dlp needs to handle.
     */
    public static String resolve(String url) throws Exception {
        if (url == null || url.trim().isEmpty()) {
            throw new IllegalArgumentException("No video URL configured");
        }
        if (looksLikeDirectMedia(url) || !needsResolution(url)) {
            return url;
        }

        File ytDlp = YtDlpBootstrap.ensure();

        // "-f b" = best pre-muxed (single stream, audio+video combined) format available.
        // Avoids needing to juggle separate video/audio streams downstream.
        ProcessBuilder pb = new ProcessBuilder(
                ytDlp.getAbsolutePath(), "-f", "b", "-g", "--no-warnings", url
        );
        pb.redirectErrorStream(false);
        Process process = pb.start();

        StringBuilder out = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
            String line;
            while ((line = reader.readLine()) != null) {
                if (out.length() > 0) out.append('\n');
                out.append(line.trim());
            }
        }

        boolean finished = process.waitFor(20, TimeUnit.SECONDS);
        if (!finished) {
            process.destroyForcibly();
            throw new IllegalStateException("yt-dlp timed out resolving " + url);
        }
        if (process.exitValue() != 0 || out.length() == 0) {
            throw new IllegalStateException("yt-dlp could not resolve " + url + " (is it a valid/live link?)");
        }

        // First line is the resolved direct URL (for "-f b" there's only ever one).
        return out.toString().split("\n")[0].trim();
    }
}
