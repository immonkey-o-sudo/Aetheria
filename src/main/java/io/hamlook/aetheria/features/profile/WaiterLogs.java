package io.hamlook.aetheria.features.profile;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import io.hamlook.aetheria.Aetheria;
import io.hamlook.aetheria.core.ATHRConfig;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;

public class WaiterLogs {

    public static Gson gson = new GsonBuilder().setPrettyPrinting().create();
    public static ArrayList<String> logs = new ArrayList<>();

    public static void addLog(String log){
        logs.add(log);
    }

    public static void saveLogs(){
        File file = new File(ATHRConfig.configDirectory,"guiWaiterLogs.log");
        try {
            if (!file.exists()) {
                file.createNewFile();
            }
            FileWriter writer = new FileWriter(file,true);
            writer.write(gson.toJson(logs));
            writer.close();
        } catch (IOException e) {
            Aetheria.logger.info("Error Saving GUI Waiter Logs.");
        }
    }

}
