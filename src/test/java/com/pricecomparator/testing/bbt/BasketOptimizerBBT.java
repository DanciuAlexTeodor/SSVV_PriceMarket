package com.pricecomparator.service.testing.bbt;

import com.pricecomparator.model.Product;
import com.pricecomparator.model.Discount;
import com.pricecomparator.repository.MarketDataRepository;
import com.pricecomparator.service.BasketOptimizer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Black Box Tests for BasketOptimizer Service
 *
 * Tests basket optimization based on functional requirements:
 * - Split shopping across stores for best prices
 * - Apply discounts correctly
 * - Calculate total savings
 * - Handle edge cases gracefully
 */
@DisplayName("BasketOptimizer - Black Box Testing")
class BasketOptimizerBBT {
    private MarketDataRepository repo;
    private BasketOptimizer optimizer;
    private final String testDate = "2025-05-01";

    @BeforeEach
    void setUp() {
        repo = mock(MarketDataRepository.class);
        optimizer = new BasketOptimizer(repo);
    }

    // ==================== BASIC FUNCTIONALITY TESTS ====================

    @Test
    @DisplayName("Should optimize simple basket with single product")
    void testOptimize_SingleProduct_SelectsBestStore() {
        // Arrange - Single product at two stores
        Product kaufland = new Product("P1", "Banana", "Fruits", "BrandA", 2.0, "kg", 10.0, "RON");
        Product lidl = new Product("P1", "Banana", "Fruits", "BrandA", 2.0, "kg", 9.0, "RON");

        Map<String, List<Product>> data = Map.of(
            "Kaufland", List.of(kaufland),
            "Lidl", List.of(lidl)
        );
        when(repo.getProductsForDate(testDate)).thenReturn(data);
        when(repo.getProduct("Kaufland", "P1")).thenReturn(kaufland);
        when(repo.getProduct("Lidl", "P1")).thenReturn(lidl);
        when(repo.getActiveDiscount(anyString(), anyString(), anyString())).thenReturn(null);

        // Act
        String result = optimizer.optimizeBasketSplit(List.of("P1"), testDate);

        // Assert
        assertNotNull(result);
        assertTrue(result.contains("Banana"), "Result should contain product name");
        assertTrue(result.contains("Lidl"), "Should select Lidl (cheaper store)");
        assertTrue(result.contains("9.0"), "Should show Lidl's price");
    }

    @Test
    @DisplayName("Should split basket across multiple stores optimally")
    void testOptimize_MultipleProducts_SplitAcrossStores() {
        // Arrange - Different products cheaper at different stores
        Product p1_kaufland = new Product("P1", "Banana", "Fruits", "BrandA", 2.0, "kg", 10.0, "RON");
        Product p1_lidl = new Product("P1", "Banana", "Fruits", "BrandA", 2.0, "kg", 12.0, "RON");

        Product p2_kaufland = new Product("P2", "Milk", "Dairy", "BrandB", 2.0, "l", 9.0, "RON");
        Product p2_lidl = new Product("P2", "Milk", "Dairy", "BrandB", 2.0, "l", 7.0, "RON");

        Map<String, List<Product>> data = Map.of(
            "Kaufland", List.of(p1_kaufland, p2_kaufland),
            "Lidl", List.of(p1_lidl, p2_lidl)
        );
        when(repo.getProductsForDate(testDate)).thenReturn(data);
        when(repo.getProduct("Kaufland", "P1")).thenReturn(p1_kaufland);
        when(repo.getProduct("Lidl", "P1")).thenReturn(p1_lidl);
        when(repo.getProduct("Kaufland", "P2")).thenReturn(p2_kaufland);
        when(repo.getProduct("Lidl", "P2")).thenReturn(p2_lidl);
        when(repo.getActiveDiscount(anyString(), anyString(), anyString())).thenReturn(null);

        // Act
        String result = optimizer.optimizeBasketSplit(List.of("P1", "P2"), testDate);

        // Assert
        assertNotNull(result);
        assertTrue(result.contains("Kaufland") && result.contains("Lidl"),
                  "Should split across both stores");
        assertTrue(result.contains("Banana") && result.contains("Milk"),
                  "Should have both products");
    }

    // ==================== QUANTITY HANDLING TESTS ====================

    @Test
    @DisplayName("Should handle product quantities correctly")
    void testOptimize_MultipleQuantities_ShowsQuantityNotation() {
        // Arrange - Product ordered 3 times
        Product product = new Product("P1", "Apple", "Fruits", "BrandA", 1.0, "kg", 5.0, "RON");

        Map<String, List<Product>> data = Map.of("Kaufland", List.of(product));
        when(repo.getProductsForDate(testDate)).thenReturn(data);
        when(repo.getProduct("Kaufland", "P1")).thenReturn(product);
        when(repo.getActiveDiscount(anyString(), anyString(), anyString())).thenReturn(null);

        // Act
        String result = optimizer.optimizeBasketSplit(List.of("P1", "P1", "P1"), testDate);

        // Assert
        assertTrue(result.contains("x3"), "Should show quantity notation x3");
        assertTrue(result.contains("15.0"), "Should calculate 3 x 5 RON = 15 RON");
    }

    @Test
    @DisplayName("Should handle single quantity without notation")
    void testOptimize_SingleQuantity_NoQuantityNotation() {
        // Arrange
        Product product = new Product("P1", "Apple", "Fruits", "BrandA", 1.0, "kg", 5.0, "RON");

        Map<String, List<Product>> data = Map.of("Kaufland", List.of(product));
        when(repo.getProductsForDate(testDate)).thenReturn(data);
        when(repo.getProduct("Kaufland", "P1")).thenReturn(product);
        when(repo.getActiveDiscount(anyString(), anyString(), anyString())).thenReturn(null);

        // Act
        String result = optimizer.optimizeBasketSplit(List.of("P1"), testDate);

        // Assert
        assertFalse(result.contains("x1"), "Should not show x1 for single quantity");
    }

    // ==================== DISCOUNT TESTS ====================

    @Test
    @DisplayName("Should apply single discount correctly")
    void testOptimize_WithDiscount_PriceReduced() {
        // Arrange - Product with 20% discount
        Product product = new Product("P1", "Banana", "Fruits", "BrandA", 2.0, "kg", 10.0, "RON");
        Discount discount = mock(Discount.class);
        when(discount.getDiscountPercent()).thenReturn(20);

        Map<String, List<Product>> data = Map.of("Kaufland", List.of(product));
        when(repo.getProductsForDate(testDate)).thenReturn(data);
        when(repo.getProduct("Kaufland", "P1")).thenReturn(product);
        when(repo.getActiveDiscount("Kaufland", "P1", testDate)).thenReturn(discount);

        // Act
        String result = optimizer.optimizeBasketSplit(List.of("P1"), testDate);

        // Assert
        assertTrue(result.contains("8.0"), "10 RON with 20% discount = 8 RON");
        assertTrue(result.contains("(-20%"), "Should show discount percentage");
    }

    @Test
    @DisplayName("Should not apply discount if none available")
    void testOptimize_WithoutDiscount_RegularPrice() {
        // Arrange - No discount
        Product product = new Product("P1", "Banana", "Fruits", "BrandA", 2.0, "kg", 10.0, "RON");

        Map<String, List<Product>> data = Map.of("Kaufland", List.of(product));
        when(repo.getProductsForDate(testDate)).thenReturn(data);
        when(repo.getProduct("Kaufland", "P1")).thenReturn(product);
        when(repo.getActiveDiscount(anyString(), anyString(), anyString())).thenReturn(null);

        // Act
        String result = optimizer.optimizeBasketSplit(List.of("P1"), testDate);

        // Assert
        assertTrue(result.contains("10.0"), "Should show regular price");
        assertFalse(result.contains("(-%"), "Should not show discount notation");
    }

    // ==================== SAVINGS CALCULATION TESTS ====================

    @Test
    @DisplayName("Should calculate total savings correctly")
    void testOptimize_CalculatesTotalSavings() {
        // Arrange - Product with discount
        Product product = new Product("P1", "Item", "Category", "Brand", 1.0, "unit", 100.0, "RON");
        Discount discount = mock(Discount.class);
        when(discount.getDiscountPercent()).thenReturn(10);

        Map<String, List<Product>> data = Map.of("Store", List.of(product));
        when(repo.getProductsForDate(testDate)).thenReturn(data);
        when(repo.getProduct("Store", "P1")).thenReturn(product);
        when(repo.getActiveDiscount("Store", "P1", testDate)).thenReturn(discount);

        // Act
        String result = optimizer.optimizeBasketSplit(List.of("P1"), testDate);

        // Assert
        assertTrue(result.contains("100.0"), "Original total should be 100");
        assertTrue(result.contains("90.0"), "Optimized total should be 90");
        assertTrue(result.contains("10.0"), "Savings should be 10");
    }

    @Test
    @DisplayName("Should show zero savings when no discounts applied")
    void testOptimize_NoSavings_ZeroShown() {
        // Arrange - No discounts
        Product product = new Product("P1", "Item", "Category", "Brand", 1.0, "unit", 50.0, "RON");

        Map<String, List<Product>> data = Map.of("Store", List.of(product));
        when(repo.getProductsForDate(testDate)).thenReturn(data);
        when(repo.getProduct("Store", "P1")).thenReturn(product);
        when(repo.getActiveDiscount(anyString(), anyString(), anyString())).thenReturn(null);

        // Act
        String result = optimizer.optimizeBasketSplit(List.of("P1"), testDate);

        // Assert
        assertTrue(result.contains("0.00"), "Savings should be 0");
    }

    // ==================== ERROR HANDLING TESTS ====================

    @Test
    @DisplayName("Should handle product not found in any store")
    void testOptimize_ProductNotFound_ShowsErrorMessage() {
        // Arrange - Product not available
        Map<String, List<Product>> data = Map.of("Kaufland", Collections.emptyList());
        when(repo.getProductsForDate(testDate)).thenReturn(data);
        when(repo.getProduct(anyString(), anyString())).thenReturn(null);

        // Act
        String result = optimizer.optimizeBasketSplit(List.of("MISSING"), testDate);

        // Assert
        assertTrue(result.contains("not found"), "Should indicate product not found");
    }

    @Test
    @DisplayName("Should handle empty basket")
    void testOptimize_EmptyBasket_NoError() {
        // Arrange
        Map<String, List<Product>> data = Map.of("Kaufland", Collections.emptyList());
        when(repo.getProductsForDate(testDate)).thenReturn(data);

        // Act
        String result = optimizer.optimizeBasketSplit(Collections.emptyList(), testDate);

        // Assert
        assertNotNull(result);
        assertTrue(result.contains("0.00"), "Empty basket should have zero total");
    }

    @Test
    @DisplayName("Should handle basket with missing and existing products")
    void testOptimize_MixedProductAvailability() {
        // Arrange - Some products available, one missing
        Product p1 = new Product("P1", "Available", "Cat", "Brand", 1.0, "unit", 10.0, "RON");

        Map<String, List<Product>> data = Map.of("Store", List.of(p1));
        when(repo.getProductsForDate(testDate)).thenReturn(data);
        when(repo.getProduct("Store", "P1")).thenReturn(p1);
        when(repo.getProduct("Store", "MISSING")).thenReturn(null);
        when(repo.getActiveDiscount(anyString(), anyString(), anyString())).thenReturn(null);

        // Act
        String result = optimizer.optimizeBasketSplit(List.of("P1", "MISSING"), testDate);

        // Assert
        assertTrue(result.contains("Available"), "Should list available product");
        assertTrue(result.contains("not found"), "Should report missing product");
    }

    // ==================== EDGE CASES ====================

    @Test
    @DisplayName("Should handle store name capitalization")
    void testOptimize_StoreNameCapitalization() {
        // Arrange
        Product product = new Product("P1", "Item", "Cat", "Brand", 1.0, "unit", 10.0, "RON");

        Map<String, List<Product>> data = Map.of("kaufland", List.of(product));
        when(repo.getProductsForDate(testDate)).thenReturn(data);
        when(repo.getProduct("kaufland", "P1")).thenReturn(product);
        when(repo.getActiveDiscount(anyString(), anyString(), anyString())).thenReturn(null);

        // Act
        String result = optimizer.optimizeBasketSplit(List.of("P1"), testDate);

        // Assert
        assertTrue(result.contains("Kaufland"), "Store name should be capitalized in output");
    }

    @Test
    @DisplayName("Should handle multiple stores with same prices")
    void testOptimize_MultipleStoresSamePrice_SelectsAny() {
        // Arrange - Same product at same price in multiple stores
        Product p1 = new Product("P1", "Item", "Cat", "Brand", 1.0, "unit", 10.0, "RON");
        Product p2 = new Product("P1", "Item", "Cat", "Brand", 1.0, "unit", 10.0, "RON");

        Map<String, List<Product>> data = Map.of(
            "Kaufland", List.of(p1),
            "Lidl", List.of(p2)
        );
        when(repo.getProductsForDate(testDate)).thenReturn(data);
        when(repo.getProduct("Kaufland", "P1")).thenReturn(p1);
        when(repo.getProduct("Lidl", "P1")).thenReturn(p2);
        when(repo.getActiveDiscount(anyString(), anyString(), anyString())).thenReturn(null);

        // Act
        String result = optimizer.optimizeBasketSplit(List.of("P1"), testDate);

        // Assert
        assertTrue(result.contains("Item"), "Should include the product");
        // Either store is acceptable when prices are equal
        assertTrue(result.contains("Kaufland") || result.contains("Lidl"));
    }

    @Test
    @DisplayName("Should accumulate subtotals correctly per store")
    void testOptimize_CorrectSubtotalAccumulation() {
        // Arrange - Multiple products in same store
        Product p1 = new Product("P1", "Apples", "Fruits", "Brand", 1.0, "kg", 5.0, "RON");
        Product p2 = new Product("P2", "Oranges", "Fruits", "Brand", 1.0, "kg", 4.0, "RON");

        Map<String, List<Product>> data = Map.of(
            "Kaufland", List.of(p1, p2),
            "Lidl", Collections.emptyList()
        );
        when(repo.getProductsForDate(testDate)).thenReturn(data);
        when(repo.getProduct("Kaufland", "P1")).thenReturn(p1);
        when(repo.getProduct("Kaufland", "P2")).thenReturn(p2);
        when(repo.getActiveDiscount(anyString(), anyString(), anyString())).thenReturn(null);

        // Act
        String result = optimizer.optimizeBasketSplit(List.of("P1", "P2"), testDate);

        // Assert
        assertTrue(result.contains("Subtotal:"), "Should show subtotal for store");
        assertTrue(result.contains("9.0"), "Subtotal should be 5+4=9");
    }
}

