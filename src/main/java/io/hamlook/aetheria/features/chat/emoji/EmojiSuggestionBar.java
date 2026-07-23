package io.hamlook.aetheria.features.chat.emoji;

import io.hamlook.aetheria.core.ATHRConfig;
import io.hamlook.aetheria.core.moulconfig.editors.ChromaColour;
import io.hamlook.aetheria.utils.render.RenderUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.GuiTextField;

import java.awt.*;
import java.util.Collections;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class EmojiSuggestionBar {

    private static final int MAX_SUGGESTIONS = 7;
    private static final int MIN_LETTERS = 2;
    private static final int ICON_SIZE = 10;
    private static final int CELL_SIZE = 18;
    private static final int GAP = 2;
    private static final int PAD = 4;

    private static final Pattern PARTIAL_TOKEN = Pattern.compile("(?:^|\\s):([a-zA-Z0-9_]{" + MIN_LETTERS + ",})$");

    private static int tokenStart = -1;
    private static List<String> matches = Collections.emptyList();
    private static int boxX, boxY, boxW, boxH;

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

    public static void complete(GuiTextField inputField, int index) {
        if (index < 0 || index >= matches.size() || tokenStart < 0) return;

        String text = inputField.getText();
        int cursor = inputField.getCursorPosition();
        if (cursor > text.length() || tokenStart > cursor) {
            clear();
            return;
        }

        String replacement = ":" + matches.get(index) + ": ";
        String newText = text.substring(0, tokenStart) + replacement + text.substring(cursor);
        inputField.setText(newText);
        inputField.setCursorPosition(tokenStart + replacement.length());
        clear();
    }

    public static void render(GuiTextField inputField, int mouseX, int mouseY) {
        if (matches.isEmpty() || inputField == null) return;

        int count = matches.size();
        boxW = PAD * 2 + count * CELL_SIZE + (count - 1) * GAP;
        boxH = PAD * 2 + CELL_SIZE;
        boxX = inputField.xPosition;
        boxY = inputField.yPosition - boxH - 2;

        int border = ChromaColour.specialToChromaRGB(ATHRConfig.feature.chat.emojiConfig.suggestionBarBorder);
        Gui.drawRect(boxX,boxY,boxX+boxW,boxY+boxH, ChromaColour.specialToChromaRGB(ATHRConfig.feature.chat.emojiConfig.suggestionBarBG));
        if(ATHRConfig.feature.chat.emojiConfig.suggestionsBar) {
            Gui.drawRect(boxX, boxY, boxX + boxW, boxY + 1, border);
            Gui.drawRect(boxX, boxY + boxH - 1, boxX + boxW, boxY + boxH, border);
            Gui.drawRect(boxX, boxY, boxX + 1, boxY + boxH, border);
            Gui.drawRect(boxX + boxW - 1, boxY, boxX + boxW, boxY + boxH, border);
        }
        int hovered = hitTest(mouseX, mouseY);

        for (int i = 0; i < count; i++) {
            String name = matches.get(i);
            int cellX = boxX + PAD + i * (CELL_SIZE + GAP);
            int cellY = boxY + PAD;

            if (i == hovered) {
                Gui.drawRect(cellX, cellY, cellX + CELL_SIZE, cellY + CELL_SIZE, 0x40FFFFFF);
            }

            RenderUtils.drawEmoji(name, cellX + (CELL_SIZE - ICON_SIZE) / 2f, cellY + (CELL_SIZE - ICON_SIZE) / 2f, ICON_SIZE);
        }

        if (hovered >= 0) {
            String tooltip = ":" + matches.get(hovered) + ":";
            FontRenderer fr = Minecraft.getMinecraft().fontRendererObj;
            int tw = fr.getStringWidth(tooltip);
            int tx = boxX + (boxW - tw) / 2;
            int ty = boxY - 11;
            Gui.drawRect(tx - 2, ty - 1, tx + tw + 2, ty + 9, 0xCC222222);
            fr.drawString(tooltip, tx, ty, 0xCCCCCC);
        }
    }

    public static int hitTest(int mouseX, int mouseY) {
        if (matches.isEmpty()) return -1;
        if (mouseX < boxX || mouseX > boxX + boxW || mouseY < boxY || mouseY > boxY + boxH) return -1;

        int relX = mouseX - (boxX + PAD);
        int slot = relX / (CELL_SIZE + GAP);
        if (slot < 0 || slot >= matches.size()) return -1;
        int slotX = boxX + PAD + slot * (CELL_SIZE + GAP);
        if (mouseX < slotX || mouseX > slotX + CELL_SIZE) return -1;
        return slot;
    }
}
