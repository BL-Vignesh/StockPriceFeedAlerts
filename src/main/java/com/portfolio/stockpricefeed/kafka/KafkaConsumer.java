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
            boolean thresholdCrossed = event.getPrice() >= p.getThresholdPrice();

            log.info("userId={} symbol={} currentValue={} gainLoss={} thresholdCrossed={}",
                    p.getUserId(), p.getStockSymbol(), currentValue, gainLoss, thresholdCrossed);

            // Step 4: Build the SSE payload for "price-update" event
            // This matches what the UI's "price-update" event handler expects
            PortFolioItemSummary itemSummary = new PortFolioItemSummary(
                    p.getId(),
                    p.getStockSymbol(),
                    p.getQuantity(),
                    p.getBuyPrice(),
                    event.getPrice(),      // currentPrice — the new live price
                    currentValue,
                    investedValue,
                    gainLoss,
                    gainLossPercent,
                    p.getThresholdPrice(),
                    thresholdCrossed
            );

            // Push to the UI via SSE (event name: "price-update")
            sseEmitterRegistry.sendPriceUpdate(p.getUserId(), itemSummary);

            // Step 5: If threshold crossed, send a separate "alert" SSE event
            if (thresholdCrossed) {
                StockPriceAlert alert = new StockPriceAlert(
                        p.getUserId(),
                        p.getStockSymbol(),
                        event.getPrice(),
                        p.getThresholdPrice(),
                        "Alert: " + p.getStockSymbol() + " crossed your threshold of ₹" + p.getThresholdPrice()
                );
                // Push to the UI via SSE (event name: "alert")
                sseEmitterRegistry.sendAlert(p.getUserId(), alert);
                log.info("Threshold alert sent → userId={} symbol={}", p.getUserId(), p.getStockSymbol());
            }
        }
    }
}
