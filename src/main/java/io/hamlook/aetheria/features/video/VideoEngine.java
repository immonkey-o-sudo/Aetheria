package io.hamlook.aetheria.features.video;

/**
 * Contract for a video playback backend. Deliberately uses only plain JDK types
 * (String, byte[], int, boolean) in its method signatures — never vlcj or JNA
 * types — because instances of the implementation are loaded through
 * {@link VideoFeatureClassLoader}, an isolated classloader boundary. Any vlcj/JNA
 * type appearing here would be a different, incompatible Class object on each
 * side of that boundary and cross-boundary calls would fail with
 * {@code ClassCastException} / {@code LinkageError}.
 */
public interface VideoEngine {

    /** Resolves (if needed) and starts playback of the given URL. Blocking — run off-thread. */
    void playUrlBlocking(String rawUrl) throws Exception;

    void setVolume(int percent);

    void pause();

    void resume();

    boolean isPlaying();

    void shutdown();

    /** @return true if a new frame was consumed (caller should re-upload the texture). */
    boolean pollFrame(FrameConsumer consumer);

    interface FrameConsumer {
        void accept(byte[] bgra, int width, int height);
    }
}
