package com.pricecomparator.service.testing.wbt;

import com.pricecomparator.model.Product;
import com.pricecomparator.repository.MarketDataRepository;
import com.pricecomparator.service.ValueUnit;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * White Box Tests for ValueUnit Service
 *
 * Tests internal implementation details including:
 * - All code branches and paths
 * - Internal calculations and conversions
 * - Unit conversion logic (g→kg, ml→L)
 * - HashMap operations and Stream operations
 * - Edge cases in calculations
 */
@DisplayName("ValueUnit - White Box Testing")
class ValueUnitWBT {
    private MarketDataRepository repo;
    private ValueUnit valueUnit;
    private final String testDate = "2025-05-01";

    @BeforeEach
    void setUp() {
        repo = mock(MarketDataRepository.class);
        valueUnit = new ValueUnit(repo);
    }

    // ==================== BRANCH COVERAGE: WEIGHT UNITS ====================
    @Nested
    @DisplayName("WEIGHT_SMALL_UNITS Branch Coverage")
    class WeightSmallUnitsBranch {
        @Test
        @DisplayName("Should execute branch for WEIGHT_SMALL_UNITS (g)")
        void testBranch_GramConversion_MultiplyBy1000() {
            // Arrange - Product in grams (WEIGHT_SMALL_UNITS)
            Product product = new Product("P1", "Coffee", "Beverages", "Brand", 500.0, "g", 25.0, "RON");
            Map<String, List<Product>> data = Map.of("Store", List.of(product));
            when(repo.getProductsForDate(testDate)).thenReturn(data);

            // Act
            var result = valueUnit.getBestValuePerUnit("P1", testDate);

            // Assert - Branch: multiply by 1000 because it's a small weight unit
            assertEquals(1, result.size());
            assertEquals(50.0, result.get("Store"), 0.01,
                        "g → multiply by 1000: 25 * 1000 / 500 = 50");
        }

        @Test
        @DisplayName("Multiple small weight conversions")
        void testMultipleSmallWeightConversions() {
            // Arrange - Multiple products with small units
            Product p1 = new Product("P1", "Item1", "Cat", "Brand", 200.0, "g", 10.0, "RON");
            Product p2 = new Product("P1", "Item1", "Cat", "Brand", 100.0, "g", 6.0, "RON");

            Map<String, List<Product>> data = Map.of(
                "Store1", List.of(p1),
                "Store2", List.of(p2)
            );
            when(repo.getProductsForDate(testDate)).thenReturn(data);

            // Act
            var result = valueUnit.getBestValuePerUnit("P1", testDate);

            // Assert - Both converted to per-kg basis
            assertEquals(50.0, result.get("Store1"), 0.01, "Store1: 10 * 1000 / 200 = 50");
            assertEquals(60.0, result.get("Store2"), 0.01, "Store2: 6 * 1000 / 100 = 60");
        }
    }

    // ==================== BRANCH COVERAGE: VOLUME UNITS ====================
    @Nested
    @DisplayName("VOLUME_SMALL_UNITS Branch Coverage")
    class VolumeSmallUnitsBranch {
        @Test
        @DisplayName("Should execute branch for VOLUME_SMALL_UNITS (ml)")
        void testBranch_MilliliterConversion_MultiplyBy1000() {
            // Arrange - Product in milliliters
            Product product = new Product("P1", "Oil", "Oils", "Brand", 250.0, "ml", 12.5, "RON");
            Map<String, List<Product>> data = Map.of("Store", List.of(product));
            when(repo.getProductsForDate(testDate)).thenReturn(data);

            // Act
            var result = valueUnit.getBestValuePerUnit("P1", testDate);

            // Assert - Branch: multiply by 1000 for small volume units
            assertEquals(1, result.size());
            assertEquals(50.0, result.get("Store"), 0.01,
                        "ml → multiply by 1000: 12.5 * 1000 / 250 = 50");
        }
    }

    // ==================== BRANCH COVERAGE: LARGE UNITS ====================
    @Nested
    @DisplayName("WEIGHT_LARGE_UNITS and VOLUME_LARGE_UNITS Branch Coverage")
    class LargeUnitsBranch {
        @Test
        @DisplayName("Should execute branch for WEIGHT_LARGE_UNITS (kg)")
        void testBranch_KgNoConversion_DivideOnly() {
            // Arrange - Product in kg (already standard unit)
            Product product = new Product("P1", "Apples", "Fruits", "Brand", 2.0, "kg", 10.0, "RON");
            Map<String, List<Product>> data = Map.of("Store", List.of(product));
            when(repo.getProductsForDate(testDate)).thenReturn(data);

            // Act
            var result = valueUnit.getBestValuePerUnit("P1", testDate);

            // Assert - Branch: no multiplication, just divide (kg)
            assertEquals(1, result.size());
            assertEquals(5.0, result.get("Store"), 0.01,
                        "kg → no conversion: 10 / 2 = 5");
        }

        @Test
        @DisplayName("Should execute branch for VOLUME_LARGE_UNITS (l)")
        void testBranch_LiterNoConversion_DivideOnly() {
            // Arrange - Product in liters
            Product product = new Product("P1", "Milk", "Dairy", "Brand", 1.0, "l", 4.5, "RON");
            Map<String, List<Product>> data = Map.of("Store", List.of(product));
            when(repo.getProductsForDate(testDate)).thenReturn(data);

            // Act
            var result = valueUnit.getBestValuePerUnit("P1", testDate);

            // Assert - Branch: no multiplication, just divide (liter)
            assertEquals(1, result.size());
            assertEquals(4.5, result.get("Store"), 0.01,
                        "l → no conversion: 4.5 / 1 = 4.5");
        }
    }

    // ==================== BRANCH COVERAGE: COUNT UNITS ====================
    @Nested
    @DisplayName("COUNT_UNITS Branch Coverage")
    class CountUnitsBranch {
        @Test
        @DisplayName("Should execute branch for COUNT_UNITS: buc")
        void testBranch_BucCountUnit() {
            // Arrange - Product counted in pieces (buc)
            Product product = new Product("P1", "Eggs", "Eggs", "Brand", 6.0, "buc", 6.0, "RON");
            Map<String, List<Product>> data = Map.of("Store", List.of(product));
            when(repo.getProductsForDate(testDate)).thenReturn(data);

            // Act
            var result = valueUnit.getBestValuePerUnit("P1", testDate);

            // Assert - Branch: count units divide only (buc)
            assertEquals(1, result.size());
            assertEquals(1.0, result.get("Store"), 0.01,
                        "buc → no conversion: 6 / 6 = 1");
        }

        @Test
        @DisplayName("Should execute branch for COUNT_UNITS: role")
        void testBranch_RoleCountUnit() {
            // Arrange - Product counted in rolls (role)
            Product product = new Product("P1", "Paper", "Household", "Brand", 4.0, "role", 8.0, "RON");
            Map<String, List<Product>> data = Map.of("Store", List.of(product));
            when(repo.getProductsForDate(testDate)).thenReturn(data);

            // Act
            var result = valueUnit.getBestValuePerUnit("P1", testDate);

            // Assert - Branch: count units divide only
            assertEquals(1, result.size());
            assertEquals(2.0, result.get("Store"), 0.01,
                        "role → no conversion: 8 / 4 = 2");
        }
    }

    // ==================== BRANCH COVERAGE: UNKNOWN UNIT ====================
    @Nested
    @DisplayName("Unknown Unit Branch Coverage")
    class UnknownUnitBranch {
        @Test
        @DisplayName("Should execute branch for unknown unit - returns price as-is")
        void testBranch_UnknownUnit_ReturnsPriceAsIs() {
            // Arrange - Product with custom/unknown unit
            Product product = new Product("P1", "Mystery", "Other", "Brand", 5.0, "custom_unit", 25.0, "RON");
            Map<String, List<Product>> data = Map.of("Store", List.of(product));
            when(repo.getProductsForDate(testDate)).thenReturn(data);

            // Act
            var result = valueUnit.getBestValuePerUnit("P1", testDate);

            // Assert - Branch: unknown unit should return basePrice as-is (division only)
            assertEquals(1, result.size());
            // For unknown unit, it still divides: 25 / 5 = 5
            assertEquals(5.0, result.get("Store"), 0.01,
                        "unknown unit → should still divide (fallback case)");
        }
    }

    // ==================== DECISION COVERAGE ====================
    @Nested
    @DisplayName("Decision Point Coverage")
    class DecisionCoverage {
        @Test
        @DisplayName("isEmpty() decision: true path - no products found")
        void testDecision_Empty_NoProductsFound() {
            // Arrange - Empty product list
            Map<String, List<Product>> data = Map.of("Store", Collections.emptyList());
            when(repo.getProductsForDate(testDate)).thenReturn(data);

            // Act
            var result = valueUnit.getBestValuePerUnit("MISSING", testDate);

            // Assert - isEmpty is true, returns empty map
            assertTrue(result.isEmpty(), "Should return empty map when no products found");
        }

        @Test
        @DisplayName("isEmpty() decision: false path - products found")
        void testDecision_NotEmpty_ProductsFound() {
            // Arrange - Products available
            Product product = new Product("P1", "Item", "Cat", "Brand", 1.0, "kg", 5.0, "RON");
            Map<String, List<Product>> data = Map.of("Store", List.of(product));
            when(repo.getProductsForDate(testDate)).thenReturn(data);

            // Act
            var result = valueUnit.getBestValuePerUnit("P1", testDate);

            // Assert - isEmpty is false, processes products
            assertFalse(result.isEmpty(), "Should return non-empty map");
            assertEquals(1, result.size());
        }

        @Test
        @DisplayName("bestStore != null decision: true path - best store found")
        void testDecision_BestStoreFound() {
            // Arrange - Multiple products, one best
            Product p1 = new Product("P1", "Item", "Cat", "Brand", 1.0, "kg", 10.0, "RON");  // 10/kg
            Product p2 = new Product("P1", "Item", "Cat", "Brand", 1.0, "kg", 8.0, "RON");   // 8/kg (best)

            Map<String, List<Product>> data = Map.of(
                "Kaufland", List.of(p1),
                "Lidl", List.of(p2)
            );
            when(repo.getProductsForDate(testDate)).thenReturn(data);

            // Act
            var result = valueUnit.getBestValuePerUnit("P1", testDate);

            // Assert - bestStore is not null
            assertEquals(2, result.size());
            assertEquals(8.0, Collections.min(result.values()), 0.01);
        }
    }

    // ==================== CALCULATION PRECISION ====================
    @Nested
    @DisplayName("Calculation Precision and Edge Cases")
    class CalculationPrecision {
        @Test
        @DisplayName("Should handle very small quantities")
        void testCalculation_VerySmallQuantity() {
            // Arrange - Very small quantity
            Product product = new Product("P1", "Gold", "Luxury", "Brand", 0.001, "kg", 50.0, "RON");
            Map<String, List<Product>> data = Map.of("Store", List.of(product));
            when(repo.getProductsForDate(testDate)).thenReturn(data);

            // Act
            var result = valueUnit.getBestValuePerUnit("P1", testDate);

            // Assert - Should calculate 50 / 0.001 = 50000
            assertEquals(50000.0, result.get("Store"), 0.01,
                        "50 RON / 0.001 kg = 50000 RON/kg");
        }

        @Test
        @DisplayName("Should handle very large quantities")
        void testCalculation_VeryLargeQuantity() {
            // Arrange - Very large quantity
            Product product = new Product("P1", "Bulk", "Bulk", "Brand", 1000.0, "kg", 1000.0, "RON");
            Map<String, List<Product>> data = Map.of("Store", List.of(product));
            when(repo.getProductsForDate(testDate)).thenReturn(data);

            // Act
            var result = valueUnit.getBestValuePerUnit("P1", testDate);

            // Assert - Should calculate 1000 / 1000 = 1
            assertEquals(1.0, result.get("Store"), 0.01,
                        "1000 RON / 1000 kg = 1 RON/kg");
        }

        @ParameterizedTest
        @ValueSource(doubles = {0.5, 1.0, 1.5, 2.5, 10.0})
        @DisplayName("Should calculate correctly for various quantities")
        void testCalculation_VariousQuantities(double quantity) {
            // Arrange - Product with specific quantity
            Product product = new Product("P1", "Item", "Cat", "Brand", quantity, "kg", 10.0, "RON");
            Map<String, List<Product>> data = Map.of("Store", List.of(product));
            when(repo.getProductsForDate(testDate)).thenReturn(data);

            // Act
            var result = valueUnit.getBestValuePerUnit("P1", testDate);

            // Assert - Result should be 10 / quantity
            double expected = 10.0 / quantity;
            assertEquals(expected, result.get("Store"), 0.01);
        }
    }

    // ==================== INTERNAL STRUCTURE TESTS ====================
    @Nested
    @DisplayName("Internal Data Structure Operations")
    class InternalStructureOperations {
        @Test
        @DisplayName("Should correctly use HashMap to store valueUnitPrices")
        void testStructure_HashMapStorage() {
            // Arrange - Multiple stores
            Product p1 = new Product("P1", "Item", "Cat", "Brand", 1.0, "kg", 5.0, "RON");
            Product p2 = new Product("P1", "Item", "Cat", "Brand", 1.0, "kg", 6.0, "RON");
            Product p3 = new Product("P1", "Item", "Cat", "Brand", 1.0, "kg", 7.0, "RON");

            Map<String, List<Product>> data = Map.of(
                "Store1", List.of(p1),
                "Store2", List.of(p2),
                "Store3", List.of(p3)
            );
            when(repo.getProductsForDate(testDate)).thenReturn(data);

            // Act
            var result = valueUnit.getBestValuePerUnit("P1", testDate);

            // Assert - All stores should be in the result
            assertEquals(3, result.size(), "Should have entry for each store");
            assertTrue(result.containsKey("Store1"));
            assertTrue(result.containsKey("Store2"));
            assertTrue(result.containsKey("Store3"));
            assertEquals(5.0, result.get("Store1"), 0.01);
            assertEquals(6.0, result.get("Store2"), 0.01);
            assertEquals(7.0, result.get("Store3"), 0.01);
        }

        @Test
        @DisplayName("Should correctly use Stream.min() to find best value")
        void testStructure_StreamMinOperation() {
            // Arrange - Find minimum value using Stream
            Product p1 = new Product("P1", "Item", "Cat", "Brand", 2.0, "kg", 14.0, "RON");  // 7/kg
            Product p2 = new Product("P1", "Item", "Cat", "Brand", 1.0, "kg", 3.0, "RON");   // 3/kg (min)
            Product p3 = new Product("P1", "Item", "Cat", "Brand", 3.0, "kg", 15.0, "RON");  // 5/kg

            Map<String, List<Product>> data = Map.of(
                "Store1", List.of(p1),
                "Store2", List.of(p2),
                "Store3", List.of(p3)
            );
            when(repo.getProductsForDate(testDate)).thenReturn(data);

            // Act
            var result = valueUnit.getBestValuePerUnit("P1", testDate);

            // Assert - Min should be 3.0 (Store2)
            double minValue = result.values().stream().min(Double::compare).orElse(Double.MAX_VALUE);
            assertEquals(3.0, minValue, 0.01, "Minimum value should be 3.0");
        }

        @Test
        @DisplayName("Should track storeUnits HashMap correctly")
        void testStructure_UnitTrackingHashMap() {
            // Arrange - Different units (converts to different standard units)
            Product p1 = new Product("P1", "Item", "Cat", "Brand", 500.0, "g", 5.0, "RON");  // → kg
            Product p2 = new Product("P1", "Item", "Cat", "Brand", 250.0, "ml", 2.5, "RON"); // → l

            Map<String, List<Product>> data = Map.of(
                "Store1", List.of(p1),
                "Store2", List.of(p2)
            );
            when(repo.getProductsForDate(testDate)).thenReturn(data);

            // Act - This calls the method that should track units internally
            var result = valueUnit.getBestValuePerUnit("P1", testDate);

            // Assert - The method should internally track units for display
            // Both should return prices with their respective conversions
            assertEquals(10.0, result.get("Store1"), 0.01, "g unit stores price in kg context");
            assertEquals(10.0, result.get("Store2"), 0.01, "ml unit stores price in l context");
        }
    }

    // ==================== LOOP AND ITERATION COVERAGE ====================
    @Nested
    @DisplayName("Loop and Iteration Coverage")
    class LoopCoverage {
        @Test
        @DisplayName("Should iterate through all stores")
        void testIteration_AllStoresProcessed() {
            // Arrange - Many stores
            Map<String, List<Product>> data = new HashMap<>();
            for (int i = 1; i <= 5; i++) {
                Product product = new Product("P1", "Item", "Cat", "Brand", 1.0, "kg", (double) i, "RON");
                data.put("Store" + i, List.of(product));
            }
            when(repo.getProductsForDate(testDate)).thenReturn(data);

            // Act
            var result = valueUnit.getBestValuePerUnit("P1", testDate);

            // Assert - All stores should be processed
            assertEquals(5, result.size(), "Should process all 5 stores");
            for (int i = 1; i <= 5; i++) {
                assertTrue(result.containsKey("Store" + i), "Store" + i + " should be in result");
            }
        }

        @Test
        @DisplayName("Should handle single store (minimal iteration)")
        void testIteration_SingleStore() {
            // Arrange - Only one store
            Product product = new Product("P1", "Item", "Cat", "Brand", 1.0, "kg", 5.0, "RON");
            Map<String, List<Product>> data = Map.of("OnlyStore", List.of(product));
            when(repo.getProductsForDate(testDate)).thenReturn(data);

            // Act
            var result = valueUnit.getBestValuePerUnit("P1", testDate);

            // Assert
            assertEquals(1, result.size());
            assertEquals(5.0, result.get("OnlyStore"), 0.01);
        }
    }

    // ==================== EDGE CASE: DUPLICATE PRODUCT NAMES ====================
    @Test
    @DisplayName("Should find productName correctly with duplicate names")
    void testEdgeCase_DuplicateProductNamesAcrossStores() {
        // Arrange - Same product name with different prices across stores
        Product p1 = new Product("P1-K", "Milk", "Dairy", "BrandA", 1.0, "l", 4.0, "RON");
        Product p2 = new Product("P1-L", "Milk", "Dairy", "BrandA", 1.0, "l", 3.5, "RON");
        Product p3 = new Product("P1-P", "Milk", "Dairy", "BrandB", 1.0, "l", 3.8, "RON");

        Map<String, List<Product>> data = Map.of(
            "Kaufland", List.of(p1),
            "Lidl", List.of(p2),
            "Profi", List.of(p3)
        );
        when(repo.getProductsForDate(testDate)).thenReturn(data);

        // Act - Query with any of the IDs
        var resultK = valueUnit.getBestValuePerUnit("P1-K", testDate);
        var resultL = valueUnit.getBestValuePerUnit("P1-L", testDate);

        // Assert - Should find same product across all stores by name
        assertEquals(3, resultK.size(), "Should find all stores for P1-K");
        assertEquals(3, resultL.size(), "Should find all stores for P1-L");
    }
}

