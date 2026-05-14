package com.portfolio.stockpricefeed.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ActiveThresholdResponse {
    private Long id;
    private String stockSymbol;
    private double buyPrice;
    private double upperLimit;
    private double lowerLimit;
    private String status; // e.g., "ACTIVE"
}
