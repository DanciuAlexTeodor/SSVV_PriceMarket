package com.pricecomparator.service;

import com.pricecomparator.model.Product;
import com.pricecomparator.model.Discount;
import com.pricecomparator.repository.MarketDataRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class BasketOptimizerTest {
    private MarketDataRepository repo;
    private BasketOptimizer optimizer;
    private final String date = "2025-05-01";

    @BeforeEach
    void setUp() {
        repo = mock(MarketDataRepository.class);
        optimizer = new BasketOptimizer(repo);
    }

    @Test
    void testOptimizeBasketSplit_basic() {
        Product p1 = new Product("P1", "Banana", "Fruits", "BrandA", 2.0, "kg", 10.0, "RON");
        Product p2 = new Product("P2", "Milk", "Dairy", "BrandB", 2.0, "l", 8.0, "RON");
        Map<String, List<Product>> data = Map.of(
                "Kaufland", List.of(p1),
                "Lidl", List.of(p2)
        );
        when(repo.getProductsForDate(date)).thenReturn(data);
        when(repo.getProduct("Kaufland", "P1")).thenReturn(p1);
        when(repo.getProduct("Lidl", "P2")).thenReturn(p2);
        when(repo.getActiveDiscount(anyString(), anyString(), anyString())).thenReturn(null);
        List<String> basket = List.of("P1", "P2");
        optimizer.optimizeBasketSplit(basket, date);
    }

    @Test
    void testOptimizeBasketSplit_withDiscount() {
        Product p1 = new Product("P1", "Banana", "Fruits", "BrandA", 2.0, "kg", 10.0, "RON");
        Discount d1 = mock(Discount.class);
        when(d1.getDiscountPercent()).thenReturn(10);
        Map<String, List<Product>> data = Map.of("Kaufland", List.of(p1));
        when(repo.getProductsForDate(date)).thenReturn(data);
        when(repo.getProduct("Kaufland", "P1")).thenReturn(p1);
        when(repo.getActiveDiscount("Kaufland", "P1", date)).thenReturn(d1);
        List<String> basket = List.of("P1");
        optimizer.optimizeBasketSplit(basket, date);
    }

    @Test
    void testOptimizeBasketSplit_missingProduct() {
        Map<String, List<Product>> data = Map.of("Kaufland", List.of());
        when(repo.getProductsForDate(date)).thenReturn(data);
        when(repo.getProduct(anyString(), anyString())).thenReturn(null);
        List<String> basket = List.of("P1");
        optimizer.optimizeBasketSplit(basket, date);
    }
} 