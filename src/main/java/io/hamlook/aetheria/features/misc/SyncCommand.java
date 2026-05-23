package io.hamlook.aetheria.features.misc;

import io.hamlook.aetheria.command.SimpleCommand;
import io.hamlook.aetheria.init.RegisterCommand;
import io.hamlook.aetheria.utils.chat.ChatUtils;
import io.hamlook.aetheria.utils.data.SkyblockData;
import net.minecraft.client.Minecraft;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayer;

import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.security.SecureRandom;
import java.util.Base64;

@RegisterCommand
public class SyncCommand extends SimpleCommand {

    private static final String API_URL = "https://capeapi.qzz.io/pending-sync";
    private static final String MOD_SECRET = "a7c0e73c-3b0b-4789-8c80-741dd09ba1bc";

    @Override
    public String getName() {
        return "sync";
    }

    @Override
    public String getUsage() {
        return "/" + getName();
    }

    @Override
    public void execute(ICommandSender sender, String[] args) throws CommandException {
        if (!(sender instanceof EntityPlayer)) return;

        if (!SkyblockData.isOnSkyblock()) {
            ChatUtils.sendMessage("§cPlease Join SkyBlock in order to sync, this is to prove that you are not using the username of someone else.");
            return;
        }

        String playerName = sender.getName();
        String syncCode = generateSyncCode();

        new Thread(() -> {
            try {
                URL url = new URL(API_URL);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();

                conn.setRequestMethod("POST");
                conn.setRequestProperty("x-playername", playerName);
                conn.setRequestProperty("x-code", syncCode);
                conn.setRequestProperty("x-mod-secret", MOD_SECRET);
                conn.setDoOutput(true);

                try (OutputStream os = conn.getOutputStream()) {
                    os.write(new byte[0]);
                    os.flush();
                }

                int responseCode = conn.getResponseCode();

                if (responseCode >= 200 && responseCode < 300) {
                    Minecraft.getMinecraft().addScheduledTask(() -> ChatUtils.sendMultilineMessage("§a[SkyAtlas] Your sync code is: §e§l" + syncCode + "§r\n§aPlease paste this code in the §9#sync§a channel on Discord within 5 minutes!"));
                } else {
                    Minecraft.getMinecraft().addScheduledTask(() -> ChatUtils.sendMessage("§c[SkyAtlas] Failed to generate sync code. API returned status " + responseCode));
                }

                conn.disconnect();

            } catch (Exception e) {
                e.printStackTrace();

                Minecraft.getMinecraft().addScheduledTask(() -> ChatUtils.sendMessage("§c[SkyAtlas] An error occurred while contacting the sync server."));
            }
        }).start();
    }

    private String generateSyncCode() {
        SecureRandom random = new SecureRandom();
        byte[] bytes = new byte[6];
        random.nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }
}