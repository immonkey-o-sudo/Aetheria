package io.hamlook.aetheria.features.dungeons.utils;

import io.hamlook.aetheria.core.ATHRConfig;
import io.hamlook.aetheria.utils.chat.ChatUtils;
import io.hamlook.aetheria.utils.time.TimeFormatter;
import net.minecraft.util.EnumChatFormatting;

public class PBTracker {

    private static final String C_VAL = EnumChatFormatting.WHITE.toString();
    private static final String C_PB = EnumChatFormatting.DARK_GRAY.toString();
    private static final String C_NEWPB = EnumChatFormatting.LIGHT_PURPLE.toString();

    public void checkAndSaveRunPb(DungeonTimers timers) {
        if (ATHRConfig.feature == null || timers.getCurrentFloor() == DungeonFloor.NONE || timers.getBossDeadTime() == 0) return;
        savePbIfBetter(timers.getCurrentFloor().name() + "_boss", timers.getBossDeadTime() - timers.getBossTime(), null);
        savePbIfBetter(timers.getCurrentFloor().name() + "_total", timers.getBossDeadTime(), "Total");
    }

    public void checkPhasePb(String key, long duration, String phaseName) {
        if (ATHRConfig.feature == null || duration <= 0) return;
        long prev = ATHRConfig.feature.dungeons.getPb(key);
        if (prev == 0 || duration < prev) {
            ATHRConfig.feature.dungeons.setPb(key, duration);
            ATHRConfig.saveConfig();
            if (phaseName != null) announceNewPb(phaseName, duration);
        }
    }

    private void savePbIfBetter(String key, long duration, String phaseName) {
        checkPhasePb(key, duration, phaseName);
    }

    private void announceNewPb(String phase, long duration) {
        if (ATHRConfig.feature == null || !ATHRConfig.feature.dungeons.dungeonOverlay.dungeonStats) return;
        String msg = C_NEWPB + "NEW PB " + phase + ": " + C_VAL + TimeFormatter.formatDungeonTime(duration);
        ChatUtils.sendMessage(msg);
        ChatUtils.sendPartyMessage("NEW PB " + phase + ": " + TimeFormatter.formatDungeonTime(duration));
    }

    public String getPbTag(String key) {
        if (ATHRConfig.feature == null) return "";
        long p = ATHRConfig.feature.dungeons.getPb(key);
        return p > 0 ? C_PB + " (PB: " + TimeFormatter.formatDungeonTime(p) + ")" : "";
    }

    public static String getFormattedPb(String arg1, String arg2) {
        if (ATHRConfig.feature == null) return "No data";

        DungeonFloor floor = DungeonFloor.fromString(arg1.toUpperCase());
        if (floor == DungeonFloor.NONE) return "Unknown floor: " + arg1;

        if (arg2 == null) {
            long pb = ATHRConfig.feature.dungeons.getPb(floor.name() + "_total");
            return pb > 0 ? floor.name() + " PB: " + TimeFormatter.formatDungeonTime(pb) : floor.name() + ": No PB";
        }

        if (arg2.equalsIgnoreCase("br")) {
            long pb = ATHRConfig.feature.dungeons.getPb(floor.name() + "_blood");
            return pb > 0 ? floor.name() + " blood rush PB: " + TimeFormatter.formatDungeonTime(pb) : floor.name() + " blood rush: No PB";
        }

        if (arg2.toLowerCase().startsWith("p")) {
            String phase = arg2.toLowerCase();
            String key = floor.name() + "_" + phase;
            long pb = ATHRConfig.feature.dungeons.getPb(key);
            String label = phaseLabel(phase);
            return pb > 0 ? floor.name() + " " + label + ": " + TimeFormatter.formatDungeonTime(pb) : floor.name() + " " + label + ": No PB";
        }

        ChatUtils.sendMessage("§6[ATHR] §cInvalid argument: §f" + arg2);
        ChatUtils.sendMessage("§6[ATHR] §eUsage:");
        ChatUtils.sendMessage("§6[ATHR]   §f!pb <floor> §7- Total run time");
        ChatUtils.sendMessage("§6[ATHR]   §f!pb <floor> br §7- Blood rush time");
        ChatUtils.sendMessage("§6[ATHR]   §f!pb <floor> p1-p5 §7- Phase times");
        ChatUtils.sendMessage("§6[ATHR] §eExamples:");
        ChatUtils.sendMessage("§6[ATHR]   §f!pb f7 §7- F7 total PB");
        ChatUtils.sendMessage("§6[ATHR]   §f!pb m7 p4 §7- M7 P4 (Necron) PB");
        ChatUtils.sendMessage("§6[ATHR]   §f!pb f2 p1 §7- F2 P1 (Scarf) PB");
        return null;
    }

    private static String phaseLabel(String phase) {
        switch (phase) {
            case "p1": return "P1";
            case "p2": return "P2";
            case "p3": return "P3";
            case "p4": return "P4";
            case "p5": return "P5";
            default: return phase.toUpperCase();
        }
    }
}
