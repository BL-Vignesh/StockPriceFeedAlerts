package com.portfolio.stockpricefeed.kafka;

import com.portfolio.stockpricefeed.dto.PortFolioItemSummary;
import com.portfolio.stockpricefeed.dto.StockPriceAlert;
import com.portfolio.stockpricefeed.entities.LivePriceStore;
import com.portfolio.stockpricefeed.entities.Portfolio;
import com.portfolio.stockpricefeed.entities.StockPriceEvent;
import com.portfolio.stockpricefeed.repository.PortFolioRepository;
import com.portfolio.stockpricefeed.sse.SseEmitterRegistry;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@Slf4j
public class KafkaConsumer {

    @Autowired
    private LivePriceStore store;

    @Autowired
    private PortFolioRepository repository;

    @Autowired
    private SseEmitterRegistry sseEmitterRegistry;

    @Autowired
    private com.portfolio.stockpricefeed.service.AlertGenerator alertGenerator;

    @Autowired
    private com.portfolio.stockpricefeed.service.MarketDataService marketDataService;

    /**
     * Listens to the Kafka topic "stock-price-topic".
     *
     * Every time a price event is published (via POST /portfolio/publish or
     * any external producer), this method:
     *   1. Updates the in-memory price cache (LivePriceStore)
     *   2. Finds all portfolio rows for this stock symbol
     *   3. Calculates gain/loss and threshold status per user
     *   4. Pushes a "price-update" SSE event to each affected user's browser
     *   5. If threshold is crossed, also pushes an "alert" SSE event
     */
    @KafkaListener(topics = "stock-price-topic", groupId = "portfolio-group")
    public void consume(StockPriceEvent event) {

        log.info("Received stock price event: symbol={} price={}", event.getSymbol(), event.getPrice());

        // Step 1: Update in-memory price cache
        store.updatePrice(event.getSymbol(), event.getPrice());

        // Step 2: Find all portfolio entries for this stock across all users
        List<Portfolio> portfolios = repository.findByStockSymbol(event.getSymbol());

        if (portfolios.isEmpty()) {
            log.debug("No portfolio entries found for symbol={}", event.getSymbol());
            return;
        }

        for (Portfolio p : portfolios) {

            // Step 3: Calculate metrics
            double currentValue    = p.getQuantity() * event.getPrice();
            double investedValue   = p.getQuantity() * p.getBuyPrice();
            double gainLoss        = currentValue - investedValue;
            double gainLossPercent = investedValue > 0 ? (gainLoss / investedValue) * 100 : 0;
            
            boolean upperCrossed = event.getPrice() >= p.getUpperLimit() && p.getUpperLimit() > 0;
            boolean lowerCrossed = event.getPrice() <= p.getLowerLimit() && p.getLowerLimit() > 0;

            log.info("userId={} symbol={} currentValue={} gainLoss={} upperCrossed={} lowerCrossed={}",
                    p.getUserId(), p.getStockSymbol(), currentValue, gainLoss, upperCrossed, lowerCrossed);

            // Step 4: Build the SSE payload for "price-update" event
            String companyName = marketDataService.getValidStocks().getOrDefault(p.getStockSymbol(), p.getStockSymbol());
            PortFolioItemSummary itemSummary = new PortFolioItemSummary(
                    p.getId(),
                    p.getStockSymbol(),
                    companyName,
                    p.getQuantity(),
                    p.getBuyPrice(),
                    event.getPrice(),
                    currentValue,
                    investedValue,
                    gainLoss,
                    gainLossPercent,
                    p.getUpperLimit(),
                    p.getLowerLimit(),
                    upperCrossed,
                    lowerCrossed
            );

            // Push to the UI via SSE
            sseEmitterRegistry.sendPriceUpdate(p.getUserId(), itemSummary);

            // Step 5: Check Alerts
            if (upperCrossed && !p.isUpperAlertSent()) {
                p.setUpperAlertSent(true);
                repository.save(p);
                StockPriceAlert alert = new StockPriceAlert(
                        p.getUserId(), p.getStockSymbol(), event.getPrice(), p.getUpperLimit(), "UPPER",
                        "Alert: " + p.getStockSymbol() + " crossed your upper limit of ₹" + p.getUpperLimit()
                );
                sseEmitterRegistry.sendAlert(p.getUserId(), alert);
                alertGenerator.processAndSendAlert(alert);
                log.info("Upper limit alert sent → userId={} symbol={}", p.getUserId(), p.getStockSymbol());
            }

            if (lowerCrossed && !p.isLowerAlertSent()) {
                p.setLowerAlertSent(true);
                repository.save(p);
                StockPriceAlert alert = new StockPriceAlert(
                        p.getUserId(), p.getStockSymbol(), event.getPrice(), p.getLowerLimit(), "LOWER",
                        "Alert: " + p.getStockSymbol() + " dropped below your lower limit of ₹" + p.getLowerLimit()
                );
                sseEmitterRegistry.sendAlert(p.getUserId(), alert);
                alertGenerator.processAndSendAlert(alert);
                log.info("Lower limit alert sent → userId={} symbol={}", p.getUserId(), p.getStockSymbol());
            }
        }
    }
}
