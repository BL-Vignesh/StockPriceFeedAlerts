package com.portfolio.stockpricefeed.repository;

import com.portfolio.stockpricefeed.entities.Stock;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface StockRepository extends JpaRepository<Stock, Long> {

    Optional<Stock> findByTickerIgnoreCase(String ticker);

    boolean existsByTickerIgnoreCase(String ticker);

    List<Stock> findByTickerContainingIgnoreCaseOrCompanyNameContainingIgnoreCase(
            String ticker, String companyName);
}