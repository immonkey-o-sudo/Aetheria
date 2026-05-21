package io.hamlook.aetheria.mixins.accessors;

import net.minecraft.client.gui.FontRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(FontRenderer.class)
public interface FontRendererAccessor {

    @Accessor("posX")
    float ATHR$getPosX();

    @Accessor("posY")
    float ATHR$getPosY();
}
