package com.portfolio.stockpricefeed.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

@Data
@AllArgsConstructor
public class PortfolioDetailResponse {

    private Long userId;
    private double totalValue;
    private double totalInvested;
    private double totalGainLoss;
    private double totalGainLossPercent;
    private List<PortFolioItemSummary> stocks;
}
