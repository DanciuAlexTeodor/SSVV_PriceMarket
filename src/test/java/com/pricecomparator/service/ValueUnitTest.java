package com.pricecomparator.service;

import com.pricecomparator.model.Product;
import com.pricecomparator.repository.MarketDataRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class ValueUnitTest {
    private MarketDataRepository repo;
    private ValueUnit valueUnit;
    private final String date = "2025-05-01";

    @BeforeEach
    void setUp() {
        repo = mock(MarketDataRepository.class);
        valueUnit = new ValueUnit(repo);
    }

    @Test
    void testBestValuePerUnit_kg() {
        Product p1 = new Product("P1", "Banana", "Fruits", "BrandA", 2.0, "kg", 10.0, "RON");
        Product p2 = new Product("P1", "Banana", "Fruits", "BrandA", 1.0, "kg", 5.0, "RON");
        Map<String, List<Product>> data = Map.of(
                "Kaufland", List.of(p1),
                "Lidl", List.of(p2)
        );
        when(repo.getProductsForDate(date)).thenReturn(data);
        Map<String, Double> result = valueUnit.getBestValuePerUnit("P1", date);
        assertEquals(2, result.size());
        assertEquals(5.0, result.get("Lidl"));
        assertEquals(5.0, Collections.min(result.values()));
    }

    @Test
    void testBestValuePerUnit_l() {
        Product p1 = new Product("P2", "Milk", "Dairy", "BrandB", 2.0, "l", 8.0, "RON");
        Product p2 = new Product("P2", "Milk", "Dairy", "BrandB", 1.0, "l", 4.5, "RON");
        Map<String, List<Product>> data = Map.of(
                "Kaufland", List.of(p1),
                "Lidl", List.of(p2)
        );
        when(repo.getProductsForDate(date)).thenReturn(data);
        Map<String, Double> result = valueUnit.getBestValuePerUnit("P2", date);
        assertEquals(2, result.size());
        assertEquals(4.5, result.get("Lidl"));
    }

    @Test
    void testBestValuePerUnit_buc() {
        Product p1 = new Product("P3", "Eggs", "Eggs", "BrandC", 10.0, "buc", 12.0, "RON");
        Product p2 = new Product("P3", "Eggs", "Eggs", "BrandC", 6.0, "buc", 6.0, "RON");
        Map<String, List<Product>> data = Map.of(
                "Kaufland", List.of(p1),
                "Lidl", List.of(p2)
        );
        when(repo.getProductsForDate(date)).thenReturn(data);
        Map<String, Double> result = valueUnit.getBestValuePerUnit("P3", date);
        assertEquals(2, result.size());
        assertEquals(1.0, result.get("Lidl"));
    }

    @Test
    void testBestValuePerUnit_unknownUnit() {
        Product p1 = new Product("P4", "Mystery", "Other", "BrandD", 2.0, "unknown", 10.0, "RON");
        Map<String, List<Product>> data = Map.of("Kaufland", List.of(p1));
        when(repo.getProductsForDate(date)).thenReturn(data);
        Map<String, Double> result = valueUnit.getBestValuePerUnit("P4", date);
        assertEquals(1, result.size());
        assertEquals(10.0, result.get("Kaufland"));
    }

    @Test
    void testBestValuePerUnit_noProduct() {
        Map<String, List<Product>> data = Map.of("Kaufland", List.of());
        when(repo.getProductsForDate(date)).thenReturn(data);
        Map<String, Double> result = valueUnit.getBestValuePerUnit("P5", date);
        assertTrue(result.isEmpty());
    }
} 