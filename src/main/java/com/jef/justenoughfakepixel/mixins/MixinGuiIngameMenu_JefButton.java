package com.jef.justenoughfakepixel.mixins;

import com.jef.justenoughfakepixel.core.JefConfig;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiIngameMenu;
import net.minecraft.client.gui.GuiScreen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;

@Mixin(GuiIngameMenu.class)
public abstract class MixinGuiIngameMenu_JefButton extends GuiScreen {

    @Shadow
    protected List<GuiButton> buttonList;

    private static final int BTN_JEF = 0x4EF;

    @Inject(method = "initGui", at = @At("TAIL"))
    private void jef$addButton(CallbackInfo ci) {

        GuiIngameMenu gui = (GuiIngameMenu)(Object)this;

        int x =
                gui.width / 2 + 104;

        int y =
                gui.height / 4 + 8;

        for (Object obj : buttonList) {

            GuiButton btn = (GuiButton)obj;

            if (Math.abs(btn.xPosition - x) < 5
                    && Math.abs(btn.yPosition - y) < 24) {

                y += 24;
            }
        }

        buttonList.add(
                new GuiButton(
                        BTN_JEF,
                        x,
                        y,
                        98,
                        20,
                        "JustEnoughFakepixel"
                )
        );
    }

    @Inject(
            method = "actionPerformed",
            at = @At("HEAD"),
            cancellable = true
    )
    private void jef$actionPerformed(
            GuiButton button,
            CallbackInfo ci
    ) {

        if (button.id == BTN_JEF) {

            JefConfig.openOptionsGui();

            ci.cancel();
        }
    }
}