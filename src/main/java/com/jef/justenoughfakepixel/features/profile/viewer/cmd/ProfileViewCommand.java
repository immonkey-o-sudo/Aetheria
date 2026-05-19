package com.jef.justenoughfakepixel.features.profile.viewer.cmd;

import com.jef.justenoughfakepixel.core.JefConfig;
import com.jef.justenoughfakepixel.core.config.command.SimpleCommand;
import com.jef.justenoughfakepixel.features.profile.viewer.ui.ProfileViewerGUI;
import com.jef.justenoughfakepixel.init.RegisterCommand;
import net.minecraft.client.Minecraft;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayer;

public class ProfileViewCommand extends SimpleCommand {
    @Override
    public String getName() {
        return "pv";
    }

    @Override
    public String getUsage() {
        return "/" + getName();
    }

    @Override
    public void execute(ICommandSender sender, String[] args) throws CommandException {
        if(!(sender instanceof EntityPlayer)) return;
        if(args.length < 1){
            JefConfig.screenToOpen = new ProfileViewerGUI(Minecraft.getMinecraft().getSession().getUsername());
            return;
        }
        String user = args[0];
        JefConfig.screenToOpen = new ProfileViewerGUI(user);
    }
}
