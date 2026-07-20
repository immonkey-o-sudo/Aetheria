package io.hamlook.aetheria.features.farming;

import io.hamlook.aetheria.core.ATHRConfig;
import io.hamlook.aetheria.core.features.farming.FarmingTrackerConfig;
import io.hamlook.aetheria.init.RegisterEvents;
import io.hamlook.aetheria.utils.Position;
import io.hamlook.aetheria.utils.Utils;
import io.hamlook.aetheria.utils.data.SkyblockData;
import io.hamlook.aetheria.utils.overlay.Overlay;
import io.hamlook.aetheria.utils.render.ItemRenderUtils;
import lombok.Getter;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.item.ItemStack;
import org.lwjgl.opengl.GL11;

import java.util.ArrayList;
import java.util.List;

@RegisterEvents
public class FarmingTrackerOverlay extends Overlay {

    @Getter
    private static FarmingTrackerOverlay instance;

    private static final int ICON_SIZE = 8;
    private static final int ICON_GAP = 2;

    private static final int COUNT_LINES_START = 3;
    private static final int SESSION_TIMER_ORDINAL = COUNT_LINES_START + Crop.all().length;
    private static final int TOTAL_CROPS_ORDINAL = SESSION_TIMER_ORDINAL + 1;

    public FarmingTrackerOverlay() {
        super(160, 70);
        instance = this;
    }

    private static FarmingTrackerConfig config() {
        return ATHRConfig.feature.farming.farmingTracker;
    }

    private boolean isInFarmingLocation() {
        SkyblockData.Location location = SkyblockData.getCurrentLocation();
        return location == SkyblockData.Location.BARN
                || location == SkyblockData.Location.PRIVATE_ISLAND
                || location == SkyblockData.Location.GARDEN;
    }

    private static String formatDuration(long ms) {
        long totalSeconds = ms / 1000;
        long hours = totalSeconds / 3600;
        long minutes = (totalSeconds % 3600) / 60;
        long seconds = totalSeconds % 60;
        return hours > 0
                ? String.format("%d:%02d:%02d", hours, minutes, seconds)
                : String.format("%02d:%02d", minutes, seconds);
    }

    private static final class Entry {
        final ItemStack icon;
        final String text;

        Entry(ItemStack icon, String text) {
            this.icon = icon;
            this.text = text;
        }
    }

    private Entry entryForOrdinal(int ordinal, boolean preview) {
        if (ordinal == 0) {
            String pausedTag = (!preview && FarmingTracker.isPaused()) ? " §7[Paused]" : "";
            return new Entry(null, "§a§lFarming Tracker" + pausedTag);
        }
        if (ordinal == 1) {
            if (preview) return new Entry(null, "§76,144,000 coins §7(3.2M/h)");
            return new Entry(null, "§7" + Utils.shortNumberFormat(FarmingTracker.currentValue(), 0)
                    + " coins §7(" + Utils.shortNumberFormat(FarmingTracker.coinsPerHour(), 0) + "/h)");
        }
        if (ordinal == 2) {
            if (preview) return new Entry(null, "§b12,480 crops/h");
            return new Entry(null, "§b" + Utils.shortNumberFormat(FarmingTracker.cropsPerHour(), 0) + " crops/h");
        }
        if (ordinal == SESSION_TIMER_ORDINAL) {
            if (preview) return new Entry(null, "§7Session: §f42:17");
            String pausedTag = FarmingTracker.isPaused() ? " §7[Paused]" : "";
            return new Entry(null, "§7Session: §f" + formatDuration(FarmingTracker.getActiveTimeMs()) + pausedTag);
        }
        if (ordinal == TOTAL_CROPS_ORDINAL) {
            if (preview) return new Entry(null, "§bTotal: §f108,240 crops");
            long total = FarmingTracker.totalRawCrops();
            if (total <= 0L) return null;
            return new Entry(null, "§bTotal: §f" + Utils.shortNumberFormat((double) total, 0) + " crops");
        }

        Crop[] crops = Crop.all();

        int countIndex = ordinal - COUNT_LINES_START;
        if (countIndex >= 0 && countIndex < crops.length) {
            Crop crop = crops[countIndex];
            ItemStack icon = crop.getIcon();

            if (preview) {
                List<String> previewParts = new ArrayList<>();
                previewParts.add("§a" + crop.displayName + ": §f12");
                if (crop.enchantedId != null) previewParts.add("§7E." + crop.displayName + ": §f3");
                if (crop.blockId != null && crop.blockDisplayName != null) {
                    previewParts.add("§7" + crop.blockDisplayName + ": §f1");
                }
                previewParts.add("§b(4,760/h)");
                return new Entry(icon, String.join(" ", previewParts));
            }

            long raw = FarmingTracker.getCount(crop.rawId);
            long ench = FarmingTracker.getCount(crop.enchantedId);
            long block = crop.blockId != null ? FarmingTracker.getCount(crop.blockId) : 0L;

            if (raw == 0L && ench == 0L && block == 0L) return null;

            List<String> parts = new ArrayList<>();
            if (raw > 0) parts.add("§a" + crop.displayName + ": §f" + Utils.shortNumberFormat((double) raw, 0));
            if (ench > 0) parts.add("§7E." + crop.displayName + ": §f" + Utils.shortNumberFormat((double) ench, 0));
            if (block > 0 && crop.blockDisplayName != null) {
                parts.add("§7" + crop.blockDisplayName + ": §f" + Utils.shortNumberFormat((double) block, 0));
            }

            double rate = FarmingTracker.getCropRate(crop);
            if (rate > 0.0) parts.add("§b(" + Utils.shortNumberFormat(rate, 0) + "/h)");

            return new Entry(icon, String.join(" ", parts));
        }

        return null;
    }

    private List<Entry> buildEntries(boolean preview) {
        List<Entry> entries = new ArrayList<>();
        for (int ordinal : config().farmingDisplayLines) {
            Entry entry = entryForOrdinal(ordinal, preview);
            if (entry != null) entries.add(entry);
        }
        return entries;
    }

    @Override
    public List<String> getLines(boolean preview) {
        List<String> lines = new ArrayList<>();
        for (Entry entry : buildEntries(preview)) lines.add(entry.text);
        return lines;
    }

    @Override
    public void render(boolean preview) {
        List<Entry> entries = buildEntries(preview);
        if (entries.isEmpty()) return;

        Minecraft mc = Minecraft.getMinecraft();
        FontRenderer fr = mc.fontRendererObj;
        float scale = getScale();

        int w = 20;
        for (Entry entry : entries) {
            int textWidth = fr.getStringWidth(entry.text);
            int lineWidth = entry.icon != null ? textWidth + ICON_SIZE + ICON_GAP + 6 : textWidth + 6;
            w = Math.max(w, lineWidth);
        }
        int h = entries.size() * LINE_HEIGHT + PADDING * 2;
        lastW = w;
        lastH = h;

        Position pos = getPosition();
        int x = pos.getAbsX(sr, (int) (w * scale));
        int y = pos.getAbsY(sr, (int) (h * scale));
        if (pos.isCenterX()) x -= (int) (w * scale / 2);
        if (pos.isCenterY()) y -= (int) (h * scale / 2);

        GL11.glPushMatrix();
        GL11.glTranslatef(x, y, 0f);
        GL11.glScalef(scale, scale, 1f);

        int bgColor = getBgColor();
        if ((bgColor >>> 24) != 0) drawRoundedRect(-PADDING, -PADDING, w, h - PADDING, getCornerRadius(), bgColor);

        int dy = 0;
        for (Entry entry : entries) {
            int textX = 0;
            if (entry.icon != null) {
                ItemRenderUtils.renderItemIcon(mc, entry.icon, 0, dy - 1, ICON_SIZE);
                textX = ICON_SIZE + ICON_GAP;
            }
            fr.drawStringWithShadow(entry.text, textX, dy, 0xFFFFFF);
            dy += LINE_HEIGHT;
        }

        GL11.glPopMatrix();
    }

    @Override
    public Position getPosition() {
        return config().farmingTrackerPosition;
    }

    @Override
    public float getScale() {
        return config().farmingTrackerScale;
    }

    @Override
    public int getBgColor() {
        return config().farmingTrackerBgColor;
    }

    @Override
    public int getCornerRadius() {
        return config().farmingTrackerCornerRadius;
    }

    @Override
    protected boolean isEnabled() {
        return config().enabled && (!config().requireFarmingIsland || isInFarmingLocation());
    }

    @Override
    protected boolean hideOnChat() {
        return config().hideOnChat;
    }

    @Override
    protected boolean hideOnTab() {
        return config().hideOnTab;
    }

    @Override
    protected boolean hideOnDebug() {
        return config().hideOnDebug;
    }
}
