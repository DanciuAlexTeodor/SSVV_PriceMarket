package com.pricecomparator.service;

import com.pricecomparator.model.Discount;
import com.pricecomparator.repository.MarketDataRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.*;

import static org.mockito.Mockito.*;

class BestDiscountsTest {
    private MarketDataRepository repo;
    private BestDiscounts bestDiscounts;
    private final String date = "2025-05-01";

    @BeforeEach
    void setUp() {
        repo = mock(MarketDataRepository.class);
        bestDiscounts = new BestDiscounts(repo);
    }

    @Test
    void testShowBestDiscounts_singleStore() {
        Discount d1 = mock(Discount.class);
        when(d1.getDiscountPercent()).thenReturn(20);
        Discount d2 = mock(Discount.class);
        when(d2.getDiscountPercent()).thenReturn(10);
        Map<String, List<Discount>> data = Map.of("Kaufland", new ArrayList<>(List.of(d1, d2)));
        when(repo.getValidDiscountsForDate(date)).thenReturn(data);
        bestDiscounts.showBestDiscounts("Kaufland", date, 2);
    }

    @Test
    void testShowBestDiscounts_allStores() {
        Discount d1 = mock(Discount.class);
        when(d1.getDiscountPercent()).thenReturn(20);
        Discount d2 = mock(Discount.class);
        when(d2.getDiscountPercent()).thenReturn(10);
        Map<String, List<Discount>> data = Map.of(
                "Kaufland", new ArrayList<>(List.of(d1)),
                "Lidl", new ArrayList<>(List.of(d2))
        );
        when(repo.getValidDiscountsForDate(date)).thenReturn(data);
        bestDiscounts.showBestDiscounts("All stores", date, 2);
    }

    @Test
    void testShowBestDiscounts_noDiscounts() {
        Map<String, List<Discount>> data = Map.of("Kaufland", new ArrayList<>());
        when(repo.getValidDiscountsForDate(date)).thenReturn(data);
        bestDiscounts.showBestDiscounts("Kaufland", date, 2);
    }
} 