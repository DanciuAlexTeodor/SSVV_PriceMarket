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
        // Return the formatted string generated directly by the optimizer
        return basketOptimizer.optimizeBasketSplit(productIds, date);
    }
}