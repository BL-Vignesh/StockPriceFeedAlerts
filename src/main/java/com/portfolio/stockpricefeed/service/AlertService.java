package com.portfolio.stockpricefeed.service;

import com.portfolio.stockpricefeed.dto.response.ActiveThresholdResponse;
import com.portfolio.stockpricefeed.dto.response.AlertHistoryResponse;
import com.portfolio.stockpricefeed.entities.AlertHistory;
import com.portfolio.stockpricefeed.entities.Portfolio;
import com.portfolio.stockpricefeed.repository.AlertHistoryRepository;
import com.portfolio.stockpricefeed.repository.PortFolioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class AlertService {

    @Autowired
    private PortFolioRepository portFolioRepository;

    @Autowired
    private AlertHistoryRepository alertHistoryRepository;

    public List<ActiveThresholdResponse> getActiveThresholds(Long userId) {
        List<Portfolio> portfolios = portFolioRepository.findByUserId(userId);
        
        return portfolios.stream()
                .filter(p -> p.getUpperLimit() > 0 || p.getLowerLimit() > 0)
                .map(p -> new ActiveThresholdResponse(
                        p.getId(),
                        p.getStockSymbol(),
                        p.getBuyPrice(),
                        p.getUpperLimit(),
                        p.getLowerLimit(),
                        "ACTIVE"
                ))
                .collect(Collectors.toList());
    }

    public List<AlertHistoryResponse> getAlertHistory(Long userId) {
        List<AlertHistory> historyList = alertHistoryRepository.findByUserIdOrderByTimestampDesc(userId);
        
        return historyList.stream()
                .map(h -> new AlertHistoryResponse(
                        h.getId(),
                        h.getStockSymbol(),
                        h.getAlertType(),
                        h.getTriggeredPrice(),
                        h.getLimitCrossed(),
                        h.getMessage(),
                        h.getTimestamp(),
                        h.getStatus()
                ))
                .collect(Collectors.toList());
    }
}
