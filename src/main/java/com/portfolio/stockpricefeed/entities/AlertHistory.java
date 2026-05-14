package com.portfolio.stockpricefeed.entities;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

@Entity
@Data
@Table(name = "alert_history")
public class AlertHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long userId;

    private String stockSymbol;

    private String alertType; // e.g., "UPPER", "LOWER"

    private double triggeredPrice;

    private double limitCrossed;

    private String message;

    private LocalDateTime timestamp;

    private String status; // e.g., "DISPATCHED", "FAILED"
}
