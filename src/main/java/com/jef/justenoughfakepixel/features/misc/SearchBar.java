package com.jef.justenoughfakepixel.features.misc;

import com.jef.justenoughfakepixel.core.JefConfig;
import com.jef.justenoughfakepixel.features.storage.StorageManager;
import com.jef.justenoughfakepixel.init.RegisterEvents;
import lombok.Getter;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiTextField;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.gui.inventory.GuiChest;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.client.gui.inventory.GuiInventory;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraftforge.client.event.GuiScreenEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.util.*;

@RegisterEvents
public class SearchBar {

    private static final Minecraft MC = Minecraft.getMinecraft();

    private static final DecimalFormat CALC_FORMAT = new DecimalFormat("#,##0.##########");
    private static final Set<Character> CALC_SYMBOLS = new HashSet<>(Arrays.asList('+', '-', '*', '/', 'x', '(', ')'));

    private static final SearchBar INSTANCE = new SearchBar();
    private static final int BAR_WIDTH = 170;
    private static final int BAR_HEIGHT = 20;
    private static GuiTextField searchBar;
    private static String searchText = "";
    private static String lastCalcInput = "";
    private static String lastCalcResult = null;

    private static GuiTextField storageSearchBar;
    @Getter
    private static String storageSearchText = "";

    public static SearchBar getInstance() {
        return INSTANCE;
    }

    public static String getSearchText() {
        return isCalcMode() ? "" : searchText;
    }

    public static boolean isCalcMode() {
        for (char c : searchText.toCharArray())
            if (CALC_SYMBOLS.contains(c)) return true;
        return false;
    }

    public static GuiTextField createStorageSearchBar(int x, int y, int width) {
        storageSearchBar = new GuiTextField(1, MC.fontRendererObj, x, y, width, BAR_HEIGHT);
        storageSearchBar.setCanLoseFocus(false);
        storageSearchBar.setMaxStringLength(50);
        storageSearchBar.setEnableBackgroundDrawing(false);
        storageSearchBar.setFocused(true);
        storageSearchBar.setText(storageSearchText);
        return storageSearchBar;
    }

    public static void drawStorageSearchBar(GuiTextField field) {
        if (field == null) return;
        com.jef.justenoughfakepixel.core.config.utils.RenderUtils.drawSearchBar(field, true);
        storageSearchText = field.getText();
    }

    public static boolean handleStorageKeyTyped(GuiTextField field, char typedChar, int keyCode) {
        if (field == null) return false;
        boolean consumed = field.textboxKeyTyped(typedChar, keyCode);
        storageSearchText = field.getText();
        return consumed;
    }

    public static boolean handleStorageMouseClick(GuiTextField field, int mouseX, int mouseY) {
        if (field == null) return false;
        field.mouseClicked(mouseX, mouseY, 0);
        return mouseX >= field.xPosition && mouseX <= field.xPosition + field.width && mouseY >= field.yPosition && mouseY <= field.yPosition + field.height;
    }

    private static boolean isEnabled() {
        return JefConfig.feature != null && JefConfig.feature.misc.searchBar;
    }

    private static boolean isSupportedGui(Object gui) {
        return gui instanceof GuiInventory || gui instanceof GuiChest;
    }

    private static void drawSearchBar(GuiTextField field, String text) {
        String suffix = calcSuffix(text);
        boolean useGoldTexture = suffix != null;

        if (useGoldTexture) {
            int x = field.xPosition, y = field.yPosition;
            int w = field.width, h = field.height;

            GlStateManager.color(1f, 1f, 1f, 1f);
            com.jef.justenoughfakepixel.core.config.utils.RenderUtils.drawSearchBar(createTempFieldWithText(field, text + " " + suffix), true, true);
        } else {
            com.jef.justenoughfakepixel.core.config.utils.RenderUtils.drawSearchBar(field, true, false);
        }
    }

    private static GuiTextField createTempFieldWithText(GuiTextField original, String text) {
        GuiTextField temp = new GuiTextField(original.getId(), MC.fontRendererObj, original.xPosition, original.yPosition, original.width, original.height);
        temp.setText(text);
        temp.setFocused(original.isFocused());
        temp.setCursorPosition(original.getCursorPosition());
        return temp;
    }

    private static String calcSuffix(String text) {
        if (text == null || text.isEmpty()) return null;
        if (!text.equals(lastCalcInput)) {
            lastCalcInput = text;
            try {
                lastCalcResult = CALC_FORMAT.format(Calculator.calculate(text));
            } catch (Calculator.CalculatorException ignored) {
                lastCalcResult = null;
            }
        }
        return lastCalcResult == null ? null : "§e= §a" + lastCalcResult;
    }

    public int getOverlayWidth() {
        return BAR_WIDTH;
    }

    public int getOverlayHeight() {
        return BAR_HEIGHT;
    }

    public void render(boolean preview) {
        ScaledResolution sr = new ScaledResolution(MC);
        com.jef.justenoughfakepixel.core.config.utils.Position pos = JefConfig.feature.misc.searchBarPos;
        int x = pos.getAbsX(sr, BAR_WIDTH);
        int y = pos.getAbsY(sr, BAR_HEIGHT);
        if (pos.isCenterX()) x -= BAR_WIDTH / 2;
        if (pos.isCenterY()) y -= BAR_HEIGHT / 2;

        Gui.drawRect(x, y, x + BAR_WIDTH, y + BAR_HEIGHT, 0xFF2C2C2C);
        Gui.drawRect(x + 1, y + 1, x + BAR_WIDTH - 1, y + BAR_HEIGHT - 1, 0xFF111111);
        MC.fontRendererObj.drawStringWithShadow("Search...", x + 5, y + BAR_HEIGHT / 2 - 4, 0x8F8F8F);
    }

    @SubscribeEvent
    public void onGuiInit(GuiScreenEvent.InitGuiEvent.Post event) {
        if (!isEnabled() || !isSupportedGui(event.gui)) return;

        int w = BAR_WIDTH, h = BAR_HEIGHT;

        ScaledResolution sr = new ScaledResolution(MC);
        com.jef.justenoughfakepixel.core.config.utils.Position pos = JefConfig.feature.misc.searchBarPos;
        int x = pos.getAbsX(sr, w);
        int y = pos.getAbsY(sr, h);
        if (pos.isCenterX()) x -= w / 2;
        if (pos.isCenterY()) y -= h / 2;

        searchBar = new GuiTextField(0, MC.fontRendererObj, x, y, w, h);
        searchBar.setCanLoseFocus(false);
        searchBar.setMaxStringLength(100);
        searchBar.setEnableBackgroundDrawing(false);
        searchBar.setFocused(false);
        searchBar.setText(searchText);
    }

    @SubscribeEvent
    public void onKeyboardInput(GuiScreenEvent.KeyboardInputEvent.Pre event) {
        if (!isEnabled() || !(event.gui instanceof GuiContainer)) return;
        if (searchBar == null || !searchBar.isFocused()) return;
        if (!Keyboard.getEventKeyState()) return;
        if (Keyboard.getEventKey() == Keyboard.KEY_ESCAPE) return;

        if (searchBar.textboxKeyTyped(Keyboard.getEventCharacter(), Keyboard.getEventKey())) {
            searchText = searchBar.getText();
            event.setCanceled(true);
        }
    }

    @SubscribeEvent
    public void onMouseInput(GuiScreenEvent.MouseInputEvent.Pre event) {
        if (!isEnabled() || !(event.gui instanceof GuiContainer)) return;
        if (searchBar == null || !Mouse.getEventButtonState()) return;

        int mouseX = Mouse.getEventX() * event.gui.width / MC.displayWidth;
        int mouseY = event.gui.height - Mouse.getEventY() * event.gui.height / MC.displayHeight - 1;

        boolean inside = mouseX >= searchBar.xPosition && mouseX <= searchBar.xPosition + searchBar.width && mouseY >= searchBar.yPosition && mouseY <= searchBar.yPosition + searchBar.height;

        searchBar.setFocused(inside);
        if (inside) searchBar.mouseClicked(mouseX, mouseY, Mouse.getEventButton());
    }

    @SubscribeEvent
    public void onDrawGui(GuiScreenEvent.DrawScreenEvent.Post event) {
        if (!isEnabled() || !isSupportedGui(event.gui) || searchBar == null) return;

        if (StorageManager.isOverlayActive()) {
            return;
        }

        drawSearchBar(searchBar, searchBar.getText());
    }

    public static class Calculator {

        private static final String BINOPS = "+-*/x";
        private static final String POSTOPS = "mkbts";
        private static final String DIGITS = "0123456789";

        public static BigDecimal calculate(String source) throws CalculatorException {
            return evaluate(shuntingYard(lex(source.toLowerCase(Locale.ROOT))));
        }

        private static void readDigitsInto(Token token, String source, boolean decimals) {
            int start = token.tokenStart + token.tokenLength;
            for (int j = 0; j + start < source.length(); j++) {
                int d = DIGITS.indexOf(source.charAt(j + start));
                if (d == -1) return;
                if (decimals) token.exponent--;
                token.numericValue = token.numericValue * 10 + d;
                token.tokenLength++;
            }
        }

        public static List<Token> lex(String source) throws CalculatorException {
            List<Token> tokens = new ArrayList<>();
            for (int i = 0; i < source.length(); ) {
                char c = source.charAt(i);
                if (Character.isWhitespace(c)) {
                    i++;
                    continue;
                }

                Token t = new Token();
                t.tokenStart = i;

                if (BINOPS.indexOf(c) != -1) {
                    t.tokenLength = 1;
                    t.type = TokenType.BINOP;
                    t.operatorValue = String.valueOf(c);
                } else if (POSTOPS.indexOf(c) != -1) {
                    t.tokenLength = 1;
                    t.type = TokenType.POSTOP;
                    t.operatorValue = String.valueOf(c);
                } else if (c == ')') {
                    t.tokenLength = 1;
                    t.type = TokenType.RPAREN;
                    t.operatorValue = ")";
                } else if (c == '(') {
                    t.tokenLength = 1;
                    t.type = TokenType.LPAREN;
                    t.operatorValue = "(";
                } else if (c == '.') {
                    t.tokenLength = 1;
                    t.type = TokenType.NUMBER;
                    readDigitsInto(t, source, true);
                    if (t.tokenLength == 1) throw new CalculatorException("Invalid number literal", i, 1);
                } else if (DIGITS.indexOf(c) != -1) {
                    t.type = TokenType.NUMBER;
                    readDigitsInto(t, source, false);
                    if (i + t.tokenLength < source.length() && source.charAt(i + t.tokenLength) == '.') {
                        t.tokenLength++;
                        readDigitsInto(t, source, true);
                    }
                } else {
                    throw new CalculatorException("Unknown character: " + c, i, 1);
                }

                tokens.add(t);
                i += t.tokenLength;
            }
            return tokens;
        }

        private static int getPrecedence(Token t) throws CalculatorException {
            switch (t.operatorValue) {
                case "+":
                case "-":
                    return 0;
                case "*":
                case "/":
                case "x":
                    return 1;
                default:
                    throw new CalculatorException("Unknown operator " + t.operatorValue, t.tokenStart, t.tokenLength);
            }
        }

        public static List<Token> shuntingYard(List<Token> tokens) throws CalculatorException {
            Deque<Token> op = new ArrayDeque<>();
            List<Token> out = new ArrayList<>();

            for (Token t : tokens) {
                switch (t.type) {
                    case NUMBER:
                    case POSTOP:
                        out.add(t);
                        break;
                    case BINOP:
                        int p = getPrecedence(t);
                        while (!op.isEmpty() && op.peek().type != TokenType.LPAREN && getPrecedence(op.peek()) >= p)
                            out.add(op.pop());
                        op.push(t);
                        break;
                    case LPAREN:
                        op.push(t);
                        break;
                    case RPAREN:
                        while (true) {
                            if (op.isEmpty())
                                throw new CalculatorException("Unbalanced right parenthesis", t.tokenStart, t.tokenLength);
                            Token l = op.pop();
                            if (l.type == TokenType.LPAREN) break;
                            out.add(l);
                        }
                        break;
                }
            }
            while (!op.isEmpty()) {
                Token l = op.pop();
                if (l.type == TokenType.LPAREN)
                    throw new CalculatorException("Unbalanced left parenthesis", l.tokenStart, l.tokenLength);
                out.add(l);
            }
            return out;
        }

        public static BigDecimal evaluate(List<Token> rpn) throws CalculatorException {
            Deque<BigDecimal> stack = new ArrayDeque<>();
            try {
                for (Token t : rpn) {
                    switch (t.type) {
                        case NUMBER:
                            stack.push(new BigDecimal(t.numericValue).scaleByPowerOfTen(t.exponent));
                            break;
                        case BINOP: {
                            BigDecimal r = stack.pop();
                            BigDecimal l = stack.pop();
                            switch (t.operatorValue) {
                                case "x":
                                case "*":
                                    stack.push(l.multiply(r).setScale(2, RoundingMode.HALF_UP));
                                    break;
                                case "/":
                                    try {
                                        BigDecimal result = l.divide(r, 10, RoundingMode.HALF_UP).stripTrailingZeros();
                                        stack.push(result.scale() < 2 ? result.setScale(2) : result);
                                    } catch (ArithmeticException e) {
                                        throw new CalculatorException("Division by zero", t.tokenStart, t.tokenLength);
                                    }
                                    break;
                                case "+":
                                    stack.push(l.add(r).setScale(2, RoundingMode.HALF_UP));
                                    break;
                                case "-":
                                    stack.push(l.subtract(r).setScale(2, RoundingMode.HALF_UP));
                                    break;
                                default:
                                    throw new CalculatorException("Unknown operator " + t.operatorValue, t.tokenStart, t.tokenLength);
                            }
                            break;
                        }
                        case POSTOP: {
                            BigDecimal v = stack.pop();
                            switch (t.operatorValue) {
                                case "s":
                                    stack.push(v.multiply(new BigDecimal(64)));
                                    break;
                                case "k":
                                    stack.push(v.multiply(new BigDecimal(1_000)));
                                    break;
                                case "m":
                                    stack.push(v.multiply(new BigDecimal(1_000_000)));
                                    break;
                                case "b":
                                    stack.push(v.multiply(new BigDecimal(1_000_000_000)));
                                    break;
                                case "t":
                                    stack.push(v.multiply(new BigDecimal("1000000000000")));
                                    break;
                                default:
                                    throw new CalculatorException("Unknown postop " + t.operatorValue, t.tokenStart, t.tokenLength);
                            }
                            break;
                        }
                        default:
                            throw new CalculatorException("Unexpected token", t.tokenStart, t.tokenLength);
                    }
                }
                return stack.pop().stripTrailingZeros();
            } catch (NoSuchElementException e) {
                throw new CalculatorException("Unfinished expression", 0, 0);
            }
        }

        public enum TokenType {NUMBER, BINOP, LPAREN, RPAREN, POSTOP}

        public static class Token {
            public TokenType type;
            String operatorValue;
            long numericValue;
            int exponent, tokenStart, tokenLength;
        }

        public static class CalculatorException extends Exception {
            int offset, length;

            public CalculatorException(String message, int offset, int length) {
                super(message);
                this.offset = offset;
                this.length = length;
            }
        }
    }
}