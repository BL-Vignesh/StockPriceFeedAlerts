package com.portfolio.stockpricefeed.service;

import com.portfolio.stockpricefeed.dto.response.HomeResponse;
import com.portfolio.stockpricefeed.dto.response.MarketTickerResponse;
import com.portfolio.stockpricefeed.entities.User;
import com.portfolio.stockpricefeed.exception.ResourceNotFoundException;
import com.portfolio.stockpricefeed.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.stereotype.Service;

import java.util.List;

/**
 * US3 - Home page service.
 * Returns user info, available menu options, and live market ticker.
 *
 * Console output:
 * [HOME] Building home page for userId=1 username=john
 * [HOME] Home page built successfully for userId=1
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class HomeService {

    private final UserRepository userRepository;
    private final MarketDataService marketDataService;

    /**
     * Builds the home page response for an authenticated user.
     *
     * Contains:
     * 1. User details (userId, username, email)
     * 2. Menu options (Portfolio, Alerts, Monitor)
     * 3. Live NSE top 20 stock prices for the scrolling ticker
     */
    public HomeResponse getHomePage(Long userId) {
        log.info("[HOME] Building home page for userId={}", userId);

        // Fetch user from DB
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + userId));

        // Build menu options (US3 requirement: Portfolio, Alert, Monitor)
        List<HomeResponse.MenuOption> menuOptions = List.of(
                HomeResponse.MenuOption.builder()
                        .name("Portfolio")
                        .description("Upload or create your stock portfolio")
                        .route("/portfolio")
                        .build(),
                HomeResponse.MenuOption.builder()
                        .name("Alert Settings")
                        .description("Set upper and lower price alert thresholds")
                        .route("/alerts")
                        .build(),
                HomeResponse.MenuOption.builder()
                        .name("Monitor Portfolio")
                        .description("View real-time gain/loss on your holdings")
                        .route("/monitor")
                        .build()
        );

        // Fetch live NSE market ticker from Yahoo Finance
        List<MarketTickerResponse> marketTicker = marketDataService.getLiveMarketTicker();

        log.info("[HOME] Home page built successfully for userId={} username={}",
                user.getId(), user.getUsername());

        return HomeResponse.builder()
                .userId(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .welcomeMessage("Welcome back, " + user.getUsername() + "!")
                .menuOptions(menuOptions)
                .marketTicker(marketTicker)
                .build();
    }
}