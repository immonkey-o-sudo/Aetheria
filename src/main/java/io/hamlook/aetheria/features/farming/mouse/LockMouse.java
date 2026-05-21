package io.hamlook.aetheria.features.farming.mouse;

import io.hamlook.aetheria.core.ATHRConfig;
import io.hamlook.aetheria.utils.KeybindHelper;
import io.hamlook.aetheria.init.RegisterEvents;
import io.hamlook.aetheria.utils.chat.ChatUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.util.EnumChatFormatting;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;

@RegisterEvents
public class LockMouse {

    private static final Minecraft mc = Minecraft.getMinecraft();
    private static final String PREFIX = EnumChatFormatting.GREEN + " " + EnumChatFormatting.RESET;

    private boolean keyWasDown = false;

    public static boolean isLocked() {
        return ATHRConfig.feature != null && ATHRConfig.feature.farming.lockMouse;
    }

    public static void setLocked(boolean locked) {
        if (ATHRConfig.feature == null) return;
        ATHRConfig.feature.farming.lockMouse = locked;
        ATHRConfig.saveConfig();
        ChatUtils.sendMessage(PREFIX + (locked ? EnumChatFormatting.GREEN + "Mouse locked." : EnumChatFormatting.RED + "Mouse unlocked."));
    }

    @SubscribeEvent
    public void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;
        if (ATHRConfig.feature == null || mc.thePlayer == null || mc.currentScreen != null) return;

        boolean keyDown = KeybindHelper.isKeyDown(ATHRConfig.feature.farming.lockMouseKey);
        if (keyDown && !keyWasDown) setLocked(!isLocked());
        keyWasDown = keyDown;
    }
}