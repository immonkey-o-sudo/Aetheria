package io.hamlook.aetheria.events;

import lombok.Getter;
import net.minecraftforge.fml.common.eventhandler.Event;

@Getter
public class ScavengerGainEvent extends Event {

    private final int amount;

    public ScavengerGainEvent(int amount) {
        this.amount = amount;
    }
}
