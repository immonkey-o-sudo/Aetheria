package io.hamlook.aetheria.features.diana.overlays;

import io.hamlook.aetheria.core.ATHRConfig;
import io.hamlook.aetheria.core.config.editors.ChromaColour;
import io.hamlook.aetheria.utils.Position;
import io.hamlook.aetheria.features.diana.DianaData;
import io.hamlook.aetheria.features.diana.DianaStats;
import io.hamlook.aetheria.init.RegisterEvents;
import io.hamlook.aetheria.utils.overlay.Overlay;
import lombok.Getter;

import java.util.ArrayList;
import java.util.List;

@RegisterEvents
public class DianaLootOverlay extends Overlay {

    @Getter
    private static DianaLootOverlay instance;

    public DianaLootOverlay() {
        super(180, LINE_HEIGHT * 9 + PADDING * 2);
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

        if (preview) {
            lines.add("§6§lDiana Loot");
            lines.add("§7Inqs since Chimera: §f4  §7[§bLS §f3§7]");
            lines.add("§dChimeras: §f1");
            lines.add("§1Feathers: §f5");
            lines.add("§2Shelmets: §f2  §5Remedies: §f1  §5Plushies: §f0");
            lines.add("§6Daedalus Sticks: §f2  §7(since last: §f12§7)");
            lines.add("§5Minos Relics: §f1  §7(since last: §f30§7)");
            lines.add("§5Souvenirs: §f2  §6Crowns: §f1");
            lines.add("§6Coins: §f1.2M");
            return lines;
        }

        DianaStats stats = DianaStats.getInstance();
        if (!stats.isTracking()) return lines;

        DianaData d = stats.getData();

        lines.add("§6§lDiana Loot");
        lines.add(String.format("§7Inqs since Chimera: §f%d%s", d.inqsSinceChimera, d.getLootsharedSuffix()));
        lines.add(String.format("§dChimeras: §f%d", d.totalChimeras));
        lines.add(String.format("§1Feathers: §f%d", d.griffinFeathers));
        lines.add(String.format("§2Shelmets: §f%d  §5Remedies: §f%d  §5Plushies: §f%d", d.dwarfTurtleShelmets, d.antiqueRemedies, d.crochetTigerPlushies));
        lines.add(String.format("§6Daedalus Sticks: §f%d  §7(since last: §f%d§7)", d.totalSticks, d.minotaursSinceStick));
        lines.add(String.format("§5Minos Relics: §f%d  §7(since last: §f%d§7)", d.totalRelics, d.champsSinceRelic));
        lines.add(String.format("§5Souvenirs: §f%d  §6Crowns: §f%d", d.souvenirs, d.crownsOfGreed));
        lines.add(String.format("§6Coins: §f%s", DianaStats.fmtCoins(d.totalCoins)));

        return lines;
    }
}