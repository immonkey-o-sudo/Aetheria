package com.jef.justenoughfakepixel.features.profile.vars;

public enum ProfileMode {
    NORMAL,
    IRONMAN;


    public String getName(){
        return this == NORMAL ? "§aNormal" : "§eIronMan";
    }
}
