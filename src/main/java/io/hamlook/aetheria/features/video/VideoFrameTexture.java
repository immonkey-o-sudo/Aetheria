package io.hamlook.aetheria.features.video;

import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL12;

import java.nio.ByteBuffer;

/**
 * Owns a single GL texture that mirrors the latest decoded video frame.
 * Call {@link #update()} once per rendered frame (e.g. from GuiVideoOverlay's
 * drawScreen) before binding/drawing it.
 * <p>
 * VLC's RV32 buffer format is byte-order B,G,R,A per pixel, which matches
 * GL_BGRA exactly — no per-pixel channel swapping needed on upload.
 */
public class VideoFrameTexture {

    private int textureId = -1;
    private int texWidth = -1;
    private int texHeight = -1;
    private ByteBuffer scratch;

    /** @return true if the bound texture changed this call (new frame or (re)allocated). */
    public boolean update() {
        return VideoPlayer.get().pollFrame(this::upload);
    }

    private void upload(byte[] bgra, int width, int height) {
        if (textureId == -1) {
            textureId = GL11.glGenTextures();
        }
        GL11.glBindTexture(GL11.GL_TEXTURE_2D, textureId);
        GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, GL11.GL_LINEAR);
        GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, GL11.GL_LINEAR);

        if (scratch == null || scratch.capacity() < bgra.length) {
            scratch = BufferUtils.createByteBuffer(bgra.length);
        }
        scratch.clear();
        scratch.put(bgra);
        scratch.flip();

        if (width != texWidth || height != texHeight) {
            // Size changed (or first frame) — full (re)allocation.
            GL11.glTexImage2D(GL11.GL_TEXTURE_2D, 0, GL11.GL_RGBA8, width, height, 0,
                    GL12.GL_BGRA, GL11.GL_UNSIGNED_BYTE, scratch);
            texWidth = width;
            texHeight = height;
        } else {
            // Same size — cheaper partial update.
            GL11.glTexSubImage2D(GL11.GL_TEXTURE_2D, 0, 0, 0, width, height,
                    GL12.GL_BGRA, GL11.GL_UNSIGNED_BYTE, scratch);
        }
    }

    public int getTextureId() {
        return textureId;
    }

    public int getWidth() {
        return texWidth;
    }

    public int getHeight() {
        return texHeight;
    }

    public boolean hasFrame() {
        return textureId != -1 && texWidth > 0;
    }

    public void delete() {
        if (textureId != -1) {
            GL11.glDeleteTextures(textureId);
            textureId = -1;
            texWidth = -1;
            texHeight = -1;
        }
    }
}
