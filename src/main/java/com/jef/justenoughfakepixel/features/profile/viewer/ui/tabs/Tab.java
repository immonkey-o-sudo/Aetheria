package com.jef.justenoughfakepixel.features.profile.viewer.ui.tabs;

import com.jef.justenoughfakepixel.features.profile.data.ProfileData;
import lombok.AllArgsConstructor;
import net.minecraft.client.Minecraft;

@AllArgsConstructor
public abstract class Tab {

    public int tabIndex;
    public String name;
    public abstract void draw(float xPos, float yPos, int width, int height, ProfileData data, Minecraft mc);

}