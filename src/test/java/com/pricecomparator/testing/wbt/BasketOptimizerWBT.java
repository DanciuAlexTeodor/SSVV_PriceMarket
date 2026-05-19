package com.pricecomparator.service.testing.wbt;

import com.pricecomparator.model.Product;
import com.pricecomparator.model.Discount;
import com.pricecomparator.repository.MarketDataRepository;
import com.pricecomparator.service.BasketOptimizer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * White Box Tests for BasketOptimizer Service
 *
 * Tests internal implementation including:
 * - All branches in discount calculation
 * - HashMap operations for product counting and store organization
 * - Price comparison logic (< operator)
 * - Discount application formula: price * (1 - discount/100)
 * - Subtotal accumulation logic
 * - Store selection logic
 */
@DisplayName("BasketOptimizer - White Box Testing")
class BasketOptimizerWBT {
    private MarketDataRepository repo;
    private BasketOptimizer optimizer;
    private final String testDate = "2025-05-01";

    @BeforeEach
    void setUp() {
        repo = mock(MarketDataRepository.class);
        optimizer = new BasketOptimizer(repo);
    }

    // ==================== DISCOUNT APPLICATION LOGIC ====================
    @Nested
    @DisplayName("Discount Application Branch Coverage")
    class DiscountApplicationBranches {
        @Test
        @DisplayName("Branch: discount != null - applies discount percentage")
        void testBranch_DiscountNotNull_AppliesDiscount() {
            // Arrange - Product with 15% discount
            Product product = new Product("P1", "Item", "Cat", "Brand", 1.0, "unit", 100.0, "RON");
            Discount discount = mock(Discount.class);
            when(discount.getDiscountPercent()).thenReturn(15);

            Map<String, List<Product>> data = Map.of("Store", List.of(product));
            when(repo.getProductsForDate(testDate)).thenReturn(data);
            when(repo.getProduct("Store", "P1")).thenReturn(product);
            when(repo.getActiveDiscount("Store", "P1", testDate)).thenReturn(discount);

            // Act
            String result = optimizer.optimizeBasketSplit(List.of("P1"), testDate);

            // Assert - Should apply formula: 100 * (1 - 15/100) = 85
            assertTrue(result.contains("85.0"), "Should calculate 100 * (1 - 0.15) = 85");
            assertTrue(result.contains("(-15%"), "Should show discount percentage");
        }

        @Test
        @DisplayName("Branch: discount == null - uses regular price")
        void testBranch_DiscountNull_RegularPrice() {
            // Arrange - No discount available
            Product product = new Product("P1", "Item", "Cat", "Brand", 1.0, "unit", 100.0, "RON");

            Map<String, List<Product>> data = Map.of("Store", List.of(product));
            when(repo.getProductsForDate(testDate)).thenReturn(data);
            when(repo.getProduct("Store", "P1")).thenReturn(product);
            when(repo.getActiveDiscount("Store", "P1", testDate)).thenReturn(null);

            // Act
            String result = optimizer.optimizeBasketSplit(List.of("P1"), testDate);

            // Assert - Should use price as-is without discount
            assertTrue(result.contains("100.0"), "Should use regular price 100");
            assertFalse(result.contains("(-%"), "Should not show discount");
        }

        @Test
        @DisplayName("Discount formula calculation: price * (1 - discount%/100)")
        void testDiscountFormula_Calculation() {
            // Arrange - Test discount formula with various percentages
            Product product = new Product("P1", "Item", "Cat", "Brand", 1.0, "unit", 200.0, "RON");
            Discount discount = mock(Discount.class);
            when(discount.getDiscountPercent()).thenReturn(25); // 25% off

            Map<String, List<Product>> data = Map.of("Store", List.of(product));
            when(repo.getProductsForDate(testDate)).thenReturn(data);
            when(repo.getProduct("Store", "P1")).thenReturn(product);
            when(repo.getActiveDiscount("Store", "P1", testDate)).thenReturn(discount);

            // Act
            String result = optimizer.optimizeBasketSplit(List.of("P1"), testDate);

            // Assert - 200 * (1 - 25/100) = 200 * 0.75 = 150
            assertTrue(result.contains("150.0"), "Should calculate 200 * (1 - 0.25) = 150");
        }
    }

    // ==================== PRICE COMPARISON LOGIC ====================
    @Nested
    @DisplayName("Price Comparison and Store Selection")
    class PriceComparisonLogic {
        @Test
        @DisplayName("Decision: discountedPrice < bestFinalPrice - selects lower price")
        void testDecision_LowerPrice_SelectsThisStore() {
            // Arrange - Product cheaper at second store
            Product expensiveStore = new Product("P1", "Item", "Cat", "Brand", 1.0, "unit", 100.0, "RON");
            Product cheapStore = new Product("P1", "Item", "Cat", "Brand", 1.0, "unit", 80.0, "RON");

            Map<String, List<Product>> data = Map.of(
                "Store1", List.of(expensiveStore),
                "Store2", List.of(cheapStore)
            );
            when(repo.getProductsForDate(testDate)).thenReturn(data);
            when(repo.getProduct("Store1", "P1")).thenReturn(expensiveStore);
            when(repo.getProduct("Store2", "P1")).thenReturn(cheapStore);
            when(repo.getActiveDiscount(anyString(), anyString(), anyString())).thenReturn(null);

            // Act
            String result = optimizer.optimizeBasketSplit(List.of("P1"), testDate);

            // Assert - Should select Store2 with price 80
            assertTrue(result.contains("Store2"), "Should select cheaper store");
            assertTrue(result.contains("80.0"), "Should show Store2's price");
        }

        @Test
        @DisplayName("Decision: discountedPrice < bestFinalPrice - false, keeps previous")
        void testDecision_HigherPrice_KeepsPrevious() {
            // Arrange - First store has best price
            Product bestStore = new Product("P1", "Item", "Cat", "Brand", 1.0, "unit", 75.0, "RON");
            Product worseStore = new Product("P1", "Item", "Cat", "Brand", 1.0, "unit", 90.0, "RON");

            Map<String, List<Product>> data = Map.of(
                "Store1", List.of(bestStore),
                "Store2", List.of(worseStore)
            );
            when(repo.getProductsForDate(testDate)).thenReturn(data);
            when(repo.getProduct("Store1", "P1")).thenReturn(bestStore);
            when(repo.getProduct("Store2", "P1")).thenReturn(worseStore);
            when(repo.getActiveDiscount(anyString(), anyString(), anyString())).thenReturn(null);

            // Act
            String result = optimizer.optimizeBasketSplit(List.of("P1"), testDate);

            // Assert - Should keep Store1
            assertTrue(result.contains("Store1"), "Should keep first store with best price");
            assertTrue(result.contains("75.0"));
        }
    }

    // ==================== PRODUCT AVAILABILITY LOGIC ====================
    @Nested
    @DisplayName("Product Availability Decision Coverage")
    class ProductAvailabilityLogic {
        @Test
        @DisplayName("Branch: product == null in store loop - continues to next")
        void testBranch_ProductNotInStore_ContinuesLoop() {
            // Arrange - Product only in one store, not in another
            Product availableProduct = new Product("P1", "Item", "Cat", "Brand", 1.0, "unit", 50.0, "RON");

            Map<String, List<Product>> data = Map.of(
                "Store1", List.of(availableProduct),
                "Store2", Collections.emptyList()
            );
            when(repo.getProductsForDate(testDate)).thenReturn(data);
            when(repo.getProduct("Store1", "P1")).thenReturn(availableProduct);
            when(repo.getProduct("Store2", "P1")).thenReturn(null);
            when(repo.getActiveDiscount(anyString(), anyString(), anyString())).thenReturn(null);

            // Act
            String result = optimizer.optimizeBasketSplit(List.of("P1"), testDate);

            // Assert - Should still find product and optimize
            assertTrue(result.contains("Item"), "Should find product in available store");
            assertTrue(result.contains("Store1"));
        }

        @Test
        @DisplayName("Decision: bestProduct != null - adds to output")
        void testDecision_ProductFound_AddsToOutput() {
            // Arrange - Product found
            Product product = new Product("P1", "Item", "Cat", "Brand", 1.0, "unit", 50.0, "RON");

            Map<String, List<Product>> data = Map.of("Store", List.of(product));
            when(repo.getProductsForDate(testDate)).thenReturn(data);
            when(repo.getProduct("Store", "P1")).thenReturn(product);
            when(repo.getActiveDiscount(anyString(), anyString(), anyString())).thenReturn(null);

            // Act
            String result = optimizer.optimizeBasketSplit(List.of("P1"), testDate);

            // Assert - Product should be in output
            assertFalse(result.isEmpty(), "Result should contain output");
            assertTrue(result.contains("Item"), "Should add product line");
        }

        @Test
        @DisplayName("Decision: bestProduct == null - adds error message")
        void testDecision_ProductNotFound_AddsErrorMessage() {
            // Arrange - Product not available anywhere
            Map<String, List<Product>> data = Map.of(
                "Store1", Collections.emptyList(),
                "Store2", Collections.emptyList()
            );
            when(repo.getProductsForDate(testDate)).thenReturn(data);
            when(repo.getProduct(anyString(), anyString())).thenReturn(null);

            // Act
            String result = optimizer.optimizeBasketSplit(List.of("MISSING"), testDate);

            // Assert - Should show error message
            assertTrue(result.contains("not found"), "Should indicate product not found");
        }
    }

    // ==================== QUANTITY COUNTING LOGIC ====================
    @Nested
    @DisplayName("Product Quantity Counting")
    class QuantityCountingLogic {
        @Test
        @DisplayName("Should use LinkedHashMap to count quantities")
        void testQuantityCounting_LinkedHashMap() {
            // Arrange - Same product multiple times
            Product product = new Product("P1", "Item", "Cat", "Brand", 1.0, "unit", 10.0, "RON");

            Map<String, List<Product>> data = Map.of("Store", List.of(product));
            when(repo.getProductsForDate(testDate)).thenReturn(data);
            when(repo.getProduct("Store", "P1")).thenReturn(product);
            when(repo.getActiveDiscount(anyString(), anyString(), anyString())).thenReturn(null);

            // Act - Add product 4 times
            String result = optimizer.optimizeBasketSplit(
                    List.of("P1", "P1", "P1", "P1"), testDate);

            // Assert - Quantity should be counted correctly
            assertTrue(result.contains("x4"), "Should accumulate quantity");
            assertTrue(result.contains("40.0"), "Should calculate 4 * 10 = 40");
        }

        @Test
        @DisplayName("Should show single quantity without x notation")
        void testQuantity_Single_NoNotation() {
            // Arrange
            Product product = new Product("P1", "Item", "Cat", "Brand", 1.0, "unit", 50.0, "RON");

            Map<String, List<Product>> data = Map.of("Store", List.of(product));
            when(repo.getProductsForDate(testDate)).thenReturn(data);
            when(repo.getProduct("Store", "P1")).thenReturn(product);
            when(repo.getActiveDiscount(anyString(), anyString(), anyString())).thenReturn(null);

            // Act
            String result = optimizer.optimizeBasketSplit(List.of("P1"), testDate);

            // Assert - No x1 notation
            assertFalse(result.contains("x1"), "Should not show x1");
        }
    }

    // ==================== SUBTOTAL ACCUMULATION ====================
    @Nested
    @DisplayName("Subtotal and Total Calculation")
    class SubtotalAccumulation {
        @Test
        @DisplayName("Should accumulate subtotals per store")
        void testAccumulation_StoreSubtotals() {
            // Arrange - Multiple products in same store
            Product p1 = new Product("P1", "Apple", "Fruit", "Brand", 1.0, "unit", 5.0, "RON");
            Product p2 = new Product("P2", "Orange", "Fruit", "Brand", 1.0, "unit", 3.0, "RON");
            Product p3 = new Product("P3", "Banana", "Fruit", "Brand", 1.0, "unit", 4.0, "RON");

            Map<String, List<Product>> data = Map.of(
                "Store", List.of(p1, p2, p3)
            );
            when(repo.getProductsForDate(testDate)).thenReturn(data);
            when(repo.getProduct("Store", "P1")).thenReturn(p1);
            when(repo.getProduct("Store", "P2")).thenReturn(p2);
            when(repo.getProduct("Store", "P3")).thenReturn(p3);
            when(repo.getActiveDiscount(anyString(), anyString(), anyString())).thenReturn(null);

            // Act
            String result = optimizer.optimizeBasketSplit(
                    List.of("P1", "P2", "P3"), testDate);

            // Assert - Subtotal should be 5 + 3 + 4 = 12
            assertTrue(result.contains("Subtotal:"), "Should show subtotal");
            assertTrue(result.contains("12.0"), "Subtotal should be 12");
        }

        @Test
        @DisplayName("Should accumulate total savings correctly")
        void testAccumulation_TotalSavings() {
            // Arrange - Products with and without discounts
            Product p1 = new Product("P1", "Item1", "Cat", "Brand", 1.0, "unit", 100.0, "RON");
            Product p2 = new Product("P2", "Item2", "Cat", "Brand", 1.0, "unit", 50.0, "RON");

            Discount d1 = mock(Discount.class);
            when(d1.getDiscountPercent()).thenReturn(10);

            Map<String, List<Product>> data = Map.of(
                "Store", List.of(p1, p2)
            );
            when(repo.getProductsForDate(testDate)).thenReturn(data);
            when(repo.getProduct("Store", "P1")).thenReturn(p1);
            when(repo.getProduct("Store", "P2")).thenReturn(p2);
            when(repo.getActiveDiscount("Store", "P1", testDate)).thenReturn(d1);
            when(repo.getActiveDiscount("Store", "P2", testDate)).thenReturn(null);

            // Act
            String result = optimizer.optimizeBasketSplit(
                    List.of("P1", "P2"), testDate);

            // Assert
            // Original: 100 + 50 = 150
            // Discounted: 90 + 50 = 140
            // Savings: 10
            assertTrue(result.contains("150"), "Original total: 150");
            assertTrue(result.contains("140"), "Optimized total: 140");
            assertTrue(result.contains("10"), "Savings: 10");
        }

        @Test
        @DisplayName("Should calculate zero savings without discounts")
        void testAccumulation_ZeroSavings() {
            // Arrange - No discounts
            Product product = new Product("P1", "Item", "Cat", "Brand", 1.0, "unit", 75.0, "RON");

            Map<String, List<Product>> data = Map.of("Store", List.of(product));
            when(repo.getProductsForDate(testDate)).thenReturn(data);
            when(repo.getProduct("Store", "P1")).thenReturn(product);
            when(repo.getActiveDiscount(anyString(), anyString(), anyString())).thenReturn(null);

            // Act
            String result = optimizer.optimizeBasketSplit(List.of("P1"), testDate);

            // Assert - Savings should be 0
            assertTrue(result.contains("0.00"), "Savings should be 0");
        }
    }

    // ==================== STORE ITEMIZATION ====================
    @Nested
    @DisplayName("Store Itemization and Organization")
    class StoreItemization {
        @Test
        @DisplayName("Should group products by store using HashMap")
        void testOrganization_GroupByStore() {
            // Arrange - Products distributed across stores
            Product k_p1 = new Product("P1", "Apple", "Fruit", "Brand", 1.0, "unit", 5.0, "RON");
            Product k_p2 = new Product("P2", "Orange", "Fruit", "Brand", 1.0, "unit", 3.0, "RON");
            Product l_p3 = new Product("P3", "Banana", "Fruit", "Brand", 1.0, "unit", 4.0, "RON");

            Map<String, List<Product>> data = Map.of(
                "Kaufland", List.of(k_p1, k_p2),
                "Lidl", List.of(l_p3)
            );
            when(repo.getProductsForDate(testDate)).thenReturn(data);
            when(repo.getProduct("Kaufland", "P1")).thenReturn(k_p1);
            when(repo.getProduct("Kaufland", "P2")).thenReturn(k_p2);
            when(repo.getProduct("Lidl", "P3")).thenReturn(l_p3);
            when(repo.getActiveDiscount(anyString(), anyString(), anyString())).thenReturn(null);

            // Act
            String result = optimizer.optimizeBasketSplit(
                    List.of("P1", "P2", "P3"), testDate);

            // Assert - Both stores should be represented
            assertTrue(result.contains("Kaufland"), "Should list Kaufland");
            assertTrue(result.contains("Lidl"), "Should list Lidl");
            assertTrue(result.contains("Apple"), "P1 should be in Kaufland");
            assertTrue(result.contains("Banana"), "P3 should be in Lidl");
        }

        @Test
        @DisplayName("Should capitalize store names in output")
        void testFormatting_StoreNameCapitalization() {
            // Arrange - Lowercase store name
            Product product = new Product("P1", "Item", "Cat", "Brand", 1.0, "unit", 10.0, "RON");

            Map<String, List<Product>> data = Map.of("store_name", List.of(product));
            when(repo.getProductsForDate(testDate)).thenReturn(data);
            when(repo.getProduct("store_name", "P1")).thenReturn(product);
            when(repo.getActiveDiscount(anyString(), anyString(), anyString())).thenReturn(null);

            // Act
            String result = optimizer.optimizeBasketSplit(List.of("P1"), testDate);

            // Assert - Should be capitalized
            assertTrue(result.contains("Store_name") || result.contains("Kaufland"),
                      "Store name should be capitalized");
        }
    }

    // ==================== EDGE CASE: PRICE PRECISION ====================
    @Nested
    @DisplayName("Price Precision and Formatting")
    class PricePrecision {
        @Test
        @DisplayName("Should format prices with 2 decimal places")
        void testPrecision_TwoDecimalPlaces() {
            // Arrange - Product with non-round price
            Product product = new Product("P1", "Item", "Cat", "Brand", 3.0, "unit", 19.99, "RON");

            Map<String, List<Product>> data = Map.of("Store", List.of(product));
            when(repo.getProductsForDate(testDate)).thenReturn(data);
            when(repo.getProduct("Store", "P1")).thenReturn(product);
            when(repo.getActiveDiscount(anyString(), anyString(), anyString())).thenReturn(null);

            // Act
            String result = optimizer.optimizeBasketSplit(List.of("P1"), testDate);

            // Assert - 19.99 * 3 = 59.97
            assertTrue(result.contains("59.97"), "Should format with 2 decimals");
        }

        @Test
        @DisplayName("Should handle discount precision")
        void testPrecision_DiscountCalculation() {
            // Arrange - Discount creating fractional result
            Product product = new Product("P1", "Item", "Cat", "Brand", 1.0, "unit", 33.33, "RON");
            Discount discount = mock(Discount.class);
            when(discount.getDiscountPercent()).thenReturn(33); // 33% off

            Map<String, List<Product>> data = Map.of("Store", List.of(product));
            when(repo.getProductsForDate(testDate)).thenReturn(data);
            when(repo.getProduct("Store", "P1")).thenReturn(product);
            when(repo.getActiveDiscount("Store", "P1", testDate)).thenReturn(discount);

            // Act
            String result = optimizer.optimizeBasketSplit(List.of("P1"), testDate);

            // Assert - Should calculate and format correctly
            // 33.33 * (1 - 33/100) = 33.33 * 0.67 = 22.33
            assertTrue(result.matches(".*22\\.\\d{2}.*"), "Should format price correctly");
        }
    }

    // ==================== MULTIPLE PRODUCTS, MULTIPLE STORES ====================
    @Test
    @DisplayName("Complex scenario: Multiple products across multiple stores with mixed discounts")
    void testComplexScenario_MultipleProductsStoresWithDiscounts() {
        // Arrange - Complex real-world scenario
        Product k_p1 = new Product("P1", "Apple", "Fruit", "Brand", 1.0, "kg", 5.0, "RON");
        Product k_p2 = new Product("P2", "Milk", "Dairy", "Brand", 1.0, "l", 4.0, "RON");

        Product l_p1 = new Product("P1", "Apple", "Fruit", "Brand", 1.0, "kg", 4.5, "RON");
        Product l_p3 = new Product("P3", "Bread", "Bakery", "Brand", 1.0, "piece", 3.0, "RON");

        Discount k_p1_discount = mock(Discount.class);
        when(k_p1_discount.getDiscountPercent()).thenReturn(10);

        Discount l_p3_discount = mock(Discount.class);
        when(l_p3_discount.getDiscountPercent()).thenReturn(20);

        Map<String, List<Product>> data = Map.of(
            "Kaufland", List.of(k_p1, k_p2),
            "Lidl", List.of(l_p1, l_p3)
        );

        when(repo.getProductsForDate(testDate)).thenReturn(data);
        when(repo.getProduct("Kaufland", "P1")).thenReturn(k_p1);
        when(repo.getProduct("Kaufland", "P2")).thenReturn(k_p2);
        when(repo.getProduct("Lidl", "P1")).thenReturn(l_p1);
        when(repo.getProduct("Lidl", "P3")).thenReturn(l_p3);
        when(repo.getProduct(anyString(), "MISSING")).thenReturn(null);

        when(repo.getActiveDiscount("Kaufland", "P1", testDate)).thenReturn(k_p1_discount);
        when(repo.getActiveDiscount("Kaufland", "P2", testDate)).thenReturn(null);
        when(repo.getActiveDiscount("Lidl", "P1", testDate)).thenReturn(null);
        when(repo.getActiveDiscount("Lidl", "P3", testDate)).thenReturn(l_p3_discount);

        // Act
        String result = optimizer.optimizeBasketSplit(
                List.of("P1", "P2", "P3"), testDate);

        // Assert
        assertNotNull(result);
        assertTrue(result.contains("Apple"), "Should include P1");
        assertTrue(result.contains("Bread"), "Should include P3");
        // P1: Kaufland 4.5 (with 10% discount = 4.05) vs Lidl 4.5 (no discount)
        // Should select Kaufland P1 with discount
        assertTrue(result.contains("(-10%"), "Should show Kaufland discount");
        assertTrue(result.contains("(-20%"), "Should show Lidl discount");
    }
}

