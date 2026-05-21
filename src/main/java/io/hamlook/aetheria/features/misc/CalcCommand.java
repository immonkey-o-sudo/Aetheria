package io.hamlook.aetheria.features.misc;

import io.hamlook.aetheria.command.SimpleCommand;
import io.hamlook.aetheria.init.RegisterCommand;
import io.hamlook.aetheria.utils.CalculatorUtils;
import io.hamlook.aetheria.utils.chat.ChatUtils;
import net.minecraft.command.ICommandSender;
import net.minecraft.util.EnumChatFormatting;

@RegisterCommand
public class CalcCommand extends SimpleCommand {

    private static final String PREFIX = EnumChatFormatting.AQUA + "[ATHR] " + EnumChatFormatting.RESET;

    @Override
    public String getName() {
        return "ATHRcalc";
    }

    @Override
    public String getUsage() {
        return "/ATHRcalc <expression>";
    }

    @Override
    public void execute(ICommandSender sender, String[] args) {
        if (args.length == 0) {
            ChatUtils.sendMessage(PREFIX + EnumChatFormatting.RED + "Usage: /ATHRcalc <expression>");
            ChatUtils.sendMessage(PREFIX + EnumChatFormatting.GRAY + "Basic Examples:");
            ChatUtils.sendMessage(PREFIX + EnumChatFormatting.GRAY + "  /ATHRcalc 2 + 2");
            ChatUtils.sendMessage(PREFIX + EnumChatFormatting.GRAY + "  /ATHRcalc 100k * 5");
            ChatUtils.sendMessage(PREFIX + EnumChatFormatting.GRAY + "  /ATHRcalc (10 + 5) * 2");
            ChatUtils.sendMessage(PREFIX + EnumChatFormatting.GRAY + "  /ATHRcalc 2^10");
            ChatUtils.sendMessage(PREFIX + EnumChatFormatting.GRAY + "Advanced Examples:");
            ChatUtils.sendMessage(PREFIX + EnumChatFormatting.GRAY + "  /ATHRcalc sin(pi/2)");
            ChatUtils.sendMessage(PREFIX + EnumChatFormatting.GRAY + "  /ATHRcalc sqrt(16)");
            ChatUtils.sendMessage(PREFIX + EnumChatFormatting.GRAY + "  /ATHRcalc log(100)");
            ChatUtils.sendMessage(PREFIX + EnumChatFormatting.GRAY + "  /ATHRcalc max(5, 10)");
            ChatUtils.sendMessage(PREFIX + EnumChatFormatting.GRAY + "Multipliers: k, m, b, t (numbers), s (stack/64)");
            ChatUtils.sendMessage(PREFIX + EnumChatFormatting.GRAY + "Functions: sin, cos, tan, sqrt, log, ln, abs, ceil, floor, pow, max, min");
            ChatUtils.sendMessage(PREFIX + EnumChatFormatting.GRAY + "Constants: pi, e");
            return;
        }

        String expression = String.join(" ", args);

        try {
            java.math.BigDecimal result = CalculatorUtils.calculate(expression);
            String formatted = CalculatorUtils.FORMAT.format(result);
            ChatUtils.sendMessage(PREFIX + EnumChatFormatting.YELLOW + expression + EnumChatFormatting.GREEN + " = " + EnumChatFormatting.AQUA + formatted);
        } catch (CalculatorUtils.CalculatorException e) {
            String msg = e.getMessage() != null ? e.getMessage() : "Invalid expression";
            ChatUtils.sendMessage(PREFIX + EnumChatFormatting.RED + "Error: " + msg);
        } catch (java.util.NoSuchElementException e) {
            ChatUtils.sendMessage(PREFIX + EnumChatFormatting.RED + "Error: Not enough values (check parentheses and operators)");
        } catch (Exception e) {
            String msg = e.getMessage() != null ? e.getMessage() : e.getClass().getSimpleName();
            ChatUtils.sendMessage(PREFIX + EnumChatFormatting.RED + "Error: " + msg);
            e.printStackTrace(); // Debug: print to console
        }
    }
}
