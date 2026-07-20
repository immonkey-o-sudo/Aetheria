package io.hamlook.aetheria.features.farming.organicmatter;

import io.hamlook.aetheria.core.GsonBuilder;
import io.hamlook.aetheria.core.ProfileManagedStorage;
import io.hamlook.aetheria.core.StorageManager;

import java.io.File;
import java.util.LinkedHashMap;
import java.util.Map;

public class OrganicMatterTrackerData extends ProfileManagedStorage implements StorageManager.AutoSaveable {

    private static OrganicMatterTrackerData INSTANCE;
    private StoredData data = new StoredData();

    private OrganicMatterTrackerData() {
        super("organic_matter_tracker.json");
    }

    public static OrganicMatterTrackerData getInstance() {
        if (INSTANCE == null) INSTANCE = new OrganicMatterTrackerData();
        return INSTANCE;
    }

    @Override
    public void load() {
        File f = resolveFile();
        if (f == null) return;
        StoredData loaded = StorageManager.loadSafe(f, StoredData.class, GsonBuilder.GSON);
        if (loaded != null) data = loaded;
    }

    public void save() {
        File f = resolveFile();
        if (f == null) return;
        StorageManager.saveAtomic(f, data, GsonBuilder.GSON);
    }

    @Override
    public void autoSave() {
        save();
    }

    public Map<String, Long> getCounts() {
        return data.counts;
    }

    public long getActiveTimeMs() {
        return data.activeTimeMs;
    }

    public void setActiveTimeMs(long ms) {
        data.activeTimeMs = ms;
    }

    public void reset() {
        data.counts.clear();
        data.activeTimeMs = 0L;
        save();
    }

    private static class StoredData {
        Map<String, Long> counts = new LinkedHashMap<>();
        long activeTimeMs = 0L;
    }
}

