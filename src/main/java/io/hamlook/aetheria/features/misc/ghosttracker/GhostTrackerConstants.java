package io.hamlook.aetheria.features.misc.ghosttracker;

import java.util.regex.Pattern;

public class GhostTrackerConstants {
    public static final Pattern RARE_DROP_PATTERN = Pattern.compile("RARE DROP!.*?§9(?<drop>[A-Za-z ]+).*?\\+(?<mf>\\d+)% Magic Find");
    public static final String COIN_DROP_MESSAGE = "§r§eThe ghost's death materialized §r§61,000,000 coins §r§efrom the mists!§r";
    public static final Pattern COMBAT_XP_PATTERN = Pattern.compile("\\+(?<gained>[\\d.]+) Combat \\((?<progress>.+)\\)");

    public static final long AUTOSAVE_INTERVAL = 300_000L;
    public static final int MIN_SCAVENGER_GAIN = 6;
    public static final int MAX_SCAVENGER_GAIN = 1000;
}
