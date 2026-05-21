package io.hamlook.aetheria.features.profile.vars;

public enum ProfileMode {
    NORMAL,
    IRONMAN;


    public String getName(){
        return this == NORMAL ? "§aNormal" : "§eIronMan";
    }
}
