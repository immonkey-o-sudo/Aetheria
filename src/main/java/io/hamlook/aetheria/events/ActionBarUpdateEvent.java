package io.hamlook.aetheria.events;

import net.minecraftforge.fml.common.eventhandler.Event;

public class ActionBarUpdateEvent extends Event {

    private final String text;

    public ActionBarUpdateEvent(String text) {
        this.text = text;
    }

    public String getText() {
        return text;
    }
}