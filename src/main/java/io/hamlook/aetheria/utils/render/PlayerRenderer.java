package io.hamlook.aetheria.utils.render;

import io.hamlook.aetheria.features.capes.Cape;
import io.hamlook.aetheria.features.capes.CapeManager;
import io.hamlook.aetheria.features.profile.viewer.SkinManager;
import com.mojang.authlib.GameProfile;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.AbstractClientPlayer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.util.ResourceLocation;
import org.lwjgl.opengl.GL11;

import java.util.HashMap;
import java.util.UUID;

public class PlayerRenderer {

    public static HashMap<String,AbstractClientPlayer> cachedModels = new HashMap<>();

    public static void renderPlayer(String username,int posX,int posY,int scale,float mX,float mY){
        renderPlayer(username,posX,posY,scale,mX,mY,true);
    }

    public static void renderPlayer(String username,int posX,int posY,int scale,float mX,float mY,boolean nametag){
        AbstractClientPlayer player;
        if(cachedModels.containsKey(username)){
            player = cachedModels.get(username);
        }else {
            player = new AbstractClientPlayer(Minecraft.getMinecraft().theWorld,
                    new GameProfile(UUID.nameUUIDFromBytes((username).getBytes()), username)) {
                @Override
                public ResourceLocation getLocationSkin() {
                    return SkinManager.getSkin(username);
                }

                @Override
                public ResourceLocation getLocationCape() {
                    Cape cape = CapeManager.getCapeForPlayer(username);
                    return cape == null ? super.getLocationCape() : cape.resourceLocation;
                }
            };
            cachedModels.put(username,player);
        }
        if(!nametag){
            player.posX = 9999999.0D;
            player.posY = 9999999.0D;
            player.posZ = 9999999.0D;
        }
        drawEntityOnScreenSmooth(posX,posY,scale,mX,mY,player);
    }
    public static void renderPlayer(AbstractClientPlayer player, int posX, int posY, int scale, float mX, float mY){
        drawEntityOnScreenSmooth(posX,posY,scale,mX,mY,player);
    }

    public static void drawEntityOnScreenSmooth(int posX, int posY, int scale, float mouseX, float mouseY, EntityLivingBase ent) {
        GlStateManager.enableColorMaterial();
        GlStateManager.pushMatrix();
        GlStateManager.translate((float)posX, (float)posY, 50.0F);
        GlStateManager.scale((float)(-scale), (float)scale, (float)scale);
        GlStateManager.rotate(180.0F, 0.0F, 0.0F, 1.0F);

        float f = (float)posX - mouseX;
        float f1 = (float)posY - 50.0F - mouseY;

        GlStateManager.rotate(135.0F, 0.0F, 1.0F, 0.0F);
        RenderHelper.enableStandardItemLighting();
        GlStateManager.rotate(-135.0F, 0.0F, 1.0F, 0.0F);

        GlStateManager.rotate(-((float)Math.atan((f / 40.0F))) * 20.0F, 0.0F, 1.0F, 0.0F);
        ent.renderYawOffset = ((float)Math.atan((f / 40.0F)) * 20.0F);
        ent.rotationYaw = ((float)Math.atan((f / 40.0F)) * 40.0F);
        ent.rotationPitch = -((float)Math.atan((f1 / 40.0F))) * 20.0F;
        ent.rotationYawHead = ent.rotationYaw;
        ent.prevRotationYawHead = ent.rotationYaw;

        GlStateManager.translate(0.0F, 0.0F, 0.0F);
        RenderManager rendermanager = Minecraft.getMinecraft().getRenderManager();
        rendermanager.setPlayerViewY(180.0F);
        rendermanager.setRenderShadow(false);

        GlStateManager.clear(GL11.GL_DEPTH_BUFFER_BIT);
        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);

        rendermanager.renderEntityWithPosYaw(ent, 0.0D, 0.0D, 0.0D, 0.0F, 1.0F);
        rendermanager.setRenderShadow(true);
        ent.renderYawOffset = 0;
        ent.rotationYaw = 0;
        ent.rotationPitch = 0;
        ent.prevRotationYawHead = 0;
        ent.rotationYawHead = 0;
        GlStateManager.popMatrix();
        RenderHelper.disableStandardItemLighting();
        GlStateManager.disableRescaleNormal();
        GlStateManager.setActiveTexture(OpenGlHelper.lightmapTexUnit);
        GlStateManager.disableTexture2D();
        GlStateManager.setActiveTexture(OpenGlHelper.defaultTexUnit);
    }

}
