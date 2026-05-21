package io.hamlook.aetheria.features.profile.viewer.ui.modules;

import io.hamlook.aetheria.utils.render.PlayerRenderer;

public class PlayerModule {

    public static void draw(int scaledX,int scaledY,int playerScale,String username,int mouseX,int mouseY){
        PlayerRenderer.renderPlayer(username,scaledX, scaledY, playerScale, mouseX, mouseY,false);
    }



}
