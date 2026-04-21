package com.jef.justenoughfakepixel.features.storage.data;

import com.google.gson.annotations.SerializedName;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompressedStreamTools;
import net.minecraft.nbt.NBTTagCompound;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Base64;

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

        try {
            NBTTagCompound compound = new NBTTagCompound();
            stack.writeToNBT(compound);
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            CompressedStreamTools.writeCompressed(compound, outputStream);
            item.data = Base64.getEncoder().encodeToString(outputStream.toByteArray());
        } catch (IOException e) {
            return null;
        }

        item.displayName = stack.getDisplayName();

        return item;
    }

    public ItemStack toItemStack() {
        if (data == null || data.isEmpty()) return null;

        try {
            byte[] bytes = Base64.getDecoder().decode(data);
            NBTTagCompound nbt = CompressedStreamTools.readCompressed(new ByteArrayInputStream(bytes));
            return ItemStack.loadItemStackFromNBT(nbt);
        } catch (Exception e) {
            return null;
        }
    }
}
