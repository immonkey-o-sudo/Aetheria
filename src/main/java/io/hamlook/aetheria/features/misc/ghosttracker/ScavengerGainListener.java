package io.hamlook.aetheria.features.misc.ghosttracker;

import io.hamlook.aetheria.Aetheria;
import io.hamlook.aetheria.events.ScavengerGainEvent;
import io.hamlook.aetheria.init.RegisterEvents;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

@RegisterEvents
public class ScavengerGainListener {

    @SubscribeEvent
    public void onScavengerGain(ScavengerGainEvent event) {
        Aetheria.logger.info("[GhostTracker/ScavengerGainListener] Scavenger gain event received: " + event.getAmount());
        GhostStats.getInstance().addScavenger(event.getAmount());
    }
}
