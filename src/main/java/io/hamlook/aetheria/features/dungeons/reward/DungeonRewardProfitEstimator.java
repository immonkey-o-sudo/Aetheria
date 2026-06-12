package io.hamlook.aetheria.features.dungeons.reward;

import io.hamlook.aetheria.core.ATHRConfig;
import io.hamlook.aetheria.init.RegisterEvents;
import io.hamlook.aetheria.utils.ContainerUtils;
import net.minecraft.block.BlockStainedGlassPane;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.init.Blocks;
import net.minecraft.inventory.ContainerChest;
import net.minecraft.inventory.Slot;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraftforge.client.event.GuiScreenEvent;
import net.minecraftforge.event.world.WorldEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import java.util.HashMap;
import java.util.Objects;

@RegisterEvents
public class DungeonRewardProfitEstimator {

    public HashMap<String,RewardEstimate> cache = new HashMap<>();

    @SubscribeEvent
    public void onUnload(WorldEvent.Unload event) {
        cache.clear();
    }

    @SubscribeEvent
    public void onDraw(GuiScreenEvent.BackgroundDrawnEvent event) {
        if(!ATHRConfig.feature.dungeons.rewardProfitEstimator) return;
        if(!(event.gui instanceof GuiContainer)) return;
        GuiContainer container = (GuiContainer) event.gui;
        if(!(container.inventorySlots instanceof ContainerChest)) return;
        ContainerChest chest = (ContainerChest) container.inventorySlots;
        String title = ContainerUtils.getTitle(chest);
        if(!title.endsWith("Chest")) return;
        String chestID = (title.replace("Chest","").trim()).toLowerCase();

        for(int i = 0; i< chest.getLowerChestInventory().getSizeInventory(); i++) {
            Slot slot = chest.getSlot(i);
            if(slot == null || !slot.getHasStack()) continue;
            ItemStack stack = slot.getStack();
            if(stack == null || Objects.equals(stack.getItem().getRegistryName(), Item.getItemFromBlock(Blocks.stained_glass_pane).getRegistryName())) continue;

        }
    }


}
