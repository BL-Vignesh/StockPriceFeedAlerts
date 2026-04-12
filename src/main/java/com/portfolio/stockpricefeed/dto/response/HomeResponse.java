package com.portfolio.stockpricefeed.dto.response;

import lombok.*;
import java.util.List;

/**
 * US3 - Home page response after successful login.
 * Contains user info, available menu options, and live market ticker.
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class HomeResponse {

    private Long userId;
    private String username;
    private String email;
    private String welcomeMessage;

    // Menu options available to the authenticated user
    private List<MenuOption> menuOptions;

    // Live NSE top 50 stock prices for the scrolling ticker
    private List<MarketTickerResponse> marketTicker;

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    @Builder
    public static class MenuOption {
        private String name;        // e.g. "Portfolio"
        private String description; // e.g. "Create, upload and manage your portfolio"
        private String route;       // e.g. "/portfolio"
    }
}