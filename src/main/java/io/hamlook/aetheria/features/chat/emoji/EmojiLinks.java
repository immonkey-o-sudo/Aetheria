package io.hamlook.aetheria.features.chat.emoji;

import io.hamlook.aetheria.core.ATHRConfig;
import net.minecraft.util.ResourceLocation;

import java.io.File;

public class EmojiLinks {

    private static final String BASE_URL = "https://raw.githubusercontent.com/iamcal/emoji-data/master/";

    private static final String BASE_RESOURCE = "aetheria";
    public static final int SHEET_RESOLUTION = 32;
    public static int SHEET_SIZE = 2108;

    public static final String GOOGLE_URL = "sheet_google_";
    public static final String IOS_URL = "sheet_apple_";
    public static final String DISCORD_URL = "sheet_twitter_";

    private static final File BASE_DIR = new File(ATHRConfig.configDirectory, "emojis");

    public static final String GOOGLE_SHEET = "google.png";
    public static final String IOS_SHEET = "ios.png";
    public static final String DISCORD_SHEET = "discord.png";

    public static String sheetToURL(String sheet){
        switch (sheet){
            case GOOGLE_SHEET:
                return GOOGLE_URL;
            case IOS_SHEET:
                return IOS_URL;
            default:
                return DISCORD_URL;
        }
    }
    public static String getSpriteURL(String PREFIX){
        return BASE_URL + PREFIX + SHEET_RESOLUTION + ".png";
    }

    public static File getSpriteFile(String sprite){
        return new File(BASE_DIR, sprite);
    }

    public static ResourceLocation getSpriteResource(String sprite){
        return new ResourceLocation(BASE_RESOURCE, sprite);
    }

    public static String getEmojiJSON() {
        return "https://raw.githubusercontent.com/iamcal/emoji-data/master/emoji.json";
    }
}
