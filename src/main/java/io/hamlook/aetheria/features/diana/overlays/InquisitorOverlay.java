package io.hamlook.aetheria.features.diana.overlays;

import io.hamlook.aetheria.core.ATHRConfig;
import io.hamlook.aetheria.core.config.editors.ChromaColour;
import io.hamlook.aetheria.utils.Position;
import io.hamlook.aetheria.features.diana.DianaStats;
import io.hamlook.aetheria.features.diana.LootshareDetect;
import io.hamlook.aetheria.init.RegisterEvents;
import io.hamlook.aetheria.utils.overlay.Overlay;
import lombok.Getter;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@RegisterEvents
public class InquisitorOverlay extends Overlay {
    @Getter
    private static InquisitorOverlay instance;

    public InquisitorOverlay() {
        super(160, LINE_HEIGHT + PADDING * 2);
        instance = this;
    }

    @Override
    protected int getBaseWidth() {
        return 160;
    }

    @Override
    public Position getPosition() {
        return ATHRConfig.feature.diana.inquisitorHp.inqHealthPos;
    }

    @Override
    public float getScale() {
        return ATHRConfig.feature.diana.inquisitorHp.inqScale;
    }

    @Override
    public int getBgColor() {
        return ChromaColour.specialToChromaRGB(ATHRConfig.feature.diana.inquisitorHp.inqBgColor);
    }

    @Override
    public int getCornerRadius() {
        return ATHRConfig.feature.diana.inquisitorHp.inqCornerRadius;
    }

    @Override
    protected boolean extraGuard() {
        return DianaStats.getInstance().isTracking();
    }

    @Override
    protected boolean isEnabled() {
        return ATHRConfig.feature.diana.enabled && ATHRConfig.feature.diana.inquisitorHp.showInqHealthOverlay;
    }

    @Override
    public List<String> getLines(boolean preview) {
        if (preview) return Collections.singletonList("§dMinos Inquisitor §c1,200,000§f/§a2,000,000HP");

        String raw = LootshareDetect.getClosestInqName();
        if (raw == null) return new ArrayList<>();
        return Collections.singletonList(raw);
    }
}