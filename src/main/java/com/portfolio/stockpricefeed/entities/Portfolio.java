package com.portfolio.stockpricefeed.entities;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.*;
import lombok.Data;

@Entity
@Data
@Table(name = "portfolio")
public class Portfolio {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    private Long id;

    private Long userId;

    private String stockSymbol;

    private int quantity;

    private Double buyPrice;
    private Double upperLimit;
    private Double lowerLimit;

    private boolean upperAlertSent;

    private boolean lowerAlertSent;

}