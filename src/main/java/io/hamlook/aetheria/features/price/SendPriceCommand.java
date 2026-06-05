package io.hamlook.aetheria.features.price;

import io.hamlook.aetheria.command.SimpleCommand;
import io.hamlook.aetheria.init.RegisterCommand;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;

@RegisterCommand
public class SendPriceCommand extends SimpleCommand {
    @Override
    public String getName() {
        return "sendprice";
    }

    @Override
    public String getUsage() {
        return "/" + getName();
    }

    @Override
    public void execute(ICommandSender sender, String[] args) throws CommandException {
        PriceDetector.sendNow();
    }
}
