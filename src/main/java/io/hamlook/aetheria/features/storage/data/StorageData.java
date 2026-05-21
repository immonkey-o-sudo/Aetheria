package io.hamlook.aetheria.features.storage.data;

import io.hamlook.aetheria.features.storage.utils.SContainer;

import java.util.LinkedHashMap;


public class StorageData {

    public static LinkedHashMap<String, SContainer> containers = new LinkedHashMap<>();


    public static void loadContainers() {
        containers = StorageSaving.loadStorageData();
    }


    public static void saveContainers() {
        StorageSaving.saveStorageData(containers.values());
    }

}