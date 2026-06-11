package io.hamlook.aetheria.features.price;

import io.hamlook.aetheria.features.price.vars.BazaarEntry;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PriceAPI {

    private static final Map<String, Double> HARDCODED_PRICES = new HashMap<>();
    private static final Map<String, CachedPrice> PRICE_CACHE = new HashMap<>();
    private static final long VALID_PRICE_CACHE_MS = 30_000L;
    private static final long NOT_FOUND_CACHE_MS = 300_000L;

    static {
        HARDCODED_PRICES.put("GHOSTLY_BOOTS", 77_000.0);
        HARDCODED_PRICES.put("BAG_OF_CASH", 1_000_000.0);
    }

    /**
     * Get the price of an item by its internal name.
     * First checks hardcoded prices, then queries the price database with caching.
     * Not found results are cached longer to prevent log spam from repeated queries.
     *
     * @param internalItemId The internal Skyblock item ID
     * @return The price in coins, or -1 if not available
     */
    public static double getPrice(String internalItemId) {
        if (internalItemId == null || internalItemId.isEmpty()) {
            return -1;
        }

        // Check hardcoded prices first
        double hardcodedPrice = getHardcodedPrice(internalItemId);
        if (hardcodedPrice > 0) {
            return hardcodedPrice;
        }

        // Check cache
        CachedPrice cached = PRICE_CACHE.get(internalItemId);
        if (cached != null && !cached.isExpired()) {
            return cached.price;
        }

        double price = getPriceFromMap(internalItemId);

        // Cache longer if price not found to reduce spam
        long cacheDuration = price > 0 ? VALID_PRICE_CACHE_MS : NOT_FOUND_CACHE_MS;
        PRICE_CACHE.put(internalItemId, new CachedPrice(price, System.currentTimeMillis(), cacheDuration));

        return price;
    }

    private static double getHardcodedPrice(String itemId) {
        Double price = HARDCODED_PRICES.get(itemId);
        return price != null ? price : -1.0;
    }

    private static double getPriceFromMap(String itemId) {
        try {
            List<BazaarEntry> prices = PriceMap.getBZPrice(itemId, 1);
            if (prices != null && !prices.isEmpty()) {
                BazaarEntry latestPrice = prices.get(0);
                return latestPrice.oSell > 0 ? latestPrice.oSell : -1;
            }
        } catch (Exception e) {
            return -1;
        }
        return -1;
    }

    private static class CachedPrice {
        double price;
        long timestamp;
        long cacheDurationMs;

        CachedPrice(double price, long timestamp, long cacheDurationMs) {
            this.price = price;
            this.timestamp = timestamp;
            this.cacheDurationMs = cacheDurationMs;
        }

        boolean isExpired() {
            return System.currentTimeMillis() - timestamp > cacheDurationMs;
        }
    }
}
