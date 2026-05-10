package com.pricecomparator.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.mockito.Mockito.*;

class PriceDataServiceTest {
    private PriceDataService priceDataService;

    @BeforeEach
    void setUp() {
        priceDataService = new PriceDataService();
    }

    @Test
    void testShowDataPointsForProduct_noFilters() {
        // This test assumes the method prints output; in a real test, capture output or refactor for return values
        priceDataService.showDataPointsForProduct("P1", "2025-05-01", null, null, null);
    }

    @Test
    void testShowDataPointsForProduct_withFilters() {
        priceDataService.showDataPointsForProduct("P1", "2025-05-01", "Fruits", "BrandA", "Kaufland");
    }

    @Test
    void testShowDataPointsForProduct_missingProduct() {
        priceDataService.showDataPointsForProduct("P999", "2025-05-01", null, null, null);
    }
} 