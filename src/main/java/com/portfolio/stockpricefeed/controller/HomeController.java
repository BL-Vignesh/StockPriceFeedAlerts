package com.portfolio.stockpricefeed.controller;

import com.portfolio.stockpricefeed.dto.response.ApiResponse;
import com.portfolio.stockpricefeed.dto.response.HomeResponse;
import com.portfolio.stockpricefeed.dto.response.MarketTickerResponse;
import com.portfolio.stockpricefeed.service.HomeService;
import com.portfolio.stockpricefeed.service.MarketDataService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * US3 - Home page controller.
 *
 * GET /api/home/{userId}        → full home page (user info + menu + market ticker)
 * GET /api/home/market-ticker   → live NSE prices only (for refreshing ticker separately)
 *
 * Both endpoints require JWT (protected).
 */
@RestController
@RequestMapping("/api/home")
@RequiredArgsConstructor
@Slf4j
public class HomeController {

    private final HomeService homeService;
    private final MarketDataService marketDataService;

    /**
     * US3 - Returns the full home page data after login.
     *
     * GET /api/home/{userId}
     * Header: Authorization: Bearer <token>
     *
     * Response:
     * {
     *   "success": true,
     *   "data": {
     *     "userId": 1,
     *     "username": "john",
     *     "welcomeMessage": "Welcome back, john!",
     *     "menuOptions": [
     *       { "name": "Portfolio",       "route": "/portfolio" },
     *       { "name": "Alert Settings",  "route": "/alerts"    },
     *       { "name": "Monitor Portfolio","route": "/monitor"  }
     *     ],
     *     "marketTicker": [
     *       { "ticker": "RELIANCE", "currentPrice": 2650.5, "change": 25.3, "direction": "UP" },
     *       { "ticker": "TCS",      "currentPrice": 3310.0, "change": -12.0, "direction": "DOWN" },
     *       ...
     *     ]
     *   }
     * }
     */
    @GetMapping("/{userId}")
    public ResponseEntity<ApiResponse<HomeResponse>> getHomePage(
            @PathVariable Long userId) {

        log.info("[API] GET /api/home/{}", userId);
        HomeResponse response = homeService.getHomePage(userId);
        return ResponseEntity.ok(ApiResponse.success("Home page loaded", response));
    }

    /**
     * US3 - Returns only the live market ticker data.
     * Used when the UI needs to refresh the bottom ticker independently.
     *
     * GET /api/home/market-ticker
     * Header: Authorization: Bearer <token>
     *
     * Response:
     * {
     *   "success": true,
     *   "data": [
     *     { "ticker": "RELIANCE", "companyName": "Reliance Industries Ltd",
     *       "currentPrice": 2650.5, "change": 25.3, "changePercent": 0.96, "direction": "UP" },
     *     ...
     *   ]
     * }
     */
    @GetMapping("/market-ticker")
    public ResponseEntity<ApiResponse<List<MarketTickerResponse>>> getMarketTicker() {
        log.info("[API] GET /api/home/market-ticker");
        List<MarketTickerResponse> ticker = marketDataService.getLiveMarketTicker();
        return ResponseEntity.ok(ApiResponse.success("Market ticker fetched", ticker));
    }

    /**
     * US4 - Returns the global stock dictionary to populate Autocomplete UI.
     */
    @GetMapping("/stocks")
    public ResponseEntity<ApiResponse<java.util.Map<String, String>>> getValidStocks() {
        return ResponseEntity.ok(ApiResponse.success("Valid stocks loaded", marketDataService.getValidStocks()));
    }
}