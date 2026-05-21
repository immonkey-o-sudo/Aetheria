package io.hamlook.aetheria.mixins;

import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.inventory.Slot;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(GuiContainer.class)
public interface MixinGuiContainer {


    @Invoker("isMouseOverSlot")
    boolean invokeIsMouseOverSlot(Slot slot, int mouseX, int mouseY);

    @Invoker("drawSlot")
    void invokeDrawSlot(Slot slot);

    @Accessor("guiLeft")
    int getGuiLeft();

    @Accessor("guiTop")
    int getGuiTop();
}
