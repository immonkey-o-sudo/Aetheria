package io.hamlook.aetheria.features.diana.overlays;

import io.hamlook.aetheria.core.ATHRConfig;
import io.hamlook.aetheria.core.moulconfig.editors.ChromaColour;
import io.hamlook.aetheria.features.diana.DianaData;
import io.hamlook.aetheria.features.diana.DianaStats;
import io.hamlook.aetheria.init.RegisterEvents;
import io.hamlook.aetheria.utils.Position;
import io.hamlook.aetheria.utils.Utils;
import io.hamlook.aetheria.utils.overlay.Overlay;
import lombok.Getter;

import java.util.ArrayList;
import java.util.List;

@RegisterEvents
public class DianaLootOverlay extends Overlay {

    @Getter
    private static DianaLootOverlay instance;

    public DianaLootOverlay() {
        super(180, LINE_HEIGHT * 10 + PADDING * 2);
        instance = this;
    }

    @Override
    protected int getBaseWidth() {
        return 180;
    }

    @Override
    public Position getPosition() {
        return ATHRConfig.feature.diana.lootOverlay.lootOverlayPos;
    }

    @Override
    public float getScale() {
        return ATHRConfig.feature.diana.lootOverlay.lootScale;
    }

    @Override
    public int getBgColor() {
        return ChromaColour.specialToChromaRGB(ATHRConfig.feature.diana.lootOverlay.lootBgColor);
    }

    @Override
    public int getCornerRadius() {
        return ATHRConfig.feature.diana.lootOverlay.lootCornerRadius;
    }

    @Override
    protected boolean isEnabled() {
        return ATHRConfig.feature.diana.enabled && ATHRConfig.feature.diana.lootOverlay.showLootOverlay;
    }

    @Override
    public List<String> getLines(boolean preview) {
        List<String> lines = new ArrayList<>();

        List<String> contentLines;

        if (preview) {
            lines.add("§6§lDiana Loot");
            contentLines = new ArrayList<>();
            contentLines.add("§7Inqs since Chimera: §f4  §7[§bLS §f3§7]");
            contentLines.add("§dChimeras: §f1");
            contentLines.add("§1Feathers: §f5");
            contentLines.add("§2Shelmets: §f2  §5Remedies: §f1  §5Plushies: §f0");
            contentLines.add("§6Daedalus Sticks: §f2  §7(since last: §f12§7)");
            contentLines.add("§5Minos Relics: §f1  §7(since last: §f30§7)");
            contentLines.add("§5Souvenirs: §f2  §6Crowns: §f1");
            contentLines.add("§6Coins: §f1.2M");
            contentLines.add("§aEstimated Profit: §f2.3M");
        } else {
            DianaStats stats = DianaStats.getInstance();
            if (!stats.isTracking()) return lines;

            lines.add("§6§lDiana Loot");
            DianaData d = stats.getData();
            contentLines = new ArrayList<>();
            contentLines.add(String.format("§7Inqs since Chimera: §f%d%s", d.inqsSinceChimera, d.getLootsharedSuffix()));
            contentLines.add(String.format("§dChimeras: §f%d", d.totalChimeras));
            contentLines.add(String.format("§1Feathers: §f%d", d.griffinFeathers));
            contentLines.add(String.format("§2Shelmets: §f%d  §5Remedies: §f%d  §5Plushies: §f%d", d.dwarfTurtleShelmets, d.antiqueRemedies, d.crochetTigerPlushies));
            contentLines.add(String.format("§6Daedalus Sticks: §f%d  §7(since last: §f%d§7)", d.totalSticks, d.minotaursSinceStick));
            contentLines.add(String.format("§5Minos Relics: §f%d  §7(since last: §f%d§7)", d.totalRelics, d.champsSinceRelic));
            contentLines.add(String.format("§5Souvenirs: §f%d  §6Crowns: §f%d", d.souvenirs, d.crownsOfGreed));
            contentLines.add(String.format("§6Coins: §f%s", Utils.shortNumberFormat(d.totalCoins, 0)));
            contentLines.add(String.format("§aEstimated Profit: §f%s", Utils.shortNumberFormat(stats.getEstimatedProfit(), 0)));
        }

        for (int idx : ATHRConfig.feature.diana.lootOverlay.lootLines) {
            if (idx >= 0 && idx < contentLines.size()) {
                lines.add(contentLines.get(idx));
            }
        }

        return lines;
    }
}