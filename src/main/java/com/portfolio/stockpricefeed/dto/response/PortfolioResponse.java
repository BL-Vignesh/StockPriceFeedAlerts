package com.portfolio.stockpricefeed.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PortfolioResponse {
    private Long id;
    private Long userId;
    private String symbol;
    private int quantity;
    private double buyPrice;
    private double thresholdPrice;
    private double currentPrice;
    private double currentValue;
    private double investedValue;
    private double gainLoss;
    private double gainLossPercent;
    private boolean thresholdCrossed;
}
