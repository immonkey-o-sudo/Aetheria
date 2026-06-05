package io.hamlook.aetheria.features.price.vars;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PriceData {

    public Map<String, List<BazaarEntry>> bazaar = new HashMap<>();
    public Map<String, List<AuctionEntry>> auction = new HashMap<>();

}