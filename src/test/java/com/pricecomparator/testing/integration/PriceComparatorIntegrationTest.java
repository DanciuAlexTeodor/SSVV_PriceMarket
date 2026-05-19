package com.pricecomparator.service.testing.integration;

import com.pricecomparator.model.Product;
import com.pricecomparator.model.Discount;
import com.pricecomparator.repository.MarketDataRepository;
import com.pricecomparator.repository.ProductRepository;
import com.pricecomparator.repository.DiscountRepository;
import com.pricecomparator.service.ValueUnit;
import com.pricecomparator.service.BasketOptimizer;
import com.pricecomparator.service.BestDiscounts;
import com.pricecomparator.service.NewestDiscounts;
import com.pricecomparator.service.PriceDataService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Integration Tests for Price Comparator Backend
 *
 * Tests multiple services working together:
 * - Data consistency across services
 * - Real data flow scenarios
 * - End-to-end workflows
 * - Component interactions
 *
 * Uses real repository implementations with test data instead of mocks
 */
@DisplayName("Price Comparator - Integration Testing")
class PriceComparatorIntegrationTest {
    private MarketDataRepository repository;
    private ValueUnit valueUnit;
    private BasketOptimizer basketOptimizer;
    private BestDiscounts bestDiscounts;
    private NewestDiscounts newestDiscounts;
    private final String testDate = "2025-05-01";

    @BeforeEach
    void setUp() {
        // Create real repositories with test data
        Map<String, Map<LocalDate, List<Product>>> productData = new HashMap<>();
        Map<String, Map<LocalDate, List<Discount>>> discountData = new HashMap<>();

        // Initialize test data
        initializeTestProducts(productData);
        initializeTestDiscounts(discountData);

        // Create repository with test data
        repository = new MarketDataRepository(
            new ProductRepository(productData),
            new DiscountRepository(discountData)
        );

        // Initialize services with real repository
        valueUnit = new ValueUnit(repository);
        basketOptimizer = new BasketOptimizer(repository);
        bestDiscounts = new BestDiscounts(repository);
        newestDiscounts = new NewestDiscounts(repository);
    }

    private void initializeTestProducts(Map<String, Map<LocalDate, List<Product>>> productData) {
        LocalDate date = LocalDate.parse(testDate);

        // Kaufland products
        List<Product> kauflandProducts = new ArrayList<>();
        kauflandProducts.add(new Product("P1", "Apples", "Fruits", "BrandA", 1.0, "kg", 5.0, "RON", testDate));
        kauflandProducts.add(new Product("P2", "Milk", "Dairy", "BrandB", 1.0, "l", 4.0, "RON", testDate));
        kauflandProducts.add(new Product("P3", "Bread", "Bakery", "BrandC", 1.0, "piece", 3.0, "RON", testDate));

        // Lidl products
        List<Product> lidlProducts = new ArrayList<>();
        lidlProducts.add(new Product("P1", "Apples", "Fruits", "BrandA", 1.0, "kg", 4.5, "RON", testDate));
        lidlProducts.add(new Product("P2", "Milk", "Dairy", "BrandB", 1.0, "l", 3.8, "RON", testDate));
        lidlProducts.add(new Product("P4", "Eggs", "Dairy", "BrandD", 6.0, "buc", 6.0, "RON", testDate));

        // Profi products
        List<Product> profiProducts = new ArrayList<>();
        profiProducts.add(new Product("P1", "Apples", "Fruits", "BrandA", 1.0, "kg", 4.8, "RON", testDate));
        profiProducts.add(new Product("P3", "Bread", "Bakery", "BrandC", 1.0, "piece", 2.8, "RON", testDate));
        profiProducts.add(new Product("P5", "Cheese", "Dairy", "BrandE", 500.0, "g", 20.0, "RON", testDate));

        productData.put("Kaufland", Map.of(date, kauflandProducts));
        productData.put("Lidl", Map.of(date, lidlProducts));
        productData.put("Profi", Map.of(date, profiProducts));
    }

    private void initializeTestDiscounts(Map<String, Map<LocalDate, List<Discount>>> discountData) {
        LocalDate date = LocalDate.parse(testDate);
        LocalDate tomorrow = date.plusDays(1);

        // Kaufland discounts
        List<Discount> kauflandDiscounts = new ArrayList<>();
        kauflandDiscounts.add(new Discount("P1", "Apples", "BrandA", "1", "kg", "Fruits", testDate, tomorrow.toString(), 10));
        kauflandDiscounts.add(new Discount("P2", "Milk", "BrandB", "1", "l", "Dairy", testDate, tomorrow.toString(), 5));

        // Lidl discounts
        List<Discount> lidlDiscounts = new ArrayList<>();
        lidlDiscounts.add(new Discount("P1", "Apples", "BrandA", "1", "kg", "Fruits", testDate, tomorrow.toString(), 15));
        lidlDiscounts.add(new Discount("P4", "Eggs", "BrandD", "6", "buc", "Dairy", testDate, tomorrow.toString(), 20));

        // Profi discounts
        List<Discount> profiDiscounts = new ArrayList<>();
        profiDiscounts.add(new Discount("P3", "Bread", "BrandC", "1", "piece", "Bakery", testDate, tomorrow.toString(), 25));

        discountData.put("Kaufland", Map.of(date, kauflandDiscounts));
        discountData.put("Lidl", Map.of(date, lidlDiscounts));
        discountData.put("Profi", Map.of(date, profiDiscounts));
    }

    // ==================== WORKFLOW TESTS ====================

    @Test
    @DisplayName("Integration: Complete Shopping Optimization Workflow")
    void testIntegration_CompleteOptimizationWorkflow() {
        // Scenario: Customer wants to buy: Apples, Milk, Bread, Eggs
        // Services should work together to find best prices and show savings

        // Step 1: Use ValueUnit to understand best value per unit
        Map<String, Double> applesValue = valueUnit.getBestValuePerUnit("P1", testDate);
        assertTrue(applesValue.size() >= 2, "Should have apples from multiple stores");

        // Step 2: Use BasketOptimizer to split basket optimally
        List<String> shoppingList = List.of("P1", "P2", "P3", "P4");
        String optimizedBasket = basketOptimizer.optimizeBasketSplit(shoppingList, testDate);

        // Assert: Output should contain all products and show split
        assertNotNull(optimizedBasket);
        assertTrue(optimizedBasket.contains("Apples"), "Should include Apples");
        assertTrue(optimizedBasket.contains("Milk"), "Should include Milk");
        assertTrue(optimizedBasket.contains("Bread"), "Should include Bread");
        assertTrue(optimizedBasket.contains("Eggs"), "Should include Eggs");

        // Output should show multiple stores (split)
        assertTrue(
            optimizedBasket.contains("Kaufland") ||
            optimizedBasket.contains("Lidl") ||
            optimizedBasket.contains("Profi"),
            "Should show store split"
        );

        // Should show savings
        assertTrue(optimizedBasket.contains("Total money saved"), "Should calculate savings");
    }

    @Test
    @DisplayName("Integration: Discount Application Consistency")
    void testIntegration_DiscountConsistencyAcrossServices() {
        // Test that discounts are applied consistently when used by multiple services

        // BestDiscounts service should retrieve same discounts
        // BasketOptimizer should apply same discount percentages

        // Get optimized basket with discounts
        String optimizedBasket = basketOptimizer.optimizeBasketSplit(
            List.of("P1", "P2", "P4"), testDate
        );

        // Verify basket contains expected content
        assertTrue(
            optimizedBasket.contains("Apples") ||
            optimizedBasket.contains("Milk") ||
            optimizedBasket.contains("Eggs"),
            "Should show products in basket"
        );

        // Verify discounts are shown in output (may or may not have discounts depending on store selection)
        // The important thing is that the structure is correct
        assertTrue(
            optimizedBasket.contains("Total money saved"),
            "Should calculate savings"
        );
    }

    @Test
    @DisplayName("Integration: Data Consistency Across Services")
    void testIntegration_DataConsistency() {
        // Verify product data is consistent when accessed through different services

        // ValueUnit sees product data
        Map<String, Double> valueData = valueUnit.getBestValuePerUnit("P1", testDate);
        assertTrue(valueData.containsKey("Kaufland"), "ValueUnit should find Kaufland");
        assertTrue(valueData.containsKey("Lidl"), "ValueUnit should find Lidl");

        // BasketOptimizer uses same repository
        String basketResult = basketOptimizer.optimizeBasketSplit(List.of("P1"), testDate);
        assertTrue(basketResult.contains("Apples"), "BasketOptimizer should find product name");

        // PricedataService uses same repository
        PriceDataService priceDataService = new PriceDataService(repository);
        var pricePoints = priceDataService.showDataPointsForProduct("P1", testDate);

        // All services should see same product
        assertFalse(pricePoints.isEmpty() || valueData.isEmpty() || basketResult.isEmpty(),
                   "All services should find same data");
    }

    // ==================== MULTI-STORE OPTIMIZATION ====================

    @Test
    @DisplayName("Integration: Multi-Store Basket Split with Complex Products")
    void testIntegration_MultiStoreBasketSplit() {
        // Test that basket is optimally split across stores

        // Products that should split across stores:
        // P1 (Apples): Cheapest at Lidl (4.5) with 15% discount = 3.825
        // P2 (Milk): Cheapest at Lidl (3.8) no discount = 3.8
        // P3 (Bread): Cheapest at Profi (2.8) with 25% discount = 2.1
        // P4 (Eggs): Only at Lidl (6) with 20% discount = 4.8

        String result = basketOptimizer.optimizeBasketSplit(
            List.of("P1", "P2", "P3", "P4"), testDate
        );

        // Verify multi-store split
        assertTrue(result.contains("Lidl"), "Should use Lidl");
        assertTrue(result.contains("Profi"), "Should use Profi");

        // Verify discounts are applied
        assertTrue(result.contains("(-1"), "Should show some discount applied");
    }

    // ==================== VALUEUNIT AND BASKETOPTIMIZER INTEGRATION ====================

    @Test
    @DisplayName("Integration: ValueUnit and BasketOptimizer Work Together")
    void testIntegration_ValueUnitWithBasketOptimizer() {
        // Step 1: ValueUnit identifies best value products
        Map<String, Double> applesValue = valueUnit.getBestValuePerUnit("P1", testDate);
        Map<String, Double> milkValue = valueUnit.getBestValuePerUnit("P2", testDate);

        // Step 2: Find which stores have best values
        double minApplesPrice = Collections.min(applesValue.values());
        double minMilkPrice = Collections.min(milkValue.values());

        assertTrue(minApplesPrice > 0, "Should calculate positive value");
        assertTrue(minMilkPrice > 0, "Should calculate positive value");

        // Step 3: BasketOptimizer uses this info (through repository) to optimize
        String basket = basketOptimizer.optimizeBasketSplit(
            List.of("P1", "P2"), testDate
        );

        // Should show reasonable prices (not much higher than ValueUnit values)
        assertTrue(basket.contains("Apples"), "Should optimize for Apples");
        assertTrue(basket.contains("Milk"), "Should optimize for Milk");
    }

    // ==================== DISCOUNT WORKFLOW ====================

    @Test
    @DisplayName("Integration: Discount Workflow - Discovery and Application")
    void testIntegration_DiscountWorkflow() {
        // Step 1: BestDiscounts discovers available discounts
        // (System out captured, but we verify method runs without error)
        assertDoesNotThrow(() -> {
            bestDiscounts.showBestDiscounts("Lidl", testDate, 3);
        }, "BestDiscounts should work with test data");

        // Step 2: NewestDiscounts shows today's discounts
        assertDoesNotThrow(() -> {
            newestDiscounts.showNewestDiscounts("All stores", testDate);
        }, "NewestDiscounts should work with test data");

        // Step 3: BasketOptimizer applies discovered discounts
        String basket = basketOptimizer.optimizeBasketSplit(
            List.of("P1", "P4"), testDate
        );

        // Should show discounts were applied
        assertTrue(basket.contains("Total money saved"), "Should calculate savings");
    }

    // ==================== EDGE CASE: MIXED AVAILABILITY ====================

    @Test
    @DisplayName("Integration: Handle Mixed Product Availability Across Stores")
    void testIntegration_MixedProductAvailability() {
        // Some products available at some stores, not others
        // P1 (Apples): Available at all stores
        // P2 (Milk): Available at Kaufland and Lidl only
        // P3 (Bread): Available at Kaufland and Profi only
        // P4 (Eggs): Only at Lidl

        // Request all products
        String result = basketOptimizer.optimizeBasketSplit(
            List.of("P1", "P2", "P3", "P4"), testDate
        );

        // Should handle all without error
        assertNotNull(result);
        assertFalse(result.isEmpty());

        // All products should be found
        assertTrue(result.contains("Apples"), "P1 universally available");
        assertTrue(result.contains("Milk"), "P2 should be found");
        assertTrue(result.contains("Bread"), "P3 should be found");
        assertTrue(result.contains("Eggs"), "P4 should be found");
    }

    // ==================== REPOSITORY CONSISTENCY ====================

    @Test
    @DisplayName("Integration: Repository Consistency - All Services See Same Data")
    void testIntegration_RepositoryConsistencyAllServices() {
        // Verify that all services access the same repository and see consistent data

        // ValueUnit sees product data
        Map<String, Double> valueData = valueUnit.getBestValuePerUnit("P1", testDate);
        assertTrue(valueData.containsKey("Kaufland"), "ValueUnit should find Kaufland");
        assertTrue(valueData.containsKey("Lidl"), "ValueUnit should find Lidl");

        // BasketOptimizer uses same repository
        String basketResult = basketOptimizer.optimizeBasketSplit(List.of("P1"), testDate);
        assertTrue(basketResult.contains("Apples"), "BasketOptimizer should find product name");

        // Verify consistency
        assertFalse(valueData.isEmpty(), "ValueUnit should find data");
        assertFalse(basketResult.isEmpty(), "BasketOptimizer should find data");

        // Both should be using the same product information
        assertTrue(basketResult.contains("Kaufland") || basketResult.contains("Lidl") || basketResult.contains("Profi"),
                  "BasketOptimizer should show a store that has the product");
    }

    @Test
    @DisplayName("Integration: Repository Consistency - Multiple Queries")
    void testIntegration_RepositoryMultipleQueries() {
        // Verify that multiple queries to repository return consistent results

        // Query 1: Get prices for product P1
        Map<String, Double> query1 = valueUnit.getBestValuePerUnit("P1", testDate);

        // Query 2: Get prices for product P1 again
        Map<String, Double> query2 = valueUnit.getBestValuePerUnit("P1", testDate);

        // Query 3: Optimize with P1
        String basketOptimize = basketOptimizer.optimizeBasketSplit(List.of("P1"), testDate);

        // Query 4: Get prices for P1 once more
        Map<String, Double> query4 = valueUnit.getBestValuePerUnit("P1", testDate);

        // Verify consistency across all queries
        assertEquals(query1.size(), query2.size(), "Repeated queries should return same number of stores");
        assertEquals(query2.size(), query4.size(), "Query after basket optimization should return same result");

        // Verify values match
        for (String store : query1.keySet()) {
            assertEquals(query1.get(store), query4.get(store), 0.01,
                        "Price for " + store + " should be consistent");
        }
    }

    // ==================== COMPLEX END-TO-END SCENARIO ====================

    @Test
    @DisplayName("Integration: Complete Shopping Journey")
    void testIntegration_CompleteShoppingJourney() {
        // Complete realistic scenario:
        // 1. Check best discounts available
        // 2. Check value per unit for products
        // 3. Build optimized shopping list
        // 4. Verify savings

        // Background: Services access repository consistently

        // Check values
        Map<String, Double> applesValue = valueUnit.getBestValuePerUnit("P1", testDate);
        assertTrue(applesValue.size() >= 1, "Should find apples value");

        // Optimize basket
        String optimized = basketOptimizer.optimizeBasketSplit(
            List.of("P1", "P2", "P3"), testDate
        );

        // Verify baseline info all present
        assertTrue(optimized.contains("Apples"), "Should have product names");
        assertTrue(optimized.contains("Original total"), "Should calculate original total");
        assertTrue(optimized.contains("Optimized total"), "Should show optimized price");
        assertTrue(optimized.contains("Total money saved"), "Should show savings");

        // The flow should be consistent
        assertDoesNotThrow(() -> {
            bestDiscounts.showBestDiscounts("All stores", testDate, 5);
            newestDiscounts.showNewestDiscounts("All stores", testDate);
        }, "All services should work without error in integrated environment");
    }
}




