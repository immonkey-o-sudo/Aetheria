package com.jef.justenoughfakepixel.features.profile;

import com.jef.justenoughfakepixel.JefMod;
import com.jef.justenoughfakepixel.utils.ColorUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.ContainerChest;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.function.Consumer;

public class GuiWaiter {

    public static final GuiWaiter INSTANCE = new GuiWaiter();
    private final Deque<PendingWait> queue = new ArrayDeque<>();
    private GuiWaiter() {}

    // ── Standard single-page wait ─────────────────────────────────────────────

    public static void waitFor(String expectedTitle, int tickDelay, int pressSlot,
                               Consumer<ContainerChest> callback) {
        JefMod.logger.info("[GuiWaiter] Queued wait for: '" + expectedTitle + "' (queue size now " + (INSTANCE.queue.size() + 1) + ")");
        INSTANCE.queue.add(new PendingWait(expectedTitle, tickDelay, pressSlot, callback, null, null, -1));
    }

    public static void waitFor(String expectedTitle, int tickDelay, int pressSlot,
                               String returnTitle, Consumer<ContainerChest> callback,
                               Consumer<ContainerChest> onReturn) {
        JefMod.logger.info("[GuiWaiter] Queued wait for: '" + expectedTitle + "' with return to '" + returnTitle + "' (queue size now " + (INSTANCE.queue.size() + 1) + ")");
        INSTANCE.queue.add(new PendingWait(expectedTitle, tickDelay, pressSlot, callback, returnTitle, onReturn, -1));
    }

    public static void waitForPaged(String expectedTitle, int tickDelay,
                                    int nextPageSlot, String nextPageItemName,
                                    int backSlot, String returnTitle,
                                    Consumer<ContainerChest> onPage,
                                    Consumer<ContainerChest> onReturn) {
        JefMod.logger.info("[GuiWaiter] Queued paged wait for: '" + expectedTitle + "'");
        INSTANCE.queue.add(new PendingWait(expectedTitle, tickDelay, -1,
                container -> INSTANCE.handlePage(container, expectedTitle, tickDelay,
                        nextPageSlot, nextPageItemName, backSlot, returnTitle, onPage, onReturn),
                null, null, -1));
    }

    /**
     * Called each time a page of a paged GUI is ready.
     * Fires onPage, then either clicks nextPageSlot (if another page exists)
     * or clicks backSlot and queues the return wait.
     */
    private void handlePage(ContainerChest container, String expectedTitle, int tickDelay,
                            int nextPageSlot, String nextPageItemName,
                            int backSlot, String returnTitle,
                            Consumer<ContainerChest> onPage,
                            Consumer<ContainerChest> onReturn) {
        // Parse this page
        onPage.accept(container);

        Minecraft mc = Minecraft.getMinecraft();
        ItemStack nextPageItem = container.getSlot(nextPageSlot).getStack();
        boolean hasNextPage = nextPageItem != null
                && ColorUtils.stripColor(nextPageItem.getDisplayName()).contains(nextPageItemName);

        if (hasNextPage) {
            JefMod.logger.info("[GuiWaiter] Paged GUI: clicking next page (slot " + nextPageSlot + ")");
            mc.playerController.windowClick(container.windowId, nextPageSlot, 0, 0, mc.thePlayer);

            // CRITICAL FIX: Tell the waiter to explicitly ignore the current Window ID so it waits for the server to load the next page
            queue.addFirst(new PendingWait(expectedTitle, tickDelay, -1,
                    next -> handlePage(next, expectedTitle, tickDelay,
                            nextPageSlot, nextPageItemName, backSlot, returnTitle, onPage, onReturn),
                    null, null, container.windowId));
        } else {
            JefMod.logger.info("[GuiWaiter] Paged GUI: last page reached, clicking back (slot " + backSlot + ")");
            if (backSlot > 0) {
                mc.playerController.windowClick(container.windowId, backSlot, 0, 0, mc.thePlayer);
                JefMod.logger.info("[GuiWaiter] Page GUI: clicked on slot " + backSlot + " | " + container.windowId);
            }
            if (returnTitle != null && onReturn != null) {
                JefMod.logger.info("[GuiWaiter] Queuing return wait for: '" + returnTitle + "'");
                queue.addFirst(new PendingWait(returnTitle, 2, -1, onReturn, null, null, container.windowId));
            }
        }
    }

    // ── Tick handler ──────────────────────────────────────────────────────────

    @SubscribeEvent
    public void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.START) return;
        if (queue.isEmpty()) return;

        PendingWait head = queue.peek();

        if (!head.guiReceived) {
            ContainerChest chest = getOpenChest(head.expectedTitle, head.ignoreWindowId);
            if (chest == null) {
                head.pollTicks++;
                if (head.pollTicks % 20 == 0) {
                    String current = getCurrentTitle();
                    JefMod.logger.info("[GuiWaiter] Still waiting for '" + head.expectedTitle
                            + "' — current screen title: '" + current + "' (" + head.pollTicks + " ticks)");
                }
                if (head.pollTicks >= 400) {
                    JefMod.logger.info("[GuiWaiter] TIMEOUT waiting for '" + head.expectedTitle + "' — cancelling remaining queue (" + queue.size() + " items)");
                    queue.clear();
                    ProfileParser.parsing = false;
                }
                return;
            }
            JefMod.logger.info("[GuiWaiter] GUI matched: '" + head.expectedTitle + "' (Window ID: " + chest.windowId + ") — starting " + head.ticksRemaining + "-tick delay");
            head.container   = chest;
            head.guiReceived = true;
            return;
        }

        if (--head.ticksRemaining > 0) return;

        JefMod.logger.info("[GuiWaiter] Firing callback for: '" + head.expectedTitle + "'");
        queue.poll();

        // Failsafe: Re-fetch the container right before callback just in case it updated during the tickDelay
        ContainerChest currentChest = getOpenChest(head.expectedTitle, -1);
        if (currentChest != null) {
            head.container = currentChest;
        }

        head.callback.accept(head.container);

        if (head.pressSlot > 0) {
            JefMod.logger.info("[GuiWaiter] Clicking slot " + head.pressSlot + " to navigate away from '" + head.expectedTitle + "'");
            Minecraft mc = Minecraft.getMinecraft();
            mc.playerController.windowClick(
                    head.container.windowId, head.pressSlot, 0, 0, mc.thePlayer
            );
        }

        if (head.returnTitle != null && head.onReturn != null) {
            JefMod.logger.info("[GuiWaiter] Queuing return wait for: '" + head.returnTitle + "'");
            queue.addFirst(new PendingWait(head.returnTitle, 2, -1, head.onReturn, null, null, head.container.windowId));
        }
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private static String getCurrentTitle() {
        if (!(Minecraft.getMinecraft().currentScreen instanceof GuiContainer)) {
            return Minecraft.getMinecraft().currentScreen == null ? "null"
                    : Minecraft.getMinecraft().currentScreen.getClass().getSimpleName();
        }
        Container container = ((GuiContainer) Minecraft.getMinecraft().currentScreen).inventorySlots;
        if (!(container instanceof ContainerChest)) return "(non-chest container)";
        return ColorUtils.stripColor(
                ((ContainerChest) container).getLowerChestInventory()
                        .getDisplayName().getUnformattedText()
        ).trim();
    }

    private static ContainerChest getOpenChest(String expectedTitle, int ignoreWindowId) {
        if (!(Minecraft.getMinecraft().currentScreen instanceof GuiContainer)) return null;
        Container container = ((GuiContainer) Minecraft.getMinecraft().currentScreen).inventorySlots;
        if (!(container instanceof ContainerChest)) return null;

        // CRITICAL FIX: Deny the container if its window ID matches the old page's ID
        if (container.windowId == ignoreWindowId) return null;

        String title = ColorUtils.stripColor(
                ((ContainerChest) container).getLowerChestInventory()
                        .getDisplayName().getUnformattedText()
        ).trim();
        return title.equals(expectedTitle) ? (ContainerChest) container : null;
    }

    // ── Internal state ────────────────────────────────────────────────────────

    private static class PendingWait {
        final String                   expectedTitle;
        final Consumer<ContainerChest> callback;
        final String                   returnTitle;
        final Consumer<ContainerChest> onReturn;
        final int                      pressSlot;
        final int                      ignoreWindowId;
        int                            ticksRemaining;
        int                            pollTicks = 0;
        ContainerChest                 container;
        boolean                        guiReceived = false;

        PendingWait(String expectedTitle, int tickDelay, int pressSlot,
                    Consumer<ContainerChest> callback,
                    String returnTitle, Consumer<ContainerChest> onReturn,
                    int ignoreWindowId) {
            this.expectedTitle  = expectedTitle;
            this.ticksRemaining = Math.max(tickDelay, 1);
            this.pressSlot      = pressSlot;
            this.callback       = callback;
            this.returnTitle    = returnTitle;
            this.onReturn       = onReturn;
            this.ignoreWindowId = ignoreWindowId;
        }
    }
}