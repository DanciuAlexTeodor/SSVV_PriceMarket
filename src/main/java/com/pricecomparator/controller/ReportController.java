package com.pricecomparator.controller;

import com.pricecomparator.service.BestDiscounts;
import com.pricecomparator.service.NewestDiscounts;
import com.pricecomparator.repository.MarketDataRepository;
import org.springframework.web.bind.annotation.*;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.Arrays;
import java.util.List;

@RestController
@RequestMapping("/api/reports")
@CrossOrigin(origins = "*")
public class ReportController {

    private final MarketDataRepository marketDataRepository;

    public ReportController(MarketDataRepository marketDataRepository) {
        this.marketDataRepository = marketDataRepository;
    }

    @GetMapping("/best-discounts")
    public List<String> getBestDiscounts(
            @RequestParam String date,
            @RequestParam(required = false) String store,
            @RequestParam(defaultValue = "10") int limit) {
        
        BestDiscounts bestDiscounts = new BestDiscounts(marketDataRepository);
        return captureConsoleOutput(() -> bestDiscounts.showBestDiscounts(store, date, limit));
    }

    @GetMapping("/newest-discounts")
    public List<String> getNewestDiscounts(
            @RequestParam String date,
            @RequestParam(required = false) String store) {
        
        NewestDiscounts newestDiscounts = new NewestDiscounts(marketDataRepository);
        return captureConsoleOutput(() -> newestDiscounts.showNewestDiscounts(store, date));
    }

    // Helper method to capture the System.out.println output
    private List<String> captureConsoleOutput(Runnable action) {
        PrintStream originalOut = System.out;
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        System.setOut(new PrintStream(bos));

        try {
            action.run();
        } finally {
            System.setOut(originalOut);
        }

        return Arrays.stream(bos.toString().split("\\r?\\n"))
                     .filter(line -> !line.trim().isEmpty())
                     .toList();
    }
}