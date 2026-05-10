package com.pricecomparator.repository;

import com.pricecomparator.model.Product;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

public class ProductRepository {
    private final Map<String, Map<LocalDate, List<Product>>> storeProductsByDate;
    private Map<String, List<Product>> cachedProducts;
    private LocalDate cachedDate;

    public ProductRepository(Map<String, Map<LocalDate, List<Product>>> storeProductsByDate) {
        this.storeProductsByDate = storeProductsByDate;
        this.cachedProducts = new HashMap<>();
    }

    /**
     * Gets all products for all stores for the most recent date before or on targetDate
     */
    public Map<String, List<Product>> getProductsForDate(String dateStr) {
        LocalDate targetDate = LocalDate.parse(dateStr);
        
        // Return cached result if already computed for this date
        if (targetDate.equals(cachedDate) && !cachedProducts.isEmpty()) {
            return cachedProducts;
        }
        
        Map<String, List<Product>> result = new HashMap<>();
        
        // For each store, find the most recent date before or on the target date
        for (String store : storeProductsByDate.keySet()) {
            Map<LocalDate, List<Product>> dateMap = storeProductsByDate.get(store);
            
            if (dateMap == null || dateMap.isEmpty()) {
                continue;
            }
            
            // Find the most recent date that's not after targetDate
            Optional<LocalDate> mostRecentDate = dateMap.keySet().stream()
                .filter(date -> !date.isAfter(targetDate))
                .max(LocalDate::compareTo);
                
            if (mostRecentDate.isPresent()) {
                result.put(store, dateMap.get(mostRecentDate.get()));
            }
        }
        
        // Cache the result
        cachedProducts = result;
        cachedDate = targetDate;
        
        return result;
    }
    
    /**
     * Gets all products for all stores with dates before or on targetDate
     */
    public Map<String, List<Product>> getAllProductsBeforeDate(String dateStr) {
        LocalDate targetDate = LocalDate.parse(dateStr);
        Map<String, List<Product>> result = new HashMap<>();
        
        // For each store, collect all products from dates before or on targetDate
        for (String store : storeProductsByDate.keySet()) {
            Map<LocalDate, List<Product>> dateMap = storeProductsByDate.get(store);
            
            if (dateMap == null || dateMap.isEmpty()) {
                continue;
            }
            
            List<Product> allProducts = dateMap.entrySet().stream()
                .filter(entry -> !entry.getKey().isAfter(targetDate))
                .flatMap(entry -> entry.getValue().stream())
                .collect(Collectors.toList());
                
            if (!allProducts.isEmpty()) {
                result.put(store, allProducts);
            }
        }
        
        return result;
    }

    public List<Product> getProductsByStore(String store, String dateStr) {
        Map<String, List<Product>> productsForDate = getProductsForDate(dateStr);
        return productsForDate.getOrDefault(store, List.of());
    }

    public Product findProductById(String store, String productId, String dateStr) {
        return getProductsByStore(store, dateStr).stream()
                .filter(p -> p.getId().equals(productId))
                .findFirst()
                .orElse(null);
    }

    public Map<String, Map<LocalDate, List<Product>>> getAllProductData() {
        return storeProductsByDate;
    }
} 