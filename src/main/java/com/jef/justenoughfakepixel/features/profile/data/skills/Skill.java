package com.jef.justenoughfakepixel.features.profile.data.skills;

import lombok.AllArgsConstructor;

import java.awt.*;

@AllArgsConstructor
public enum Skill {

    FARMING("Farming",19,new Color(255,255,0)),
    MINING("Mining",20,new Color(0, 190, 234)),
    COMBAT("Combat",21,new Color(150, 0, 0)),
    FORAGING("Foraging",22,new Color(25, 120, 25)),
    FISHING("Fishing",23,new Color(0, 223, 0)),
    ENCHANTING("Enchanting",24,new Color(134, 0, 237)),
    ALCHEMY("Alchemy",25,new Color(200, 60, 0)),
    RUNECRAFTING("Runecrafting",29,new Color(200, 150, 0)),
    SOCIAL("Social",30,new Color(0, 223, 0)),
    TAMING("Taming",32,new Color(211, 211, 211)),
    CARPENTRY("Carpentry",33,new Color(255, 200, 0));

    public final String name;
    public final int slot;
    public final Color skillColor;
    public static Skill get(String s) {
        for(Skill skill : Skill.values()) {
            if(skill.name.equalsIgnoreCase(s)) return skill;
        }
        return null;
    }
}
