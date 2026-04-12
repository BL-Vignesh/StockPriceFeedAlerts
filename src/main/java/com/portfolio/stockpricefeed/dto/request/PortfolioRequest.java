package com.portfolio.stockpricefeed.dto.request;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PortfolioRequest {
    private Long userId;
    private String symbol;
    private int quantity;
    private double buyPrice;
    private double thresholdPrice;
}