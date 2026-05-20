package com.jef.justenoughfakepixel.features.itemList.recipe;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.jef.justenoughfakepixel.features.itemList.SkyblockItem;
import com.jef.justenoughfakepixel.utils.render.ItemRenderUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.ScaledResolution;
import org.lwjgl.opengl.GL11;
import java.util.ArrayList;
import java.util.List;

public class DropsRecipe extends Recipe {
    private final List<JsonObject> recipes;
    private static final int COLS = 7;
    private static final int S    = 18;
    private List<JsonObject> sortedDrops;

    public DropsRecipe(SkyblockItem t, List<JsonObject> recipes) {
        super(t);
        this.recipes = recipes;
    }

    @Override public String typeLabel()      { return "§6Mob Drops"; }
    @Override public int    typeLabelColor() { return 0xFFAA00; }

    @Override
    public int[] preferredSize() {
        int n = getSortedDrops().size();
        int rows = (int) Math.ceil((double) n / COLS);
        return new int[]{ COLS * S + 20, Math.max(rows, 1) * S + 10 };
    }

    private List<JsonObject> getSortedDrops() {
        if (sortedDrops != null) return sortedDrops;
        sortedDrops = new ArrayList<>();
        if (recipes.isEmpty() || !recipes.get(0).has("drops")) return sortedDrops;
        JsonArray arr = recipes.get(0).get("drops").getAsJsonArray();
        for (JsonElement el : arr) sortedDrops.add(el.getAsJsonObject());
        sortedDrops.sort((a, b) -> Float.compare(
                RecipeUtils.parseChance(b.has("chance") ? b.get("chance").getAsString() : "0"),
                RecipeUtils.parseChance(a.has("chance") ? a.get("chance").getAsString() : "0")));
        return sortedDrops;
    }

    private String mobName() {
        return recipes.isEmpty() ? "Unknown"
                : recipes.get(0).has("name") ? recipes.get(0).get("name").getAsString() : "Unknown";
    }

    @Override
    public void draw(int mouseX, int mouseY, int x, int y, int width, int height,
                     int scrollY, FontRenderer fr, GuiScreen gui) {
        List<JsonObject> drops = getSortedDrops();
        int rows = (int) Math.ceil((double) drops.size() / COLS);
        int gridW = COLS * S;
        int gridH = rows * S;
        int gridX = x + (width  - gridW) / 2;
        int gridY = y + (height - gridH) / 2;

        ScaledResolution sr = new ScaledResolution(Minecraft.getMinecraft());
        int sf = sr.getScaleFactor();
        GL11.glEnable(GL11.GL_SCISSOR_TEST);
        GL11.glScissor(x * sf, (Minecraft.getMinecraft().displayHeight - (y + height) * sf), width * sf, height * sf);

        for (int i = 0; i < drops.size(); i++) {
            JsonObject drop = drops.get(i);
            String dropId = drop.has("id") ? drop.get("id").getAsString().split(":")[0] : "";
            SkyblockItem dropItem = RecipeUtils.resolve(dropId);

            int col = i % COLS, row = i / COLS;
            int sx = gridX + col * S, sy = gridY + row * S - scrollY;

            if (sy + S < y || sy > y + height) continue;

            RecipeUtils.drawSlot(sx, sy, S);
            if (dropItem != null && dropItem.getStack() != null)
                ItemRenderUtils.drawItemStack(dropItem.getStack(), sx + 1, sy + 1);
        }

        GL11.glDisable(GL11.GL_SCISSOR_TEST);
    }

    @Override
    public List<String> getTooltip(int mouseX, int mouseY, int x, int y,
                                   int width, int height, int scrollY) {
        List<JsonObject> drops = getSortedDrops();
        int rows = (int) Math.ceil((double) drops.size() / COLS);
        int gridW = COLS * S;
        int gridH = rows * S;
        int gridX = x + (width  - gridW) / 2;
        int gridY = y + (height - gridH) / 2;

        for (int i = 0; i < drops.size(); i++) {
            int col = i % COLS, row = i / COLS;
            int sx = gridX + col * S, sy = gridY + row * S - scrollY;
            if (mouseX < sx || mouseX >= sx + S || mouseY < sy || mouseY >= sy + S) continue;

            JsonObject drop = drops.get(i);
            String dropId = drop.has("id") ? drop.get("id").getAsString().split(":")[0] : "";
            SkyblockItem dropItem = RecipeUtils.resolve(dropId);
            if (dropItem == null) return null;

            String rawChance = drop.has("chance") ? drop.get("chance").getAsString() : "100%";
            String fmt = RecipeUtils.formatChance(rawChance);
            List<String> tip = RecipeUtils.buildItemTooltip(dropItem);
            tip.add("§8---------------");
            tip.add("§7Chance: " + RecipeUtils.getChanceColor(fmt) + fmt);
            return tip;
        }
        return null;
    }
}