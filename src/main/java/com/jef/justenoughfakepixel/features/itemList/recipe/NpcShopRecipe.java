package com.jef.justenoughfakepixel.features.itemList.recipe;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.jef.justenoughfakepixel.features.itemList.SkyblockItem;
import com.jef.justenoughfakepixel.utils.render.ItemRenderUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.ScaledResolution;
import org.lwjgl.opengl.GL11;

import java.util.List;

public class NpcShopRecipe extends Recipe {
    private final List<JsonObject> recipes;
    private static final int COLS = 7;
    private static final int ROWS = 5;
    private static final int S    = 18;

    public NpcShopRecipe(SkyblockItem t, List<JsonObject> recipes) {
        super(t);
        this.recipes = recipes;
    }

    @Override public String typeLabel()      { return "§9NPC Shop"; }
    @Override public int    typeLabelColor() { return 0x55FFFF; }

    @Override
    public int[] preferredSize() {
        return new int[]{ COLS * S + 20, ROWS * S + 10 };
    }

    @Override
    public void draw(int mouseX, int mouseY, int x, int y, int width, int height,
                     int scrollY, FontRenderer fr, GuiScreen gui) {
        int gridW = COLS * S;
        int gridH = ROWS * S;
        int gridX = x + (width  - gridW) / 2;
        int gridY = y + (height - gridH) / 2;

        ScaledResolution sr = new ScaledResolution(Minecraft.getMinecraft());
        int sf = sr.getScaleFactor();
        GL11.glEnable(GL11.GL_SCISSOR_TEST);
        GL11.glScissor(gridX * sf, (Minecraft.getMinecraft().displayHeight - (gridY + gridH) * sf), gridW * sf, gridH * sf);

        for (int i = 0; i < COLS * ROWS; i++) {
            int col = i % COLS, row = i / COLS;
            int sx = gridX + col * S, sy = gridY + row * S;

            RecipeUtils.drawSlot(sx, sy, S);

            if (i >= recipes.size()) continue;

            JsonObject recipe = recipes.get(i);
            if (!recipe.has("result")) continue;

            String resultRaw = recipe.get("result").getAsString();
            String[] rp = resultRaw.split(":");
            String rAmt = rp.length > 1 ? rp[1] : "1";
            SkyblockItem resultItem = RecipeUtils.resolve(rp[0]);

            if (resultItem != null && resultItem.getStack() != null) {
                ItemRenderUtils.drawItemStack(resultItem.getStack(), sx + 1, sy + 1);
                // RecipeUtils.drawAmount(fr, rAmt, sx, sy); // assuming drawAmount exists or we can just skip it if it doesn't
            }
        }

        GL11.glDisable(GL11.GL_SCISSOR_TEST);
    }

    @Override
    public List<String> getTooltip(int mouseX, int mouseY, int x, int y,
                                   int width, int height, int scrollY) {
        int gridW = COLS * S, gridH = ROWS * S;
        int gridX = x + (width  - gridW) / 2;
        int gridY = y + (height - gridH) / 2;

        for (int i = 0; i < COLS * ROWS; i++) {
            int col = i % COLS, row = i / COLS;
            int sx = gridX + col * S, sy = gridY + row * S;
            if (mouseX < sx || mouseX >= sx + S || mouseY < sy || mouseY >= sy + S) continue;

            if (i < recipes.size()) {
                JsonObject recipe = recipes.get(i);
                if (!recipe.has("result")) return null;
                String[] rp = recipe.get("result").getAsString().split(":");
                SkyblockItem rItem = RecipeUtils.resolve(rp[0]);
                String rAmt = rp.length > 1 ? rp[1] : "1";
                if (rItem == null) return null;

                List<String> tip = RecipeUtils.buildItemTooltipWithAmount(rItem, rAmt);
                tip.add("§8---------------");
                if (recipe.has("cost") && recipe.get("cost").isJsonArray()) {
                    JsonArray costArr = recipe.get("cost").getAsJsonArray();
                    for (int c = 0; c < costArr.size(); c++) {
                        String[] cp = costArr.get(c).getAsString().split(":");
                        String rId = cp[0], cAmt = cp.length > 1 ? cp[1] : "1";
                        if (rId.equals("SKYBLOCK_COIN")) {
                            tip.add("§7Cost: §6" + cAmt + " Coins");
                        } else {
                            SkyblockItem costItem = RecipeUtils.resolve(rId);
                            if (costItem != null) tip.add("§7Cost: " + costItem.displayName + " §8x" + cAmt);
                        }
                    }
                }
                return tip;
            }
        }
        return null;
    }
}