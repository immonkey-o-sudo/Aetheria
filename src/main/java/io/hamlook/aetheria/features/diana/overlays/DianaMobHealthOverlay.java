package io.hamlook.aetheria.features.diana.overlays;

import io.hamlook.aetheria.core.ATHRConfig;
import io.hamlook.aetheria.core.moulconfig.editors.ChromaColour;
import io.hamlook.aetheria.features.diana.DianaStats;
import io.hamlook.aetheria.features.diana.LootshareDetect;
import io.hamlook.aetheria.init.RegisterEvents;
import io.hamlook.aetheria.utils.Position;
import io.hamlook.aetheria.utils.overlay.Overlay;
import lombok.Getter;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@RegisterEvents
public class DianaMobHealthOverlay extends Overlay {

    @Getter
    private static DianaMobHealthOverlay instance;

    public DianaMobHealthOverlay() {
        super(180, LINE_HEIGHT + PADDING * 2);
        instance = this;
    }

    @Override
    protected int getBaseWidth() {
        return 180;
    }

    @Override
    public Position getPosition() {
        return ATHRConfig.feature.diana.dianaMobHp.dianaMobHealthPos;
    }

    @Override
    public float getScale() {
        return ATHRConfig.feature.diana.dianaMobHp.mobScale;
    }

    @Override
    public int getBgColor() {
        return ChromaColour.specialToChromaRGB(ATHRConfig.feature.diana.dianaMobHp.mobBgColor);
    }

    @Override
    public int getCornerRadius() {
        return ATHRConfig.feature.diana.dianaMobHp.mobCornerRadius;
    }

    @Override
    protected boolean extraGuard() {
        DianaStats s = DianaStats.getInstance();
        return s.isTracking() && s.isDianaMayor();
    }

    @Override
    protected boolean isEnabled() {
        return ATHRConfig.feature.diana.enabled && ATHRConfig.feature.diana.dianaMobHp.showDianaMobHealthOverlay;
    }

    @Override
    public List<String> getLines(boolean preview) {
        if (preview) return Collections.singletonList("§2[Lv260] ✤✿ Gaia Construct 839.6k§f/§a1.5M§c❤");

        String raw = LootshareDetect.getClosestNonInqMobName();
        if (raw == null) return new ArrayList<>();
        return Collections.singletonList(raw);
    }
}