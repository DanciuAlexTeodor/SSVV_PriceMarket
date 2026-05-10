package com.pricecomparator.loader;

import java.io.File;
import java.util.*;
import java.time.LocalDate;

import com.pricecomparator.model.Discount;
import com.pricecomparator.model.Product;

public class MarketDataLoader {
    
    public static final String RESOURCE_PATH = "src/main/resources/";

    /**
     * Loads all product files from resources folder
     */
    public static Map<String, Map<LocalDate, List<Product>>> loadAllProductFiles() {
        Map<String, Map<LocalDate, List<Product>>> storeProductsByDate = new HashMap<>();
        File folder = new File(RESOURCE_PATH);
        File[] files = folder.listFiles();
        
        if (files == null) return storeProductsByDate;
        
        for (File file : files) {
            String fileName = file.getName();
            if (fileName.endsWith(".csv") && !fileName.contains("discounts")) {
                String[] parts = fileName.replace(".csv", "").split("_");
                if (parts.length < 2) continue;
                String storeName = parts[0];
                // Normalize store name: capitalize first letter, lowercase the rest
                storeName = storeName.substring(0, 1).toUpperCase() + storeName.substring(1).toLowerCase();
                String fileDateStr = parts[1];
                
                try {
                    LocalDate fileDate = LocalDate.parse(fileDateStr);
                    List<Product> products = ProductLoader.loadFromCSV(file.getPath());
                    
                    // Set datePosted for each product based on file date
                    for (Product product : products) {
                        product.setDatePosted(fileDateStr);
                    }
                    
                    // Get or create the date map for this store
                    Map<LocalDate, List<Product>> dateMap = storeProductsByDate.computeIfAbsent(storeName, k -> new HashMap<>());
                    dateMap.put(fileDate, products);
                    
                    //System.out.println("Loaded " + products.size() + " products for " + storeName + " from " + fileName);
                } catch (Exception e) {
                    System.out.println("Error loading products from " + fileName + ": " + e.getMessage());
                }
            }
        }
        
        return storeProductsByDate;
    }

    /**
     * Loads all discount files from resources folder
     */
    public static Map<String, Map<LocalDate, List<Discount>>> loadAllDiscountFiles() {
        Map<String, Map<LocalDate, List<Discount>>> storeDiscountsByDate = new HashMap<>();
        File folder = new File(RESOURCE_PATH);
        File[] files = folder.listFiles();
        
        if (files == null) return storeDiscountsByDate;
        
        for (File file : files) {
            String fileName = file.getName();
            if (fileName.contains("discounts") && fileName.endsWith(".csv")) {
                String[] parts = fileName.replace(".csv", "").split("_");
                if (parts.length < 3) continue;
                String storeName = parts[0];
                // Normalize store name: capitalize first letter, lowercase the rest
                storeName = storeName.substring(0, 1).toUpperCase() + storeName.substring(1).toLowerCase();
                String fileDateStr = parts[2];
                
                try {
                    LocalDate fileDate = LocalDate.parse(fileDateStr);
                    List<Discount> discounts = DiscountLoader.loadFromCSV(file.getPath());
                    
                    // Set datePosted for each discount based on file date
                    for (Discount discount : discounts) {
                        discount.setDatePosted(fileDateStr);
                    }
                    
                    // Get or create the date map for this store
                    Map<LocalDate, List<Discount>> dateMap = storeDiscountsByDate.computeIfAbsent(storeName, k -> new HashMap<>());
                    dateMap.put(fileDate, discounts);
                    
                    //System.out.println("Loaded " + discounts.size() + " discounts for " + storeName + " from " + fileName);
                } catch (Exception e) {
                    System.out.println("Error loading discounts from " + fileName + ": " + e.getMessage());
                }
            }
        }
        
        return storeDiscountsByDate;
    }
}
