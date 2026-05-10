package com.pricecomparator.repository;

import com.pricecomparator.model.Product;
import com.pricecomparator.model.Discount;
import com.pricecomparator.loader.MarketDataLoader;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.time.LocalDate;

public class MarketDataRepository {
    private final ProductRepository productRepository;
    private final DiscountRepository discountRepository;

    public MarketDataRepository(ProductRepository productRepository, DiscountRepository discountRepository) {
        this.productRepository = productRepository;
        this.discountRepository = discountRepository;
    }
    
    /**
     * Creates a MarketDataRepository by loading all data from files
     */
    public static MarketDataRepository createFromFiles() {
        Map<String, Map<LocalDate, List<Product>>> productData = MarketDataLoader.loadAllProductFiles();
        Map<String, Map<LocalDate, List<Discount>>> discountData = MarketDataLoader.loadAllDiscountFiles();
        
        return new MarketDataRepository(
            new ProductRepository(productData),
            new DiscountRepository(discountData)
        );
    }

    public Map<String, List<Product>> getProductsForDate(String date) {
        return productRepository.getProductsForDate(date);
    }
    
    public Map<String, List<Product>> getAllProductsBeforeDate(String date) {
        return productRepository.getAllProductsBeforeDate(date);
    }

    public Map<String, List<Discount>> getDiscountsForDate(String date) {
        return discountRepository.getDiscountsForDate(date);
    }
    
    public Map<String, List<Discount>> getAllDiscountsBeforeDate(String date) {
        return discountRepository.getAllDiscountsBeforeDate(date);
    }

    public Map<String, List<Discount>> getValidDiscountsForDate(String date) {
        Map<String, List<Discount>> result = new HashMap<>();
        
        // For each store, get active discounts on the target date
        for (String store : getDiscountsForDate(date).keySet()) {
            List<Discount> activeDiscounts = discountRepository.getActiveDiscounts(store, date);
            if (!activeDiscounts.isEmpty()) {
                result.put(store, activeDiscounts);
            }
        }
        
        return result;
    }

    public Product getProduct(String store, String productId, String date) {
        return productRepository.findProductById(store, productId, date);
    }
    
    public Product getProduct(String store, String productId) {
        // This is for backward compatibility - using current date
        LocalDate now = LocalDate.now();
        return productRepository.findProductById(store, productId, now.toString());
    }

    public Discount getActiveDiscount(String store, String productId, String date) {
        return discountRepository.findDiscountForProduct(store, productId, date);
    }
} 