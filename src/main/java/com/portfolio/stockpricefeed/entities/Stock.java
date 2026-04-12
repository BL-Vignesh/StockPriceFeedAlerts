package com.portfolio.stockpricefeed.entities;

import jakarta.persistence.*;
import lombok.*;

/**
 * US4 - Master list of valid stocks (company name + ticker symbol).
 * Pre-seeded with NSE top stocks.
 * All portfolio entries are validated against this table.
 */
@Entity
@Table(name = "stocks")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Stock {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String ticker;          // e.g. "RELIANCE"

    @Column(nullable = false)
    private String companyName;     // e.g. "Reliance Industries Ltd"

    @Column(nullable = false)
    private String exchange;        // e.g. "NSE"
}