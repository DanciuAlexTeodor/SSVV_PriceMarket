package com.pricecomparator.controller;

import com.pricecomparator.model.Product;
import com.pricecomparator.repository.MarketDataRepository;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/products")
@CrossOrigin(origins = "*") // Allows the frontend to access these endpoints
public class ProductController {

    private final MarketDataRepository marketDataRepository;

    public ProductController(MarketDataRepository marketDataRepository) {
        this.marketDataRepository = marketDataRepository;
    }

    // Create a new product
    @PostMapping
    public String addProduct(
            @RequestParam String store, 
            @RequestParam String date, 
            @RequestBody Product product) {
        marketDataRepository.addProduct(store, date, product);
        return "Product created successfully in " + store + " for date " + date;
    }

    // Update an existing product
    @PutMapping("/{id}")
    public String updateProduct(
            @PathVariable String id,
            @RequestParam String store, 
            @RequestParam String date, 
            @RequestBody Product product) {
        marketDataRepository.updateProduct(store, date, id, product);
        return "Product " + id + " updated successfully.";
    }

    // Delete a product
    @DeleteMapping("/{id}")
    public String deleteProduct(
            @PathVariable String id,
            @RequestParam String store, 
            @RequestParam String date) {
        marketDataRepository.deleteProduct(store, date, id);
        return "Product " + id + " deleted successfully.";
    }
}