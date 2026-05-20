package com.jef.justenoughfakepixel.features.itemList;

import com.jef.justenoughfakepixel.core.JefConfig;
import com.jef.justenoughfakepixel.core.config.utils.TextRenderUtils;
import com.jef.justenoughfakepixel.core.config.gui.GuiTextures;
import com.jef.justenoughfakepixel.features.misc.SearchBar;
import com.jef.justenoughfakepixel.init.RegisterEvents;
import com.jef.justenoughfakepixel.utils.data.SkyblockData;
import com.jef.justenoughfakepixel.utils.render.ItemRenderUtils;
import com.jef.justenoughfakepixel.utils.render.NineSliceUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiTextField;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraftforge.client.event.GuiScreenEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.GL11;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@RegisterEvents
public class ItemPaneRenderer {

    private static final int S        = 18;
    private static final int PAD      = 4;
    private static final int NAV_H    = 22;
    private static final int SEARCH_H = 20;
    private static final int DROP_PAD = 4;
    private static final int DROP_H   = S + DROP_PAD * 2 + 10;

    private List<ItemFamily> filteredFamilies = new ArrayList<>();
    private GuiTextField searchField;
    private String lastSearchText = "";
    private int currentPage = 0;

    private String dropdownFamilyId = null;
    private int dropdownSlotX, dropdownSlotY;

    private int paneX, paneY, paneW, paneH;
    private int cols, rows, itemsPerPage;

    public ItemPaneRenderer() {
        updateSearch("");
    }

    private void updateSearch(String q) {
        String lq = q.toLowerCase();
        filteredFamilies = ItemRegistry.familyRegistry.values().stream()
                .filter(fam -> {
                    String name = stripColor(fam.displayName).toLowerCase();
                    if (name.contains(lq)) return true;
                    return fam.members.stream().anyMatch(i ->
                            (i.skyblockID != null && i.skyblockID.toLowerCase().contains(lq)) ||
                                    stripColor(i.displayName).toLowerCase().contains(lq));
                })
                .collect(Collectors.toList());
        currentPage = 0;
    }

    private static String stripColor(String s) {
        return s == null ? "" : s.replaceAll("§.", "");
    }

    private boolean shouldShow() {
        if (JefConfig.feature == null) return false;
        if (ItemRegistry.familyRegistry == null || ItemRegistry.familyRegistry.isEmpty()) return false;
        return SkyblockData.isOnSkyblock();
    }

    private int totalPages() {
        if (itemsPerPage <= 0) return 1;
        return Math.max(1, (int) Math.ceil((double) filteredFamilies.size() / itemsPerPage));
    }

    private void computeGeometry(int screenW, int screenH) {
        paneX = screenW / 2 + 2;
        paneY = 2;
        paneW = screenW - paneX - 2;
        paneH = screenH - 4;
        int gridH = paneH - NAV_H - SEARCH_H - PAD * 3;
        cols = Math.max(1, (paneW - PAD * 2) / S);
        rows = Math.max(1,  gridH / S);
        itemsPerPage = cols * rows;
    }

    @SubscribeEvent
    public void onDraw(GuiScreenEvent.DrawScreenEvent.Post event) {
        if (!(event.gui instanceof GuiContainer)) return;
        if (!shouldShow()) return;

        Minecraft mc = Minecraft.getMinecraft();
        computeGeometry(event.gui.width, event.gui.height);
        currentPage = Math.max(0, Math.min(currentPage, totalPages() - 1));

        int mouseX = event.mouseX, mouseY = event.mouseY;

        int sbY = paneY + paneH - SEARCH_H - PAD;
        if (searchField == null) {
            searchField = SearchBar.createStorageSearchBar(paneX + PAD, sbY, paneW - PAD * 2);
        } else {
            searchField.xPosition = paneX + PAD;
            searchField.yPosition = sbY;
        }
        String cur = SearchBar.getStorageSearchText();
        if (!cur.equals(lastSearchText)) { lastSearchText = cur; updateSearch(cur); }

        GlStateManager.color(1f, 1f, 1f, 1f);
        GlStateManager.enableBlend();
        NineSliceUtils.draw(GuiTextures.storageBackground(1), paneX, paneY, paneW, paneH, 6, 18);

        int navY  = paneY + PAD;
        int btnW  = 50;
        int prevX = paneX + PAD, nextX = paneX + paneW - PAD - btnW;
        boolean hP = mouseX >= prevX && mouseX < prevX + btnW && mouseY >= navY && mouseY < navY + NAV_H;
        boolean hN = mouseX >= nextX && mouseX < nextX + btnW && mouseY >= navY && mouseY < navY + NAV_H;

        NineSliceUtils.draw(GuiTextures.storageBackground(1), prevX, navY, btnW, NAV_H, 6, 18);
        if (hP) Gui.drawRect(prevX, navY, prevX + btnW, navY + NAV_H, 0x33FFFFFF);
        mc.fontRendererObj.drawStringWithShadow("◄ Prev",
                prevX + (btnW - mc.fontRendererObj.getStringWidth("◄ Prev")) / 2f,
                navY  + (NAV_H - mc.fontRendererObj.FONT_HEIGHT) / 2f, hP ? 0xFFFFAA : 0xFFFFFF);

        NineSliceUtils.draw(GuiTextures.storageBackground(1), nextX, navY, btnW, NAV_H, 6, 18);
        if (hN) Gui.drawRect(nextX, navY, nextX + btnW, navY + NAV_H, 0x33FFFFFF);
        mc.fontRendererObj.drawStringWithShadow("Next ►",
                nextX + (btnW - mc.fontRendererObj.getStringWidth("Next ►")) / 2f,
                navY  + (NAV_H - mc.fontRendererObj.FONT_HEIGHT) / 2f, hN ? 0xFFFFAA : 0xFFFFFF);

        String pageStr = "Page: " + (currentPage + 1) + " / " + totalPages();
        mc.fontRendererObj.drawStringWithShadow(pageStr,
                paneX + (paneW - mc.fontRendererObj.getStringWidth(pageStr)) / 2f,
                navY  + (NAV_H - mc.fontRendererObj.FONT_HEIGHT) / 2f, 0xCCCCCC);

        int gridX = paneX + PAD;
        int gridY = paneY + PAD + NAV_H + PAD;
        int gridH = rows * S;

        ScaledResolution sr = new ScaledResolution(mc);
        int sf = sr.getScaleFactor();
        GL11.glEnable(GL11.GL_SCISSOR_TEST);
        GL11.glScissor(paneX * sf, (mc.displayHeight - (gridY + gridH) * sf), paneW * sf, gridH * sf);

        int start = currentPage * itemsPerPage;
        String nowHovered = null;
        int   nowHovX = 0, nowHovY = 0;
        ItemFamily tooltipFamily = null;
        SkyblockItem tooltipItem  = null;

        for (int i = 0; i < itemsPerPage; i++) {
            int idx = start + i;
            if (idx >= filteredFamilies.size()) break;

            ItemFamily fam = filteredFamilies.get(idx);
            SkyblockItem rep = fam.representative();
            int col = i % cols, row = i / cols;
            int sx  = gridX + col * S, sy = gridY + row * S;

            GlStateManager.color(1f, 1f, 1f, 1f);
            mc.getTextureManager().bindTexture(GuiTextures.storageSlot(1));
            Gui.drawModalRectWithCustomSizedTexture(sx, sy, 0, 0, S, S, S, S);

            if (rep != null && rep.getStack() != null)
                ItemRenderUtils.drawItemStack(rep.getStack(), sx + 1, sy + 1);

            if (fam.hasDropdown()) {
                Gui.drawRect(sx + S - 4, sy + S - 4, sx + S - 1, sy + S - 1, 0xFFFFDD44);
            }

            boolean hovered = mouseX >= sx && mouseX < sx + S && mouseY >= sy && mouseY < sy + S;
            if (hovered) {
                GlStateManager.enableBlend();
                GlStateManager.disableDepth();
                Gui.drawRect(sx, sy, sx + S, sy + S, 0x80FFFFFF);
                GlStateManager.enableDepth();
                nowHovered = fam.familyId;
                nowHovX = sx; nowHovY = sy;
                if (!fam.hasDropdown()) tooltipItem = rep;
                else                    tooltipFamily = fam;
            }
        }

        GL11.glDisable(GL11.GL_SCISSOR_TEST);

        if (nowHovered != null) {
            dropdownFamilyId = nowHovered;
            dropdownSlotX = nowHovX;
            dropdownSlotY = nowHovY;
        } else if (!isMouseOverDropdown(mouseX, mouseY)) {
            dropdownFamilyId = null;
        }

        searchField.updateCursorCounter();
        SearchBar.drawStorageSearchBar(searchField);

        SkyblockItem dropdownTooltipItem = null;
        if (dropdownFamilyId != null) {
            ItemFamily dropFam = ItemRegistry.familyRegistry.get(dropdownFamilyId);
            if (dropFam != null && dropFam.hasDropdown()) {
                dropdownTooltipItem = drawDropdown(mc, dropFam, mouseX, mouseY);
            }
        }

        SkyblockItem tipItem = dropdownTooltipItem != null ? dropdownTooltipItem : tooltipItem;
        if (tipItem != null) {
            List<String> tip = new ArrayList<>();
            tip.add(tipItem.displayName);
            if (tipItem.baseLore != null) tip.addAll(tipItem.baseLore);
            TextRenderUtils.drawHoveringText(tip, mouseX, mouseY, mc.fontRendererObj);
        } else if (tooltipFamily != null) {
            List<String> tip = new ArrayList<>();
            tip.add(tooltipFamily.displayName);
            tip.add("§7" + tooltipFamily.members.size() + " variants – hover to expand");
            TextRenderUtils.drawHoveringText(tip, mouseX, mouseY, mc.fontRendererObj);
        }
    }

    private SkyblockItem drawDropdown(Minecraft mc, ItemFamily fam, int mouseX, int mouseY) {
        int members = fam.members.size();
        int dropW   = members * (S + 2) + DROP_PAD * 2;
        int dropH   = DROP_H;

        int dx = dropdownSlotX;
        int dy = dropdownSlotY + S + 2;
        if (dx + dropW > paneX + paneW) dx = paneX + paneW - dropW - 2;
        if (dy + dropH > paneY + paneH) dy = dropdownSlotY - dropH - 2;

        GlStateManager.color(1f, 1f, 1f, 1f);
        NineSliceUtils.draw(GuiTextures.storageBackground(1), dx, dy, dropW, dropH, 6, 18);

        SkyblockItem hovered = null;
        for (int i = 0; i < members; i++) {
            SkyblockItem mem = fam.members.get(i);
            int sx = dx + DROP_PAD + i * (S + 2);
            int sy = dy + DROP_PAD;

            mc.getTextureManager().bindTexture(GuiTextures.storageSlot(1));
            Gui.drawModalRectWithCustomSizedTexture(sx, sy, 0, 0, S, S, S, S);
            if (mem.getStack() != null)
                ItemRenderUtils.drawItemStack(mem.getStack(), sx + 1, sy + 1);

            String label = mem.familyMemberLabel != null ? mem.familyMemberLabel : "";
            int lw = mc.fontRendererObj.getStringWidth(label);
            mc.fontRendererObj.drawStringWithShadow(label, sx + (S - lw) / 2f, sy + S + 1, 0xCCCCCC);

            boolean h = mouseX >= sx && mouseX < sx + S && mouseY >= sy && mouseY < sy + S;
            if (h) {
                GlStateManager.enableBlend();
                GlStateManager.disableDepth();
                Gui.drawRect(sx, sy, sx + S, sy + S, 0x80FFFFFF);
                GlStateManager.enableDepth();
                hovered = mem;
            }
        }
        return hovered;
    }

    private boolean isMouseOverDropdown(int mx, int my) {
        if (dropdownFamilyId == null) return false;
        ItemFamily fam = ItemRegistry.familyRegistry.get(dropdownFamilyId);
        if (fam == null) return false;
        int members = fam.members.size();
        int dropW = members * (S + 2) + DROP_PAD * 2;
        int dropH = DROP_H;
        int dx = dropdownSlotX;
        int dy = dropdownSlotY + S + 2;
        if (dx + dropW > paneX + paneW) dx = paneX + paneW - dropW - 2;
        if (dy + dropH > paneY + paneH) dy = dropdownSlotY - dropH - 2;
        return mx >= dx && mx < dx + dropW && my >= dy && my < dy + dropH;
    }

    @SubscribeEvent
    public void onMouse(GuiScreenEvent.MouseInputEvent.Pre event) {
        if (!(event.gui instanceof GuiContainer)) return;
        if (!shouldShow()) return;

        Minecraft mc = Minecraft.getMinecraft();
        int mouseX = Mouse.getEventX() * event.gui.width / mc.displayWidth;
        int mouseY = event.gui.height - Mouse.getEventY() * event.gui.height / mc.displayHeight - 1;

        int dw = Mouse.getEventDWheel();
        if (dw != 0 && mouseX >= paneX && mouseX < paneX + paneW) {
            if (dw > 0) currentPage = Math.max(0, currentPage - 1);
            else        currentPage = Math.min(totalPages() - 1, currentPage + 1);
            return;
        }

        if (!Mouse.getEventButtonState() || Mouse.getEventButton() != 0) return;

        int navY = paneY + PAD, btnW = 50;
        int prevX = paneX + PAD, nextX = paneX + paneW - PAD - btnW;
        if (mouseX >= prevX && mouseX < prevX + btnW && mouseY >= navY && mouseY < navY + NAV_H) {
            currentPage = Math.max(0, currentPage - 1); event.setCanceled(true); return;
        }
        if (mouseX >= nextX && mouseX < nextX + btnW && mouseY >= navY && mouseY < navY + NAV_H) {
            currentPage = Math.min(totalPages() - 1, currentPage + 1); event.setCanceled(true); return;
        }

        if (searchField != null && SearchBar.handleStorageMouseClick(searchField, mouseX, mouseY)) {
            event.setCanceled(true); return;
        }

        if (dropdownFamilyId != null) {
            ItemFamily fam = ItemRegistry.familyRegistry.get(dropdownFamilyId);
            if (fam != null && fam.hasDropdown()) {
                int members = fam.members.size();
                int dropW   = members * (S + 2) + DROP_PAD * 2;
                int dx      = dropdownSlotX;
                int dy      = dropdownSlotY + S + 2;
                if (dx + dropW > paneX + paneW) dx = paneX + paneW - dropW - 2;
                if (dy + DROP_H > paneY + paneH) dy = dropdownSlotY - DROP_H - 2;

                for (int i = 0; i < members; i++) {
                    int sx = dx + DROP_PAD + i * (S + 2), sy = dy + DROP_PAD;
                    if (mouseX >= sx && mouseX < sx + S && mouseY >= sy && mouseY < sy + S) {
                        mc.displayGuiScreen(new RecipeViewerGUI(fam.members.get(i)));
                        event.setCanceled(true); return;
                    }
                }
            }
        }

        int gridX = paneX + PAD, gridY = paneY + PAD + NAV_H + PAD;
        for (int i = 0; i < itemsPerPage; i++) {
            int idx = currentPage * itemsPerPage + i;
            if (idx >= filteredFamilies.size()) break;
            ItemFamily fam = filteredFamilies.get(idx);
            int sx = gridX + (i % cols) * S, sy = gridY + (i / cols) * S;
            if (mouseX >= sx && mouseX < sx + S && mouseY >= sy && mouseY < sy + S) {
                if (!fam.hasDropdown() && fam.representative() != null) {
                    mc.displayGuiScreen(new RecipeViewerGUI(fam.representative()));
                }
                event.setCanceled(true); return;
            }
        }
    }

    @SubscribeEvent
    public void onKey(GuiScreenEvent.KeyboardInputEvent.Pre event) {
        if (!(event.gui instanceof GuiContainer)) return;
        if (!shouldShow()) return;
        if (searchField == null || !searchField.isFocused()) return;
        if (!Keyboard.getEventKeyState()) return;
        char ch = Keyboard.getEventCharacter();
        int key = Keyboard.getEventKey();
        if (SearchBar.handleStorageKeyTyped(searchField, ch, key)) {
            updateSearch(SearchBar.getStorageSearchText());
            event.setCanceled(true);
        }
    }
}