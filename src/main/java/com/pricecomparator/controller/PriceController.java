package com.pricecomparator.controller;

import com.pricecomparator.model.PricePoint;
import com.pricecomparator.service.PriceDataService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.CrossOrigin;

import java.util.List;

@RestController
@RequestMapping("/api/prices")
@CrossOrigin(origins = "*") // Allows the frontend to make requests without CORS errors
public class PriceController {

    private final PriceDataService priceDataService;

    // Spring automatically injects the PriceDataService Bean we declared in AppConfig
    public PriceController(PriceDataService priceDataService) {
        this.priceDataService = priceDataService;
    }

    // Endpoint: GET /api/prices/P001?date=2025-05-10
    @GetMapping("/{productId}")
    public List<PricePoint> getPricePoints(
            @PathVariable String productId,
            @RequestParam(defaultValue = "2026-05-10") String date) {
        
        // This calls the existing logic you already had for the console app!
        return priceDataService.showDataPointsForProduct(productId, date);
    }
}