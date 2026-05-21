package io.hamlook.aetheria.command;

import io.hamlook.aetheria.core.ATHRConfig;
import io.hamlook.aetheria.utils.chat.ChatUtils;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import org.apache.commons.lang3.StringUtils;

import java.util.Collections;
import java.util.List;

public class Command extends SimpleCommand {

    @Override
    public String getName() { return "ATHR"; }

    @Override
    public String getUsage() { return "/ATHR | /ATHR config | /ATHR <category> | /ATHR reload"; }

    @Override
    public List<String> getAliases() { return Collections.singletonList("aetheria"); }

    @Override
    public void execute(ICommandSender sender, String[] args) throws CommandException {
        if (args.length > 0 && args[0].equalsIgnoreCase("reload")) {
            ATHRConfig.reloadRepo();
            ChatUtils.sendMessage("§a[ATHR] §fRepo refresh triggered.");
        } else if (args.length > 0 && args[0].equalsIgnoreCase("config")) {
            ATHRConfig.openGui();
        } else if (args.length == 0) {
            ATHRConfig.openOptionsGui();
        } else {
            ATHRConfig.openCategory(StringUtils.join(args, " "));
        }
    }
}