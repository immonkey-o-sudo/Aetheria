package io.hamlook.aetheria.features.profile.data.bags;

import io.hamlook.aetheria.features.profile.data.bags.vars.Bait;
import lombok.AllArgsConstructor;

import java.util.EnumMap;

@AllArgsConstructor
public class FishingData {

    public EnumMap<Bait,Integer> baits;

}
