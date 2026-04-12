package com.portfolio.stockpricefeed.service;

import com.portfolio.stockpricefeed.dto.response.StockResponse;
import com.portfolio.stockpricefeed.entities.Stock;
import com.portfolio.stockpricefeed.repository.StockRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

/**
 * US4 - Stock master service.
 * Provides valid stock list and ticker validation.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class StockService {

    private final StockRepository stockRepository;

    /**
     * Returns all stocks in the master list.
     * Console output:
     * [STOCK] Fetching all stocks → count=50
     */
    public List<StockResponse> getAllStocks() {
        List<Stock> stocks = stockRepository.findAll();
        log.info("[STOCK] Fetching all stocks → count={}", stocks.size());
        return stocks.stream().map(this::toResponse).collect(Collectors.toList());
    }

    /**
     * Search stocks by ticker or company name.
     * Console output:
     * [STOCK] Search → query=RELI → found=2
     */
    public List<StockResponse> searchStocks(String query) {
        List<Stock> results = stockRepository
                .findByTickerContainingIgnoreCaseOrCompanyNameContainingIgnoreCase(query, query);
        log.info("[STOCK] Search → query={} → found={}", query, results.size());
        return results.stream().map(this::toResponse).collect(Collectors.toList());
    }

    /**
     * Validates that a ticker exists in the master list.
     * Used by PortfolioService before saving any holding.
     */
    public Stock validateAndGetStock(String ticker) {
        return stockRepository.findByTickerIgnoreCase(ticker)
                .orElseThrow(() -> {
                    log.warn("[STOCK] Invalid ticker: {}", ticker);
                    return new com.portfolio.stockpricefeed.exception.InvalidTickerException(ticker);
                });
    }

    private StockResponse toResponse(Stock stock) {
        return StockResponse.builder()
                .id(stock.getId())
                .ticker(stock.getTicker())
                .companyName(stock.getCompanyName())
                .exchange(stock.getExchange())
                .build();
    }
}