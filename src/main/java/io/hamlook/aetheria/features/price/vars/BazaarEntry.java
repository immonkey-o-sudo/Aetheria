package io.hamlook.aetheria.features.price.vars;

import lombok.AllArgsConstructor;

@AllArgsConstructor
public class BazaarEntry {

    public String itemID;
    public double iBuy;
    public double iSell;
    public double oBuy;
    public double oSell;
    public PriceType priceType;
    public long timestamp;

}
