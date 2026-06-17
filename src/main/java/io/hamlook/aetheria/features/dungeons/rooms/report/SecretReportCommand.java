package io.hamlook.aetheria.features.dungeons.rooms.report;

import io.hamlook.aetheria.command.ASMCommand;
import io.hamlook.aetheria.core.ATHRConfig;
import io.hamlook.aetheria.features.dungeons.rooms.DungeonRoom;
import io.hamlook.aetheria.features.dungeons.rooms.DungeonRoomDetector;
import io.hamlook.aetheria.init.RegisterCommand;
import io.hamlook.aetheria.utils.chat.ChatUtils;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;

@RegisterCommand
public class SecretReportCommand extends ASMCommand {

    @Override
    public String getName() {
        return "report-secret";
    }

    @Override
    public String getUsage() {
        return "/" + getName();
    }

    @Override
    public void execute(ICommandSender sender, String[] args) throws CommandException {
        DungeonRoom room = DungeonRoomDetector.getCurrentRoom();
        if(room == null){
            ChatUtils.sendMessage("§cYou can only send secret-report in a valid Dungeon room!");
            return;
        }
        ATHRConfig.screenToOpen = new SecretReportGUI(room);
    }
}
