package com.pricecomparator.service;

import com.pricecomparator.model.Product;
import com.pricecomparator.repository.MarketDataRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.*;

import static org.mockito.Mockito.*;

class NewestDiscountsTest {
    private MarketDataRepository repo;
    private NewestDiscounts newestDiscounts;
    private final String date = "2025-05-01";

    @BeforeEach
    void setUp() {
        repo = mock(MarketDataRepository.class);
        newestDiscounts = new NewestDiscounts(repo);
    }

    @Test
    void testShowNewestDiscounts_withProducts() {
        Product p1 = new Product("P1", "Banana", "Fruits", "BrandA", 2.0, "kg", 10.0, "RON");
        Map<String, List<Product>> data = Map.of("Kaufland", List.of(p1));
        when(repo.getProductsForDate(date)).thenReturn(data);
        newestDiscounts.showNewestDiscounts("Kaufland", date);
    }

    @Test
    void testShowNewestDiscounts_noProducts() {
        Map<String, List<Product>> data = Map.of("Kaufland", List.of());
        when(repo.getProductsForDate(date)).thenReturn(data);
        newestDiscounts.showNewestDiscounts("Kaufland", date);
    }
} 