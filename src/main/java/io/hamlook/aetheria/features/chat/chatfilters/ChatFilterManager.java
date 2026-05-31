package io.hamlook.aetheria.features.chat.chatfilters;

import com.google.gson.reflect.TypeToken;
import io.hamlook.aetheria.core.ATHRConfig;
import io.hamlook.aetheria.core.GsonBuilder;
import io.hamlook.aetheria.core.StorageManager;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.IChatComponent;

import java.io.File;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

public class ChatFilterManager {

    public static List<ChatFilter> chatFilters = new ArrayList<>();
    private static File file;

    public static void initialise() {
        file = new File(ATHRConfig.configDirectory, "chatFilters.json");
        chatFilters.clear();
        Type type = new TypeToken<List<ChatFilter>>() {}.getType();
        List<ChatFilter> loaded = StorageManager.loadSafe(file, type, GsonBuilder.GSON);
        if (loaded != null) chatFilters = loaded;
    }

    public static void saveToFile() {
        StorageManager.saveAtomic(file, chatFilters, GsonBuilder.GSON);
    }

    public static IChatComponent applyFilters(IChatComponent message) {
        if (message == null) return null;
        String msg = message.getFormattedText();
        for (ChatFilter filter : chatFilters) {
            msg = filter.applyFilter(msg);
            if (msg == null) return null;
        }
        if (msg.equals(message.getFormattedText())) return message;
        return new ChatComponentText(msg.trim());
    }
}
