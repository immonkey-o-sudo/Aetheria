package io.hamlook.aetheria.features.diana.overlays;

import io.hamlook.aetheria.core.ATHRConfig;
import io.hamlook.aetheria.core.moulconfig.editors.ChromaColour;
import io.hamlook.aetheria.features.diana.DianaData;
import io.hamlook.aetheria.features.diana.DianaStats;
import io.hamlook.aetheria.init.RegisterEvents;
import io.hamlook.aetheria.utils.Position;
import io.hamlook.aetheria.utils.overlay.Overlay;
import lombok.Getter;

import java.util.ArrayList;
import java.util.List;

@RegisterEvents
public class DianaEventOverlay extends Overlay {

    @Getter
    private static DianaEventOverlay instance;

    public DianaEventOverlay() {
        super(180, LINE_HEIGHT * 10 + PADDING * 2);
        instance = this;
    }

    @Override
    protected int getBaseWidth() {
        return 180;
    }

    @Override
    public Position getPosition() {
        return ATHRConfig.feature.diana.eventOverlay.eventOverlayPos;
    }

    @Override
    public float getScale() {
        return ATHRConfig.feature.diana.eventOverlay.eventScale;
    }

    @Override
    public int getBgColor() {
        return ChromaColour.specialToChromaRGB(ATHRConfig.feature.diana.eventOverlay.eventBgColor);
    }

    @Override
    public int getCornerRadius() {
        return ATHRConfig.feature.diana.eventOverlay.eventCornerRadius;
    }

    @Override
    protected boolean isEnabled() {
        return ATHRConfig.feature.diana.enabled && ATHRConfig.feature.diana.eventOverlay.showEventOverlay;
    }

    @Override
    public List<String> getLines(boolean preview) {
        List<String> lines = new ArrayList<>();

        List<String> contentLines;

        if (preview) {
            lines.add("§e§lDiana Event");
            contentLines = new ArrayList<>();
            contentLines.add("§9Total Mobs: §f165");
            contentLines.add("§1Playtime: §f2h 30m  §1Session: §f45m");
            contentLines.add("§eBurrows: §f42  §7(§a120.0§7/hr)");
            contentLines.add("§dInquisitor §d4.20% §f(7) §7[§bLS §f3§7]");
            contentLines.add("§6Minotaur §d12.30% §f(45)");
            contentLines.add("§5Minos Champion §d8.10% §f(30)");
            contentLines.add("§fGaia Construct §d5.00% §f(8)");
            contentLines.add("§aMinos Hunter §d20.00% §f(33)");
            contentLines.add("§eSiamese Lynx §d10.00% §f(17)");
        } else {
            DianaStats stats = DianaStats.getInstance();
            if (!stats.isTracking()) return lines;

            lines.add("§e§lDiana Event");
            DianaData d = stats.getData();
            double bph = stats.getBph();
            contentLines = new ArrayList<>();
            contentLines.add(String.format("§9Total Mobs: §f%d", d.totalMobs));
            contentLines.add(String.format("§1Playtime: §f%s  §1Session: §f%s", DianaStats.formatTime(d.activeTimeMs), DianaStats.formatTime(stats.getSessionTimeMs())));
            contentLines.add(String.format("§eBurrows: §f%d  §7(§a%.1f§7/hr)", d.totalBorrows, bph));
            contentLines.add(String.format("§dInquisitor §d%s §f(%d)%s", stats.formatMobPct(d.totalInqs), d.totalInqs, d.getLootsharedSuffix()));
            contentLines.add(String.format("§6Minotaur §d%s §f(%d)", stats.formatMobPct(d.totalMinotaurs), d.totalMinotaurs));
            contentLines.add(String.format("§5Minos Champion §d%s §f(%d)", stats.formatMobPct(d.totalChamps), d.totalChamps));
            contentLines.add(String.format("§fGaia Construct §d%s §f(%d)", stats.formatMobPct(d.totalGaiaConstructs), d.totalGaiaConstructs));
            contentLines.add(String.format("§aMinos Hunter §d%s §f(%d)", stats.formatMobPct(d.totalMinosHunters), d.totalMinosHunters));
            contentLines.add(String.format("§eSiamese Lynx §d%s §f(%d)", stats.formatMobPct(d.totalSiameseLynxes), d.totalSiameseLynxes));
        }

        for (int idx : ATHRConfig.feature.diana.eventOverlay.eventLines) {
            if (idx >= 0 && idx < contentLines.size()) {
                lines.add(contentLines.get(idx));
            }
        }

        return lines;
    }
}