package com.pricecomparator.service;

import java.util.*;
import com.pricecomparator.repository.MarketDataRepository;
import com.pricecomparator.model.Discount;

public class BestDiscounts {
    private final MarketDataRepository marketDataRepository;

    public BestDiscounts(MarketDataRepository marketDataRepository) {
        this.marketDataRepository = marketDataRepository;
    }

    public void showBestDiscounts(String store, String date, int numberOfOffers) {
        // Normalize store name for consistency
        if (!store.equals("All stores")) {
            store = store.substring(0, 1).toUpperCase() + store.substring(1).toLowerCase();
        }
        
        //[] Get the discounts
        Map<String, List<Discount>> storeDiscounts = marketDataRepository.getValidDiscountsForDate(date);
        
        if (store.equals("All stores")) {
            // /* Handle all stores - merge all discounts
            List<Discount> allDiscounts = new ArrayList<>();
            storeDiscounts.forEach((storeName, discounts) -> {
                allDiscounts.addAll(discounts);
            });
            
            if (allDiscounts.isEmpty()) {
                System.out.println("No discounts found for any store on " + date);
                return;
            }
            
            //[] Sort discounts by discount percentage in descending order
            allDiscounts.sort((d1, d2) -> Integer.compare(d2.getDiscountPercent(), d1.getDiscountPercent()));
            
            System.out.println("Top " + numberOfOffers + " discounts across all stores on " + date + ":");
            for (int i = 0; i < Math.min(numberOfOffers, allDiscounts.size()); i++) {
                System.out.println(allDiscounts.get(i));
            }
        } else {
            // Handle specific store
            List<Discount> discounts = storeDiscounts.get(store);
            
            if (discounts == null || discounts.isEmpty()) {
                System.out.println("No discounts found for " + store + " on " + date);
                return;
            }
            
            // Sort discounts by discount percentage in descending order
            discounts.sort((d1, d2) -> Integer.compare(d2.getDiscountPercent(), d1.getDiscountPercent()));
            
            //[] Output the result
            System.out.println("Top " + numberOfOffers + " discounts for " + store + " on " + date + ":");
            for (int i = 0; i < Math.min(numberOfOffers, discounts.size()); i++) {
                System.out.println(discounts.get(i));
            }
        }
    }
}
