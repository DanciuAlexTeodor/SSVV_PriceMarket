package com.pricecomparator.service;

import java.util.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.stream.Collectors;

import com.pricecomparator.model.Discount;
import com.pricecomparator.model.PricePoint;
import com.pricecomparator.model.Product;
import com.pricecomparator.repository.MarketDataRepository;

public class PriceDataService {
    private final MarketDataRepository marketDataRepository;
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    public PriceDataService() {
        this.marketDataRepository = MarketDataRepository.createFromFiles();
    }

    public PriceDataService(MarketDataRepository marketDataRepository) {
        this.marketDataRepository = marketDataRepository;
    }
    
    /**
     * Show data points for a product with optional filters
     * 
     * @param productIdInput The product ID entered by the user
     * @param date The date to check (products before this date)
     * @return List of price points
     */
    public List<PricePoint> showDataPointsForProduct(String productIdInput, String date) {
        return showDataPointsForProduct(productIdInput, date, null, null, null);
    }

    /**
     * Shows the price history and discount timeline for a product, with optional filters.
     * This method is split into smaller helpers for clarity and maintainability.
     */
    public List<PricePoint> showDataPointsForProduct(String productIdInput, String date, 
                                              String filterCategory, String filterBrand, String filterStore) {
        // Get all products and discounts before the given date
        Map<String, List<Product>> storeProducts = marketDataRepository.getAllProductsBeforeDate(date);
        Map<String, List<Discount>> storeDiscounts = marketDataRepository.getAllDiscountsBeforeDate(date);
        List<PricePoint> pricePoints = new ArrayList<>();

        // 1. Find the product name for the given product ID
        String targetProductName = findProductNameById(productIdInput, storeProducts);
        if (targetProductName == null || targetProductName.isEmpty()) {
            System.out.println("Product not found with ID: " + productIdInput);
            return pricePoints;
        }

        System.out.println("\n===== Price History for " + targetProductName + " =====");
        if (filterCategory != null) System.out.println("Filter: Category = " + filterCategory);
        if (filterBrand != null) System.out.println("Filter: Brand = " + filterBrand);
        if (filterStore != null) System.out.println("Filter: Store = " + filterStore);

        // 2. Group product IDs by store for this product name
        Map<String, Set<String>> storeProductIds = groupProductIdsByStore(targetProductName, storeProducts);

        // 3. Build the price timeline for each store
        Map<String, SortedMap<LocalDate, PricePoint>> priceTimeline = new HashMap<>();
        Map<String, Product> latestProducts = new HashMap<>();
        buildPriceTimelines(storeProducts, storeDiscounts, storeProductIds, filterCategory, filterBrand, filterStore, priceTimeline, latestProducts, pricePoints);

        // 4. Print the timeline and available filters
        printPriceTimelineAndFilters(priceTimeline, storeProductIds, latestProducts, targetProductName, storeProducts);

        return pricePoints;
    }

    /**
     * Groups all product IDs by store for a given product name.
     */
    private Map<String, Set<String>> groupProductIdsByStore(String targetProductName, Map<String, List<Product>> storeProducts) {
        Map<String, Set<String>> storeProductIds = new HashMap<>();
        for (String store : storeProducts.keySet()) {
            for (Product product : storeProducts.get(store)) {
                if (product.getName().equalsIgnoreCase(targetProductName)) {
                    storeProductIds.computeIfAbsent(store, k -> new HashSet<>()).add(product.getId());
                }
            }
        }
        return storeProductIds;
    }

    /**
     * Builds the price timeline for each store, including discounts and price changes.
     */
    private void buildPriceTimelines(
            Map<String, List<Product>> storeProducts,
            Map<String, List<Discount>> storeDiscounts,
            Map<String, Set<String>> storeProductIds,
            String filterCategory,
            String filterBrand,
            String filterStore,
            Map<String, SortedMap<LocalDate, PricePoint>> priceTimeline,
            Map<String, Product> latestProducts,
            List<PricePoint> pricePoints
    ) {
        for (String store : storeProducts.keySet()) {
            // Apply store filter if specified
            if (filterStore != null && !store.equalsIgnoreCase(filterStore)) {
                continue;
            }
            // Skip stores that don't have any matching products
            if (!storeProductIds.containsKey(store)) continue;
            // Get all matching product IDs for this store
            Set<String> matchingProductIds = storeProductIds.get(store);
            // Filter products that match any of the matching IDs
            List<Product> storeProductList = storeProducts.get(store).stream()
                .filter(p -> matchingProductIds.contains(p.getId()))
                .collect(Collectors.toList());
            if (storeProductList.isEmpty()) continue;
            // Apply category and brand filters
            storeProductList = storeProductList.stream()
                .filter(p -> filterCategory == null || p.getCategory().equalsIgnoreCase(filterCategory))
                .filter(p -> filterBrand == null || p.getBrand().equalsIgnoreCase(filterBrand))
                .collect(Collectors.toList());
            if (storeProductList.isEmpty()) continue;
            // Sort products by date (older to newer)
            storeProductList.sort(Comparator.comparing(p -> 
                LocalDate.parse(p.getDatePosted() != null ? p.getDatePosted() : "2025-05-01")));
            // Keep track of the latest product for this store
            latestProducts.put(store, storeProductList.get(storeProductList.size() - 1));
            // Initialize timeline for this store
            SortedMap<LocalDate, PricePoint> storeTimeline = new TreeMap<>();
            priceTimeline.put(store, storeTimeline);
            // Add initial price points for each product update
            for (Product product : storeProductList) {
                String fileDate = product.getDatePosted();
                if (fileDate == null) {
                    System.out.println("Warning: Product " + product.getName() + " at " + 
                        store + " has no posted date");
                    continue;
                }
                LocalDate productDate = LocalDate.parse(fileDate);
                double basePrice = product.getPrice();
                // Create initial price point at the product's date
                PricePoint basePoint = new PricePoint(
                    product.getId(),
                    product.getName(),
                    product.getCategory(),
                    product.getBrand(),
                    basePrice,
                    store,
                    fileDate
                );
                storeTimeline.put(productDate, basePoint);
            }
            // Add discount price points for all matching product IDs at this store
            addDiscountPricePoints(store, matchingProductIds, storeDiscounts, storeProductList, storeTimeline);
            // Add all timeline points to our result list
            pricePoints.addAll(storeTimeline.values());
        }
    }

    /**
     * Adds price points for discounts to the store's price timeline.
     */
    private void addDiscountPricePoints(
            String store,
            Set<String> matchingProductIds,
            Map<String, List<Discount>> storeDiscounts,
            List<Product> storeProductList,
            SortedMap<LocalDate, PricePoint> storeTimeline
    ) {
        for (String productId : matchingProductIds) {
            List<Discount> productDiscounts = storeDiscounts.getOrDefault(store, Collections.emptyList())
                .stream()
                .filter(d -> d.getProductId().equals(productId))
                .collect(Collectors.toList());
            if (!productDiscounts.isEmpty()) {
                // For each discount, add price points at start and end dates
                for (Discount discount : productDiscounts) {
                    LocalDate startDate = LocalDate.parse(discount.getFromDate());
                    LocalDate endDate = LocalDate.parse(discount.getToDate());
                    // Find which product price applies at this discount's start date
                    Product applicableProduct = findProductForDate(storeProductList, startDate);
                    if (applicableProduct == null) continue;
                    double basePrice = applicableProduct.getPrice();
                    double discountedPrice = basePrice * (1 - discount.getDiscountPercent() / 100.0);
                    // Create price point at discount start date with discounted price
                    PricePoint startPoint = new PricePoint(
                        applicableProduct.getId(),
                        applicableProduct.getName(),
                        applicableProduct.getCategory(),
                        applicableProduct.getBrand(),
                        discountedPrice,
                        store,
                        discount.getFromDate()
                    );
                    storeTimeline.put(startDate, startPoint);
                    // Create price point at discount end date (day after) with regular price
                    // But only if there's no other price change on that exact date
                    LocalDate dayAfterDiscount = endDate.plusDays(1);
                    // Find which product price applies after the discount ends
                    Product applicableProductAfter = findProductForDate(storeProductList, dayAfterDiscount);
                    if (applicableProductAfter != null) {
                        double afterPrice = applicableProductAfter.getPrice();
                        // Add a price point for the return to regular price
                        PricePoint endPoint = new PricePoint(
                            applicableProductAfter.getId(),
                            applicableProductAfter.getName(),
                            applicableProductAfter.getCategory(),
                            applicableProductAfter.getBrand(),
                            afterPrice,
                            store,
                            dayAfterDiscount.format(DATE_FORMATTER)
                        );
                        // Only add if there's not already a price point for this date
                        if (!storeTimeline.containsKey(dayAfterDiscount)) {
                            storeTimeline.put(dayAfterDiscount, endPoint);
                        }
                    }
                }
            }
        }
    }

    /**
     * Prints the price timeline and available filter options for the user.
     */
    private void printPriceTimelineAndFilters(
            Map<String, SortedMap<LocalDate, PricePoint>> priceTimeline,
            Map<String, Set<String>> storeProductIds,
            Map<String, Product> latestProducts,
            String targetProductName,
            Map<String, List<Product>> storeProducts
    ) {
        if (priceTimeline.isEmpty()) {
            System.out.println("No price data found for product: " + targetProductName);
        } else {
            System.out.println("\n===== Price Timeline by Store =====");
            for (String store : priceTimeline.keySet()) {
                System.out.println("\nStore: " + store + " (Product IDs: " + String.join(", ", storeProductIds.get(store)) + ")");
                SortedMap<LocalDate, PricePoint> timeline = priceTimeline.get(store);
                for (Map.Entry<LocalDate, PricePoint> entry : timeline.entrySet()) {
                    LocalDate pointDate = entry.getKey();
                    PricePoint point = entry.getValue();
                    // Find if this price is a discount by checking if it's less than the base price
                    Product latestProduct = latestProducts.get(store);
                    boolean isDiscount = point.getPrice() < latestProduct.getPrice();
                    System.out.printf("  %s: %.2f RON%s (ID: %s)%n", 
                        pointDate, 
                        point.getPrice(),
                        isDiscount ? " (discounted)" : "",
                        point.getId());
                }
            }
        }
        // Show filter options at the end for user reference
        System.out.println("\nAvailable filter options:");
        Set<String> availableCategories = new HashSet<>();
        Set<String> availableBrands = new HashSet<>();
        Set<String> availableStores = new HashSet<>();
        for (String store : storeProducts.keySet()) {
            if (storeProductIds.containsKey(store)) {
                availableStores.add(store);
                for (Product product : storeProducts.get(store)) {
                    if (product.getName().equalsIgnoreCase(targetProductName)) {
                        availableCategories.add(product.getCategory());
                        availableBrands.add(product.getBrand());
                    }
                }
            }
        }
        System.out.println("Categories: " + String.join(", ", availableCategories));
        System.out.println("Brands: " + String.join(", ", availableBrands));
        System.out.println("Stores: " + String.join(", ", availableStores));
    }

    /**
     * Find the product name for a given product ID
     * @param productId The ID to look up
     * @param storeProducts Map of store products
     * @return The product name, or null if not found
     */
    private String findProductNameById(String productId, Map<String, List<Product>> storeProducts) {
        for (String store : storeProducts.keySet()) {
            for (Product product : storeProducts.get(store)) {
                if (product.getId().equals(productId)) {
                    return product.getName();
                }
            }
        }
        return null;
    }
    
    /**
     * Find which product's price is applicable for a given date
     * Returns the most recent product before or on the given date
     */
    private Product findProductForDate(List<Product> products, LocalDate date) {
        if (products == null || products.isEmpty()) return null;
        
        // Start with the first product as a fallback
        Product applicable = products.get(0);
        
        for (Product product : products) {
            if (product.getDatePosted() == null) continue;
            
            LocalDate productDate = LocalDate.parse(product.getDatePosted());
            
            // If this product is more recent than our current pick,
            // but still not after the target date, use it
            if (!productDate.isAfter(date) && 
                (applicable.getDatePosted() == null || 
                 productDate.isAfter(LocalDate.parse(applicable.getDatePosted())))) {
                applicable = product;
            }
        }
        
        return applicable;
    }
    
    /**
     * Get the file date from a product
     */
    private String getProductFileDate(Product product, String store) {
        // Get the datePosted from the product if available
        if (product.getDatePosted() != null) {
            return product.getDatePosted();
        }
        
        // Fallback to current date if we can't determine file date
        return LocalDate.now().format(DATE_FORMATTER);
    }
}
