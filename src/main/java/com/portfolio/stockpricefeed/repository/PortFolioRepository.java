package com.portfolio.stockpricefeed.repository;

import com.portfolio.stockpricefeed.entities.Portfolio;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PortFolioRepository extends JpaRepository<Portfolio, Long> {

    // Used by KafkaConsumer: find all users holding a given stock symbol
    List<Portfolio> findByStockSymbol(String symbol);

    // Used by PortFolioService: load all stocks for a given user
    List<Portfolio> findByUserId(Long userId);
}
