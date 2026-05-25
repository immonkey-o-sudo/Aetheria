package io.hamlook.aetheria.features.qol

import io.hamlook.aetheria.core.ATHRConfig
import io.hamlook.aetheria.core.config.editors.ChromaColour
import io.hamlook.aetheria.init.RegisterEvents
import io.hamlook.aetheria.utils.chat.ChatUtils
import io.hamlook.aetheria.utils.data.SkyblockData
import io.hamlook.aetheria.utils.render.RenderUtils
import net.minecraft.client.Minecraft
import net.minecraft.client.gui.inventory.GuiInventory
import net.minecraft.client.renderer.GlStateManager
import net.minecraft.inventory.Container
import net.minecraftforge.client.event.GuiScreenEvent
import net.minecraftforge.fml.common.eventhandler.EventPriority
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
import org.lwjgl.input.Keyboard

@RegisterEvents
class SlotBinds {

    private var pendingSlot: Int? = null

    private fun cfg() = ATHRConfig.feature?.qol?.slotBinds

    private fun isEnabled(): Boolean {
        val c = cfg() ?: return false
        return c.enabled && (!c.skyblockOnly || SkyblockData.isOnSkyblock())
    }

    private fun binds() = cfg()?.binds

    private fun Int.isHotbar() = this in 36..44
    private fun Int.isArmor() = this in 5..8
    private fun Int.isValidSlot() = this in 5 until 45
    private fun isShiftDown() = Keyboard.isKeyDown(Keyboard.KEY_LSHIFT) || Keyboard.isKeyDown(Keyboard.KEY_RSHIFT)

    private fun canBind(a: Int, b: Int) = !(a.isArmor() && !b.isHotbar()) && !(b.isArmor() && !a.isHotbar())

    private fun slotCenter(container: Container, slotIndex: Int, gui: GuiInventory): Pair<Int, Int>? {
        val slot = container.getSlot(slotIndex) ?: return null
        return (slot.xDisplayPosition + gui.guiLeft + 8) to (slot.yDisplayPosition + gui.guiTop + 8)
    }

    private fun MutableMap<Int, Int>.removeBind(slot: Int) {
        val partner = remove(slot)
        if (partner != null) remove(partner)
    }

    private fun MutableMap<Int, Int>.addBind(a: Int, b: Int) {
        removeBind(a)
        removeBind(b)

        this[a] = b
        this[b] = a
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    fun onMouseClick(event: GuiScreenEvent.MouseInputEvent.Pre) {
        if (!isEnabled() || !isShiftDown()) return
        val gui = event.gui as? GuiInventory ?: return
        if (!org.lwjgl.input.Mouse.getEventButtonState() || org.lwjgl.input.Mouse.getEventButton() != 0) return
        val clicked = gui.slotUnderMouse?.slotNumber?.takeIf { it.isValidSlot() } ?: return
        val bound = binds()?.get(clicked) ?: return
        if (!bound.isValidSlot() || bound == clicked) return

        val (from, to) = if (clicked.isHotbar()) bound to clicked else if (bound.isHotbar()) clicked to bound else return

        Minecraft.getMinecraft().playerController.windowClick(
            gui.inventorySlots.windowId, from, to - 36, 2, Minecraft.getMinecraft().thePlayer
        )
        event.isCanceled = true
    }

    @SubscribeEvent
    fun onKeyPress(event: GuiScreenEvent.KeyboardInputEvent.Pre) {
        if (!isEnabled()) return
        val gui = event.gui as? GuiInventory ?: return
        val c = cfg() ?: return
        if (c.bindKey == Keyboard.KEY_NONE || Keyboard.getEventKey() != c.bindKey || !Keyboard.getEventKeyState()) return

        val clicked = gui.slotUnderMouse?.slotNumber?.takeIf { it.isValidSlot() } ?: return
        event.isCanceled = true

        val pending = pendingSlot
        if (pending != null) {
            pendingSlot = null
            when {
                pending == clicked -> ChatUtils.sendMessage("§cCan't bind a slot to itself.")
                !pending.isHotbar() && !clicked.isHotbar() -> ChatUtils.sendMessage("§cOne slot must be in the hotbar.")
                !canBind(pending, clicked) -> ChatUtils.sendMessage("§cArmor slots can only be bound to hotbar slots.")
                else -> {
                    c.binds.addBind(pending, clicked)
                    ATHRConfig.saveConfig()
                }
            }
        } else {
            if (c.binds.containsKey(clicked)) {
                c.binds.removeBind(clicked)
                ATHRConfig.saveConfig()
            } else {
                pendingSlot = clicked
            }
        }
    }

    @SubscribeEvent
    fun onDraw(event: GuiScreenEvent.DrawScreenEvent.Post) {
        if (!isEnabled()) return
        val gui = event.gui as? GuiInventory ?: return
        val c = cfg() ?: return
        val container = gui.inventorySlots ?: return
        val color = ChromaColour.specialToChromaRGB(c.lineColor)

        GlStateManager.pushMatrix()
        GlStateManager.translate(0f, 0f, 999f)

        try {
            val pending = pendingSlot
            if (pending != null) {
                val (sx, sy) = slotCenter(container, pending, gui) ?: return
                RenderUtils.drawLine(sx, sy, event.mouseX, event.mouseY, color, 2f)
                return
            }

            if (c.alwaysShowLines) {
                // Draw all binds, iterate unique pairs (avoid drawing A→B and B→A twice)
                val drawn = mutableSetOf<Int>()
                for ((from, to) in c.binds) {
                    if (drawn.contains(from)) continue
                    drawn.add(from)
                    drawn.add(to)
                    val (sx, sy) = slotCenter(container, from, gui) ?: continue
                    val (ex, ey) = slotCenter(container, to, gui) ?: continue
                    RenderUtils.drawLine(sx, sy, ex, ey, color, 2f)
                }
            } else {
                if (!isShiftDown()) return
                val hovered = gui.slotUnderMouse?.slotNumber?.takeIf { it.isValidSlot() } ?: return
                val bound = c.binds[hovered]?.takeIf { it.isValidSlot() && it != hovered } ?: return
                val (sx, sy) = slotCenter(container, hovered, gui) ?: return
                val (ex, ey) = slotCenter(container, bound, gui) ?: return
                RenderUtils.drawLine(sx, sy, ex, ey, color, 2f)
            }
        } finally {
            GlStateManager.popMatrix()
        }
    }

    @SubscribeEvent
    fun onGuiClose(event: GuiScreenEvent.InitGuiEvent.Pre) {
        if (event.gui == null) pendingSlot = null
    }
}
