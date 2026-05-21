package io.hamlook.aetheria.features.misc.SkyblockExp;

import io.hamlook.aetheria.core.ATHRConfig;
import io.hamlook.aetheria.events.ActionBarXpGainEvent;
import io.hamlook.aetheria.init.RegisterEvents;
import io.hamlook.aetheria.utils.chat.ChatUtils;
import net.minecraft.util.EnumChatFormatting;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

@RegisterEvents
public class SkyblockXpInChat {

    private static final String PREFIX = EnumChatFormatting.DARK_AQUA + "[SkyBlock XP] " + EnumChatFormatting.RESET;

    @SubscribeEvent
    public void onXpGain(ActionBarXpGainEvent event) {
        if (ATHRConfig.feature == null || !ATHRConfig.feature.misc.skyblockXpInChat) return;

        ChatUtils.sendMessage(PREFIX + event.getFormattedText());
    }
}