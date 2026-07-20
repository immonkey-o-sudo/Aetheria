package io.hamlook.aetheria.mixins;

import io.hamlook.aetheria.features.farming.mouse.LockMouse;
import io.hamlook.aetheria.features.farming.sensitivityreducer.SensitivityReducer;
import net.minecraft.client.Minecraft;
import net.minecraft.util.MouseHelper;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(MouseHelper.class)
public class MixinMouseHelper_SensitivityReducer {

    @Shadow public int deltaX;
    @Shadow public int deltaY;

    @Inject(method = "mouseXYChange", at = @At("RETURN"))
    private void ATHR$reduceSensitivity(CallbackInfo ci) {
        if (Minecraft.getMinecraft().currentScreen != null) return;
        if (LockMouse.isLocked()) return;
        if (!SensitivityReducer.isActive()) return;

        float scale = SensitivityReducer.getSensitivityScale();
        deltaX = Math.round(deltaX * scale);
        deltaY = Math.round(deltaY * scale);
    }
}
