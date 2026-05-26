package io.hamlook.aetheria.features.scoreboard;

import io.hamlook.aetheria.core.ATHRConfig;
import io.hamlook.aetheria.core.config.editors.ChromaColour;
import io.hamlook.aetheria.utils.Position;
import io.hamlook.aetheria.features.mining.fetchur.FetchurData;
import io.hamlook.aetheria.init.RegisterEvents;
import io.hamlook.aetheria.utils.ColorUtils;
import io.hamlook.aetheria.utils.chat.ChatUtils;
import io.hamlook.aetheria.utils.data.SkyblockData;
import io.hamlook.aetheria.utils.data.TablistParser;
import io.hamlook.aetheria.utils.overlay.Overlay;
import io.hamlook.aetheria.utils.overlay.OverlayUtils;
import io.hamlook.aetheria.features.storage.StorageManager;
import lombok.Getter;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import net.minecraftforge.event.world.WorldEvent;
import org.lwjgl.input.Keyboard;
import org.lwjgl.opengl.GL11;

import java.util.*;
import java.util.regex.Pattern;

@RegisterEvents
public class CustomScoreboard extends Overlay {

    private static final int PAD_X = 4;
    private static final int PAD_Y = 4;
    private static final int LINE_GAP = 1;
    private static final int SUPERSAMPLE = 2;

    // How often to re-parse: every 4 client ticks (~67 ms, yes that 67 was intentional, at 20 tps)
    private static final int PARSE_INTERVAL = 4;

    // ── known line IDs (must match exampleText indices in Scoreboard.java) ───
    private static final int LINE_SERVER       = 0;
    private static final int LINE_TIME         = 1;
    private static final int LINE_PROFILE_TYPE = 2;
    private static final int LINE_SEASON       = 3;
    private static final int LINE_ISLAND       = 4;
    private static final int LINE_LOCATION     = 5;
    private static final int LINE_EMPTY1       = 6;
    private static final int LINE_PURSE        = 7;
    private static final int LINE_BANK         = 8;
    private static final int LINE_POWDER       = 9;
    private static final int LINE_HEAT         = 10;
    private static final int LINE_BITS         = 11;
    private static final int LINE_GEMS         = 12;
    private static final int LINE_NORTHSTARS   = 13;
    private static final int LINE_EVENT        = 14;
    private static final int LINE_POWER        = 15;
    private static final int LINE_COOKIE       = 16;
    private static final int LINE_EMPTY3       = 17;
    private static final int LINE_FETCHUR      = 18;
    private static final int LINE_SLAYER       = 19;
    private static final int LINE_EMPTY4       = 20;
    private static final int LINE_EMPTY5       = 21;
    private static final int LINE_EMPTY6       = 22;
    private static final int LINE_EMPTY7       = 23;
    private static final int LINE_EXTRA        = 24;
    private static final int LINE_EMPTY2       = 25;

    private static final String LOC_SYMBOL_NORMAL = "⏣";
    private static final String LOC_SYMBOL_RIFT   = "ф";

    private static final Pattern SERVER_PATTERN           = Pattern.compile("\\s*\\d{2}/\\d{2}/\\d{2}.*");
    private static final Pattern SEASON_PATTERN           = Pattern.compile("\\s*(?:(?:Late|Early) )?(?:Spring|Summer|Autumn|Winter) \\d+.*");
    private static final Pattern TIME_PATTERN             = Pattern.compile("\\s*\\d+:\\d+(?:am|pm).*");
    private static final Pattern PROFILE_TYPE_PATTERN    = Pattern.compile("Ironman|Stranded|Bingo|Classic");
    private static final Pattern PURSE_PATTERN            = Pattern.compile("(?:Piggy|Purse): [\\d,.]+");
    private static final Pattern BANK_PATTERN             = Pattern.compile("Bank: .+");
    private static final Pattern BITS_PATTERN             = Pattern.compile("Bits: [\\d,.]+");
    private static final Pattern EVENT_PATTERN            = Pattern.compile("(?:Fishing Festival|Mining Fiesta|Spooky Festival|Season of Jerry|Traveling Zoo|New Year Celebration|Election|Fallen Star|Festival of Gifts).*");
    private static final Pattern SLAYER_PATTERN           = Pattern.compile("Slayer Quest");
    private static final Pattern COOKIE_SUPPRESS_PATTERN = Pattern.compile("Cookie Buff.*|\\d+d\\s+\\d+h.*");
    private static final Pattern NORTHSTARS_PATTERN       = Pattern.compile("North Stars: [\\d,]+");
    private static final Pattern HEAT_PATTERN             = Pattern.compile("Heat: .+");

    // ── state ─────────────────────────────────────────────────────────────────

    @Getter
    private static CustomScoreboard instance;

    // Cached display lines — updated by onTick, read by render()
    private List<String> cachedLines = new ArrayList<>();

    // Cached line order — invalidated when the config list object changes
    private List<Integer> cachedLineOrder = null;
    private List<?> lastLineOrderSource   = null;

    // Dirty-check: skip re-parse when raw lines haven't changed
    private int  lastRawHash   = 0;
    private boolean wasDown    = false;
    private int  tickCounter   = 0;

    public CustomScoreboard() {
        super(130, 90);
        instance = this;
    }

    // ── helpers ───────────────────────────────────────────────────────────────

    public static boolean isActive() {
        return ATHRConfig.feature != null && ATHRConfig.feature.scoreboard.enabled;
    }

    /**
     * Returns the cached line order, recomputing only when the config list
     * reference changes (i.e. after the user edits it in the GUI).
     */
    private List<Integer> getLineOrder() {
        List<?> raw = ATHRConfig.feature.scoreboard.scoreboardLines;
        if (raw == lastLineOrderSource && cachedLineOrder != null) return cachedLineOrder;
        List<Integer> result = new ArrayList<>();
        if (raw != null)
            for (Object o : raw)
                if (o instanceof Number) result.add(((Number) o).intValue());
        lastLineOrderSource = raw;
        cachedLineOrder = result;
        return result;
    }

    private static String formatPowder(long v) {
        if (v >= 1_000_000) return String.format("%.1fM", v / 1_000_000.0);
        if (v >= 1_000)     return String.format("%.1fK", v / 1_000.0);
        return Long.toString(v);
    }

    private String toTitleCase(String s) {
        StringBuilder sb = new StringBuilder();
        for (String word : s.toLowerCase().split("_"))
            sb.append(Character.toUpperCase(word.charAt(0))).append(word.substring(1)).append(" ");
        return sb.toString().trim();
    }

    private int xFor(String line, int boxW, int alignment) {
        Minecraft mc = Minecraft.getMinecraft();
        int w = mc.fontRendererObj.getStringWidth(line);
        switch (alignment) {
            case 1: return PAD_X + (boxW - PAD_X * 2 - w) / 2;
            case 2: return boxW - PAD_X - w;
            default: return PAD_X;
        }
    }

    // ── Overlay contract ──────────────────────────────────────────────────────

    @Override public Position getPosition()   { return ATHRConfig.feature.scoreboard.position; }
    @Override public float   getScale()       { return ATHRConfig.feature.scoreboard.scale; }
    @Override public int     getBgColor()     { return ChromaColour.specialToChromaRGB(ATHRConfig.feature.scoreboard.scoreboardBg); }
    @Override public int     getCornerRadius(){ return (int) ATHRConfig.feature.scoreboard.cornerRadius; }
    @Override protected boolean extraGuard()  { return isActive(); }
    @Override protected boolean isEnabled()   {
        return isActive()
                && !Minecraft.getMinecraft().gameSettings.showDebugInfo
                && !StorageManager.isOverlayActive();
    }

    /**
     * Pure getter — returns the last lines computed by the tick handler.
     * Zero allocation, zero parsing, safe to call from render every frame.
     */
    @Override
    public List<String> getLines(boolean preview) {
        if (preview) return buildLines(true);  // preview always needs live data
        return cachedLines;
    }

    // ── tick-driven parse ─────────────────────────────────────────────────────

    /**
     * Runs every PARSE_INTERVAL client ticks. Reads raw scoreboard lines once,
     * dirty-checks against the previous hash, and if anything changed rebuilds
     * the display list and pushes it to CustomScoreboardAPI.
     */
    @SubscribeEvent
    public void onTick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;
        if (!isActive()) return;
        if ((tickCounter = (tickCounter + 1) % PARSE_INTERVAL) != 0) return;

        List<String> raw = SkyblockData.getScoreboardLines();

        // Dirty check — skip expensive rebuild if nothing changed
        int hash = raw.hashCode();
        if (hash == lastRawHash) return;
        lastRawHash = hash;

        List<String> built = buildLines(false);
        cachedLines = built;

        // Sync API (moved out of getLines so it only runs on actual changes)
        List<String> clean = new ArrayList<>(built.size());
        for (String line : built) clean.add(ColorUtils.stripColor(line));
        CustomScoreboardAPI.update(clean);
    }

    @SubscribeEvent
    public void onWorldUnload(WorldEvent.Unload event) {
        cachedLines  = new ArrayList<>();
        lastRawHash  = 0;
        tickCounter  = 0;
    }

    // ── core parse + transform ────────────────────────────────────────────────

    /**
     * Parses raw scoreboard lines and builds the ordered display list.
     * Called at most every PARSE_INTERVAL ticks (or on preview demand).
     * All heavy work lives here — completely isolated from the render path.
     */
    private List<String> buildLines(boolean preview) {
        List<String> raw = new ArrayList<>(SkyblockData.getScoreboardLines());
        if (raw.isEmpty()) return new ArrayList<>();
        Collections.reverse(raw);

        // ── pre-strip all lines once ──────────────────────────────────────────
        // Avoids repeated ColorUtils.stripColor calls inside the parse loop.
        String[] stripped = new String[raw.size()];
        for (int i = 0; i < raw.size(); i++)
            stripped[i] = ColorUtils.stripColor(raw.get(i)).trim();

        // Outside Skyblock — return vanilla lines as-is
        if (!preview && !SkyblockData.isOnSkyblock()) {
            List<String> result = new ArrayList<>();
            String vanillaTitle = SkyblockData.getScoreboardTitle();
            if (vanillaTitle == null || vanillaTitle.isEmpty()) {
                try {
                    net.minecraft.scoreboard.ScoreObjective obj =
                            Minecraft.getMinecraft().theWorld.getScoreboard().getObjectiveInDisplaySlot(1);
                    if (obj != null) vanillaTitle = obj.getDisplayName();
                } catch (Exception ignored) {}
            }
            if (vanillaTitle != null && !vanillaTitle.isEmpty()) result.add(vanillaTitle);
            result.addAll(raw);
            return result;
        }

        boolean inDungeon = SkyblockData.isInDungeon();

        // ── parse: single pass, pre-stripped lines ────────────────────────────
        String serverRaw      = null;
        String seasonRaw      = null;
        String timeRaw        = null;
        String locationRaw    = null;
        String purseRaw       = null;
        String bankRaw        = null;
        String bitsRaw        = null;
        String profileTypeRaw = null;
        String northStarsRaw  = null;
        String heatRaw        = null;
        String websiteRaw     = null;
        List<String> eventLines  = new ArrayList<>();
        List<String> slayerLines = new ArrayList<>();
        Set<String>  claimed     = new LinkedHashSet<>();

        for (int i = 0; i < raw.size(); i++) {
            String l = raw.get(i);
            String c = stripped[i];
            if (c.isEmpty()) continue;

            if (locationRaw == null && (l.contains(LOC_SYMBOL_NORMAL) || l.contains(LOC_SYMBOL_RIFT))) {
                locationRaw = l; claimed.add(l); continue;
            }
            if (serverRaw == null && SERVER_PATTERN.matcher(c).matches()) {
                serverRaw = l; claimed.add(l); continue;
            }
            if (seasonRaw == null && SEASON_PATTERN.matcher(c).matches()) {
                seasonRaw = l; claimed.add(l); continue;
            }
            if (timeRaw == null && TIME_PATTERN.matcher(c).matches()) {
                timeRaw = l; claimed.add(l); continue;
            }
            if (purseRaw == null && PURSE_PATTERN.matcher(c).find()) {
                purseRaw = l; claimed.add(l); continue;
            }
            if (bankRaw == null && BANK_PATTERN.matcher(c).find()) {
                bankRaw = l; claimed.add(l); continue;
            }
            if (bitsRaw == null && BITS_PATTERN.matcher(c).find()) {
                bitsRaw = l; claimed.add(l); continue;
            }
            if (COOKIE_SUPPRESS_PATTERN.matcher(c).find()) {
                claimed.add(l); continue;
            }
            if (profileTypeRaw == null && PROFILE_TYPE_PATTERN.matcher(c).find()) {
                profileTypeRaw = l; claimed.add(l); continue;
            }
            if (EVENT_PATTERN.matcher(c).find()) {
                eventLines.add(l); claimed.add(l);
                if (i + 1 < raw.size() && !stripped[i + 1].isEmpty()) {
                    eventLines.add(raw.get(i + 1)); claimed.add(raw.get(i + 1)); i++;
                }
                continue;
            }
            if (slayerLines.isEmpty() && SLAYER_PATTERN.matcher(c).find()) {
                claimed.add(l); slayerLines.add(l);
                for (int off = 1; off <= 2 && (i + off) < raw.size(); off++) {
                    String next = raw.get(i + off);
                    if (!stripped[i + off].isEmpty()) { slayerLines.add(next); claimed.add(next); }
                }
                i += 2;
                continue;
            }
            // Website: simple contains is faster than a full regex match
            if (websiteRaw == null && c.contains("fakepixel")) {
                websiteRaw = l; claimed.add(l); continue;
            }
            if (northStarsRaw == null && NORTHSTARS_PATTERN.matcher(c).find()) {
                northStarsRaw = l; claimed.add(l); continue;
            }
            if (heatRaw == null && HEAT_PATTERN.matcher(c).find()) {
                heatRaw = l; claimed.add(l);
            }
        }

        // ── unknown lines (preserve original scoreboard order) ────────────────
        List<String> unknownLines = new ArrayList<>();
        for (int i = 0; i < raw.size(); i++) {
            String l = raw.get(i);
            if (claimed.contains(l)) continue;
            String c = stripped[i];
            if (c.isEmpty()) continue;
            UnknownLinesHandler.handle(l);
            unknownLines.add(l);
        }

        // ── transform: build ordered display list ─────────────────────────────
        List<String> lines = new ArrayList<>();

        String title = SkyblockData.getScoreboardTitle();
        if (title != null && !title.isEmpty()) lines.add(title);

        for (int id : getLineOrder()) {
            switch (id) {
                case LINE_SERVER:
                    if (serverRaw != null) lines.add(serverRaw);
                    break;
                case LINE_SEASON:
                    if (seasonRaw != null) lines.add(seasonRaw);
                    break;
                case LINE_TIME:
                    if (timeRaw != null) lines.add(timeRaw);
                    break;
                case LINE_PROFILE_TYPE:
                    if (profileTypeRaw != null) lines.add(profileTypeRaw);
                    break;
                case LINE_ISLAND: {
                    SkyblockData.Location loc = SkyblockData.getCurrentLocation();
                    if (loc != SkyblockData.Location.NONE) {
                        String name;
                        if      (loc == SkyblockData.Location.CRIMSON_ISLE) name = "Crimson Isles";
                        else if (loc == SkyblockData.Location.HUB)          name = "Skyblock Hub";
                        else                                                  name = toTitleCase(loc.name());
                        lines.add("㋖ §b" + name);
                    }
                    break;
                }
                case LINE_LOCATION:
                    if (locationRaw != null) lines.add(locationRaw);
                    break;
                case LINE_PURSE:
                    if (purseRaw != null) lines.add(purseRaw);
                    break;
                case LINE_BANK:
                    if (!inDungeon) {
                        if (bankRaw != null) {
                            lines.add(bankRaw);
                        } else {
                            String bank = BankParser.getBank();
                            if (bank != null) lines.add("§fBank: §6" + bank);
                        }
                    }
                    break;
                case LINE_BITS:
                    if (bitsRaw != null) lines.add(bitsRaw);
                    break;
                case LINE_POWDER:
                    if (SkyblockData.isOnSkyblock() && !inDungeon
                            && (SkyblockData.getCurrentLocation() == SkyblockData.Location.DWARVEN
                            || SkyblockData.getCurrentLocation() == SkyblockData.Location.CRYSTAL_HOLLOWS)) {
                        long mithril  = TablistParser.getMithrilPowder();
                        long gemstone = TablistParser.getGemstonePowder();
                        long glacite  = TablistParser.getGlacitePowder();
                        if (mithril > 0 || gemstone > 0 || glacite > 0) {
                            lines.add("§9§lPowder");
                            if (mithril  > 0) lines.add(" §7- §fMithril: §2"  + formatPowder(mithril));
                            if (gemstone > 0) lines.add(" §7- §fGemstone: §d" + formatPowder(gemstone));
                            if (glacite  > 0) lines.add(" §7- §fGlacite: §b"  + formatPowder(glacite));
                        }
                    }
                    break;
                case LINE_GEMS:
                    if (!inDungeon) {
                        String gems = TablistParser.readGems();
                        if (gems != null) lines.add("§fGems: §a" + gems);
                    }
                    break;
                case LINE_EVENT:
                    lines.addAll(eventLines);
                    break;
                case LINE_COOKIE:
                    if (!inDungeon) {
                        String cookie = TablistParser.readCookieBuff();
                        if (cookie != null && !cookie.toLowerCase().contains("not active"))
                            lines.add("§dCookie Buff: §f" + cookie);
                    }
                    break;
                case LINE_POWER: {
                    String power = MaxwellPowerSync.getPower();
                    if (power != null && SkyblockData.isOnSkyblock())
                        lines.add("§fPower: §d" + power);
                    break;
                }
                case LINE_FETCHUR:
                    if (SkyblockData.isOnSkyblock())
                        lines.add("§fFetchur: §e" + FetchurData.getTodaysItem());
                    break;
                case LINE_SLAYER:
                    if (!inDungeon) lines.addAll(slayerLines);
                    break;
                case LINE_EXTRA:
                    if (!inDungeon) lines.addAll(unknownLines);
                    break;
                case LINE_NORTHSTARS:
                    if (northStarsRaw != null) lines.add(northStarsRaw);
                    break;
                case LINE_HEAT:
                    if (heatRaw != null) lines.add(heatRaw);
                    break;
                case LINE_EMPTY1: case LINE_EMPTY2: case LINE_EMPTY3: case LINE_EMPTY4:
                case LINE_EMPTY5: case LINE_EMPTY6: case LINE_EMPTY7:
                    if (SkyblockData.isOnSkyblock() && !inDungeon) lines.add("");
                    break;
            }
        }

        // Dungeons: always append unknown lines regardless of LINE_EXTRA config
        if (inDungeon && !unknownLines.isEmpty())
            lines.addAll(unknownLines);

        // Website line always last (claimed out of the main loop, appended here)
        if (websiteRaw != null) lines.add(websiteRaw);

        return lines;
    }

    // ── render ────────────────────────────────────────────────────────────────

    @Override
    public void render(boolean preview) {
        if (!preview && !extraGuard()) return;
        if (!preview && ATHRConfig.feature.scoreboard.hideOnTab && OverlayUtils.shouldHide()) return;

        // getLines() is now a pure cache read — zero cost
        List<String> lines = getLines(preview);
        if (lines.isEmpty()) return;

        boolean down = Keyboard.isKeyDown(ATHRConfig.feature.debug.scoreboardDebugConfig.scoreboardDebugKey);
        if (down && !wasDown && ATHRConfig.feature.debug.scoreboardDebugConfig.scoreboardDebug)
            ChatUtils.sendMessage(CustomScoreboardAPI.toJson());
        wasDown = down;

        Minecraft mc   = Minecraft.getMinecraft();
        float scale    = getScale();
        int lh         = LINE_HEIGHT + LINE_GAP;
        int ss         = SUPERSAMPLE;
        int alignment  = ATHRConfig.feature.scoreboard.lineAlignment;
        int minWidth   = ATHRConfig.feature.scoreboard.minWidth;

        int maxW = minWidth;
        for (String line : lines)
            maxW = Math.max(maxW, mc.fontRendererObj.getStringWidth(line));

        int boxW = maxW + PAD_X * 2;
        int boxH = lines.size() * lh + PAD_Y * 2 - LINE_GAP;
        lastW = boxW;
        lastH = boxH;

        ScaledResolution sr = new ScaledResolution(mc);
        Position pos = getPosition();

        int x = pos.getAbsX(sr, (int) (boxW * scale));
        int y = pos.getAbsY(sr, (int) (boxH * scale));
        if (pos.isCenterX()) x -= (int) (boxW * scale / 2);
        if (pos.isCenterY()) y -= (int) (boxH * scale / 2);

        GL11.glPushMatrix();
        GL11.glTranslatef(x, y, 0);
        GL11.glScalef(scale / ss, scale / ss, 1f);

        GlStateManager.enableBlend();
        GlStateManager.tryBlendFuncSeparate(770, 771, 1, 0);

        int bgColor = getBgColor();
        if ((bgColor >>> 24) != 0)
            drawRoundedRect(0, 0, boxW * ss, boxH * ss, getCornerRadius() * ss, bgColor);

        GL11.glScalef(ss, ss, 1f);

        int textY = PAD_Y;
        if (SkyblockData.isOnSkyblock()) {
            // Line 0 is the Skyblock title — always centered
            String firstLine = lines.get(0);
            int titleX = (boxW - mc.fontRendererObj.getStringWidth(firstLine)) / 2;
            mc.fontRendererObj.drawStringWithShadow(firstLine, titleX, textY, -1);
            textY += lh;
            for (int i = 1; i < lines.size(); i++) {
                String line = lines.get(i);
                mc.fontRendererObj.drawStringWithShadow(line, xFor(line, boxW, alignment), textY, 0xFFFFFF);
                textY += lh;
            }
        } else {
            String firstLine = lines.get(0);
            int titleX = (boxW - mc.fontRendererObj.getStringWidth(firstLine)) / 2;
            mc.fontRendererObj.drawStringWithShadow(firstLine, titleX, textY, -1);
            textY += lh;
            for (int i = 1; i < lines.size(); i++) {
                String line = lines.get(i);
                mc.fontRendererObj.drawStringWithShadow(line, xFor(line, boxW, alignment), textY, 0xFFFFFF);
                textY += lh;
            }
        }

        GlStateManager.disableBlend();
        GL11.glPopMatrix();
    }
}