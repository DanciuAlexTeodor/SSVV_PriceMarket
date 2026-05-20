package com.pricecomparator.service.testing.bbt;

import com.pricecomparator.model.Product;
import com.pricecomparator.repository.MarketDataRepository;
import com.pricecomparator.service.ValueUnit;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Black Box Tests for ValueUnit Service
 *
 * Tests the ValueUnit service based on functional requirements only,
 * without knowledge of internal implementation details.
 *
 * Focus: Inputs and expected outputs for value per unit calculations
 */
@DisplayName("ValueUnit - Black Box Testing")
class ValueUnitBBT {
    private MarketDataRepository repo;
    private ValueUnit valueUnit;
    private final String testDate = "2025-05-01";

    @BeforeEach
    void setUp() {
        repo = mock(MarketDataRepository.class);
        valueUnit = new ValueUnit(repo);
    }

    // ==================== VALID INPUT TESTS ====================

    @Test
    @DisplayName("Should correctly calculate price per kg for weight-based products")
    void testValuePerUnit_WithKgProducts() {
        // Arrange - Products with weight in kg
        Product kauflandProduct = new Product("P1", "Banana", "Fruits", "BrandA", 2.0, "kg", 10.0, "RON");
        Product lidlProduct = new Product("P1", "Banana", "Fruits", "BrandA", 2.5, "kg", 11.25, "RON");

        Map<String, List<Product>> data = Map.of(
            "Kaufland", List.of(kauflandProduct),
            "Lidl", List.of(lidlProduct)
        );
        when(repo.getProductsForDate(testDate)).thenReturn(data);

        // Act
        var result = valueUnit.getBestValuePerUnit("P1", testDate);

        // Assert - Should calculate price per kg correctly
        assertEquals(2, result.size(), "Should have prices for both stores");
        assertEquals(5.0, result.get("Kaufland"), 0.01, "Kaufland: 10 RON / 2 kg = 5 RON/kg");
        assertEquals(4.5, result.get("Lidl"), 0.01, "Lidl: 11.25 RON / 2.5 kg = 4.5 RON/kg");
        assertEquals(4.5, Collections.min(result.values()), 0.01, "Lidl should have best value");
    }

    @Test
    @DisplayName("Should correctly calculate price per liter for volume-based products")
    void testValuePerUnit_WithLiterProducts() {
        // Arrange - Products with volume in liters
        Product kauflandProduct = new Product("P2", "Milk", "Dairy", "BrandB", 1.0, "l", 4.5, "RON");
        Product lidlProduct = new Product("P2", "Milk", "Dairy", "BrandB", 2.0, "l", 8.5, "RON");

        Map<String, List<Product>> data = Map.of(
            "Kaufland", List.of(kauflandProduct),
            "Lidl", List.of(lidlProduct)
        );
        when(repo.getProductsForDate(testDate)).thenReturn(data);

        // Act
        var result = valueUnit.getBestValuePerUnit("P2", testDate);

        // Assert
        assertEquals(2, result.size());
        assertEquals(4.5, result.get("Kaufland"), 0.01, "Kaufland: 4.5 RON / 1 L = 4.5 RON/L");
        assertEquals(4.25, result.get("Lidl"), 0.01, "Lidl: 8.5 RON / 2 L = 4.25 RON/L");
    }

    @Test
    @DisplayName("Should correctly calculate price per unit for count-based products (buc)")
    void testValuePerUnit_WithCountBasedProducts() {
        // Arrange - Products sold by count (buc = pieces)
        Product kauflandProduct = new Product("P3", "Eggs", "Eggs", "BrandC", 10.0, "buc", 12.0, "RON");
        Product lidlProduct = new Product("P3", "Eggs", "Eggs", "BrandC", 6.0, "buc", 6.0, "RON");

        Map<String, List<Product>> data = Map.of(
            "Kaufland", List.of(kauflandProduct),
            "Lidl", List.of(lidlProduct)
        );
        when(repo.getProductsForDate(testDate)).thenReturn(data);

        // Act
        var result = valueUnit.getBestValuePerUnit("P3", testDate);

        // Assert
        assertEquals(2, result.size());
        assertEquals(1.2, result.get("Kaufland"), 0.01, "Kaufland: 12 RON / 10 buc = 1.2 RON/buc");
        assertEquals(1.0, result.get("Lidl"), 0.01, "Lidl: 6 RON / 6 buc = 1 RON/buc");
    }

    // ==================== UNIT CONVERSION TESTS ====================

    @Test
    @DisplayName("Should convert grams to kg (small weight unit)")
    void testValuePerUnit_WithGramsConversion() {
        // Arrange - Product in grams (should be converted to kg)
        Product kauflandProduct = new Product("P4", "Coffee", "Beverages", "BrandD", 500.0, "g", 25.0, "RON");

        Map<String, List<Product>> data = Map.of("Kaufland", List.of(kauflandProduct));
        when(repo.getProductsForDate(testDate)).thenReturn(data);

        // Act
        var result = valueUnit.getBestValuePerUnit("P4", testDate);

        // Assert - 500g at 25 RON should convert to 50 RON/kg (25 * 1000/500)
        assertEquals(1, result.size());
        assertEquals(50.0, result.get("Kaufland"), 0.01, "500g at 25 RON = 50 RON/kg");
    }

    @Test
    @DisplayName("Should convert milliliters to liters (small volume unit)")
    void testValuePerUnit_WithMillilitersConversion() {
        // Arrange - Product in milliliters
        Product kauflandProduct = new Product("P5", "Oil", "Oils", "BrandE", 500.0, "ml", 15.0, "RON");

        Map<String, List<Product>> data = Map.of("Kaufland", List.of(kauflandProduct));
        when(repo.getProductsForDate(testDate)).thenReturn(data);

        // Act
        var result = valueUnit.getBestValuePerUnit("P5", testDate);

        // Assert - 500ml at 15 RON = 30 RON/L
        assertEquals(1, result.size());
        assertEquals(30.0, result.get("Kaufland"), 0.01, "500ml at 15 RON = 30 RON/L");
    }

    // ==================== EDGE CASE TESTS ====================

    @Test
    @DisplayName("Should handle unknown unit gracefully")
    void testValuePerUnit_WithUnknownUnit() {
        // Arrange - Product with unknown unit
        Product kauflandProduct = new Product("P6", "Mystery", "Other", "BrandF", 2.0, "unknown_unit", 10.0, "RON");

        Map<String, List<Product>> data = Map.of("Kaufland", List.of(kauflandProduct));
        when(repo.getProductsForDate(testDate)).thenReturn(data);

        // Act
        var result = valueUnit.getBestValuePerUnit("P6", testDate);

        // Assert - Should return price as-is with warning
        assertEquals(1, result.size());
        assertEquals(10.0, result.get("Kaufland"), 0.01, "Should return price for unknown unit");
    }

    @Test
    @DisplayName("Should handle product not found gracefully")
    void testValuePerUnit_ProductNotFound() {
        // Arrange - No product with this ID
        Map<String, List<Product>> data = Map.of("Kaufland", Collections.emptyList());
        when(repo.getProductsForDate(testDate)).thenReturn(data);

        // Act
        var result = valueUnit.getBestValuePerUnit("NONEXISTENT", testDate);

        // Assert - Should return empty map
        assertTrue(result.isEmpty(), "Should return empty map for non-existent product");
    }

    @Test
    @DisplayName("Should handle empty stores list")
    void testValuePerUnit_EmptyStores() {
        // Arrange - No stores available
        Map<String, List<Product>> data = Collections.emptyMap();
        when(repo.getProductsForDate(testDate)).thenReturn(data);

        // Act
        var result = valueUnit.getBestValuePerUnit("P1", testDate);

        // Assert
        assertTrue(result.isEmpty(), "Should handle empty stores");
    }

    // ==================== PARAMETRIZED TESTS ====================

    @ParameterizedTest
    @CsvSource({
        "P1, 1000.0, g, 25.0, 25.0",           // 1000g at 25 RON = 25 RON/kg
        "P2, 250.0, ml, 5.0, 20.0",             // 250ml at 5 RON = 20 RON/L
        "P3, 2.0, kg, 10.0, 5.0",               // 2kg at 10 RON = 5 RON/kg
        "P4, 1.0, l, 4.5, 4.5"                  // 1L at 4.5 RON = 4.5 RON/L
    })
    @DisplayName("Should calculate correct values for various units and quantities")
    void testValuePerUnit_VariousUnitsAndQuantities(String productId, double quantity, String unit,
                                                     double price, double expectedValue) {
        // Arrange
        Product product = new Product(productId, "Product", "Category", "Brand", quantity, unit, price, "RON");
        Map<String, List<Product>> data = Map.of("Store", List.of(product));
        when(repo.getProductsForDate(testDate)).thenReturn(data);

        // Act
        var result = valueUnit.getBestValuePerUnit(productId, testDate);

        // Assert
        assertEquals(1, result.size());
        assertEquals(expectedValue, result.get("Store"), 0.01,
                     "Value calculation incorrect for " + unit);
    }

    @Test
    @DisplayName("Should identify correct best value among multiple stores")
    void testValuePerUnit_BestValueIdentification() {
        // Arrange - Same product at different prices in multiple stores
        Product p1 = new Product("P1", "Apple", "Fruits", "BrandA", 1.0, "kg", 3.5, "RON");  // 3.5/kg
        Product p2 = new Product("P1", "Apple", "Fruits", "BrandA", 2.0, "kg", 6.0, "RON");  // 3.0/kg
        Product p3 = new Product("P1", "Apple", "Fruits", "BrandA", 1.5, "kg", 5.7, "RON");  // 3.8/kg

        Map<String, List<Product>> data = Map.of(
            "Kaufland", List.of(p1),
            "Lidl", List.of(p2),
            "Profi", List.of(p3)
        );
        when(repo.getProductsForDate(testDate)).thenReturn(data);

        // Act
        var result = valueUnit.getBestValuePerUnit("P1", testDate);

        // Assert
        assertEquals(3, result.size());
        double minValue = Collections.min(result.values());
        assertEquals(3.0, minValue, 0.01, "Lidl should have best value (3 RON/kg)");
        assertEquals("Lidl",
                     result.entrySet().stream()
                           .filter(e -> e.getValue().equals(minValue))
                           .map(Map.Entry::getKey)
                           .findFirst()
                           .orElse(null),
                     "Lidl should be identified as best store");
    }

    @Test
    @DisplayName("Should handle multiple products with same name but different IDs")
    void testValuePerUnit_SameProductDifferentIds() {
        // Arrange - Different store IDs for same product
        Product p1 = new Product("P1-K", "Milk", "Dairy", "BrandA", 1.0, "l", 4.0, "RON");
        Product p2 = new Product("P1-L", "Milk", "Dairy", "BrandA", 1.0, "l", 3.5, "RON");

        Map<String, List<Product>> data = Map.of(
            "Kaufland", List.of(p1),
            "Lidl", List.of(p2)
        );
        when(repo.getProductsForDate(testDate)).thenReturn(data);

        // Act - Query with first store's ID
        var resultK = valueUnit.getBestValuePerUnit("P1-K", testDate);

        // Assert
        assertEquals(2, resultK.size(), "Should get products for same product name");
        assertTrue(resultK.containsKey("Kaufland"));
        assertTrue(resultK.containsKey("Lidl"));
    }
}

