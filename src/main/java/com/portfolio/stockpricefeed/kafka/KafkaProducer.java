package com.portfolio.stockpricefeed.kafka;

import com.portfolio.stockpricefeed.entities.StockPriceEvent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
public class KafkaProducer {

    @Autowired
    private KafkaTemplate<String, StockPriceEvent> kafkaTemplate;

    /**
     * Publishes a stock price event to the Kafka topic "stock-price-topic".
     *
     * Called by PortFolioController's POST /portfolio/publish endpoint.
     * The UI has a "Publish Price" button that triggers this.
     *
     * After publishing, KafkaConsumer picks it up and pushes SSE to the UI.
     */
    public void publishPrice(StockPriceEvent event) {
        kafkaTemplate.send("stock-price-topic", event.getSymbol(), event);
    }
}
