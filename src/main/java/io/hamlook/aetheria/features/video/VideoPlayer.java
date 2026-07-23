package io.hamlook.aetheria.features.video;

import uk.co.caprica.vlcj.factory.MediaPlayerFactory;
import uk.co.caprica.vlcj.player.base.MediaPlayer;
import uk.co.caprica.vlcj.player.base.MediaPlayerEventAdapter;
import uk.co.caprica.vlcj.player.embedded.EmbeddedMediaPlayer;
import uk.co.caprica.vlcj.player.embedded.videosurface.callback.BufferFormatCallback;
import uk.co.caprica.vlcj.player.embedded.videosurface.callback.RenderCallback;
import uk.co.caprica.vlcj.player.embedded.videosurface.callback.format.RV32BufferFormat;
import uk.co.caprica.vlcj.player.embedded.videosurface.CallbackVideoSurface;
import uk.co.caprica.vlcj.player.embedded.videosurface.VideoSurfaceAdapters;
import uk.co.caprica.vlcj.player.embedded.videosurface.callback.BufferFormat;

import java.nio.ByteBuffer;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Headless (no native window) VLC player. Frames are delivered as raw BGRA
 * byte buffers via {@link RenderCallback}, which {@link VideoFrameTexture}
 * uploads straight into an OpenGL texture each Minecraft frame.
 * <p>
 * Requires a system install of 64-bit VLC (libvlc discovered automatically
 * by vlcj's NativeDiscovery) — this class does not bundle VLC itself.
 */
public class VideoPlayer {

    private MediaPlayerFactory factory;
    private EmbeddedMediaPlayer mediaPlayer;

    private volatile byte[] frontBuffer = new byte[0];
    private final AtomicInteger width = new AtomicInteger(0);
    private final AtomicInteger height = new AtomicInteger(0);
    private final AtomicBoolean frameDirty = new AtomicBoolean(false);
    private final Object bufferLock = new Object();

    private static VideoPlayer instance;

    public static VideoPlayer get() {
        if (instance == null) instance = new VideoPlayer();
        return instance;
    }

    private void ensureStarted() {
        if (mediaPlayer != null) return;

        factory = new MediaPlayerFactory();
        CallbackVideoSurface surface = new CallbackVideoSurface(
                new BufferFormatCallback() {
                    @Override
                    public BufferFormat getBufferFormat(int sourceWidth, int sourceHeight) {
                        width.set(sourceWidth);
                        height.set(sourceHeight);
                        return new RV32BufferFormat(sourceWidth, sourceHeight);
                    }

                    @Override
                    public void allocatedBuffers(ByteBuffer[] buffers) {
                        // no-op, we copy out in the render callback instead of holding these directly
                    }
                },
                new RenderCallback() {
                    @Override
                    public void display(MediaPlayer mediaPlayer, ByteBuffer[] nativeBuffers, BufferFormat bufferFormat) {
                        ByteBuffer buf = nativeBuffers[0];
                        int size = buf.remaining();
                        byte[] copy = new byte[size];
                        buf.get(copy);
                        synchronized (bufferLock) {
                            frontBuffer = copy;
                        }
                        frameDirty.set(true);
                    }
                },
                true,
                VideoSurfaceAdapters.getVideoSurfaceAdapter()
        );

        mediaPlayer = factory.mediaPlayers().newEmbeddedMediaPlayer();
        mediaPlayer.videoSurface().set(surface);

        mediaPlayer.events().addMediaPlayerEventListener(new MediaPlayerEventAdapter() {
            @Override
            public void finished(MediaPlayer mp) {
                if (VideoOverlayConfig.get().loop) {
                    mp.controls().play();
                }
            }
            @Override
            public void error(MediaPlayer mp) {
                System.err.println("[ATHR/VideoOverlay] VLC reported a playback error.");
            }
        });
    }

    /** Resolves (if needed) and starts playback of the given URL. Blocking network/process call — run off-thread. */
    public void playUrlBlocking(String rawUrl) throws Exception {
        String direct = VideoStreamResolver.resolve(rawUrl);
        ensureStarted();
        mediaPlayer.audio().setVolume(clampVolume(VideoOverlayConfig.get().volume));
        mediaPlayer.media().play(direct);
    }

    public void setVolume(int percent) {
        if (mediaPlayer != null) mediaPlayer.audio().setVolume(clampVolume(percent));
    }

    public void pause() {
        if (mediaPlayer != null) mediaPlayer.controls().setPause(true);
    }

    public void resume() {
        if (mediaPlayer != null) mediaPlayer.controls().setPause(false);
    }

    public boolean isPlaying() {
        return mediaPlayer != null && mediaPlayer.status().isPlaying();
    }

    private static int clampVolume(int v) {
        return Math.max(0, Math.min(100, v));
    }

    /** @return true if a new frame was consumed (caller should re-upload the texture). */
    public boolean pollFrame(FrameConsumer consumer) {
        if (!frameDirty.compareAndSet(true, false)) return false;
        byte[] snapshot;
        synchronized (bufferLock) {
            snapshot = frontBuffer;
        }
        int w = width.get(), h = height.get();
        if (w <= 0 || h <= 0 || snapshot.length < w * h * 4) return false;
        consumer.accept(snapshot, w, h);
        return true;
    }

    public void shutdown() {
        if (mediaPlayer != null) {
            mediaPlayer.controls().stop();
            mediaPlayer.release();
            mediaPlayer = null;
        }
        if (factory != null) {
            factory.release();
            factory = null;
        }
    }

    public interface FrameConsumer {
        void accept(byte[] bgra, int width, int height);
    }
}
