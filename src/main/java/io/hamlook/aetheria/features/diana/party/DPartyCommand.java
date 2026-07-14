package io.hamlook.aetheria.features.diana.party;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import io.hamlook.aetheria.Aetheria;
import io.hamlook.aetheria.command.ASMCommand;
import io.hamlook.aetheria.init.RegisterCommand;
import io.hamlook.aetheria.network.NetworkGuard;
import io.hamlook.aetheria.utils.chat.ChatUtils;
import io.netty.util.concurrent.CompleteFuture;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.util.BlockPos;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

@RegisterCommand
public class DPartyCommand extends ASMCommand {
    @Override
    public String getName() {
        return "dparty";
    }

    @Override
    public String getUsage() {
        return "/" + getName() + getArgs();
    }

    private String getArgs() {
        return "<join|create|leave|disband>";
    }

    @Override
    public List<String> addTabCompletionOptions(ICommandSender sender, String[] args, BlockPos pos) {
        if(args.length == 0) return Arrays.asList("join", "create", "leave", "disband");
        if(args.length == 1){
            String argument = args[0];
            String[] options = new String[] {"join", "create", "leave", "disband"};
            return Arrays.stream(options).filter(s -> s.toLowerCase().startsWith(argument.toLowerCase())).collect(Collectors.toList());
        }
        return new ArrayList<>();
    }

    @Override
    public void execute(ICommandSender sender, String[] args) throws CommandException {
        if(args.length < 1) {
            ChatUtils.sendMessage("§cPlease Enter a Sub Command");
            return;
        }
        switch (args[0].toLowerCase()) {
            case "create":
                createParty(args);
                break;
            case "join":
                joinParty(args);
                break;
            case "leave":
                leaveParty();
                break;
            case "disband":
                disbandParty();
        }
    }

    public void disbandParty() {
        if (!DianaPartyConnector.isConnected) {
            ChatUtils.sendMessage("§cYou are not connected to the api, please try again. If the issue persists, make sure you have API usage allowed");
            if (NetworkGuard.apiAllowed()) {
                DianaPartyConnector.connectToAPI();
            }
            return;
        }
        if (!DianaPartyConnector.isInParty()) {
            ChatUtils.sendMessage("§cYou are not in a Diana Party.");
            return;
        }
        CompletableFuture<String> future = DianaPartyConnector.disbandParty();
        if(future == null){
            ChatUtils.sendMessage("§cYou are not in a Diana Party.");
            return;
        }
        future.thenAccept(response -> {
            JsonObject json = JsonParser.parseString(response).getAsJsonObject();
            int code = json.getAsJsonObject("data").get("code").getAsInt();
            if(code == 200){
                ChatUtils.sendMessage("§aSuccessfully Disbanded Diana Party.");
            }else {
                String msg = json.getAsJsonObject("data").get("message").getAsString();
                ChatUtils.sendMessage("§cError While Disbanding Party§7[§c" + code + "§7]: §c" + msg);
            }
        });
    }

    public void leaveParty() {
        if (!DianaPartyConnector.isConnected) {
            ChatUtils.sendMessage("§cYou are not connected to the api, please try again. If the issue persists, make sure you have API usage allowed");
            if (NetworkGuard.apiAllowed()) {
                DianaPartyConnector.connectToAPI();
            }
            return;
        }
        if (!DianaPartyConnector.isInParty()) {
            ChatUtils.sendMessage("§cYou are not in a Diana Party.");
            return;
        }
        CompletableFuture<String> future = DianaPartyConnector.leaveParty();
        if(future == null){
            ChatUtils.sendMessage("§cYou are not in a Diana Party.");
            return;
        }
        future.thenAccept(response -> {
            JsonObject json = JsonParser.parseString(response).getAsJsonObject();
            int code = json.getAsJsonObject("data").get("code").getAsInt();
            if(code == 200){
                ChatUtils.sendMessage("§aSuccessfully Left Diana Party.");
            }else {
                String msg = json.getAsJsonObject("data").get("message").getAsString();
                ChatUtils.sendMessage("§cError While Leaving Party§7[§c" + code + "§7]: §c" + msg);
            }
        });
    }

    public void joinParty(String[] args) {
        if (args.length < 2) {
            ChatUtils.sendMessage("§cPlease enter a valid party ID");
            return;
        }
        if (!DianaPartyConnector.isConnected) {
            ChatUtils.sendMessage("§cYou are not connected to the api, please try again. If the issue persists, make sure you have API usage allowed");
            if (NetworkGuard.apiAllowed()) {
                DianaPartyConnector.connectToAPI();
            }
            return;
        }
        if (DianaPartyConnector.isInParty()) {
            ChatUtils.sendMessage("§cYou are already in a diana party, Please leave or disband the party before joining a new one.");
            return;
        }
        String pID = args[1];
        CompletableFuture<String> future = DianaPartyConnector.joinParty(pID);
        if (future == null){
            ChatUtils.sendMessage("§cEncountered an error while joining party, Please try again in 15 seconds.");
            return;
        }
        future.thenAccept(response -> {
            JsonObject json = JsonParser.parseString(response).getAsJsonObject();
            int code = json.getAsJsonObject("data").get("code").getAsInt();
            if(code == 200){
                String name = json.getAsJsonObject("data").get("partyName").getAsString();
                ChatUtils.sendMessage("§aSuccessfully Joined Diana Party: " + name);
            }else {
                String msg = json.getAsJsonObject("data").get("message").getAsString();
                ChatUtils.sendMessage("§cError While Joining Party§7[§c" + code + "§7]: §c" + msg);
            }
        }).exceptionally(ex -> {
            Aetheria.logger.severe("[D-Party] Join Error: " + ex.getMessage());
            return null;
        });
    }

    public void createParty(String[] args) throws CommandException {
        if (args.length < 2) {
            ChatUtils.sendMessage("§cPlease enter a valid party name");
            return;
        }
        if (!DianaPartyConnector.isConnected) {
            ChatUtils.sendMessage("§cYou are not connected to the api, please try again. If the issue persists, make sure you have API usage allowed");
            if (NetworkGuard.apiAllowed()) {
                DianaPartyConnector.connectToAPI();
            }
            return;
        }
        if (DianaPartyConnector.isInParty()) {
            ChatUtils.sendMessage("§cYou are already in a diana party, Please leave or disband the party before making a new one.");
            return;
        }
        String pName = args[1];
        CompletableFuture<String> future = DianaPartyConnector.createParty(pName);
        if (future == null){
            ChatUtils.sendMessage("§cEncountered an error while creating party, Please try again in 15 seconds.");
            return;
        }
        future.thenAccept(response -> {
            JsonObject json = JsonParser.parseString(response).getAsJsonObject();
            int code = json.getAsJsonObject("data").get("code").getAsInt();
            if(code == 200){
                ChatUtils.sendMessage("§aSuccessfully Created D-Party: " + pName);
            }else {
                String msg = json.getAsJsonObject("data").get("message").getAsString();
                ChatUtils.sendMessage("§cError While Creating Party§7[§c" + code + "§7]: §c" + msg);
            }
        });
    }
}
