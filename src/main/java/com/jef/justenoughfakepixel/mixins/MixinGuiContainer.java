package com.jef.justenoughfakepixel.mixins;

import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.inventory.Slot;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.gen.Invoker;

/**
 * Mixin to access protected methods in GuiContainer
 * Allows the storage overlay to interact with chest slots
 */
@Mixin(GuiContainer.class)
public interface MixinGuiContainer {

    /**
     * Access the protected isMouseOverSlot method
     */
    @Invoker("isMouseOverSlot")
    boolean invokeIsMouseOverSlot(Slot slot, int mouseX, int mouseY);

    /**
     * Access the protected drawSlot method to render slots on the overlay
     */
    @Invoker("drawSlot")
    void invokeDrawSlot(Slot slot);

    /**
     * Access guiLeft field
     */
    @Accessor("guiLeft")
    int getGuiLeft();

    /**
     * Access guiTop field
     */
    @Accessor("guiTop")
    int getGuiTop();
}
