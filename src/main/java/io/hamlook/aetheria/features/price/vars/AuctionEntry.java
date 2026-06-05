package io.hamlook.aetheria.features.price.vars;

import com.google.gson.JsonObject;
import lombok.AllArgsConstructor;

import java.util.List;

@AllArgsConstructor
public class AuctionEntry {

    public String itemID;
    public List<String> itemLore;
    public JsonObject extraAttributes;
    public double price;
    public PriceType type;
    public long timestamp,expireTime;
    public String playerUsername;
}