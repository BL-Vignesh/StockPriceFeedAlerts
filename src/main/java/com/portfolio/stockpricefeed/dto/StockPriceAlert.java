package com.portfolio.stockpricefeed.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class StockPriceAlert {

    private Long userId;
    private String symbol;
    private Double currentPrice;
    private Double limitCrossed;
    private String alertType; // "UPPER" or "LOWER"
    private String message;
}
