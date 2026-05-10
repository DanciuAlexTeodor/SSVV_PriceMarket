package com.pricecomparator.repository;

import com.pricecomparator.model.Product;
import com.pricecomparator.model.Discount;
import com.pricecomparator.loader.MarketDataLoader;
import java.util.ArrayList;
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

    // --- Admin CRUD Operations for Discount ---
    public void addDiscount(String store, String dateStr, Discount discount) {
        discountRepository.addDiscount(store, dateStr, discount);
    }
    public void updateDiscount(String store, String dateStr, String productId, Discount newDiscount) {
        discountRepository.updateDiscount(store, dateStr, productId, newDiscount);
    }
    public void deleteDiscount(String store, String dateStr, String productId) {
        discountRepository.deleteDiscount(store, dateStr, productId);
    }

    // --- Admin CRUD Operations for Product ---
    public void addProduct(String store, String dateStr, Product product) {
        LocalDate date = LocalDate.parse(dateStr);
        Map<String, Map<LocalDate, List<Product>>> allData = productRepository.getAllProductData();
        
        // Ensure store and date structures exist directly in the raw data
        allData.computeIfAbsent(store, k -> new HashMap<>())
               .computeIfAbsent(date, k -> new ArrayList<>());

        List<Product> list = allData.get(store).get(date);
        
        // If it already exists for this exact date, replace it, otherwise add it.
        boolean found = false;
        for (int i = 0; i < list.size(); i++) {
            if (list.get(i).getId().equals(product.getId())) {
                list.set(i, product);
                found = true;
                break;
            }
        }
        if (!found) {
            list.add(product);
        }
        
        // Clear the cache so searches immediately see the new data
        productRepository.clearCache();
    }

    public void updateProduct(String store, String dateStr, String productId, Product newProduct) {
        LocalDate date = LocalDate.parse(dateStr);
        Map<String, Map<LocalDate, List<Product>>> allData = productRepository.getAllProductData();
        
        if (allData.containsKey(store) && allData.get(store).containsKey(date)) {
            List<Product> list = allData.get(store).get(date);
            for (int i = 0; i < list.size(); i++) {
                if (list.get(i).getId().equals(productId)) {
                    list.set(i, newProduct);
                    break;
                }
            }
        }
        
        // Clear the cache so searches immediately see the updated data
        productRepository.clearCache();
    }

    public void deleteProduct(String store, String dateStr, String productId) {
        LocalDate date = LocalDate.parse(dateStr);
        Map<String, Map<LocalDate, List<Product>>> allData = productRepository.getAllProductData();
        
        if (allData.containsKey(store) && allData.get(store).containsKey(date)) {
            allData.get(store).get(date).removeIf(p -> p.getId().equals(productId));
        }
        
        // Clear the cache so searches immediately reflect the deletion
        productRepository.clearCache();
    }
} 