package com.portfolio.stockpricefeed.controller;

import com.portfolio.stockpricefeed.dto.PortfolioDetailResponse;
import com.portfolio.stockpricefeed.dto.PortFolioSummary;
import com.portfolio.stockpricefeed.entities.Portfolio;
import com.portfolio.stockpricefeed.entities.StockPriceEvent;
import com.portfolio.stockpricefeed.kafka.KafkaProducer;
import com.portfolio.stockpricefeed.repository.PortFolioRepository;
import com.portfolio.stockpricefeed.service.PortFolioService;
import com.portfolio.stockpricefeed.sse.SseEmitterRegistry;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.List;

@RestController
@RequestMapping("/portfolio")
public class PortFolioController {

    @Autowired
    private PortFolioService service;

    @Autowired
    private PortFolioRepository repository;

    @Autowired
    private SseEmitterRegistry sseEmitterRegistry;

    @Autowired
    private KafkaProducer kafkaProducer;

    @GetMapping("/{userId}/summary")
    public PortFolioSummary getPortfolioSummary(@PathVariable Long userId) {
        return service.getPortfolio(userId);
    }

    @GetMapping("/{userId}")
    public PortfolioDetailResponse getPortfolioDetail(@PathVariable Long userId) {
        return service.getPortfolioDetail(userId);
    }

    @PostMapping("/add")
    public Portfolio savePortfolio(@RequestBody Portfolio p) {
        return service.savePortfolio(p);
    }

    // US5: Bulk add from Excel parsing
    @PostMapping(value = "/bulk", consumes = org.springframework.http.MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<List<Portfolio>> saveBulkPortfolios(
            @RequestParam("file") org.springframework.web.multipart.MultipartFile file, 
            @RequestParam("userId") Long userId) {
        return ResponseEntity.ok(service.saveBulkFromExcel(file, userId));
    }

    // US7: Update quantity/price
    @PutMapping("/{id}")
    public ResponseEntity<Portfolio> updatePortfolio(@PathVariable Long id, @RequestBody Portfolio p) {
        return ResponseEntity.ok(service.updatePortfolio(id, p));
    }

    // US8: Update limits specifically
    @PutMapping("/{id}/limits")
    public ResponseEntity<Portfolio> updateLimits(@PathVariable Long id, @RequestParam double upperLimit, @RequestParam double lowerLimit) {
        return ResponseEntity.ok(service.updateLimits(id, upperLimit, lowerLimit));
    }

    // US7: Delete
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletePortfolio(@PathVariable Long id) {
        service.deletePortfolio(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/publish")
    public String publishPrice(@RequestBody StockPriceEvent event) {
        kafkaProducer.publishPrice(event);
        return "Price event published: " + event.getSymbol() + " @ " + event.getPrice();
    }

    @GetMapping(value = "/{userId}/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter streamPortfolioUpdates(@PathVariable Long userId) {
        return sseEmitterRegistry.register(userId);
    }
}
