package com.portfolio.stockpricefeed.sse;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
@Slf4j
public class SseEmitterRegistry {

    // One SSE connection per userId — thread-safe map
    private final Map<Long, SseEmitter> emitters = new ConcurrentHashMap<>();

    /**
     * Called when the UI opens EventSource to /portfolio/{userId}/stream.
     * Creates a 5-minute SSE connection for that user.
     * The UI's EventSource will auto-reconnect when it times out.
     */
    public SseEmitter register(Long userId) {
        SseEmitter emitter = new SseEmitter(5 * 60 * 1000L); // 5 minutes

        emitter.onCompletion(() -> {
            emitters.remove(userId);
            log.info("SSE connection closed for userId={}", userId);
        });
        emitter.onTimeout(() -> {
            emitters.remove(userId);
            log.info("SSE connection timed out for userId={}", userId);
        });
        emitter.onError(e -> {
            emitters.remove(userId);
            log.warn("SSE error for userId={}: {}", userId, e.getMessage());
        });

        emitters.put(userId, emitter);
        log.info("SSE client registered for userId={}", userId);
        return emitter;
    }

    /**
     * Sends a "price-update" SSE event to the UI.
     *
     * Called by KafkaConsumer after every Kafka message is consumed.
     * The UI listens: eventSource.addEventListener("price-update", handler)
     *
     * Payload: PortFolioItemSummary JSON with symbol, currentPrice, gainLoss, etc.
     */
    public void sendPriceUpdate(Long userId, Object data) {
        SseEmitter emitter = emitters.get(userId);
        if (emitter == null) {
            log.debug("No SSE connection found for userId={}, skipping price-update", userId);
            return;
        }
        try {
            emitter.send(SseEmitter.event()
                    .name("price-update")   // UI listens for this event name
                    .data(data));           // serialized as JSON automatically
        } catch (IOException e) {
            emitters.remove(userId);
            log.warn("Failed to send price-update to userId={}, removed emitter", userId);
        }
    }

    /**
     * Sends an "alert" SSE event to the UI when a stock crosses the threshold.
     *
     * Called by KafkaConsumer when thresholdCrossed = true.
     * The UI listens: eventSource.addEventListener("alert", handler)
     *
     * Payload: StockPriceAlert JSON with symbol, currentPrice, thresholdPrice, message.
     */
    public void sendAlert(Long userId, Object data) {
        SseEmitter emitter = emitters.get(userId);
        if (emitter == null) {
            log.debug("No SSE connection found for userId={}, skipping alert", userId);
            return;
        }
        try {
            emitter.send(SseEmitter.event()
                    .name("alert")          // UI listens for this event name
                    .data(data));
        } catch (IOException e) {
            emitters.remove(userId);
            log.warn("Failed to send alert to userId={}, removed emitter", userId);
        }
    }
}

