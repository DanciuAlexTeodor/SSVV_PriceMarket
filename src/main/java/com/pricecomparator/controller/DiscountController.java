package com.pricecomparator.controller;

import com.pricecomparator.model.Discount;
import com.pricecomparator.repository.MarketDataRepository;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/discounts")
@CrossOrigin(origins = "*")
public class DiscountController {

    private final MarketDataRepository marketDataRepository;

    public DiscountController(MarketDataRepository marketDataRepository) {
        this.marketDataRepository = marketDataRepository;
    }

    // Create a new discount
    @PostMapping
    public String addDiscount(
            @RequestParam String store, 
            @RequestParam String date, 
            @RequestBody Discount discount) {
        marketDataRepository.addDiscount(store, date, discount);
        return "Discount added successfully.";
    }

    // Update an existing discount
    @PutMapping("/{productId}")
    public String updateDiscount(
            @PathVariable String productId,
            @RequestParam String store, 
            @RequestParam String date, 
            @RequestBody Discount discount) {
        marketDataRepository.updateDiscount(store, date, productId, discount);
        return "Discount updated successfully.";
    }

    // Delete a discount
    @DeleteMapping("/{productId}")
    public String deleteDiscount(
            @PathVariable String productId,
            @RequestParam String store, 
            @RequestParam String date) {
        marketDataRepository.deleteDiscount(store, date, productId);
        return "Discount removed successfully.";
    }
}