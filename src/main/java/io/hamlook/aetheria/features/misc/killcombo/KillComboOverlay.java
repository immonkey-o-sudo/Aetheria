package io.hamlook.aetheria.features.misc.killcombo;

import io.hamlook.aetheria.core.ATHRConfig;
import io.hamlook.aetheria.core.moulconfig.editors.ChromaColour;
import io.hamlook.aetheria.init.RegisterEvents;
import io.hamlook.aetheria.utils.Position;
import io.hamlook.aetheria.utils.data.SkyblockData;
import io.hamlook.aetheria.utils.overlay.Overlay;
import lombok.Getter;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@RegisterEvents
public class KillComboOverlay extends Overlay {

    @Getter
    private static KillComboOverlay instance;

    public KillComboOverlay() {
        super(180, LINE_HEIGHT * 6 + PADDING * 2);
        instance = this;
    }

    private static String formatStatLine(String statType, int value, int currentCombo) {
        String line;
        int finalMilestone;
        switch (statType) {
            case "✯ Magic Find":
                line = "§b+" + value + "% §b✯ Magic Find";
                finalMilestone = 25;
                break;
            case "coins per kill":
                line = "§8+§6" + value + "§7 coins per kill";
                finalMilestone = 30;
                break;
            case "☯ Combat Wisdom":
                line = "§3+" + value + "§3☯ Combat Wisdom";
                finalMilestone = 20;
                break;
            default:
                line = "§f+" + value + " §f" + statType;
                finalMilestone = Integer.MAX_VALUE;
        }
        if (currentCombo >= finalMilestone) line += " §cMAX";
        return line;
    }

    @Override
    protected int getBaseWidth() {
        return 180;
    }

    @Override
    public Position getPosition() {
        return ATHRConfig.feature.misc.killCombo.killComboPos;
    }

    @Override
    public float getScale() {
        return ATHRConfig.feature.misc.killCombo.scale;
    }

    @Override
    public int getBgColor() {
        return ChromaColour.specialToChromaRGB(ATHRConfig.feature.misc.killCombo.bgColor);
    }

    @Override
    public int getCornerRadius() {
        return ATHRConfig.feature.misc.killCombo.cornerRadius;
    }

    @Override
    protected boolean isEnabled() {
        return ATHRConfig.feature.misc.killCombo.enabled && SkyblockData.isOnSkyblock();
    }

    @Override
    public List<String> getLines(boolean preview) {
        List<String> lines = new ArrayList<>();
        lines.add("§6§lKill Combo Tracker");

        KillComboTracker tracker = KillComboTracker.getInstance();
        List<String> contentLines = new ArrayList<>();

        if (preview) {
            contentLines.add("§7Current: §a+15");
            contentLines.add("§7Highest: §e30");
            contentLines.add("§b+3% §b✯ Magic Find");
            contentLines.add("§8+§610§7 coins per kill");
            contentLines.add("§3+15§3☯ Combat Wisdom §cMAX");
        } else {
            if (tracker.isActive()) {
                int combo = tracker.getCurrentCombo();
                String color = KillComboTracker.getComboColor(combo);
                contentLines.add("§7Current: " + color + "+" + combo);
            } else if (tracker.getHighestCombo() > 0) {
                contentLines.add("§7Current: §8(expired)");
            } else {
                return new ArrayList<>();
            }

            contentLines.add("§7Highest: §e" + tracker.getHighestCombo());

            Map<String, KillComboTracker.ComboStat> stats = tracker.getStats();
            int currentCombo = tracker.getCurrentCombo();

            String[] statOrder = {"✯ Magic Find", "coins per kill", "☯ Combat Wisdom"};
            for (String statType : statOrder) {
                KillComboTracker.ComboStat stat = stats.get(statType);
                if (stat == null) continue;
                String line = formatStatLine(statType, stat.totalValue, currentCombo);
                if (line != null) contentLines.add(line);
            }
        }

        for (int idx : ATHRConfig.feature.misc.killCombo.killComboLines) {
            if (idx >= 0 && idx < contentLines.size()) {
                String line = contentLines.get(idx);
                if (line != null) lines.add(line);
            }
        }

        return lines;
    }
}
