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
import com.portfolio.stockpricefeed.repository.UserRepository;
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

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class PortFolioService {

    @Autowired
    private PortFolioRepository repository;

    @Autowired
    private LivePriceStore store;

    @Autowired
    private MarketDataService marketDataService;

    @Autowired
    private UserRepository userRepository;

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
            boolean upperCrossed = currentPrice >= p.getUpperLimit() && p.getUpperLimit() > 0;
            boolean lowerCrossed = currentPrice <= p.getLowerLimit() && p.getLowerLimit() > 0;
            String companyName = marketDataService.getValidStocks().getOrDefault(p.getStockSymbol(), p.getStockSymbol());

            items.add(new PortFolioItemSummary(
                    p.getId(),
                    p.getStockSymbol(),
                    companyName,
                    p.getQuantity(),
                    p.getBuyPrice(),
                    currentPrice,
                    currentValue,
                    invested,
                    gainLoss,
                    gainLossPercent,
                    p.getUpperLimit(),
                    p.getLowerLimit(),
                    upperCrossed,
                    lowerCrossed));

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
        if (!userRepository.existsById(userId)) {
            throw new RuntimeException("User not found with ID: " + userId);
        }

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
                double upperLimit = getNumericValue(row.getCell(3));
                double lowerLimit = getNumericValue(row.getCell(4));

                // Find existing
                Optional<Portfolio> existingOpt = repository.findByUserId(userId)
                    .stream()
                    .filter(p -> p.getStockSymbol().equals(symbol))
                    .findFirst();

                if (existingOpt.isPresent()) {
                    Portfolio existing = existingOpt.get();
                    existing.setQuantity(quantity);
                    existing.setBuyPrice(buyPrice);
                    existing.setUpperLimit(upperLimit);
                    existing.setLowerLimit(lowerLimit);
                    existing.setUpperAlertSent(false); // Reset alert flag on update
                    existing.setLowerAlertSent(false);
                    savedPortfolios.add(repository.save(existing));
                } else {
                    Portfolio newPortfolio = new Portfolio();
                    newPortfolio.setUserId(userId);
                    newPortfolio.setStockSymbol(symbol);
                    newPortfolio.setQuantity(quantity);
                    newPortfolio.setBuyPrice(buyPrice);
                    newPortfolio.setUpperLimit(upperLimit);
                    newPortfolio.setLowerLimit(lowerLimit);
                    newPortfolio.setUpperAlertSent(false); // Init flag
                    newPortfolio.setLowerAlertSent(false);
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

    @Transactional
    public Portfolio savePortfolio(Portfolio p) {
        log.debug("[PORTFOLIO] Saving portfolio for user: {} symbol: {}", p.getUserId(), p.getStockSymbol());
        if (!userRepository.existsById(p.getUserId())) {
            throw new RuntimeException("User not found with ID: " + p.getUserId());
        }
        
        Map<String, String> validStocks = marketDataService.getValidStocks();
        String symbol = p.getStockSymbol().toUpperCase();
        if (!validStocks.containsKey(symbol)) {
            throw new RuntimeException("Invalid stock symbol: " + p.getStockSymbol());
        }
        
        p.setStockSymbol(symbol);

        // Check if the user already holds this stock
        Optional<Portfolio> existingOpt = repository.findByUserId(p.getUserId())
                .stream()
                .filter(existing -> existing.getStockSymbol().equals(symbol))
                .findFirst();

        if (existingOpt.isPresent()) {
            Portfolio existing = existingOpt.get();
            
            // If everything is exactly the same, restrict it from adding/updating
            if (existing.getQuantity() == p.getQuantity() &&
                Double.compare(Optional.ofNullable(existing.getBuyPrice()).orElse(0.0), Optional.ofNullable(p.getBuyPrice()).orElse(0.0)) == 0 &&
                Double.compare(Optional.ofNullable(existing.getUpperLimit()).orElse(0.0), Optional.ofNullable(p.getUpperLimit()).orElse(0.0)) == 0 &&
                Double.compare(Optional.ofNullable(existing.getLowerLimit()).orElse(0.0), Optional.ofNullable(p.getLowerLimit()).orElse(0.0)) == 0) {
                throw new RuntimeException("This exact portfolio entry already exists. No changes were made.");
            }
            
            // Otherwise, update the existing entry (Upsert)
            existing.setQuantity(p.getQuantity());
            existing.setBuyPrice(p.getBuyPrice());
            existing.setUpperLimit(p.getUpperLimit());
            existing.setLowerLimit(p.getLowerLimit());
            existing.setUpperAlertSent(false); 
            existing.setLowerAlertSent(false); 
            return repository.save(existing);
        }

        return repository.save(p);
    }

    // US7: Update Data
    @Transactional
    public Portfolio updatePortfolio(Long id, Portfolio updatedData) {
        Portfolio existing = repository.findById(id).orElseThrow(() -> new RuntimeException("Holding not found"));
        existing.setQuantity(updatedData.getQuantity());
        existing.setBuyPrice(updatedData.getBuyPrice());
        
        // If threshold changed, reset the alert flag so they can receive an alert again
        if (existing.getUpperLimit() != updatedData.getUpperLimit()) {
            existing.setUpperAlertSent(false);
        }
        if (existing.getLowerLimit() != updatedData.getLowerLimit()) {
            existing.setLowerAlertSent(false);
        }
        existing.setUpperLimit(updatedData.getUpperLimit());
        existing.setLowerLimit(updatedData.getLowerLimit());
        
        return repository.save(existing);
    }
    
    // US8: Update Thresholds Specifically
    @Transactional
    public Portfolio updateLimits(Long id, double upperLimit, double lowerLimit) {
        Portfolio existing = repository.findById(id).orElseThrow(() -> new RuntimeException("Holding not found"));
        existing.setUpperLimit(upperLimit);
        existing.setLowerLimit(lowerLimit);
        existing.setUpperAlertSent(false); // Reset the flag because they updated the threshold
        existing.setLowerAlertSent(false);
        return repository.save(existing);
    }

    // US7: Delete Data
    @Transactional
    public void deletePortfolio(Long id) {
        repository.deleteById(id);
    }
}
