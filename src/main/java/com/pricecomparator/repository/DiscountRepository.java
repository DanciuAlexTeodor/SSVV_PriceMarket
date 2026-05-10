package com.pricecomparator.repository;

import com.pricecomparator.model.Discount;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

public class DiscountRepository {
    private final Map<String, Map<LocalDate, List<Discount>>> storeDiscountsByDate;
    private Map<String, List<Discount>> cachedDiscounts;
    private LocalDate cachedDate;
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    public DiscountRepository(Map<String, Map<LocalDate, List<Discount>>> storeDiscountsByDate) {
        this.storeDiscountsByDate = storeDiscountsByDate;
        this.cachedDiscounts = new HashMap<>();
    }

    /**
     * Gets all discounts for all stores for the most recent date before or on targetDate
     */
    public Map<String, List<Discount>> getDiscountsForDate(String dateStr) {
        LocalDate targetDate = LocalDate.parse(dateStr);
        
        // Return cached result if already computed for this date
        if (targetDate.equals(cachedDate) && !cachedDiscounts.isEmpty()) {
            return cachedDiscounts;
        }
        
        Map<String, List<Discount>> result = new HashMap<>();
        
        // For each store, find the most recent date before or on the target date
        for (String store : storeDiscountsByDate.keySet()) {
            Map<LocalDate, List<Discount>> dateMap = storeDiscountsByDate.get(store);
            
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
        cachedDiscounts = result;
        cachedDate = targetDate;
        
        return result;
    }
    
    /**
     * Gets all discounts for all stores with dates before or on targetDate
     */
    public Map<String, List<Discount>> getAllDiscountsBeforeDate(String dateStr) {
        LocalDate targetDate = LocalDate.parse(dateStr);
        Map<String, List<Discount>> result = new HashMap<>();
        
        // For each store, collect all discounts from dates before or on targetDate
        for (String store : storeDiscountsByDate.keySet()) {
            Map<LocalDate, List<Discount>> dateMap = storeDiscountsByDate.get(store);
            
            if (dateMap == null || dateMap.isEmpty()) {
                continue;
            }
            
            List<Discount> allDiscounts = dateMap.entrySet().stream()
                .filter(entry -> !entry.getKey().isAfter(targetDate))
                .flatMap(entry -> entry.getValue().stream())
                .collect(Collectors.toList());
                
            if (!allDiscounts.isEmpty()) {
                result.put(store, allDiscounts);
            }
        }
        
        return result;
    }

    public List<Discount> getDiscountsByStore(String store, String dateStr) {
        Map<String, List<Discount>> discountsForDate = getDiscountsForDate(dateStr);
        return discountsForDate.getOrDefault(store, List.of());
    }

    public List<Discount> getActiveDiscounts(String store, String dateStr) {
        LocalDate targetDate = LocalDate.parse(dateStr, DATE_FORMATTER);
        List<Discount> storeDiscounts = getDiscountsByStore(store, dateStr);
        
        return storeDiscounts.stream()
            .filter(d -> isDiscountValid(d, targetDate))
            .collect(Collectors.toList());
    }

    public Discount findDiscountForProduct(String store, String productId, String dateStr) {
        LocalDate targetDate = LocalDate.parse(dateStr, DATE_FORMATTER);
        return getDiscountsByStore(store, dateStr).stream()
            .filter(d -> d.getProductId().equals(productId))
            .filter(d -> isDiscountValid(d, targetDate))
            .findFirst()
            .orElse(null);
    }

    private boolean isDiscountValid(Discount discount, LocalDate targetDate) {
        LocalDate fromDate = LocalDate.parse(discount.getFromDate(), DATE_FORMATTER);
        LocalDate toDate = LocalDate.parse(discount.getToDate(), DATE_FORMATTER);
        return !targetDate.isBefore(fromDate) && !targetDate.isAfter(toDate);
    }

    public Map<String, Map<LocalDate, List<Discount>>> getAllDiscountData() {
        return storeDiscountsByDate;
    }
} 