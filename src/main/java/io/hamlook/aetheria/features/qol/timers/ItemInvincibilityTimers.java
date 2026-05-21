package io.hamlook.aetheria.features.qol.timers;

import io.hamlook.aetheria.repo.ATHRRepo;
import io.hamlook.aetheria.repo.RepoHandler;
import io.hamlook.aetheria.repo.TimerRepo;
import io.hamlook.aetheria.utils.item.ItemStackFinder;
import io.hamlook.aetheria.utils.time.TimerManager;
import net.minecraft.item.ItemStack;

import java.util.List;

public class ItemInvincibilityTimers {

    private static final TimerManager timerManager = new TimerManager(TimerRepo.getInvincibilityDurations());

    static {
        RepoHandler.addListener(ATHRRepo.KEY_TIMERS, () -> timerManager.updateDurations(TimerRepo.getInvincibilityDurations()));
    }

    public static void markActive(String itemId) {
        timerManager.markActive(itemId);
    }

    public static void markActive(String itemId, long durationMs) {
        timerManager.markActive(itemId, durationMs);
    }

    public static long getRemainingMs(String itemId) {
        return timerManager.getRemainingMs(itemId);
    }

    public static boolean isActive(String itemId) {
        return timerManager.isActive(itemId);
    }

    public static List<String> getActiveTimers() {
        return timerManager.getActiveTimers();
    }

    public static ItemStack findItemStack(String itemId) {
        return ItemStackFinder.findItemStack(itemId);
    }
}