package io.hamlook.aetheria.features.storage.data;

import com.google.gson.annotations.SerializedName;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.JsonToNBT;
import net.minecraft.nbt.NBTTagCompound;

public class ItemData {

    @SerializedName("d")
    public String data;

    @SerializedName("n")
    public String displayName;

    public ItemData() {
    }

    public static ItemData fromItemStack(ItemStack stack) {
        if (stack == null) return null;

        ItemData item = new ItemData();

        NBTTagCompound compound = new NBTTagCompound();
        stack.writeToNBT(compound);
        item.data = compound.toString();

        item.displayName = stack.getDisplayName();

        return item;
    }

    public ItemStack toItemStack() {
        if (data == null || data.isEmpty()) return null;

        try {
            NBTTagCompound nbt = JsonToNBT.getTagFromJson(data);
            return ItemStack.loadItemStackFromNBT(nbt);
        } catch (Exception e) {
            return null;
        }
    }
}
