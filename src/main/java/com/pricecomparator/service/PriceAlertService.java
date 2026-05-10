package com.pricecomparator.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.pricecomparator.model.PriceAlert;
import com.pricecomparator.model.Product;
import com.pricecomparator.repository.AlertRepository;
import com.pricecomparator.repository.MarketDataRepository;
import com.pricecomparator.model.Discount;

public class PriceAlertService {
    private final AlertRepository alertRepository;
    private final MarketDataRepository marketDataRepository;

    public PriceAlertService(AlertRepository alertRepository, MarketDataRepository marketDataRepository) {
        this.alertRepository = alertRepository;
        this.marketDataRepository = marketDataRepository;
    }

    public void createAlert(String productId, String productName, double targetPrice, String userId) {
        PriceAlert alert = new PriceAlert(productId, productName, targetPrice, userId);
        alertRepository.addAlert(alert);
    }

    public List<PriceAlert> getActiveAlerts() {
        return alertRepository.getActiveAlerts();
    }

    public List<PriceAlert> checkAlerts(String date) {
        List<PriceAlert> triggeredAlerts = new ArrayList<>();
        // Getting the products for the current day
        Map<String, List<Product>> storeProducts = marketDataRepository.getProductsForDate(date);

        
        for (PriceAlert alert : alertRepository.getActiveAlerts()) {
            // For each active alert I am searching for the best price I can find
            double bestPrice = findBestPrice(alert.getProductId(), storeProducts, date);
            
            if (bestPrice <= alert.getTargetPrice()) {
                triggeredAlerts.add(alert);
                System.out.println("PRICE ALERT: " + alert.getProductName() + 
                                  " is now available at " + bestPrice + 
                                  " (target: " + alert.getTargetPrice() + ")");
            }
        }
        return triggeredAlerts;
    }

    private double findBestPrice(String productId, Map<String, List<Product>> storeProducts, String date) {
        double bestPrice = Double.MAX_VALUE;
        
        for (String store : storeProducts.keySet()) {
            Product product = storeProducts.get(store).stream()
                .filter(p -> p.getId().equals(productId))
                .findFirst()
                .orElse(null);
                
            if (product != null) {
                double price = product.getPrice();
                
                // Apply any active discounts
                Discount discount = marketDataRepository.getActiveDiscount(store, productId, date);
                if (discount != null) {
                    price = price * (1 - discount.getDiscountPercent() / 100.0);
                }
                
                // Update best price if this one is lower
                if (price < bestPrice) {
                    bestPrice = price;
                    //System.out.println("Found price " + price + " for " + product.getName() + " at " + store);
                }
            }
        }
        
        if (bestPrice == Double.MAX_VALUE) {
            // If no price found, return a very high value to prevent false triggers
            return Double.MAX_VALUE;
        }
        
        return bestPrice;
    }

    public void deleteAlert(String productId) {
        alertRepository.deleteAlert(productId);
    }
}
