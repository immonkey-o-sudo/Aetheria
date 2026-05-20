package com.jef.justenoughfakepixel.features.itemList;

import com.jef.justenoughfakepixel.core.config.gui.GuiTextures;
import com.jef.justenoughfakepixel.core.config.utils.TextRenderUtils;
import com.jef.justenoughfakepixel.features.itemList.recipe.Recipe;
import com.jef.justenoughfakepixel.features.itemList.recipe.RecipeFactory;
import com.jef.justenoughfakepixel.utils.render.ItemRenderUtils;
import com.jef.justenoughfakepixel.utils.render.NineSliceUtils;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.item.ItemStack;
import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;

import java.io.IOException;
import java.util.List;

public class RecipeViewerGUI extends GuiScreen {

    private static final int MIN_W = 220;
    private static final int MAX_W = 360;
    private static final int MIN_H = 130;
    private static final int MAX_H = 260;
    static final int HEADER_H = 36;
    static final int NAV_H = 20;

    private final SkyblockItem item;
    private final List<Recipe>  recipes;
    private int recipeIndex = 0;
    private int scrollY     = 0;

    private int boxX, boxY, boxW, boxH;

    public RecipeViewerGUI(SkyblockItem item) {
        this.item    = item;
        this.recipes = RecipeFactory.build(item);
    }

    @Override public void initGui()      { Keyboard.enableRepeatEvents(true);  recipeIndex = 0; scrollY = 0; }
    @Override public void onGuiClosed() { Keyboard.enableRepeatEvents(false); }
    @Override public boolean doesGuiPauseGame() { return false; }

    private void computeBox() {
        int contentW, contentH;
        if (!recipes.isEmpty()) {
            Recipe r = recipes.get(recipeIndex);
            int[] sz = r.preferredSize();
            contentW = sz[0];
            contentH = sz[1];
        } else {
            contentW = 160;
            contentH = 80;
        }
        boxW = Math.max(MIN_W, Math.min(MAX_W, contentW + 20));
        boxH = Math.max(MIN_H, Math.min(MAX_H, HEADER_H + contentH + NAV_H + 10));
        boxX = (width  - boxW) / 2;
        boxY = (height - boxH) / 2;
    }

    @Override
    protected void keyTyped(char c, int key) throws IOException {
        if (key == Keyboard.KEY_ESCAPE || key == mc.gameSettings.keyBindInventory.getKeyCode())
            mc.displayGuiScreen(null);
    }

    @Override
    protected void mouseClicked(int mx, int my, int btn) throws IOException {
        if (recipes.size() <= 1) return;
        int arrowY = boxY + boxH - NAV_H;
        if (my >= arrowY && my <= arrowY + NAV_H) {
            if (mx >= boxX + 10 && mx <= boxX + 30) {
                recipeIndex = (recipeIndex - 1 + recipes.size()) % recipes.size();
                scrollY = 0;
            } else if (mx >= boxX + boxW - 30 && mx <= boxX + boxW - 10) {
                recipeIndex = (recipeIndex + 1) % recipes.size();
                scrollY = 0;
            }
        }
    }

    @Override
    public void handleMouseInput() throws IOException {
        super.handleMouseInput();
        int dw = Mouse.getEventDWheel();
        if (dw != 0) { scrollY += dw > 0 ? -20 : 20; if (scrollY < 0) scrollY = 0; }
    }


    @Override
    public void drawScreen(int mx, int my, float pt) {
        drawRect(0, 0, width, height, 0xA0000000);
        computeBox();

        NineSliceUtils.draw(GuiTextures.storageBackground(1), boxX, boxY, boxW, boxH, 6, 18);

        int cx = boxX + boxW / 2;

        if (recipes.isEmpty()) {
            // No recipe – just show icon + name + lore
            ItemStack stack = item.getStack();
            String nameStr = item.displayName != null ? item.displayName : "";
            int nameW = fontRendererObj.getStringWidth(nameStr);
            int rowW = (stack != null ? 18 + 4 : 0) + nameW;
            int rowX = cx - rowW / 2;
            int rowY = boxY + 12;
            
            if (stack != null) {
                ItemRenderUtils.drawItemStack(stack, rowX, rowY - 4);
                rowX += 22;
            }
            fontRendererObj.drawStringWithShadow(nameStr, rowX, rowY, 0xFFFFFF);
            
            if (item.baseLore != null) {
                int ly = boxY + 32;
                for (int i = 0; i < Math.min(item.baseLore.size(), 8); i++) {
                    drawCenteredString(fontRendererObj, item.baseLore.get(i), cx, ly, 0xFFFFFF);
                    ly += 10;
                }
            }
        } else {
            Recipe current = recipes.get(recipeIndex);

            // Single row: [TypeLabel] [Icon] [Item Name]
            String typeLabel = current.typeLabel();
            String nameStr  = item.displayName != null ? item.displayName : "";
            ItemStack stack = item.getStack();
            
            int typeW = fontRendererObj.getStringWidth(typeLabel);
            int nameW = fontRendererObj.getStringWidth(nameStr);
            int iconW = stack != null ? 18 : 0;
            
            // Layout: type (gap 4) icon (gap 4) name
            int totalW = typeW + (iconW > 0 ? 4 + iconW : 0) + 4 + nameW;
            int startX = cx - totalW / 2;
            int currentX = startX;
            int textY = boxY + 12;
            
            fontRendererObj.drawStringWithShadow(typeLabel, currentX, textY, current.typeLabelColor());
            currentX += typeW + 4;
            
            if (stack != null) {
                ItemRenderUtils.drawItemStack(stack, currentX, textY - 4);
                currentX += 18 + 4;
            }
            fontRendererObj.drawStringWithShadow(nameStr, currentX, textY, 0xFFFFFF);

            // content area (below header, above nav bar)
            int contentX = boxX;
            int contentY = boxY + HEADER_H;
            int contentH = boxH - HEADER_H - NAV_H;
            current.draw(mx, my, contentX, contentY, boxW, contentH, scrollY, fontRendererObj, this);

            if (recipes.size() > 1) {
                int arrowY = boxY + boxH - NAV_H + 4;
                drawCenteredString(fontRendererObj, "<", boxX + 20, arrowY, 0xFFFFFF);
                drawCenteredString(fontRendererObj, ">", boxX + boxW - 20, arrowY, 0xFFFFFF);
                drawCenteredString(fontRendererObj, "§7" + (recipeIndex + 1) + " / " + recipes.size(), cx, arrowY, 0xFFFFFF);
            }

            List<String> tip = current.getTooltip(mx, my, contentX, contentY, boxW, contentH, scrollY);
            if (tip != null && !tip.isEmpty()) {
                TextRenderUtils.drawHoveringText(tip, mx, my, fontRendererObj);
            }
        }

        super.drawScreen(mx, my, pt);
    }
}