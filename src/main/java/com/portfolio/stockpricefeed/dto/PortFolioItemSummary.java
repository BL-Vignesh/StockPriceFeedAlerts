package com.portfolio.stockpricefeed.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class PortFolioItemSummary {

    private Long id;
    private String symbol;
    private String companyName;
    private int quantity;
    private double buyPrice;
    private double currentPrice;
    private double currentValue;
    private double investedValue;
    private double gainLoss;
    private double gainLossPercent;
    private double upperLimit;
    private double lowerLimit;
    private boolean upperCrossed;
    private boolean lowerCrossed;
}
