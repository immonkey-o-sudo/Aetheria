package io.hamlook.aetheria.mixins.chat;

import io.hamlook.aetheria.core.ATHRConfig;
import io.hamlook.aetheria.features.chat.emoji.EmojiSuggestionBar;
import net.minecraft.client.gui.GuiChat;
import net.minecraft.client.gui.GuiTextField;
import org.lwjgl.input.Keyboard;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Drives the emoji shortcode suggestion popup in the chat box: renders it
 * above the input field, completes the top match on Tab, and completes
 * whichever entry is clicked.
 */
@Mixin(GuiChat.class)
public class MixinGuiChat_EmojiSuggestions {

    @Shadow
    protected GuiTextField inputField;

    private static boolean enabled() {
        return ATHRConfig.feature != null
                && ATHRConfig.feature.chat.emojiConfig.enabled
                && ATHRConfig.feature.chat.emojiConfig.suggestionsEnabled;
    }

    @Inject(method = "keyTyped", at = @At("HEAD"), cancellable = true)
    private void ATHR$onKeyTypedHead(char typedChar, int keyCode, CallbackInfo ci) {
        if (!enabled() || inputField == null) return;

        if (keyCode == Keyboard.KEY_TAB && EmojiSuggestionBar.hasSuggestion()) {
            EmojiSuggestionBar.complete(inputField);
            ci.cancel();
        }
    }

    @Inject(method = "keyTyped", at = @At("RETURN"))
    private void ATHR$onKeyTypedReturn(char typedChar, int keyCode, CallbackInfo ci) {
        if (!enabled() || inputField == null) return;
        if (keyCode == Keyboard.KEY_RETURN || keyCode == Keyboard.KEY_ESCAPE) {
            EmojiSuggestionBar.clear();
            return;
        }
        EmojiSuggestionBar.update(inputField.getText(), inputField.getCursorPosition());
    }

    @Inject(method = "mouseClicked", at = @At("HEAD"), cancellable = true)
    private void ATHR$onMouseClicked(int mouseX, int mouseY, int mouseButton, CallbackInfo ci) {
        if (!enabled() || inputField == null || mouseButton != 0) return;

        int index = EmojiSuggestionBar.hitTest(mouseX, mouseY);
        if (index >= 0) {
            EmojiSuggestionBar.complete(inputField, index);
            ci.cancel();
        }
    }

    @Inject(method = "drawScreen", at = @At("RETURN"))
    private void ATHR$drawSuggestions(int mouseX, int mouseY, float partialTicks, CallbackInfo ci) {
        if (!enabled()) return;
        EmojiSuggestionBar.render(inputField);
    }
}
