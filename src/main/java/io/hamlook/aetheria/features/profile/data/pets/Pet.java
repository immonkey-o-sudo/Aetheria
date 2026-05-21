package io.hamlook.aetheria.features.profile.data.pets;

import io.hamlook.aetheria.features.profile.data.ItemData;
import lombok.AllArgsConstructor;

@AllArgsConstructor
public class Pet {

    public ItemData data;
    public int page;
    public boolean equipped;

}
