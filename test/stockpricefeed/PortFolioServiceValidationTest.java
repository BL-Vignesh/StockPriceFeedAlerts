package stockpricefeed;

import com.portfolio.stockpricefeed.entities.LivePriceStore;
import com.portfolio.stockpricefeed.entities.Portfolio;
import com.portfolio.stockpricefeed.repository.PortFolioRepository;
import com.portfolio.stockpricefeed.repository.UserRepository;
import com.portfolio.stockpricefeed.service.MarketDataService;
import com.portfolio.stockpricefeed.service.PortFolioService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PortFolioServiceValidationTest {

    @Mock private PortFolioRepository repository;
    @Mock private LivePriceStore store;
    @Mock private MarketDataService marketDataService;
    @Mock private UserRepository userRepository;

    @InjectMocks private PortFolioService portfolioService;

    @BeforeEach
    void setUp() {
        // Setup valid stocks dict to allow RELIANCE
        lenient().when(marketDataService.getValidStocks()).thenReturn(Map.of("RELIANCE", "Reliance Industries Ltd"));
        lenient().when(userRepository.existsById(1L)).thenReturn(true);
    }

    @Test
    void testSavePortfolio_invalidQuantity_throwsException() {
        Portfolio p = new Portfolio();
        p.setUserId(1L);
        p.setStockSymbol("RELIANCE");
        p.setQuantity(0);
        p.setBuyPrice(2500.0);

        assertThatThrownBy(() -> portfolioService.savePortfolio(p))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Quantity must be greater than zero");
    }

    @Test
    void testSavePortfolio_invalidBuyPrice_throwsException() {
        Portfolio p = new Portfolio();
        p.setUserId(1L);
        p.setStockSymbol("RELIANCE");
        p.setQuantity(10);
        p.setBuyPrice(-100.0);

        assertThatThrownBy(() -> portfolioService.savePortfolio(p))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Buy price must be greater than zero");
    }

    @Test
    void testSavePortfolio_negativeUpperLimit_throwsException() {
        Portfolio p = new Portfolio();
        p.setUserId(1L);
        p.setStockSymbol("RELIANCE");
        p.setQuantity(10);
        p.setBuyPrice(2500.0);
        p.setUpperLimit(-5.0);
        p.setLowerLimit(10.0);

        assertThatThrownBy(() -> portfolioService.savePortfolio(p))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Upper limit percentage cannot be negative");
    }

    @Test
    void testSavePortfolio_negativeLowerLimit_throwsException() {
        Portfolio p = new Portfolio();
        p.setUserId(1L);
        p.setStockSymbol("RELIANCE");
        p.setQuantity(10);
        p.setBuyPrice(2500.0);
        p.setUpperLimit(10.0);
        p.setLowerLimit(-5.0);

        assertThatThrownBy(() -> portfolioService.savePortfolio(p))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Lower limit percentage cannot be negative");
    }

    @Test
    void testSavePortfolio_lowerLimitGreaterEqual100_throwsException() {
        Portfolio p = new Portfolio();
        p.setUserId(1L);
        p.setStockSymbol("RELIANCE");
        p.setQuantity(10);
        p.setBuyPrice(2500.0);
        p.setUpperLimit(10.0);
        p.setLowerLimit(105.0); // 105%

        assertThatThrownBy(() -> portfolioService.savePortfolio(p))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Lower limit percentage must be strictly less than 100%");
    }

    @Test
    void testSavePortfolio_upperLimitBelowCurrentPrice_throwsException() {
        Portfolio p = new Portfolio();
        p.setUserId(1L);
        p.setStockSymbol("RELIANCE");
        p.setQuantity(10);
        p.setBuyPrice(2500.0);
        p.setUpperLimit(10.0); // Target = ₹2750.0
        p.setLowerLimit(0.0);

        when(store.getPrice("RELIANCE")).thenReturn(2800.0); // Current ₹2800.0 >= ₹2750.0

        assertThatThrownBy(() -> portfolioService.savePortfolio(p))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Current price")
                .hasMessageContaining("is already higher than or equal to the target upper price");
    }

    @Test
    void testSavePortfolio_lowerLimitAboveCurrentPrice_throwsException() {
        Portfolio p = new Portfolio();
        p.setUserId(1L);
        p.setStockSymbol("RELIANCE");
        p.setQuantity(10);
        p.setBuyPrice(2500.0);
        p.setUpperLimit(0.0);
        p.setLowerLimit(10.0); // Target = ₹2250.0

        when(store.getPrice("RELIANCE")).thenReturn(2200.0); // Current ₹2200.0 <= ₹2250.0

        assertThatThrownBy(() -> portfolioService.savePortfolio(p))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Current price")
                .hasMessageContaining("is already lower than or equal to the target lower price");
    }
}
