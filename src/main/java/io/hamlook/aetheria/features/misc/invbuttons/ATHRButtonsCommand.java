package io.hamlook.aetheria.features.misc.invbuttons;

import io.hamlook.aetheria.core.ATHRConfig;
import io.hamlook.aetheria.command.SimpleCommand;
import io.hamlook.aetheria.init.RegisterCommand;
import net.minecraft.command.ICommandSender;

@RegisterCommand
public class ATHRButtonsCommand extends SimpleCommand {
    @Override
    public String getName() {
        return "ATHRbuttons";
    }

    @Override
    public String getUsage() {
        return "/ATHRbuttons";
    }

    @Override
    public void execute(ICommandSender sender, String[] args) {
        ATHRConfig.openInvButtonEditor();
    }
}