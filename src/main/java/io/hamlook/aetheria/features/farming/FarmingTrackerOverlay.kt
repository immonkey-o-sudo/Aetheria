package io.hamlook.aetheria.features.farming

import io.hamlook.aetheria.core.ATHRConfig
import io.hamlook.aetheria.init.RegisterEvents
import io.hamlook.aetheria.utils.Position
import io.hamlook.aetheria.utils.Utils
import io.hamlook.aetheria.utils.overlay.Overlay
import io.hamlook.aetheria.utils.render.ItemRenderUtils
import net.minecraft.client.Minecraft
import net.minecraft.item.ItemStack
import org.lwjgl.opengl.GL11

@RegisterEvents
class FarmingTrackerOverlay : Overlay(160, 70) {

    companion object {
        @JvmStatic
        private var instance: FarmingTrackerOverlay? = null

        @JvmStatic
        fun getInstance(): FarmingTrackerOverlay? = instance

        private const val ICON_SIZE = 8
        private const val ICON_GAP = 2
    }

    init {
        instance = this
    }

    private val config get() = ATHRConfig.feature.farming.farmingTracker

    // Ordinal 0 = title, 1 = value/rate, 2 = total crops/hour, 3+i = crop
    // family line for FarmingTracker.getCrops()[i]. Must stay in sync with
    // FarmingTrackerConfig's exampleText ordering. Icon is only non-null for
    // crop-family lines, and shown once at the start of the line — not once
    // per raw/enchanted/block sub-form within it.
    private fun entryForOrdinal(ordinal: Int, preview: Boolean): Pair<ItemStack?, String>? {
        if (ordinal == 0) {
            val pausedTag = if (!preview && FarmingTracker.isPaused()) " §7[Paused]" else ""
            return null to "§a§lFarming Tracker$pausedTag"
        }
        if (ordinal == 1) {
            if (preview) return null to "§76,144,000 coins §7(3.2M/h)"
            return null to "§7${Utils.shortNumberFormat(FarmingTracker.currentValue(), 0)} coins §7(${Utils.shortNumberFormat(FarmingTracker.coinsPerHour(), 0)}/h)"
        }
        if (ordinal == 2) {
            if (preview) return null to "§b12,480 crops/h"
            return null to "§b${Utils.shortNumberFormat(FarmingTracker.cropsPerHour(), 0)} crops/h"
        }

        val crops = FarmingTracker.getCrops()
        val index = ordinal - 3
        if (index < 0 || index >= crops.size) return null
        val crop = crops[index]
        val icon = FarmingTracker.getCropIcon(crop)

        if (preview) {
            return icon to "§a${crop.displayName}: §f12 §7E.${crop.displayName}: §f3"
        }

        val raw = FarmingTracker.getCount(crop.rawId)
        val ench = FarmingTracker.getCount(crop.enchantedId)
        val block = if (crop.blockId != null) FarmingTracker.getCount(crop.blockId) else 0L

        if (raw == 0L && ench == 0L && block == 0L) return null

        val parts = mutableListOf<String>()
        if (raw > 0) parts.add("§a${crop.displayName}: §f${Utils.shortNumberFormat(raw.toDouble(), 0)}")
        if (ench > 0) parts.add("§7E.${crop.displayName}: §f${Utils.shortNumberFormat(ench.toDouble(), 0)}")
        if (block > 0 && crop.blockChatName != null) parts.add("§7${crop.blockChatName}: §f${Utils.shortNumberFormat(block.toDouble(), 0)}")

        return icon to parts.joinToString(" ")
    }

    private fun buildEntries(preview: Boolean): List<Pair<ItemStack?, String>> {
        val entries = mutableListOf<Pair<ItemStack?, String>>()
        for (ordinal in config.farmingDisplayLines) {
            val entry = entryForOrdinal(ordinal, preview)
            if (entry != null) entries.add(entry)
        }
        return entries
    }

    // Kept for Overlay's abstract contract / anything that reads plain text lines.
    override fun getLines(preview: Boolean): List<String> = buildEntries(preview).map { it.second }

    // Full override, not just getLines() — the base Overlay.render() only knows
    // how to draw plain text, with no hook for icons, so the background-box
    // sizing and line-drawing loop are reimplemented here to make room for an
    // icon at the start of each crop line.
    override fun render(preview: Boolean) {
        val entries = buildEntries(preview)
        if (entries.isEmpty()) return

        val mc = Minecraft.getMinecraft()
        val fr = mc.fontRendererObj
        val scale = getScale()

        var w = 20
        for ((icon, text) in entries) {
            val textWidth = fr.getStringWidth(text)
            val lineWidth = if (icon != null) textWidth + ICON_SIZE + ICON_GAP + 6 else textWidth + 6
            w = maxOf(w, lineWidth)
        }
        val h = entries.size * LINE_HEIGHT + PADDING * 2
        lastW = w
        lastH = h

        val pos = getPosition()
        var x = pos.getAbsX(sr, (w * scale).toInt())
        var y = pos.getAbsY(sr, (h * scale).toInt())
        if (pos.isCenterX()) x -= (w * scale / 2).toInt()
        if (pos.isCenterY()) y -= (h * scale / 2).toInt()

        GL11.glPushMatrix()
        GL11.glTranslatef(x.toFloat(), y.toFloat(), 0f)
        GL11.glScalef(scale, scale, 1f)

        val bgColor = getBgColor()
        if ((bgColor ushr 24) != 0) Overlay.drawRoundedRect(-PADDING, -PADDING, w, h - PADDING, getCornerRadius(), bgColor)

        var dy = 0
        for ((icon, text) in entries) {
            var textX = 0
            if (icon != null) {
                ItemRenderUtils.renderItemIcon(mc, icon, 0, dy - 1, ICON_SIZE)
                textX = ICON_SIZE + ICON_GAP
            }
            fr.drawStringWithShadow(text, textX.toFloat(), dy.toFloat(), 0xFFFFFF)
            dy += LINE_HEIGHT
        }

        GL11.glPopMatrix()
    }

    override fun getPosition(): Position = config.farmingTrackerPosition
    override fun getScale(): Float = config.farmingTrackerScale
    override fun getBgColor(): Int = config.farmingTrackerBgColor
    override fun getCornerRadius(): Int = config.farmingTrackerCornerRadius
    override fun isEnabled(): Boolean = config.enabled

    override fun hideOnChat() = config.hideOnChat
    override fun hideOnTab() = config.hideOnTab
    override fun hideOnDebug() = config.hideOnDebug
}
