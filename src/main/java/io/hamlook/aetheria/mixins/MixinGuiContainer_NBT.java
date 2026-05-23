package io.hamlook.aetheria.mixins;

import io.hamlook.aetheria.core.ATHRConfig;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.EnumChatFormatting;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(GuiContainer.class)
public class MixinGuiContainer_NBT {

    @Shadow
    private Slot theSlot;

    @Inject(method = "keyTyped", at = @At("HEAD"))
    private void onKeyTyped(char typedChar, int keyCode, CallbackInfo ci) {
        if (keyCode == ATHRConfig.feature.debug.copyNBTKey && ATHRConfig.feature.debug.copyNBTData) {
            if (this.theSlot != null && this.theSlot.getHasStack()) {
                ItemStack stack = this.theSlot.getStack();

                if (stack.hasTagCompound()) {
                    String prettyNbt = justEnoughfakepixel$formatNBT(stack.getTagCompound(), 2);

                    GuiScreen.setClipboardString(prettyNbt);

                    Minecraft.getMinecraft().thePlayer.addChatMessage(
                            new ChatComponentText(EnumChatFormatting.GREEN + "Copied NBT to clipboard!")
                    );
                } else {
                    Minecraft.getMinecraft().thePlayer.addChatMessage(
                            new ChatComponentText(EnumChatFormatting.RED + "This item has no NBT data.")
                    );
                }
            }
        }
    }

    @Unique
    private static String justEnoughfakepixel$formatNBT(NBTBase nbt, int indent) {
        StringBuilder builder = new StringBuilder();
        String spaces = new String(new char[indent]).replace("\0", "  ");

        if (nbt instanceof NBTTagCompound) {
            NBTTagCompound compound = (NBTTagCompound) nbt;
            builder.append("{\n");
            for (String key : compound.getKeySet()) {
                NBTBase tag = compound.getTag(key);
                builder.append(spaces).append("  \"").append(key).append("\": ");
                builder.append(justEnoughfakepixel$formatNBT(tag, indent + 1)).append(",\n");
            }
            builder.append(spaces).append("}");
        }
        else if (nbt instanceof NBTTagList) {
            NBTTagList list = (NBTTagList) nbt;
            builder.append("[\n");
            for (int i = 0; i < list.tagCount(); i++) {
                builder.append(spaces).append("  ");
                builder.append(justEnoughfakepixel$formatNBT(list.get(i), indent + 1)).append(",\n");
            }
            builder.append(spaces).append("]");
        }
        else {
            builder.append(nbt.toString());
        }

        return builder.toString().replaceAll(",\n" +
                "\\s*([]}])", "\n$1");
    }

}
