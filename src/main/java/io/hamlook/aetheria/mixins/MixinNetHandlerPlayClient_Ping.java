package io.hamlook.aetheria.mixins;

import io.hamlook.aetheria.events.PacketReceiveStatsEvent;
import net.minecraft.client.network.NetHandlerPlayClient;
import net.minecraft.network.play.server.S37PacketStatistics;
import net.minecraftforge.common.MinecraftForge;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(NetHandlerPlayClient.class)
public class MixinNetHandlerPlayClient_Ping {

    @Inject(method = "handleStatistics", at = @At("HEAD"))
    private void ATHR$onStatistics(S37PacketStatistics packet, CallbackInfo ci) {
        MinecraftForge.EVENT_BUS.post(new PacketReceiveStatsEvent(packet));
    }
}