package io.hamlook.aetheria.features.chat.emoji;

import io.hamlook.aetheria.utils.render.RenderUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.GuiTextField;

import java.util.Collections;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Tracks the ":partial" emoji shortcode the player is currently typing in the
 * chat box and renders a popup with up to {@link #MAX_SUGGESTIONS} matching
 * emoji above the input field. Typing 3+ letters after an un-closed leading
 * colon triggers it; Tab or a click completes with a match.
 */
public class EmojiSuggestionBar {

    private static final int MAX_SUGGESTIONS = 5;
    private static final int MIN_LETTERS = 3;
    private static final int ROW_H = 14;
    private static final int ICON_SIZE = 10;

    // An un-closed ":name" token: the colon sits at the start of the message or
    // right after whitespace (so timestamps/URLs like "10:30" don't trigger it),
    // followed by 3+ word characters, right up to the cursor.
    private static final Pattern PARTIAL_TOKEN = Pattern.compile("(?:^|\\s):([a-zA-Z0-9_]{" + MIN_LETTERS + ",})$");

    private static int tokenStart = -1; // index of the ':' in the input text
    private static List<String> matches = Collections.emptyList();

    // Bounds of the last rendered popup, cached for click hit-testing.
    private static int boxX, boxY, boxW;

    private EmojiSuggestionBar() {
    }

    public static void update(String text, int cursor) {
        if (text == null || cursor < 0 || cursor > text.length()) {
            clear();
            return;
        }

        Matcher m = PARTIAL_TOKEN.matcher(text.substring(0, cursor));
        if (!m.find()) {
            clear();
            return;
        }

        String partial = m.group(1);
        tokenStart = m.start() + (m.group().startsWith(":") ? 0 : 1);
        matches = EmojiManager.search(partial, MAX_SUGGESTIONS);
        if (matches.isEmpty()) clear();
    }

    public static void clear() {
        tokenStart = -1;
        matches = Collections.emptyList();
    }

    public static boolean hasSuggestion() {
        return !matches.isEmpty();
    }

    public static void complete(GuiTextField inputField) {
        complete(inputField, 0);
    }

    // Replaces the ":partial" the player typed with the full ":name: " of the
    // chosen match and moves the cursor to just after it.
    public static void complete(GuiTextField inputField, int index) {
        if (index < 0 || index >= matches.size() || tokenStart < 0) return;

        String text = inputField.getText();
        int cursor = inputField.getCursorPosition();
        if (tokenStart > text.length() || cursor > text.length() || tokenStart > cursor) {
            clear();
            return;
        }

        String replacement = ":" + matches.get(index) + ": ";
        String newText = text.substring(0, tokenStart) + replacement + text.substring(cursor);
        inputField.setText(newText);
        inputField.setCursorPosition(tokenStart + replacement.length());
        clear();
    }

    public static void render(GuiTextField inputField) {
        if (matches.isEmpty() || inputField == null) return;

        FontRenderer fr = Minecraft.getMinecraft().fontRendererObj;

        int width = 100;
        for (String name : matches) {
            width = Math.max(width, fr.getStringWidth(":" + name + ":") + ICON_SIZE + 16);
        }
        int height = matches.size() * ROW_H + 4;

        boxX = inputField.xPosition;
        boxY = inputField.yPosition - height - 2;
        boxW = width;

        RenderUtils.drawFloatingRectDark(boxX, boxY, boxW, height);

        int rowY = boxY + 2;
        for (String name : matches) {
            RenderUtils.drawEmoji(name, boxX + 4, rowY + 1, ICON_SIZE);
            fr.drawStringWithShadow(":" + name + ":", boxX + ICON_SIZE + 8, rowY + 3, 0xFFFFFF);
            rowY += ROW_H;
        }
    }

    // Returns the match index the given screen coords land on, or -1 if the
    // popup isn't showing or the click missed it. Only valid right after a
    // render() call in the same frame (uses the cached box bounds).
    public static int hitTest(int mouseX, int mouseY) {
        if (matches.isEmpty()) return -1;
        int height = matches.size() * ROW_H + 4;
        if (mouseX < boxX || mouseX > boxX + boxW || mouseY < boxY || mouseY > boxY + height) return -1;

        int index = (mouseY - (boxY + 2)) / ROW_H;
        return (index >= 0 && index < matches.size()) ? index : -1;
    }
}
