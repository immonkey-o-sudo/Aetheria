package io.hamlook.aetheria.mixins;

import io.hamlook.aetheria.events.PacketReceiveTimeUpdateEvent;
import net.minecraft.client.network.NetHandlerPlayClient;
import net.minecraft.network.play.server.S03PacketTimeUpdate;
import net.minecraftforge.common.MinecraftForge;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(NetHandlerPlayClient.class)
public class MixinNetHandlerPlayClient_Tps {

    // Fire an event on every time update packet so TPS can be tracked
    @Inject(method = "handleTimeUpdate", at = @At("HEAD"))
    private void ATHR$onTimeUpdate(S03PacketTimeUpdate packet, CallbackInfo ci) {
        MinecraftForge.EVENT_BUS.post(new PacketReceiveTimeUpdateEvent(packet));
    }
}