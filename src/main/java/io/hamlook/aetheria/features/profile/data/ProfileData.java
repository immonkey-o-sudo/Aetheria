package io.hamlook.aetheria.features.profile.data;

import io.hamlook.aetheria.features.profile.data.bags.BagsData;
import io.hamlook.aetheria.features.profile.data.base.BaseData;
import io.hamlook.aetheria.features.profile.data.collection.CollectionsData;
import io.hamlook.aetheria.features.profile.data.dungeon.DungeonData;
import io.hamlook.aetheria.features.profile.data.inventory.InventoryData;
import io.hamlook.aetheria.features.profile.data.pets.PetsData;
import io.hamlook.aetheria.features.profile.data.skills.SkillsData;
import io.hamlook.aetheria.features.profile.data.slayer.SlayersData;
import io.hamlook.aetheria.features.profile.data.storage.StorageData;
import io.hamlook.aetheria.features.profile.data.wardrobe.WardrobeData;
import lombok.AllArgsConstructor;


@AllArgsConstructor
public class ProfileData {

    public BaseData baseData;
    public InventoryData inventoryData;
    public SkillsData skillData;
    public HOTMData hotmData;
    public DungeonData dungeonData;
    public SlayersData slayersData;
    public WardrobeData wardrobeData;
    public PetsData petsData;
    public StorageData storageData;
    public BagsData bagsData;
    public CollectionsData collectionData;

}
