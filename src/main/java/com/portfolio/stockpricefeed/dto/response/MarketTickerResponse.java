package com.portfolio.stockpricefeed.dto.response;

import lombok.*;

/**
 * US3 - Live market ticker data for one stock.
 * Shown in the scrolling ticker at the bottom of the home page.
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class MarketTickerResponse {

    private String ticker;          // e.g. "RELIANCE.NS"
    private String companyName;     // e.g. "Reliance Industries Ltd"
    private double currentPrice;    // live price from Yahoo Finance
    private double change;          // price change from previous close
    private double changePercent;   // % change
    private String direction;       // "UP", "DOWN", "FLAT"
}