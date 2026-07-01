package io.hamlook.aetheria.utils.overlay;

import io.hamlook.aetheria.core.ATHRConfig;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

public abstract class SimpleOverlay {

    public abstract boolean shouldRender();

    public abstract void render(ScaledResolution sr);

    @SubscribeEvent
    public final void onRenderOverlay(RenderGameOverlayEvent.Post event) {
        if (event.type != RenderGameOverlayEvent.ElementType.ALL) return;
        if (ATHRConfig.feature == null) return;
        boolean shouldHide = (hideOnChat() && OverlayUtils.isChatOpen())
            || (hideOnTab() && OverlayUtils.isTabHeld())
            || (hideOnDebug() && OverlayUtils.isDebugActive())
            || OverlayUtils.isStorageActive();
        if (shouldHide) return;
        if (!shouldRender()) return;
        render(event.resolution);
    }

    protected boolean hideOnChat()   { return true; }
    protected boolean hideOnTab()    { return true; }
    protected boolean hideOnDebug()  { return true; }
}
