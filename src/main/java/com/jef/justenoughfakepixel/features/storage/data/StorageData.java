package com.jef.justenoughfakepixel.features.storage.data;

import com.jef.justenoughfakepixel.features.storage.utils.SContainer;

import java.util.LinkedHashMap;


public class StorageData {

    public static LinkedHashMap<String, SContainer> containers = new LinkedHashMap<>();


    public static void loadContainers() {
        containers = new LinkedHashMap<>(StorageSaving.loadStorageData());
    }


    public static void saveContainers() {
        StorageSaving.saveStorageData(containers.values());
    }

}
