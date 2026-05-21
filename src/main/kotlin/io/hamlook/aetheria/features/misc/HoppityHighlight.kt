package io.hamlook.aetheria.features.misc

import io.hamlook.aetheria.core.ATHRConfig
import io.hamlook.aetheria.init.RegisterEvents
import io.hamlook.aetheria.utils.ColorUtils
import io.hamlook.aetheria.utils.render.HighlightUtils
import net.minecraft.inventory.ContainerChest

@RegisterEvents
object HoppityHighlight {

    private const val HIGHLIGHT_COLOR = 0x8000FF00.toInt()

    init {
        HighlightUtils.registerHighlighter { gui, slot ->
            if (ATHRConfig.feature?.misc?.hoppityHighlight != true) return@registerHighlighter null

            val container = gui.inventorySlots as? ContainerChest ?: return@registerHighlighter null
            if (!container.lowerChestInventory.displayName.unformattedText.contains("Hoppity")) return@registerHighlighter null

            val stack = slot.stack ?: return@registerHighlighter null
            if (ColorUtils.stripColor(stack.displayName).contains("NEW RABBIT!")) HIGHLIGHT_COLOR else null
        }
    }
}