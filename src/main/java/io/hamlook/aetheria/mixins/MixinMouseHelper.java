package io.hamlook.aetheria.mixins;

import io.hamlook.aetheria.core.ATHRConfig;
import io.hamlook.aetheria.features.qol.CursorResetHandler;
import io.hamlook.aetheria.features.storage.StorageManager;
import net.minecraft.util.MouseHelper;
import org.lwjgl.input.Mouse;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(MouseHelper.class)
public class MixinMouseHelper {

    @Inject(method = "ungrabMouseCursor", at = @At("HEAD"), cancellable = true)
    private void ungrabMouseCursor(CallbackInfo ci) {
        // Prevent cursor reset while storage overlay is active (e.g. during container switch)
        if (StorageManager.isOverlayActive()) {
            ci.cancel();
            Mouse.setGrabbed(false);
            Mouse.setCursorPosition(CursorResetHandler.cachedX, CursorResetHandler.cachedY);
            return;
        }
        if (ATHRConfig.feature.qol.preventCursorReset) {
            ci.cancel();
            Mouse.setGrabbed(false);
            Mouse.setCursorPosition(CursorResetHandler.cachedX, CursorResetHandler.cachedY);
        }
    }
}