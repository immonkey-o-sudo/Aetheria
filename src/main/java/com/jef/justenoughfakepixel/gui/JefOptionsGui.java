package com.jef.justenoughfakepixel.gui;

import com.jef.justenoughfakepixel.JefMod;
import com.jef.justenoughfakepixel.core.JefConfig;
import com.jef.justenoughfakepixel.features.capes.ui.CapeSelectorGUI;
import com.jef.justenoughfakepixel.repo.JefRepo;
import com.jef.justenoughfakepixel.repo.RepoHandler;
import com.jef.justenoughfakepixel.repo.data.UpdateData;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.renderer.GlStateManager;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL14;

import java.awt.*;
import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class JefOptionsGui extends GuiScreen {

    private static final int BTN_CONFIG = 0;
    private static final int BTN_WAYPOINTS = 1;
    private static final int BTN_CAPES = 2;
    private static final int BTN_DISCORD = 3;
    private static final int BTN_GITHUB = 4;
    private static final int BTN_MODRINTH = 5;

    private static final String TITLE = "JustEnoughFakepixel";

    private static final Random RNG = new Random();

    private static final int PARTICLE_COUNT = 55;

    private final List<Particle> particles = new ArrayList<>();

    private float globalTime = 0f;
    private float openProgress = 0f;
    private float splashBounce = 0f;

    private String updateVersion = null;

    private float updateButtonX = -1;
    private float updateButtonY = -1;
    private float updateButtonW = -1;
    private float updateButtonH = -1;

    private static class Particle {

        float x;
        float y;

        float vx;
        float vy;

        float life;
        float lifeSpeed;

        float hue;

        float size;

        float twinkle;
        float twinkleSpeed;

        Particle(int w, int h) {
            scatter(w, h);
            life = RNG.nextFloat();
        }

        void scatter(int w, int h) {

            x = RNG.nextFloat() * w;
            y = RNG.nextFloat() * h;

            double angle = RNG.nextDouble() * Math.PI * 2;

            float speed = 0.05f + RNG.nextFloat() * 0.25f;

            vx = (float)(Math.cos(angle) * speed);
            vy = (float)(Math.sin(angle) * speed) - 0.08f;

            life = 0f;

            lifeSpeed = 0.0015f + RNG.nextFloat() * 0.003f;

            hue = 0.48f + RNG.nextFloat() * 0.22f;

            size = 0.8f + RNG.nextFloat() * 2.8f;

            twinkle = RNG.nextFloat() * (float)(Math.PI * 2);

            twinkleSpeed = 0.03f + RNG.nextFloat() * 0.05f;
        }

        void tick(int w, int h) {

            x += vx;
            y += vy;

            life += lifeSpeed;

            twinkle =
                    (twinkle + twinkleSpeed)
                            % (float)(Math.PI * 2);

            if (life >= 1f
                    || x < -30
                    || x > w + 30
                    || y < -30
                    || y > h + 30) {

                scatter(w, h);
            }
        }

        float alpha() {

            if (life < 0.12f) {
                return life / 0.12f;
            }

            if (life > 0.80f) {
                return 1f - (life - 0.80f) / 0.20f;
            }

            return 1f;
        }

        float currentSize() {

            return size
                    * (0.88f + 0.12f * (float)Math.sin(twinkle));
        }
    }

    @Override
    public void initGui() {

        super.initGui();

        openProgress = 0f;

        particles.clear();

        for (int i = 0; i < PARTICLE_COUNT; i++) {
            particles.add(new Particle(width, height));
        }

        UpdateData upd = RepoHandler.get(
                JefRepo.KEY_UPDATE,
                UpdateData.class,
                new UpdateData()
        );

        if (isNewer(JefMod.VERSION, upd.version)) {
            updateVersion = upd.version;
        }

        int cx = width / 2;

        int btnW = 180;
        int btnH = 22;
        int gap = 6;

        int startY = height / 2 + 10;

        buttonList.add(new GuiButton(
                BTN_CONFIG,
                cx - btnW / 2,
                startY,
                btnW,
                btnH,
                "\u2699  Config"
        ));

        buttonList.add(new GuiButton(
                BTN_WAYPOINTS,
                cx - btnW / 2,
                startY + (btnH + gap),
                btnW,
                btnH,
                "\u2726  Waypoints"
        ));

        buttonList.add(new GuiButton(
                BTN_CAPES,
                cx - btnW / 2,
                startY + (btnH + gap) * 2,
                btnW,
                btnH,
                "\u2728  Cape Selector"
        ));

        int sw = 88;

        buttonList.add(new GuiButton(
                BTN_DISCORD,
                width - sw * 3 - 12,
                height - btnH - 6,
                sw,
                btnH,
                "Discord"
        ));

        buttonList.add(new GuiButton(
                BTN_GITHUB,
                width - sw * 2 - 8,
                height - btnH - 6,
                sw,
                btnH,
                "GitHub"
        ));

        buttonList.add(new GuiButton(
                BTN_MODRINTH,
                width - sw - 4,
                height - btnH - 6,
                sw,
                btnH,
                "Modrinth"
        ));
    }

    @Override
    public void drawScreen(
            int mouseX,
            int mouseY,
            float partialTicks
    ) {

        globalTime += 0.018f;

        splashBounce =
                (splashBounce + 0.04f)
                        % (float)(Math.PI * 2);

        openProgress =
                Math.min(1f, openProgress + 0.045f);

        drawDefaultBackground();

        drawRect(
                0,
                0,
                width,
                height,
                0x90000000
        );

        GlStateManager.disableTexture2D();

        GlStateManager.enableBlend();

        GlStateManager.disableDepth();

        GL14.glBlendFuncSeparate(
                GL11.GL_SRC_ALPHA,
                GL11.GL_ONE,
                GL11.GL_ONE,
                GL11.GL_ONE
        );

        drawSoftBloom(
                width / 2f,
                height / 2f,
                Math.min(width, height) * 0.55f,
                0.04f,
                0.56f
        );

        GL11.glBlendFunc(
                GL11.GL_SRC_ALPHA,
                GL11.GL_ONE
        );

        for (Particle p : particles) {

            p.tick(width, height);

            Color col = Color.getHSBColor(
                    p.hue,
                    0.70f,
                    1f
            );

            float alpha =
                    p.alpha()
                            * openProgress
                            * 0.65f;

            GL11.glColor4f(
                    col.getRed() / 255f,
                    col.getGreen() / 255f,
                    col.getBlue() / 255f,
                    alpha
            );

            drawDot(
                    p.x,
                    p.y,
                    p.currentSize()
            );
        }

        GlStateManager.enableTexture2D();

        GlStateManager.enableBlend();

        GlStateManager.enableDepth();

        GL14.glBlendFuncSeparate(
                GL11.GL_SRC_ALPHA,
                GL11.GL_ONE_MINUS_SRC_ALPHA,
                GL11.GL_ONE,
                GL11.GL_ZERO
        );

        GlStateManager.color(
                1f,
                1f,
                1f,
                1f
        );

        float scale =
                Math.max(
                        1.8f,
                        Math.min(2.8f, width / 155f)
                );

        float scaledW =
                fontRendererObj.getStringWidth(TITLE)
                        * scale;

        float titleX =
                (width - scaledW) / 2f;

        float titleY =
                height * 0.22f;

        GlStateManager.pushMatrix();

        GlStateManager.translate(
                titleX,
                titleY,
                0f
        );

        GlStateManager.scale(
                scale,
                scale,
                1f
        );

        int curX = 0;

        int len = TITLE.length();

        for (int i = 0; i < len; i++) {

            float t =
                    (float)i
                            / Math.max(1, len - 1);

            float hue =
                    0.53f + t * 0.22f;

            float wave =
                    globalTime * 1.4f
                            - t * 3.5f;

            float shimmer =
                    (float)(Math.sin(wave) * 0.5f + 0.5f);

            float saturation =
                    0.75f - shimmer * 0.20f;

            float brightness =
                    0.85f + shimmer * 0.15f;

            Color col = Color.getHSBColor(
                    hue,
                    saturation,
                    brightness
            );

            int argb =
                    (0xFF << 24)
                            | (col.getRed() << 16)
                            | (col.getGreen() << 8)
                            | col.getBlue();

            float bob =
                    (float)(
                            Math.sin(globalTime * 0.9f + i * 0.35f)
                                    * 0.6f
                    );

            GlStateManager.pushMatrix();

            GlStateManager.translate(
                    0f,
                    bob,
                    0f
            );

            String ch =
                    String.valueOf(TITLE.charAt(i));

            fontRendererObj.drawStringWithShadow(
                    ch,
                    curX,
                    0,
                    argb
            );

            GlStateManager.popMatrix();

            curX += fontRendererObj.getStringWidth(ch);
        }

        GlStateManager.popMatrix();

        String ver = "v" + JefMod.VERSION;

        float verX =
                (width - fontRendererObj.getStringWidth(ver))
                        / 2f;

        float verY =
                titleY
                        + fontRendererObj.FONT_HEIGHT * scale
                        + 5f;

        fontRendererObj.drawStringWithShadow(
                ver,
                verX,
                verY,
                blendColor(0xFF4A9EA8, openProgress)
        );

        if (updateVersion != null) {

            float pulse =
                    0.75f
                            + (float)Math.sin(globalTime * 3f) * 0.15f;

            String updateText =
                    "Update Version";

            int textWidth =
                    fontRendererObj.getStringWidth(updateText);

            float padX = 8f;
            float padY = 4f;

            updateButtonW =
                    textWidth + padX * 2f;

            updateButtonH =
                    fontRendererObj.FONT_HEIGHT + padY * 2f;

            updateButtonX =
                    (width - updateButtonW) / 2f;

            updateButtonY =
                    verY + 14;

            GlStateManager.disableTexture2D();

            GL11.glBlendFunc(
                    GL11.GL_SRC_ALPHA,
                    GL11.GL_ONE
            );

            GL11.glColor4f(
                    0.20f * pulse,
                    0.55f * pulse,
                    1.45f * pulse,
                    0.22f
            );

            GL11.glBegin(GL11.GL_QUADS);

            GL11.glVertex2f(updateButtonX, updateButtonY);
            GL11.glVertex2f(updateButtonX + updateButtonW, updateButtonY);
            GL11.glVertex2f(updateButtonX + updateButtonW, updateButtonY + updateButtonH);
            GL11.glVertex2f(updateButtonX, updateButtonY + updateButtonH);

            GL11.glEnd();

            GlStateManager.enableTexture2D();

            int glow =
                    new Color(
                            0.45f * pulse,
                            0.92f * pulse,
                            1f,
                            1f
                    ).getRGB();

            fontRendererObj.drawStringWithShadow(
                    updateText,
                    updateButtonX + padX,
                    updateButtonY + padY,
                    glow
            );
        }

        if (updateVersion != null) {

            String splash =
                    "\u2726" + updateVersion + " available";

            float splashScale =
                    scale * 0.52f;

            float bounce =
                    1f
                            - (float)(
                            Math.abs(Math.sin(splashBounce))
                                    * 0.06f
                    );

            float anchorX =
                    titleX + scaledW + 10f;

            float anchorY =
                    titleY
                            + (fontRendererObj.FONT_HEIGHT
                            * scale
                            * 0.3f) + 15f;

            GlStateManager.pushMatrix();

            GlStateManager.translate(
                    anchorX,
                    anchorY,
                    0f
            );

            GlStateManager.rotate(
                    -12f,
                    0f,
                    0f,
                    1f
            );

            GlStateManager.scale(
                    splashScale * bounce,
                    splashScale * bounce,
                    1f
            );

            fontRendererObj.drawStringWithShadow(
                    splash,
                    -fontRendererObj.getStringWidth(splash) / 2f,
                    -fontRendererObj.FONT_HEIGHT / 2f,
                    0xFF00FFB3
            );

            GlStateManager.popMatrix();
        }

        for (GuiButton button : buttonList) {

            if (button.id == BTN_CONFIG
                    || button.id == BTN_WAYPOINTS
                    || button.id == BTN_CAPES) {

                GL11.glBlendFunc(
                        GL11.GL_SRC_ALPHA,
                        GL11.GL_ONE
                );

                float pulse =
                        0.72f
                                + (float)Math.sin(globalTime * 2.2f) * 0.08f;

                GL11.glColor4f(
                        0.45f * pulse,
                        0.72f * pulse,
                        1.45f * pulse,
                        0.78f
                );
            }

            else {

                GL11.glBlendFunc(
                        GL11.GL_SRC_ALPHA,
                        GL11.GL_ONE_MINUS_SRC_ALPHA
                );

                GL11.glColor4f(
                        1f,
                        1f,
                        1f,
                        1f
                );
            }

            button.drawButton(
                    mc,
                    mouseX,
                    mouseY
            );
        }

        GL11.glBlendFunc(
                GL11.GL_SRC_ALPHA,
                GL11.GL_ONE_MINUS_SRC_ALPHA
        );

        GlStateManager.color(
                1f,
                1f,
                1f,
                1f
        );
    }

    @Override
    protected void mouseClicked(
            int mouseX,
            int mouseY,
            int mouseButton
    ) throws IOException {

        super.mouseClicked(
                mouseX,
                mouseY,
                mouseButton
        );

        if (updateVersion != null) {

            if (mouseX >= updateButtonX
                    && mouseX <= updateButtonX + updateButtonW
                    && mouseY >= updateButtonY
                    && mouseY <= updateButtonY + updateButtonH) {

                tryBrowse(
                        "https://modrinth.com/mod/justenoughfakepixel"
                );
            }
        }
    }

    @Override
    protected void actionPerformed(GuiButton button)
            throws IOException {

        switch (button.id) {

            case BTN_CONFIG:
                JefConfig.openGui();
                break;

            case BTN_WAYPOINTS:
                JefConfig.openWaypointGroupGui();
                break;

            case BTN_CAPES:
                JefConfig.screenToOpen =
                        new CapeSelectorGUI();
                break;

            case BTN_DISCORD:
                tryBrowse(
                        "https://discord.gg/HHf5yqSy9R"
                );
                break;

            case BTN_GITHUB:
                tryBrowse(
                        "https://github.com/JustEnoughFakepixel/JustEnoughFakepixel"
                );
                break;

            case BTN_MODRINTH:
                tryBrowse(
                        "https://modrinth.com/mod/justenoughfakepixel"
                );
                break;
        }
    }

    private void drawDot(
            float cx,
            float cy,
            float r
    ) {

        GL11.glBegin(GL11.GL_TRIANGLE_FAN);

        GL11.glVertex2f(cx, cy);

        int segs =
                Math.max(8, (int)(r * 4));

        for (int i = 0; i <= segs; i++) {

            double a =
                    i * Math.PI * 2 / segs;

            GL11.glVertex2f(
                    cx + (float)(Math.cos(a) * r),
                    cy + (float)(Math.sin(a) * r)
            );
        }

        GL11.glEnd();
    }

    private void drawSoftBloom(
            float cx,
            float cy,
            float r,
            float peakAlpha,
            float hueHint
    ) {

        Color inner =
                Color.getHSBColor(
                        hueHint,
                        0.40f,
                        0.20f
                );

        int segs = 48;

        GL11.glBegin(GL11.GL_TRIANGLE_FAN);

        GL11.glColor4f(
                inner.getRed() / 255f,
                inner.getGreen() / 255f,
                inner.getBlue() / 255f,
                peakAlpha
        );

        GL11.glVertex2f(cx, cy);

        GL11.glColor4f(0f, 0f, 0f, 0f);

        for (int i = 0; i <= segs; i++) {

            double a =
                    i * Math.PI * 2 / segs;

            GL11.glVertex2f(
                    cx + (float)(Math.cos(a) * r),
                    cy + (float)(Math.sin(a) * r)
            );
        }

        GL11.glEnd();
    }

    private int blendColor(int argb, float alphaMul) {

        int a =
                (int)(((argb >> 24) & 0xFF) * alphaMul);

        return (argb & 0x00FFFFFF) | (a << 24);
    }

    private static boolean isNewer(
            String current,
            String latest
    ) {

        if (latest == null) {
            return false;
        }

        String[] c =
                current
                        .replaceAll("[^0-9.]", "")
                        .split("\\.");

        String[] l =
                latest
                        .replaceAll("[^0-9.]", "")
                        .split("\\.");

        int len =
                Math.max(c.length, l.length);

        for (int i = 0; i < len; i++) {

            int cv =
                    i < c.length
                            ? parseSafe(c[i])
                            : 0;

            int lv =
                    i < l.length
                            ? parseSafe(l[i])
                            : 0;

            if (lv > cv) return true;

            if (lv < cv) return false;
        }

        return false;
    }

    private static int parseSafe(String s) {

        try {
            return Integer.parseInt(s);
        }
        catch (Exception e) {
            return 0;
        }
    }

    private void tryBrowse(String url) {

        try {
            Desktop.getDesktop().browse(new URI(url));
        }
        catch (Exception ignored) {}
    }

    @Override
    public boolean doesGuiPauseGame() {
        return false;
    }

    @Override
    public void onGuiClosed() {
        particles.clear();
    }
}