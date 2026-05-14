package com.portfolio.stockpricefeed.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class AlertHistoryResponse {
    private Long id;
    private String stockSymbol;
    private String alertType;
    private double triggeredPrice;
    private double limitCrossed;
    private String message;
    private LocalDateTime timestamp;
    private String status;
}
