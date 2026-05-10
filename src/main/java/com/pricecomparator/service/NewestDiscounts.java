package com.pricecomparator.service;

import java.util.*;

import com.pricecomparator.model.Discount;
import com.pricecomparator.repository.MarketDataRepository;

public class NewestDiscounts {
    private final MarketDataRepository marketDataRepository;
    
    public NewestDiscounts(MarketDataRepository marketDataRepository) {
        this.marketDataRepository = marketDataRepository;
    }
    
    public void showNewestDiscounts(String store, String todayDate) {
        // Normalize store name for consistency
        if (!store.equals("All stores")) {
            store = store.substring(0, 1).toUpperCase() + store.substring(1).toLowerCase();
        }
        
        Map<String, List<Discount>> storeDiscounts = marketDataRepository.getValidDiscountsForDate(todayDate);
        
        storeDiscounts.forEach((storeName, discounts) -> {
            //[] This filter keeps ONLY discounts that start exactly on todayDate
            discounts.removeIf(discount -> !discount.getFromDate().equals(todayDate));
        });


        // Display for all stores option
        if (store.equals("All stores")) {
            System.out.println("Newest discounts for all stores on " + todayDate + ":");
            storeDiscounts.forEach((storeName, discounts) -> {
                if (!discounts.isEmpty()) {
                    System.out.println("Store: " + storeName);
                    for (Discount discount : discounts) {
                        System.out.println(discount);
                    }
                }
            });
        } else {
            // Display for each store individually
            List<Discount> discounts = storeDiscounts.get(store);
            if (discounts == null || discounts.isEmpty()) {
                System.out.println("No discounts found for " + store + " on " + todayDate);
                return;
            }
            System.out.println("Newest discounts for " + store + " on " + todayDate + ":");
            for (Discount discount : discounts) {
                System.out.println(discount);
            }
        }
    }
    
}
