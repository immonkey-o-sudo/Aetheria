package io.hamlook.aetheria.features.farming.sensitivityreducer;

import io.hamlook.aetheria.core.ATHRConfig;
import io.hamlook.aetheria.core.features.farming.SensitivityReducerConfig;
import io.hamlook.aetheria.core.moulconfig.editors.ChromaColour;
import io.hamlook.aetheria.init.RegisterEvents;
import io.hamlook.aetheria.utils.Position;
import io.hamlook.aetheria.utils.overlay.Overlay;
import lombok.Getter;
import org.lwjgl.opengl.GL11;

import java.util.ArrayList;
import java.util.List;

@RegisterEvents
public class PitchYawOverlay extends Overlay {

    @Getter
    private static PitchYawOverlay instance;

    public PitchYawOverlay() {
        super(90, 24);
        instance = this;
    }

    private static SensitivityReducerConfig config() {
        return ATHRConfig.feature.farming.sensitivityReducer;
    }

    @Override
    public Position getPosition() {
        return config().pitchYawOverlayPos;
    }

    @Override
    public float getScale() {
        return config().pitchYawOverlayScale;
    }

    @Override
    public int getBgColor() {
        return 0x64000000;
    }

    @Override
    public int getCornerRadius() {
        return 4;
    }

    @Override
    protected boolean isEnabled() {
        return ATHRConfig.feature != null && config().showPitchYawOverlay;
    }

    @Override
    protected boolean extraGuard() {
        return mc.thePlayer != null;
    }

    @Override
    public List<String> getLines(boolean preview) {
        List<String> lines = new ArrayList<>();

        if (preview) {
            lines.add("Pitch: 12.3456");
            lines.add("Yaw: -45.6789");
            return lines;
        }

        if (mc.thePlayer == null) return lines;

        lines.add(String.format("Pitch: %.4f", mc.thePlayer.rotationPitch));
        lines.add(String.format("Yaw: %.4f", mc.thePlayer.rotationYaw));
        return lines;
    }

    // Overridden (rather than relying on the base per-line white rendering) so the
    // "Pitch"/"Yaw" labels can use a full custom/chroma RGB color while the numbers
    // stay a plain, fixed color.
    @Override
    public void render(boolean preview) {
        if (!preview && !extraGuard()) return;

        List<String> lines = getLines(preview);
        if (lines.isEmpty()) return;

        int labelColor = ChromaColour.specialToChromaRGB(config().pitchYawLabelColor);

        float scale = getScale();
        int w = getBaseWidth();
        for (String line : lines) w = Math.max(w, mc.fontRendererObj.getStringWidth(line) + PADDING * 2);
        int h = lines.size() * LINE_HEIGHT + PADDING * 2;
        lastW = w;
        lastH = h;

        Position pos = getPosition();
        int x = pos.getAbsX(sr, (int) (w * scale));
        int y = pos.getAbsY(sr, (int) (h * scale));
        if (pos.isCenterX()) x -= (int) (w * scale / 2);
        if (pos.isCenterY()) y -= (int) (h * scale / 2);

        GL11.glPushMatrix();
        GL11.glTranslatef(x, y, 0);
        GL11.glScalef(scale, scale, 1f);

        int bgColor = getBgColor();
        if ((bgColor >>> 24) != 0) drawRoundedRect(-PADDING, -PADDING, w, h - PADDING, getCornerRadius(), bgColor);

        int dy = 0;
        for (String line : lines) {
            int splitIdx = line.indexOf(':') + 1;
            String label = line.substring(0, splitIdx);
            String value = line.substring(splitIdx);

            mc.fontRendererObj.drawStringWithShadow(label, 0, dy, labelColor);
            mc.fontRendererObj.drawStringWithShadow(value, mc.fontRendererObj.getStringWidth(label), dy, 0xFFFFFF);
            dy += LINE_HEIGHT;
        }

        GL11.glPopMatrix();
    }
}
