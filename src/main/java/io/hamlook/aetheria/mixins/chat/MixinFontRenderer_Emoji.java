package io.hamlook.aetheria.mixins.chat;

import io.hamlook.aetheria.core.ATHRConfig;
import io.hamlook.aetheria.features.chat.emoji.EmojiManager;
import io.hamlook.aetheria.utils.render.RenderUtils;
import net.minecraft.client.gui.FontRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Swaps ":name:" chat tokens (Discord/Slack-style shortcodes) for the matching
 * emoji texture wherever text is drawn through FontRenderer - chat log, tab
 * list, signs, etc. This is a purely local render swap: nothing is sent over
 * the network, so a player without the mod (or with this feature off) just
 * sees the literal ":name:" text like normal.
 */
@Mixin(FontRenderer.class)
public abstract class MixinFontRenderer_Emoji {

    private static final Pattern EMOJI_PATTERN = Pattern.compile(":([a-zA-Z0-9_]{2,}):");

    @Shadow
    protected abstract int renderString(String text, float x, float y, int color, boolean dropShadow);

    @Shadow
    protected abstract void resetStyles();

    @Shadow
    protected abstract void enableAlpha();

    @Inject(method = "drawString(Ljava/lang/String;FFIZ)I", at = @At("HEAD"), cancellable = true)
    private void ATHR$drawStringWithEmojis(String text, float x, float y, int color, boolean dropShadow, CallbackInfoReturnable<Integer> cir) {
        if (ATHRConfig.feature == null || !ATHRConfig.feature.chat.emojiConfig.enabled) return;
        if (!EmojiManager.isLoaded() || text == null || !text.contains(":")) return;

        Matcher matcher = EMOJI_PATTERN.matcher(text);
        if (!matcher.find()) return;
        matcher.reset();

        FontRenderer fr = (FontRenderer) (Object) this;
        float scale = ATHRConfig.feature.chat.emojiConfig.scale;
        float cursorX = x;
        int lastEnd = 0;

        while (matcher.find()) {
            cursorX = drawPlain(fr, text.substring(lastEnd, matcher.start()), cursorX, y, color, dropShadow);

            String key = matcher.group(1);
            float size = fr.FONT_HEIGHT * scale;
            float emojiY = y - (size - fr.FONT_HEIGHT) / 2f;

            if (EmojiManager.exists(key) && RenderUtils.drawEmoji(key, cursorX, emojiY, size)) {
                resetStyles();
                enableAlpha();
                cursorX += size + 1;
            } else {
                cursorX = drawPlain(fr, matcher.group(), cursorX, y, color, dropShadow);
            }

            lastEnd = matcher.end();
        }

        if (lastEnd < text.length()) {
            cursorX = drawPlain(fr, text.substring(lastEnd), cursorX, y, color, dropShadow);
        }

        cir.setReturnValue((int) cursorX);
        cir.cancel();
    }

    // Mirrors what the vanilla drawString(...) we cancelled would have done for a
    // plain (non-emoji) segment: a darkened offset copy for the shadow, then the
    // real text on top.
    private float drawPlain(FontRenderer fr, String segment, float x, float y, int color, boolean dropShadow) {
        if (segment.isEmpty()) return x;
        resetStyles();
        enableAlpha();
        if (dropShadow) {
            renderString(segment, x + 1, y + 1, color, true);
            renderString(segment, x, y, color, false);
        } else {
            renderString(segment, x, y, color, false);
        }
        return x + fr.getStringWidth(segment);
    }
}
