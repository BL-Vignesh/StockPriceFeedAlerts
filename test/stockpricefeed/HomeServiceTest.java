package stockpricefeed;

import com.portfolio.stockpricefeed.dto.response.HomeResponse;
import com.portfolio.stockpricefeed.dto.response.MarketTickerResponse;
import com.portfolio.stockpricefeed.entities.User;
import com.portfolio.stockpricefeed.exception.ResourceNotFoundException;
import com.portfolio.stockpricefeed.repository.UserRepository;
import com.portfolio.stockpricefeed.service.HomeService;
import com.portfolio.stockpricefeed.service.MarketDataService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * US3 - Unit tests for HomeService.
 */
@ExtendWith(MockitoExtension.class)
class HomeServiceTest {

    @Mock private UserRepository userRepository;
    @Mock private MarketDataService marketDataService;

    @InjectMocks private HomeService homeService;

    private User user;
    private List<MarketTickerResponse> mockTicker;

    @BeforeEach
    void setUp() {
        user = User.builder()
                .id(1L).username("john").email("john@example.com").build();

        mockTicker = List.of(
                MarketTickerResponse.builder()
                        .ticker("RELIANCE").companyName("Reliance Industries Ltd")
                        .currentPrice(2650.5).change(25.3).changePercent(0.96).direction("UP")
                        .build(),
                MarketTickerResponse.builder()
                        .ticker("TCS").companyName("Tata Consultancy Services Ltd")
                        .currentPrice(3310.0).change(-12.0).changePercent(-0.36).direction("DOWN")
                        .build()
        );
    }

    @Test
    @DisplayName("US3 - getHomePage: returns user info, menu options and market ticker")
    void getHomePage_success() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(marketDataService.getLiveMarketTicker()).thenReturn(mockTicker);

        HomeResponse response = homeService.getHomePage(1L);

        // User info
        assertThat(response.getUserId()).isEqualTo(1L);
        assertThat(response.getUsername()).isEqualTo("john");
        assertThat(response.getEmail()).isEqualTo("john@example.com");
        assertThat(response.getWelcomeMessage()).contains("john");

        // Menu options — must have exactly 3 (Portfolio, Alert Settings, Monitor)
        assertThat(response.getMenuOptions()).hasSize(3);
        assertThat(response.getMenuOptions())
                .extracting(HomeResponse.MenuOption::getName)
                .containsExactly("Portfolio", "Alert Settings", "Monitor Portfolio");
        assertThat(response.getMenuOptions())
                .extracting(HomeResponse.MenuOption::getRoute)
                .containsExactly("/portfolio", "/alerts", "/monitor");

        // Market ticker
        assertThat(response.getMarketTicker()).hasSize(2);
        assertThat(response.getMarketTicker().get(0).getTicker()).isEqualTo("RELIANCE");
        assertThat(response.getMarketTicker().get(0).getDirection()).isEqualTo("UP");
        assertThat(response.getMarketTicker().get(1).getDirection()).isEqualTo("DOWN");
    }

    @Test
    @DisplayName("US3 - getHomePage: throws ResourceNotFoundException when user not found")
    void getHomePage_userNotFound() {
        when(userRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> homeService.getHomePage(99L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("99");

        // Market ticker should NOT be called if user not found
        verify(marketDataService, never()).getLiveMarketTicker();
    }

    @Test
    @DisplayName("US3 - getHomePage: still returns home page even if market ticker fails")
    void getHomePage_marketTickerFails_graceful() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        // Market data service returns empty list (simulates all failed fetches)
        when(marketDataService.getLiveMarketTicker()).thenReturn(List.of());

        HomeResponse response = homeService.getHomePage(1L);

        // Home page still loads, just with empty ticker
        assertThat(response.getUserId()).isEqualTo(1L);
        assertThat(response.getMenuOptions()).hasSize(3);
        assertThat(response.getMarketTicker()).isEmpty();
    }

    @Test
    @DisplayName("US3 - getHomePage: welcome message uses username correctly")
    void getHomePage_welcomeMessage() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(marketDataService.getLiveMarketTicker()).thenReturn(mockTicker);

        HomeResponse response = homeService.getHomePage(1L);

        assertThat(response.getWelcomeMessage()).isEqualTo("Welcome back, john!");
    }
}