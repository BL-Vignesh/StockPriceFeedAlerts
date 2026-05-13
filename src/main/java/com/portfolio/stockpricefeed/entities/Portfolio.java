package com.portfolio.stockpricefeed.entities;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Data
@Table(name = "portfolio")
public class Portfolio {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long userId;

    private String stockSymbol;

    private int quantity;

    private double buyPrice;

    private double upperLimit;

    private double lowerLimit;

    private boolean upperAlertSent;

    private boolean lowerAlertSent;

}