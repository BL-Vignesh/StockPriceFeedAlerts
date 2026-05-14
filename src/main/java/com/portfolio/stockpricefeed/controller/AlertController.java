package com.portfolio.stockpricefeed.controller;

import com.portfolio.stockpricefeed.dto.response.ActiveThresholdResponse;
import com.portfolio.stockpricefeed.dto.response.AlertHistoryResponse;
import com.portfolio.stockpricefeed.dto.response.ApiResponse;
import com.portfolio.stockpricefeed.service.AlertService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/alerts")
@RequiredArgsConstructor
@Slf4j
public class AlertController {

    private final AlertService alertService;

    @GetMapping("/{userId}/active")
    public ResponseEntity<ApiResponse<List<ActiveThresholdResponse>>> getActiveThresholds(@PathVariable Long userId) {
        log.info("[API] GET /api/alerts/{}/active", userId);
        List<ActiveThresholdResponse> activeThresholds = alertService.getActiveThresholds(userId);
        return ResponseEntity.ok(ApiResponse.success("Active thresholds fetched", activeThresholds));
    }

    @GetMapping("/{userId}/history")
    public ResponseEntity<ApiResponse<List<AlertHistoryResponse>>> getAlertHistory(@PathVariable Long userId) {
        log.info("[API] GET /api/alerts/{}/history", userId);
        List<AlertHistoryResponse> history = alertService.getAlertHistory(userId);
        return ResponseEntity.ok(ApiResponse.success("Alert history fetched", history));
    }
}
