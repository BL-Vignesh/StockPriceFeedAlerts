package com.portfolio.stockpricefeed.service;

import com.portfolio.stockpricefeed.dto.PortfolioDetailResponse;
import com.portfolio.stockpricefeed.dto.PortFolioItemSummary;
import com.portfolio.stockpricefeed.dto.PortFolioSummary;
import com.portfolio.stockpricefeed.entities.LivePriceStore;
import com.portfolio.stockpricefeed.entities.Portfolio;
import com.portfolio.stockpricefeed.repository.PortFolioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Cell;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
public class PortFolioService {

    @Autowired
    private PortFolioRepository repository;

    @Autowired
    private LivePriceStore store;

    @Autowired
    private MarketDataService marketDataService;

    public PortFolioSummary getPortfolio(Long userId) {
        List<Portfolio> stocks = repository.findByUserId(userId);
        double totalValue = 0;
        double investedValue = 0;
        for (Portfolio p : stocks) {
            double currentPrice = store.getPrice(p.getStockSymbol());
            totalValue += p.getQuantity() * currentPrice;
            investedValue += p.getQuantity() * p.getBuyPrice();
        }
        return new PortFolioSummary(totalValue, totalValue - investedValue);
    }

    public PortfolioDetailResponse getPortfolioDetail(Long userId) {
        List<Portfolio> stocks = repository.findByUserId(userId);

        double totalValue = 0;
        double totalInvested = 0;
        List<PortFolioItemSummary> items = new ArrayList<>();

        for (Portfolio p : stocks) {
            double currentPrice = store.getPrice(p.getStockSymbol());
            double currentValue = p.getQuantity() * currentPrice;
            double invested = p.getQuantity() * p.getBuyPrice();
            double gainLoss = currentValue - invested;
            double gainLossPercent = invested > 0 ? (gainLoss / invested) * 100 : 0;
            boolean thresholdCrossed = currentPrice >= p.getThresholdPrice();

            items.add(new PortFolioItemSummary(
                    p.getId(),
                    p.getStockSymbol(),
                    p.getQuantity(),
                    p.getBuyPrice(),
                    currentPrice,
                    currentValue,
                    invested,
                    gainLoss,
                    gainLossPercent,
                    p.getThresholdPrice(),
                    thresholdCrossed));

            totalValue += currentValue;
            totalInvested += invested;
        }

        double totalGainLoss = totalValue - totalInvested;
        double totalGainLossPercent = totalInvested > 0 ? (totalGainLoss / totalInvested) * 100 : 0;

        return new PortfolioDetailResponse(
                userId,
                totalValue,
                totalInvested,
                totalGainLoss,
                totalGainLossPercent,
                items);
    }
    
    // US5: Bulk Add Data from Excel (.xls or .xlsx)
    @Transactional
    public List<Portfolio> saveBulkFromExcel(MultipartFile file, Long userId) {
        List<Portfolio> savedPortfolios = new ArrayList<>();
        Map<String, String> validStocks = marketDataService.getValidStocks();

        try (InputStream is = file.getInputStream();
             Workbook workbook = WorkbookFactory.create(is)) {

            Sheet sheet = workbook.getSheetAt(0);
            for (Row row : sheet) {
                if (row.getRowNum() == 0) continue; // Skip header

                Cell symbolCell = row.getCell(0);
                if (symbolCell == null || symbolCell.getStringCellValue().trim().isEmpty()) continue;

                String symbol = symbolCell.getStringCellValue().trim().toUpperCase();

                if (!validStocks.containsKey(symbol)) {
                    continue; // Skip invalid stocks
                }

                int quantity = (int) getNumericValue(row.getCell(1));
                double buyPrice = getNumericValue(row.getCell(2));
                double thresholdPrice = getNumericValue(row.getCell(3));

                // Find existing
                Optional<Portfolio> existingOpt = repository.findByUserId(userId)
                    .stream()
                    .filter(p -> p.getStockSymbol().equals(symbol))
                    .findFirst();

                if (existingOpt.isPresent()) {
                    Portfolio existing = existingOpt.get();
                    existing.setQuantity(quantity);
                    existing.setBuyPrice(buyPrice);
                    existing.setThresholdPrice(thresholdPrice);
                    savedPortfolios.add(repository.save(existing));
                } else {
                    Portfolio newPortfolio = new Portfolio();
                    newPortfolio.setUserId(userId);
                    newPortfolio.setStockSymbol(symbol);
                    newPortfolio.setQuantity(quantity);
                    newPortfolio.setBuyPrice(buyPrice);
                    newPortfolio.setThresholdPrice(thresholdPrice);
                    savedPortfolios.add(repository.save(newPortfolio));
                }
            }
        } catch (Exception e) {
            throw new RuntimeException("Failed to parse Excel file: " + e.getMessage());
        }

        return savedPortfolios;
    }

    private double getNumericValue(Cell cell) {
        if (cell == null) return 0.0;
        switch (cell.getCellType()) {
            case NUMERIC: 
                return cell.getNumericCellValue();
            case STRING: 
                try {
                    return Double.parseDouble(cell.getStringCellValue().trim());
                } catch (NumberFormatException e) {
                    return 0.0;
                }
            default: return 0.0;
        }
    }

    // US7: Update Data
    @Transactional
    public Portfolio updatePortfolio(Long id, Portfolio updatedData) {
        Portfolio existing = repository.findById(id).orElseThrow(() -> new RuntimeException("Holding not found"));
        existing.setQuantity(updatedData.getQuantity());
        existing.setBuyPrice(updatedData.getBuyPrice());
        existing.setThresholdPrice(updatedData.getThresholdPrice());
        return repository.save(existing);
    }
    
    // US8: Update Threshold Specifically
    @Transactional
    public Portfolio updateThreshold(Long id, double newThreshold) {
        Portfolio existing = repository.findById(id).orElseThrow(() -> new RuntimeException("Holding not found"));
        existing.setThresholdPrice(newThreshold);
        return repository.save(existing);
    }

    // US7: Delete Data
    @Transactional
    public void deletePortfolio(Long id) {
        repository.deleteById(id);
    }
}
