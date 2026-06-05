package com.portfolio.stockpricefeed.service;

import com.portfolio.stockpricefeed.dto.response.MarketTickerResponse;
import com.portfolio.stockpricefeed.entities.LivePriceStore;
import com.portfolio.stockpricefeed.entities.StockPriceEvent;
import com.portfolio.stockpricefeed.kafka.KafkaProducer;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.*;
import java.util.stream.Collectors;

/**
 * US3 - Fetches live NSE top stock prices from Yahoo Finance API.
 *
 * Yahoo Finance endpoint (free, no API key needed):
 * https://query1.finance.yahoo.com/v8/finance/chart/{symbol}.NS
 *
 * NSE tickers use suffix ".NS" on Yahoo Finance.
 * e.g. RELIANCE → RELIANCE.NS
 *
 * Console output:
 * [MARKET] Fetching live prices for 20 NSE stocks...
 * [MARKET] RELIANCE.NS → price=2650.5 change=+25.3 (+0.96%)
 * [MARKET] TCS.NS → price=3310.0 change=-12.0 (-0.36%)
 * [MARKET] Fetch complete → 20/20 stocks fetched successfully
 */
@Service
@Slf4j
public class MarketDataService {

    @Autowired(required = false)
    private LivePriceStore livePriceStore;

    @Autowired(required = false)
    private KafkaProducer kafkaProducer;

    private final RestTemplate restTemplate;

    // Yahoo Finance chart API — no API key required
    @Value("${app.yahoo.finance.url:https://query1.finance.yahoo.com/v8/finance/chart}")
    private String yahooFinanceUrl;

    // NSE top 20 tickers (Yahoo Finance format: TICKER.NS)
    private static final List<String> NSE_TICKERS = List.of(
            "RELIANCE", "TCS", "HDFCBANK", "INFY", "ICICIBANK",
            "HINDUNILVR", "ITC", "SBIN", "BHARTIARTL", "BAJFINANCE",
            "KOTAKBANK", "WIPRO", "AXISBANK", "ASIANPAINT", "MARUTI",
            "SUNPHARMA", "TATAMOTORS", "ULTRACEMCO", "TITAN", "NESTLEIND"
    );

    // Map ticker → company name (for display)
    private static final Map<String, String> COMPANY_NAMES = Map.ofEntries(
            Map.entry("RELIANCE",    "Reliance Industries Ltd"),
            Map.entry("TCS",         "Tata Consultancy Services Ltd"),
            Map.entry("HDFCBANK",    "HDFC Bank Ltd"),
            Map.entry("INFY",        "Infosys Ltd"),
            Map.entry("ICICIBANK",   "ICICI Bank Ltd"),
            Map.entry("HINDUNILVR",  "Hindustan Unilever Ltd"),
            Map.entry("ITC",         "ITC Ltd"),
            Map.entry("SBIN",        "State Bank of India"),
            Map.entry("BHARTIARTL",  "Bharti Airtel Ltd"),
            Map.entry("BAJFINANCE",  "Bajaj Finance Ltd"),
            Map.entry("KOTAKBANK",   "Kotak Mahindra Bank Ltd"),
            Map.entry("WIPRO",       "Wipro Ltd"),
            Map.entry("AXISBANK",    "Axis Bank Ltd"),
            Map.entry("ASIANPAINT",  "Asian Paints Ltd"),
            Map.entry("MARUTI",      "Maruti Suzuki India Ltd"),
            Map.entry("SUNPHARMA",   "Sun Pharmaceutical Industries Ltd"),
            Map.entry("TATAMOTORS",  "Tata Motors Ltd"),
            Map.entry("ULTRACEMCO",  "UltraTech Cement Ltd"),
            Map.entry("TITAN",       "Titan Company Ltd"),
            Map.entry("NESTLEIND",   "Nestle India Ltd")
    );

    public MarketDataService() {
        this.restTemplate = new RestTemplate();
    }

    public Map<String, String> getValidStocks() {
        return COMPANY_NAMES;
    }

    /**
     * Fetches live prices for all NSE top 20 stocks.
     * Skips any stock that fails to fetch (network error, market closed etc.)
     * and logs a warning — never throws, always returns what it can.
     */
    public List<MarketTickerResponse> getLiveMarketTicker() {
        log.info("[MARKET] Fetching live prices for {} NSE stocks...", NSE_TICKERS.size());

        List<MarketTickerResponse> results = NSE_TICKERS.stream()
                .map(ticker -> {
                    try {
                        // Introduce 300ms pacing delay to avoid hitting rate limits (HTTP 429)
                        Thread.sleep(300);
                        return fetchSingleStock(ticker);
                    } catch (InterruptedException ie) {
                        Thread.currentThread().interrupt();
                        log.error("[MARKET] Ticker fetch interrupted for {}", ticker, ie);
                        return MarketTickerResponse.builder()
                                .ticker(ticker)
                                .companyName(COMPANY_NAMES.getOrDefault(ticker, ticker))
                                .currentPrice(0.0)
                                .change(0.0)
                                .changePercent(0.0)
                                .direction("FLAT")
                                .build();
                    } catch (Exception e) {
                        log.warn("[MARKET] Failed to fetch {} → {}", ticker, e.getMessage());
                        // Return placeholder so the ticker list is not broken
                        return MarketTickerResponse.builder()
                                .ticker(ticker)
                                .companyName(COMPANY_NAMES.getOrDefault(ticker, ticker))
                                .currentPrice(0.0)
                                .change(0.0)
                                .changePercent(0.0)
                                .direction("FLAT")
                                .build();
                    }
                })
                .collect(Collectors.toList());

        long successCount = results.stream()
                .filter(r -> r.getCurrentPrice() > 0)
                .count();

        log.info("[MARKET] Fetch complete → {}/{} stocks fetched successfully",
                successCount, NSE_TICKERS.size());

        return results;
    }

    /**
     * Periodically polls Yahoo Finance API for the live prices of NSE top 20 stocks,
     * updates the in-memory cache, and publishes the updates to Kafka.
     * Scheduled to run every 5 minutes (300,000 ms).
     */
    @Scheduled(fixedRate = 300000)
    public void scheduledFetchAndPublish() {
        log.info("[SCHEDULED] Starting periodic stock price fetch...");
        List<MarketTickerResponse> liveMarketTickers = getLiveMarketTicker();

        int publishedCount = 0;
        for (MarketTickerResponse ticker : liveMarketTickers) {
            if (ticker.getCurrentPrice() > 0) {
                if (livePriceStore != null) {
                    livePriceStore.updatePrice(ticker.getTicker(), ticker.getCurrentPrice());
                }
                if (kafkaProducer != null) {
                    StockPriceEvent event = new StockPriceEvent(ticker.getTicker(), ticker.getCurrentPrice());
                    kafkaProducer.publishPrice(event);
                    publishedCount++;
                }
            }
        }
        log.info("[SCHEDULED] Periodic fetch and publish complete. Published {} updates.", publishedCount);
    }

    /**
     * Fetches live price for a single NSE stock using Yahoo Finance chart API.
     *
     * Yahoo Finance response structure (simplified):
     * {
     *   "chart": {
     *     "result": [{
     *       "meta": {
     *         "regularMarketPrice": 2650.5,
     *         "previousClose": 2625.2,
     *         "currency": "INR"
     *       }
     *     }]
     *   }
     * }
     */
    @SuppressWarnings("unchecked")
    private MarketTickerResponse fetchSingleStock(String ticker) {
        String symbol = ticker + ".NS";  // Yahoo Finance NSE suffix
        String url = UriComponentsBuilder
                .fromHttpUrl(yahooFinanceUrl + "/" + symbol)
                .queryParam("interval", "1d")
                .queryParam("range", "1d")
                .toUriString();

        HttpHeaders headers = new HttpHeaders();
        headers.set("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36");
        HttpEntity<Void> entity = new HttpEntity<>(headers);

        ResponseEntity<Map> responseEntity = restTemplate.exchange(url, HttpMethod.GET, entity, Map.class);
        Map<String, Object> response = responseEntity.getBody();

        if (response == null) {
            throw new RuntimeException("Null response from Yahoo Finance for " + symbol);
        }

        // Navigate the nested Yahoo Finance JSON structure
        Map<String, Object> chart   = (Map<String, Object>) response.get("chart");
        List<Object> resultList     = (List<Object>) chart.get("result");

        if (resultList == null || resultList.isEmpty()) {
            throw new RuntimeException("No data returned for " + symbol);
        }

        Map<String, Object> result  = (Map<String, Object>) resultList.get(0);
        Map<String, Object> meta    = (Map<String, Object>) result.get("meta");

        double currentPrice  = getDouble(meta, "regularMarketPrice");
        double previousClose = getDouble(meta, "previousClose");
        double change        = currentPrice - previousClose;
        double changePct     = previousClose > 0 ? (change / previousClose) * 100 : 0.0;
        String direction     = change > 0 ? "UP" : (change < 0 ? "DOWN" : "FLAT");

        log.info("[MARKET] {} → price={} change={} ({:.2f}%)",
                symbol, currentPrice, change, changePct);

        return MarketTickerResponse.builder()
                .ticker(ticker)
                .companyName(COMPANY_NAMES.getOrDefault(ticker, ticker))
                .currentPrice(currentPrice)
                .change(Math.round(change * 100.0) / 100.0)
                .changePercent(Math.round(changePct * 100.0) / 100.0)
                .direction(direction)
                .build();
    }

    private double getDouble(Map<String, Object> map, String key) {
        Object value = map.get(key);
        if (value == null) return 0.0;
        if (value instanceof Double)  return (Double) value;
        if (value instanceof Integer) return ((Integer) value).doubleValue();
        if (value instanceof Long)    return ((Long) value).doubleValue();
        if (value instanceof Number)  return ((Number) value).doubleValue();
        return 0.0;
    }
}