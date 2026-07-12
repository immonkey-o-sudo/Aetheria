package io.hamlook.aetheria.features.farming.organicmatter;

import io.hamlook.aetheria.core.ATHRConfig;
import io.hamlook.aetheria.core.features.farming.OrganicMatterTrackerConfig;
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
public class OrganicMatterTrackerOverlay extends Overlay {

    @Getter
    private static OrganicMatterTrackerOverlay instance;

    private static final int ICON_SIZE = 8;
    private static final int ICON_GAP = 2;

    private static final int TITLE_ORDINAL = 0;
    private static final int TOTAL_OM_ORDINAL = 1;
    private static final int OM_PER_HOUR_ORDINAL = 2;
    private static final int SESSION_TIMER_ORDINAL = 3;
    private static final int TOTAL_ITEMS_ORDINAL = 4;

    public OrganicMatterTrackerOverlay() {
        super(160, 70);
        instance = this;
    }

    private static OrganicMatterTrackerConfig config() {
        return ATHRConfig.feature.farming.organicMatterTracker;
    }

    private boolean isInFarmingLocation() {
        SkyblockData.Location location = SkyblockData.getCurrentLocation();
        return location == SkyblockData.Location.BARN
                || location == SkyblockData.Location.PRIVATE_ISLAND
                || location == SkyblockData.Location.GARDEN;
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
        if (ordinal == TITLE_ORDINAL) {
            String pausedTag = (!preview && OrganicMatterTracker.isPaused()) ? " §7[Paused]" : "";
            return new Entry(null, "§a§lOrganic Matter Tracker" + pausedTag);
        }
        if (ordinal == TOTAL_OM_ORDINAL) {
            if (preview) return new Entry(null, "§aTotal Organic Matter: §f4,830,000");
            return new Entry(null, "§aTotal Organic Matter: §f"
                    + Utils.shortNumberFormat(OrganicMatterTracker.totalOrganicMatter(), 0));
        }
        if (ordinal == OM_PER_HOUR_ORDINAL) {
            if (preview) return new Entry(null, "§a402,500/h organic matter");
            return new Entry(null, "§a" + Utils.shortNumberFormat(OrganicMatterTracker.organicMatterPerHour(), 0) + "/h organic matter");
        }
        if (ordinal == SESSION_TIMER_ORDINAL) {
            if (preview) return new Entry(null, "§1Playtime: §f2h 30m  §1Session: §f45m");
            String pausedTag = OrganicMatterTracker.isPaused() ? " §7[Paused]" : "";
            return new Entry(null, "§1Playtime: §f" + Utils.formatDuration(OrganicMatterTracker.getActiveTimeMs())
                    + "  §1Session: §f" + Utils.formatDuration(OrganicMatterTracker.getSessionTimeMs()) + pausedTag);
        }
        if (ordinal == TOTAL_ITEMS_ORDINAL) {
            if (preview) return new Entry(null, "§bTotal: §f108,240 items");
            long total = OrganicMatterTracker.totalItems();
            if (total <= 0L) return null;
            return new Entry(null, "§bTotal: §f" + Utils.shortNumberFormat((double) total, 0) + " items");
        }

        return null;
    }

    private List<Entry> buildEntries(boolean preview) {
        List<Entry> entries = new ArrayList<>();
        for (int ordinal : config().organicMatterDisplayLines) {
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
        return config().organicMatterTrackerPosition;
    }

    @Override
    public float getScale() {
        return config().organicMatterTrackerScale;
    }

    @Override
    public int getBgColor() {
        return config().organicMatterTrackerBgColor;
    }

    @Override
    public int getCornerRadius() {
        return config().organicMatterTrackerCornerRadius;
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

