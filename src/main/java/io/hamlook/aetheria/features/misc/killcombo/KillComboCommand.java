package io.hamlook.aetheria.features.misc.killcombo;

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
public class KillComboCommand extends ASMCommand {

    private static final String PREFIX = EnumChatFormatting.GOLD + "" + EnumChatFormatting.BOLD + "[KillCombo] " + EnumChatFormatting.RESET;

    @Override
    public String getName() {
        return "killcombo";
    }

    @Override
    public String getUsage() {
        return "/killcombo <reset|toggle>";
    }

    @Override
    public List<String> getAliases() {
        return Collections.singletonList("kc");
    }

    @Override
    public void execute(ICommandSender sender, String[] args) {
        if (args.length == 0) {
            ChatUtils.sendMessage(PREFIX + EnumChatFormatting.YELLOW + "Usage: /kc <reset|toggle>");
            return;
        }

        switch (args[0].toLowerCase()) {
            case "reset":
                KillComboTracker tracker = KillComboTracker.getInstance();
                tracker.reset();
                ChatUtils.sendMessage(PREFIX + EnumChatFormatting.GREEN + "Kill Combo data reset.");
                break;

            case "toggle":
                ATHRConfig.feature.misc.killCombo.enabled = !ATHRConfig.feature.misc.killCombo.enabled;
                ATHRConfig.saveConfig();
                boolean nowEnabled = ATHRConfig.feature.misc.killCombo.enabled;
                ChatUtils.sendMessage(PREFIX + (nowEnabled ? EnumChatFormatting.GREEN + "Kill Combo tracker enabled." : EnumChatFormatting.RED + "Kill Combo tracker disabled."));
                break;

            default:
                ChatUtils.sendMessage(PREFIX + EnumChatFormatting.RED + "Unknown subcommand. Use: reset, toggle");
        }
    }

    @Override
    public List<String> addTabCompletionOptions(ICommandSender sender, String[] args, BlockPos pos) {
        if (args.length == 1) return Arrays.asList("reset", "toggle");
        return Collections.emptyList();
    }
}
