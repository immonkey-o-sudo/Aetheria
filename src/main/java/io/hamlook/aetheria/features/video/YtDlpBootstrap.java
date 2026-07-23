package io.hamlook.aetheria.features.video;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * Ensures a working yt-dlp.exe is present under config/Aetheria/bin/.
 * Downloaded lazily the first time a YouTube/Twitch URL needs resolving,
 * mirroring the download pattern already used by ModUpdater.
 * Windows-only by design (matches the feature's target platform).
 */
public final class YtDlpBootstrap {

    private static final String DOWNLOAD_URL =
            "https://github.com/yt-dlp/yt-dlp/releases/latest/download/yt-dlp.exe";

    private static final File BIN_DIR = new File("config/Aetheria/bin");
    private static final File EXE = new File(BIN_DIR, "yt-dlp.exe");

    private YtDlpBootstrap() {}

    /** Returns the yt-dlp executable, downloading it first if necessary. Blocking — call off the render thread. */
    public static File ensure() throws Exception {
        if (EXE.exists() && EXE.length() > 0) return EXE;

        BIN_DIR.mkdirs();
        System.out.println("[ATHR/VideoOverlay] Downloading yt-dlp.exe (first run only)...");

        HttpURLConnection conn = (HttpURLConnection) new URL(DOWNLOAD_URL).openConnection();
        conn.setRequestProperty("User-Agent", "Aetheria-VideoOverlay");
        conn.setInstanceFollowRedirects(true);
        conn.setConnectTimeout(15_000);
        conn.setReadTimeout(30_000);

        if (conn.getResponseCode() != 200) {
            throw new IllegalStateException("yt-dlp download failed, HTTP " + conn.getResponseCode());
        }

        try (InputStream in = conn.getInputStream();
             FileOutputStream out = new FileOutputStream(EXE)) {
            byte[] buf = new byte[8192];
            int read;
            while ((read = in.read(buf)) != -1) out.write(buf, 0, read);
        }

        System.out.println("[ATHR/VideoOverlay] yt-dlp.exe ready at " + EXE.getAbsolutePath());
        return EXE;
    }
}
