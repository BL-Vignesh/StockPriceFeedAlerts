package com.portfolio.stockpricefeed.entities;

import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class LivePriceStore {

    // Thread-safe in-memory map of symbol → latest price
    private final Map<String, Double> priceMap = new ConcurrentHashMap<>();

    public void updatePrice(String symbol, Double price) {
        priceMap.put(symbol, price);
    }

    public Double getPrice(String symbol) {
        // Returns 0.0 if no price has been received yet for this symbol
        return priceMap.getOrDefault(symbol, 0.0);
    }
}
