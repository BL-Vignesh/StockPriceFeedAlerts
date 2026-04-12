package com.portfolio.stockpricefeed.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class PortFolioSummary {

    private double totalValue;

    private double gainLoss;

}
