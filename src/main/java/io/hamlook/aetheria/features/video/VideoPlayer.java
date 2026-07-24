package io.hamlook.aetheria.features.video;

/**
 * Facade used by the rest of the video-overlay feature. Delegates to a
 * {@link VideoEngine} implementation ({@link VlcVideoEngine}) that is loaded
 * through {@link VideoFeatureClassLoader} — an isolated classloader that
 * guarantees this mod's own bundled vlcj/JNA classes are used, regardless of
 * whatever (older, incompatible) JNA version Forge/Minecraft/another mod may
 * already have put on the shared classpath. See {@link VideoFeatureClassLoader}
 * for the full explanation.
 * <p>
 * Nothing outside this class should reference {@code VlcVideoEngine} or any
 * vlcj/JNA type directly — always go through this facade (or the plain
 * {@link VideoEngine} interface) so the isolation boundary stays intact.
 */
public class VideoPlayer {

    private static VideoPlayer instance;

    private final VideoEngine engine;

    public static VideoPlayer get() {
        if (instance == null) instance = new VideoPlayer();
        return instance;
    }

    private VideoPlayer() {
        this.engine = loadEngine();
    }

    private static VideoEngine loadEngine() {
        try {
            Class<?> engineClass = VideoFeatureClassLoader.get()
                    .loadClass("io.hamlook.aetheria.features.video.VlcVideoEngine");
            return (VideoEngine) engineClass.getDeclaredConstructor().newInstance();
        } catch (ReflectiveOperationException | LinkageError e) {
            throw new IllegalStateException("Failed to load the isolated video playback engine", e);
        }
    }

    /** Resolves (if needed) and starts playback of the given URL. Blocking network/process call — run off-thread. */
    public void playUrlBlocking(String rawUrl) throws Exception {
        engine.playUrlBlocking(rawUrl);
    }

    public void setVolume(int percent) {
        engine.setVolume(percent);
    }

    public void pause() {
        engine.pause();
    }

    public void resume() {
        engine.resume();
    }

    public boolean isPlaying() {
        return engine.isPlaying();
    }

    /** @return true if a new frame was consumed (caller should re-upload the texture). */
    public boolean pollFrame(FrameConsumer consumer) {
        return engine.pollFrame(consumer::accept);
    }

    public void shutdown() {
        engine.shutdown();
    }

    public interface FrameConsumer {
        void accept(byte[] bgra, int width, int height);
    }
}
