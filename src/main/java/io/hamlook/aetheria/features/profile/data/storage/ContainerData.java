package io.hamlook.aetheria.features.profile.data.storage;

import io.hamlook.aetheria.features.profile.data.ItemData;
import lombok.AllArgsConstructor;

import java.util.HashMap;

@AllArgsConstructor
public class ContainerData {

    public String containerID;
    public HashMap<Integer, ItemData> data;

}
