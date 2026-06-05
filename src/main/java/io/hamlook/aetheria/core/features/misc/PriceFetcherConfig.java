package io.hamlook.aetheria.core.features.misc;

import com.google.gson.annotations.Expose;
import io.hamlook.aetheria.core.config.gui.config.ConfigAnnotations;

public class PriceFetcherConfig {

    @Expose
    @ConfigAnnotations.ConfigOption(name = "Enable", desc = "Dynamically Fetch Prices while you play the game without affecting performance.")
    @ConfigAnnotations.ConfigEditorBoolean
    public boolean enabled = true;

    @Expose
    @ConfigAnnotations.ConfigOption(name = "Enable Auction Fetching", desc = "Allow parsing prices while you browse the auction house.")
    @ConfigAnnotations.ConfigEditorBoolean
    public boolean auctionEnabled = true;

    @Expose
    @ConfigAnnotations.ConfigOption(name = "Enable Bazaar Fetching", desc = "Allow parsing prices while you browse the bazaar.")
    @ConfigAnnotations.ConfigEditorBoolean
    public boolean bazaarEnabled = true;

    @Expose
    @ConfigAnnotations.ConfigOption(name = "Send Fetched Data to API", desc = "Allow sending the data to the database, so it can actually be used.")
    @ConfigAnnotations.ConfigEditorBoolean
    public boolean sendToDB = true;

}
