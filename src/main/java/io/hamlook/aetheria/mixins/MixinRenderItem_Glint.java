package io.hamlook.aetheria.mixins;

// Ported from https://github.com/Polyfrost/PolyPatcher/blob/main/src/main/java/club/sk1er/patcher/mixins/features/disableenchantglint/RenderItemMixin_DisableEnchantGlint.java

import io.hamlook.aetheria.core.ATHRConfig;
import net.minecraft.client.renderer.entity.RenderItem;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(RenderItem.class)
public class MixinRenderItem_Glint {

    @Inject(method = "renderEffect", at = @At("HEAD"), cancellable = true)
    private void ATHR$disableEnchantGlint(CallbackInfo ci) {
        if (ATHRConfig.feature != null && ATHRConfig.feature.qol.disableEnchantGlint)
            ci.cancel();
    }
}