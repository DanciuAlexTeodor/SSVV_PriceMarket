package com.pricecomparator.controller;

import com.pricecomparator.service.BasketOptimizer;
import com.pricecomparator.repository.MarketDataRepository;
import org.springframework.web.bind.annotation.*;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

@RestController
@RequestMapping("/api/optimize")
@CrossOrigin(origins = "*")
public class OptimizerController {

    private final BasketOptimizer basketOptimizer;

    public OptimizerController(MarketDataRepository marketDataRepository) {
        this.basketOptimizer = new BasketOptimizer(marketDataRepository);
    }

    @PostMapping
    public String optimizeBasket(@RequestParam String date, @RequestBody List<String> productIds) {
        // Run the original optimization logic (which writes to output/optimized_basket_...)
        basketOptimizer.optimizeBasketSplit(productIds, date);
        
        // Read the generated file and return it as a structured string back to the frontend
        try {
            String fileName = "output/optimized_basket_" + date + ".txt";
            return Files.readString(Paths.get(fileName));
        } catch (IOException e) {
            return "Error reading optimization result: " + e.getMessage();
        }
    }
}