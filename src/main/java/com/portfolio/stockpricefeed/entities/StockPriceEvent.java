package com.portfolio.stockpricefeed.entities;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class StockPriceEvent {

    private String symbol;
    private Double price;

}
