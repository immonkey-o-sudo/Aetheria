package io.hamlook.aetheria.features.misc.ghosttracker;

import io.hamlook.aetheria.command.ASMCommand;
import io.hamlook.aetheria.core.ATHRConfig;
import io.hamlook.aetheria.init.RegisterCommand;
import io.hamlook.aetheria.utils.chat.ChatUtils;
import net.minecraft.command.ICommandSender;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumChatFormatting;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

@RegisterCommand
public class GhostTrackerCommand extends ASMCommand {

    private static final String PREFIX = EnumChatFormatting.DARK_AQUA + "[GhostTracker] " + EnumChatFormatting.RESET;

    @Override
    public String getName() {
        return "ghosttracker";
    }

    @Override
    public String getUsage() {
        return "/ghosttracker <reset|toggle>";
    }

    @Override
    public List<String> getAliases() {
        return Arrays.asList("gt", "ghost");
    }

    @Override
    public void execute(ICommandSender sender, String[] args) {
        if (args.length == 0) {
            ChatUtils.sendMessage(PREFIX + EnumChatFormatting.YELLOW + "Usage: /ghosttracker <reset|toggle>");
            return;
        }

        switch (args[0].toLowerCase()) {
            case "reset":
                GhostStats.getInstance().reset();
                ChatUtils.sendMessage(PREFIX + EnumChatFormatting.GREEN + "Ghost Tracker data reset.");
                break;
            case "toggle":
                ATHRConfig.feature.misc.ghostTrackerConfig.ghostTrackerEnabled = !ATHRConfig.feature.misc.ghostTrackerConfig.ghostTrackerEnabled;
                ATHRConfig.saveConfig();
                ChatUtils.sendMessage(PREFIX + (ATHRConfig.feature.misc.ghostTrackerConfig.ghostTrackerEnabled ? EnumChatFormatting.GREEN + "Ghost Tracker enabled." : EnumChatFormatting.RED + "Ghost Tracker disabled."));
                break;
            default:
                ChatUtils.sendMessage(PREFIX + EnumChatFormatting.RED + "Unknown subcommand. Use:reset, toggle");
        }
    }

    @Override
    public List<String> addTabCompletionOptions(ICommandSender sender, String[] args, BlockPos pos) {
        if (args.length == 1) return Arrays.asList("reset", "toggle");
        return Collections.emptyList();
    }
}
