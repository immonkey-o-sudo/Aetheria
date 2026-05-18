package com.jef.justenoughfakepixel.features.profile.data.slayer;

import lombok.AllArgsConstructor;

import java.awt.*;

@AllArgsConstructor
public enum Slayer {

    ZOMBIE("Zombie Slayer",9,new Color(25, 120, 25)),
    SPIDER("Spider Slayer",11,new Color(150, 0, 0)),
    WOLF("Wolf Slayer",13,new Color(230, 230, 230)),
    ENDERMAN("Enderman Slayer",15,new Color(134, 0, 237)),
    BLAZE("Blaze Slayer",17,new Color(200, 60, 0))
    ;
    public final String itemName;
    public final int itemSlot;
    public final Color guiColor;
}
