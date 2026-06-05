package stockpricefeed;

import com.portfolio.stockpricefeed.dto.response.MarketTickerResponse;
import com.portfolio.stockpricefeed.service.MarketDataService;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class MarketDataServiceLiveTest {

    @Test
    void testYahooFinanceApiFetchesLivePrices() {
        // Arrange: Instantiate the service manually
        MarketDataService marketDataService = new MarketDataService();
        
        // Inject the default Yahoo Finance URL property into the private field using ReflectionTestUtils
        ReflectionTestUtils.setField(marketDataService, "yahooFinanceUrl", "https://query1.finance.yahoo.com/v8/finance/chart");

        System.out.println("--- STARTING LIVE YAHOO FINANCE API FETCH TEST ---");

        // Act: Call the method that communicates with Yahoo Finance API
        List<MarketTickerResponse> tickers = marketDataService.getLiveMarketTicker();

        // Assert: Ensure tickers list is not empty
        assertThat(tickers).isNotEmpty();
        
        // Count how many tickers were successfully fetched (currentPrice > 0)
        long successCount = tickers.stream()
                .filter(r -> r.getCurrentPrice() > 0.0)
                .count();

        System.out.println("\n--- LIVE TICKER TEST RESULTS ---");
        System.out.println("Successfully fetched " + successCount + " / " + tickers.size() + " stock tickers.");
        
        for (MarketTickerResponse ticker : tickers) {
            System.out.printf("%s (%s): Price=%.2f, Change=%.2f (%.2f%%) [%s]%n",
                    ticker.getTicker(),
                    ticker.getCompanyName(),
                    ticker.getCurrentPrice(),
                    ticker.getChange(),
                    ticker.getChangePercent(),
                    ticker.getDirection());
        }

        // Verify that we successfully fetched at least 1 stock price (indicating the API is functional)
        assertThat(successCount).isGreaterThan(0);
    }
}
