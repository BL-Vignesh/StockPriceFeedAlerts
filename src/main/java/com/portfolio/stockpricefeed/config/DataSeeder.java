package com.portfolio.stockpricefeed.config;

import com.portfolio.stockpricefeed.entities.Portfolio;
import com.portfolio.stockpricefeed.repository.PortFolioRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

/**
 * DataSeeder — runs once at application startup.
 *
 * Seeds sample portfolio data for userId=1 so the UI
 * is not empty when you first open it.
 *
 * Skips seeding if data already exists (safe to restart).
 *
 * To reset: change ddl-auto to 'create' once, restart, then change back to 'update'.
 */
@Component
@Slf4j
public class DataSeeder implements CommandLineRunner {

    @Autowired
    private PortFolioRepository repository;

    @Override
    public void run(String... args) {
        if (repository.count() > 0) {
            log.info("Portfolio data already exists — skipping seed.");
            return;
        }

        log.info("Seeding sample portfolio data for userId=1...");

        Portfolio aapl = new Portfolio();
        aapl.setUserId(1L);
        aapl.setStockSymbol("AAPL");
        aapl.setQuantity(10);
        aapl.setBuyPrice(150.0);
        aapl.setThresholdPrice(180.0);

        Portfolio tsla = new Portfolio();
        tsla.setUserId(1L);
        tsla.setStockSymbol("TSLA");
        tsla.setQuantity(5);
        tsla.setBuyPrice(200.0);
        tsla.setThresholdPrice(250.0);

        Portfolio googl = new Portfolio();
        googl.setUserId(1L);
        googl.setStockSymbol("GOOGL");
        googl.setQuantity(2);
        googl.setBuyPrice(130.0);
        googl.setThresholdPrice(160.0);

        Portfolio infy = new Portfolio();
        infy.setUserId(1L);
        infy.setStockSymbol("INFY");
        infy.setQuantity(20);
        infy.setBuyPrice(15.0);
        infy.setThresholdPrice(20.0);

        repository.save(aapl);
        repository.save(tsla);
        repository.save(googl);
        repository.save(infy);

        log.info("Seeded 4 portfolio entries for userId=1.");
    }
}

