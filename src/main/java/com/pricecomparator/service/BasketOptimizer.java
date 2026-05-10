package com.pricecomparator.service;
import java.io.BufferedWriter;
import java.io.File;
import java.io.OutputStreamWriter;
import java.io.FileOutputStream;
import java.nio.charset.StandardCharsets;
import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

import java.util.*;

import com.pricecomparator.repository.MarketDataRepository;
import com.pricecomparator.model.Discount;
import com.pricecomparator.model.Product;

public class BasketOptimizer {
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    private final MarketDataRepository marketDataRepository;

    public BasketOptimizer(MarketDataRepository marketDataRepository) {
        this.marketDataRepository = marketDataRepository;
    }

    /**
     * For each product, finds the store with the lowest price (considering discounts),
     * and outputs a shopping list split by store, with subtotals and total savings.
     *
     * @param basketProductIds List of product IDs (with repetitions for quantity)
     * @param date The date for which to optimize prices
     */
    public void optimizeBasketSplit(List<String> basketProductIds, String date) {
        // get data by the given date
        Map<String, List<Product>> storeProducts = marketDataRepository.getProductsForDate(date);

        // Maps to accumulate shopping list lines and subtotals per store
        Map<String, List<String>> storeToItems = new HashMap<>();
        Map<String, Double> storeToCost = new HashMap<>();

        double totalOriginalPrice = 0;
        double totalDiscountedPrice = 0;
        //[] Uses a map to count product quantities.
        Map<String, Integer> productCounts = new LinkedHashMap<>();
        for (String productId : basketProductIds) {
            productCounts.put(productId, productCounts.getOrDefault(productId, 0) + 1);
        }
        List<String> outputLines = new ArrayList<>();

        outputLines.add("Optimized Basket Split for " + date + ":\n");

        //[] For each unique product 
        for (Map.Entry<String, Integer> entry : productCounts.entrySet()) {
            String productId = entry.getKey();
            int quantity = entry.getValue();
            Product bestProduct = null;
            String bestStore = null;
            double bestFinalPrice = Double.MAX_VALUE;
            int appliedDiscount = 0;

            //[] Search all stores for the best price (with discount) 
            for (String store : storeProducts.keySet()) {
                Product product = marketDataRepository.getProduct(store, productId);
                if (product == null) continue;

                double price = product.getPrice();
                Discount discount = marketDataRepository.getActiveDiscount(store, productId, date);
                int discountPercent = discount != null ? discount.getDiscountPercent() : 0;

                double discountedPrice = price * (1 - discountPercent / 100.0);

                //[] Track the best (lowest) price and store
                if (discountedPrice < bestFinalPrice) {
                    bestProduct = product;
                    bestStore = store;
                    bestFinalPrice = discountedPrice;
                    appliedDiscount = discountPercent;
                }
            }

            if (bestProduct != null) {
                // Format the shopping list line for this product and store
                String line = "- " + bestProduct.getName() + (quantity > 1 ? " x" + quantity : "") + ": " + String.format("%.2f", bestFinalPrice * quantity)
                        + " RON" + (appliedDiscount > 0 ? " (-" + appliedDiscount + "%)" : "");

                // Add to the store's shopping list and subtotal
                storeToItems.computeIfAbsent(bestStore, k -> new ArrayList<>()).add(line);
                storeToCost.put(bestStore, storeToCost.getOrDefault(bestStore, 0.0) + bestFinalPrice * quantity);

                //[] Accumulate totals for original and discounted prices
                totalOriginalPrice += bestProduct.getPrice() * quantity;
                totalDiscountedPrice += bestFinalPrice * quantity;
            } else {
                outputLines.add("Product " + productId + " not found in any store.\n");
            }
        }

        //[] Output the shopping list
        for (String store : storeToItems.keySet()) {
            outputLines.add("\n" + capitalize(store) + " Shopping List:");
            outputLines.addAll(storeToItems.get(store));
            outputLines.add("Subtotal: " + String.format("%.2f", storeToCost.get(store)) + " RON\n");
        }

        // Output [] totals and savings
        double savings = totalOriginalPrice - totalDiscountedPrice;
        outputLines.add("Original total (no discounts): " + String.format("%.2f", totalOriginalPrice) + " RON");
        outputLines.add("Optimized total: " + String.format("%.2f", totalDiscountedPrice) + " RON");
        outputLines.add("Total money saved: " + String.format("%.2f", savings) + " RON");

        // Write the result to a file and print the location
        writeOutputToFile("output/optimized_basket_" + date + ".txt", outputLines);
        System.out.println("Result saved to: output/optimized_basket_" + date + ".txt");
    }

    private static void writeOutputToFile(String filePath, List<String> lines) {
        try {
            new File("output").mkdir(); // create output folder if missing
            BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(
                    new FileOutputStream(filePath), StandardCharsets.UTF_8));

            for (String line : lines) {
                writer.write(line);
                writer.newLine();
            }

            writer.close();
        } catch (IOException e) {
            System.err.println("Failed to write output file: " + e.getMessage());
        }
    }

    private static String capitalize(String text) {
        if (text == null || text.isEmpty()) return text;
        return text.substring(0, 1).toUpperCase() + text.substring(1).toLowerCase();
    }
}
