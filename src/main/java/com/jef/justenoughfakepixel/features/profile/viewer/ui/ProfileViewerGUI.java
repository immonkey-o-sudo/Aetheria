package com.jef.justenoughfakepixel.features.profile.viewer.ui;

import com.jef.justenoughfakepixel.core.config.gui.GuiTextures;
import com.jef.justenoughfakepixel.features.profile.viewer.PlayerProfile;
import com.jef.justenoughfakepixel.features.profile.viewer.ProfileViewerAPI;
import com.jef.justenoughfakepixel.utils.render.NineSliceUtils;
import com.jef.justenoughfakepixel.utils.render.ResolutionUtils;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.util.ResourceLocation;

public class ProfileViewerGUI extends GuiScreen {

    // UI Data
    private static final ResourceLocation CONTAINER_BG = GuiTextures.CAPES_UI;

    // Player Data
    public String activeProfile, username;
    public PlayerProfile playerProfile;

    // State Trackers
    public boolean isFetching = true;
    public boolean hasError = false;

    public ProfileViewerGUI(String username) {
        this.username = username;
        this.activeProfile = "";

        new Thread(() -> {
            try {
                if (ProfileViewerAPI.profileHashMap.containsKey(username)) {
                    this.playerProfile = ProfileViewerAPI.profileHashMap.get(username);
                } else {
                    this.playerProfile = ProfileViewerAPI.fetchUser(username);
                    if (this.playerProfile != null) {
                        ProfileViewerAPI.profileHashMap.put(username, this.playerProfile);
                    }
                }

                if (this.playerProfile != null && this.playerProfile.profiles != null && !this.playerProfile.profiles.isEmpty()) {
                    this.activeProfile = this.playerProfile.profiles.get(0).baseData.playerProfile;
                }
            } catch (Exception e) {
                e.printStackTrace();
                this.hasError = true;
            } finally {
                this.isFetching = false;
            }
        }, "JEF-GUI-FetchThread").start();
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {

        int boxW = (int) ResolutionUtils.getXStatic(900);
        int boxH = (int) ResolutionUtils.getYStatic(600);
        int boxX = (this.width / 2) - (boxW / 2);
        int boxY = (this.height / 2) - (boxH / 2);

        NineSliceUtils.draw(CONTAINER_BG, boxX, boxY, boxW, boxH, 6, 18);

        int centerX = boxX + (boxW / 2);
        int centerY = boxY + (boxH / 2);

        if (isFetching) {
            String text = "Fetching data...";
            int textWidth = fontRendererObj.getStringWidth(text);
            drawString(fontRendererObj, text, centerX - (textWidth / 2), centerY - (fontRendererObj.FONT_HEIGHT / 2), 0xFFFFAA00); // Yellow/Orange

        } else if (hasError) {
            String text = "An error occurred while fetching!";
            int textWidth = fontRendererObj.getStringWidth(text);
            drawString(fontRendererObj, text, centerX - (textWidth / 2), centerY - (fontRendererObj.FONT_HEIGHT / 2), 0xFFFF5555); // Light Red

        } else if (this.playerProfile == null) {
            String text = this.username + " (Not In Database)";
            int textWidth = fontRendererObj.getStringWidth(text);
            drawString(fontRendererObj, text, centerX - (textWidth / 2), centerY - (fontRendererObj.FONT_HEIGHT / 2), 0xFFAAAAAA); // Gray

        } else {
            drawString(fontRendererObj, this.playerProfile.player_name + " (Fetched)", boxX + 5, boxY + 5, 0xFF55FF55); // Green

            // TODO: Draw the rest of fetched profile UI

        }

        super.drawScreen(mouseX, mouseY, partialTicks);
    }
}