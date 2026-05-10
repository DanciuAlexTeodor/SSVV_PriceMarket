package com.pricecomparator.service;

import com.pricecomparator.model.PriceAlert;
import com.pricecomparator.model.Product;
import com.pricecomparator.model.Discount;
import com.pricecomparator.repository.AlertRepository;
import com.pricecomparator.repository.MarketDataRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class PriceAlertServiceTest {
    private AlertRepository alertRepo;
    private MarketDataRepository marketRepo;
    private PriceAlertService alertService;
    private final String date = "2025-05-01";

    @BeforeEach
    void setUp() {
        alertRepo = mock(AlertRepository.class);
        marketRepo = mock(MarketDataRepository.class);
        alertService = new PriceAlertService(alertRepo, marketRepo);
    }

    @Test
    void testCreateAndGetActiveAlerts() {
        PriceAlert alert = new PriceAlert("P1", "Banana", 5.0, "user1");
        when(alertRepo.getActiveAlerts()).thenReturn(List.of(alert));
        alertService.createAlert("P1", "Banana", 5.0, "user1");
        List<PriceAlert> alerts = alertService.getActiveAlerts();
        assertEquals(1, alerts.size());
        assertEquals("Banana", alerts.get(0).getProductName());
    }

    @Test
    void testCheckAlerts_triggered() {
        PriceAlert alert = new PriceAlert("P1", "Banana", 10.0, "user1");
        Product product = new Product("P1", "Banana", "Fruits", "BrandA", 1.0, "kg", 8.0, "RON");
        Map<String, List<Product>> data = Map.of("Kaufland", List.of(product));
        when(alertRepo.getActiveAlerts()).thenReturn(List.of(alert));
        when(marketRepo.getProductsForDate(date)).thenReturn(data);
        when(marketRepo.getActiveDiscount(anyString(), anyString(), anyString())).thenReturn(null);
        List<PriceAlert> triggered = alertService.checkAlerts(date);
        assertEquals(1, triggered.size());
        assertEquals("Banana", triggered.get(0).getProductName());
    }

    @Test
    void testCheckAlerts_notTriggered() {
        PriceAlert alert = new PriceAlert("P1", "Banana", 5.0, "user1");
        Product product = new Product("P1", "Banana", "Fruits", "BrandA", 1.0, "kg", 8.0, "RON");
        Map<String, List<Product>> data = Map.of("Kaufland", List.of(product));
        when(alertRepo.getActiveAlerts()).thenReturn(List.of(alert));
        when(marketRepo.getProductsForDate(date)).thenReturn(data);
        when(marketRepo.getActiveDiscount(anyString(), anyString(), anyString())).thenReturn(null);
        List<PriceAlert> triggered = alertService.checkAlerts(date);
        assertTrue(triggered.isEmpty());
    }

    @Test
    void testNoAlerts() {
        when(alertRepo.getActiveAlerts()).thenReturn(List.of());
        List<PriceAlert> alerts = alertService.getActiveAlerts();
        assertTrue(alerts.isEmpty());
    }
} 