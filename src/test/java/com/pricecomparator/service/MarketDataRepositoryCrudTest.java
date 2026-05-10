package com.pricecomparator.service;

import com.pricecomparator.model.Discount;
import com.pricecomparator.model.Product;
import com.pricecomparator.repository.DiscountRepository;
import com.pricecomparator.repository.MarketDataRepository;
import com.pricecomparator.repository.ProductRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class MarketDataRepositoryCrudTest {

    private MarketDataRepository marketDataRepository;
    private final String testDate = "2025-05-10";
    private final String store = "Kaufland";

    @BeforeEach
    void setUp() {
        // Initialize empty mock data maps for clean testing
        Map<String, Map<LocalDate, List<Product>>> productData = new HashMap<>();
        Map<String, Map<LocalDate, List<Discount>>> discountData = new HashMap<>();
        
        // Setup initial structure so our lists exist and can be safely appended to
        Map<LocalDate, List<Product>> storeProducts = new HashMap<>();
        storeProducts.put(LocalDate.parse(testDate), new ArrayList<>());
        productData.put(store, storeProducts);

        ProductRepository productRepository = new ProductRepository(productData);
        DiscountRepository discountRepository = new DiscountRepository(discountData);

        marketDataRepository = new MarketDataRepository(productRepository, discountRepository);
    }

    @Test
    void testProductCrudOperations() {
        String productId = "P999";
        
        // 1. Create (Add)
        Product newProduct = new Product(productId, "Test Product", "Snacks", "TestBrand", 1.0, "buc", 10.5, "RON", testDate);
        marketDataRepository.addProduct(store, testDate, newProduct);
        
        // Verify Create
        Product fetchedProduct = marketDataRepository.getProduct(store, productId, testDate);
        assertNotNull(fetchedProduct, "Product should be retrievable after being added.");
        assertEquals("Test Product", fetchedProduct.getName());
        assertEquals(10.5, fetchedProduct.getPrice());

        // 2. Update
        Product updatedProduct = new Product(productId, "Test Product", "Snacks", "TestBrand", 1.0, "buc", 8.5, "RON", testDate);
        marketDataRepository.updateProduct(store, testDate, productId, updatedProduct);
        
        // Verify Update
        Product fetchedUpdatedProduct = marketDataRepository.getProduct(store, productId, testDate);
        assertNotNull(fetchedUpdatedProduct);
        assertEquals(8.5, fetchedUpdatedProduct.getPrice(), "Product price should be updated to 8.5.");

        // 3. Delete
        marketDataRepository.deleteProduct(store, testDate, productId);
        
        // Verify Delete
        Product deletedProduct = marketDataRepository.getProduct(store, productId, testDate);
        assertNull(deletedProduct, "Product should be null after being deleted.");
    }

    @Test
    void testDiscountCrudOperations() {
        String productId = "P888";
        
        // 1. Create (Add)
        Discount newDiscount = new Discount(productId, "Discounted Product", "BrandX", "1", "buc", "Drinks", "2025-05-01", "2025-05-31", 20, testDate);
        marketDataRepository.addDiscount(store, testDate, newDiscount);
        
        // Verify Create
        Discount fetchedDiscount = marketDataRepository.getActiveDiscount(store, productId, testDate);
        assertNotNull(fetchedDiscount, "Discount should be retrievable after being added.");
        assertEquals(20, fetchedDiscount.getDiscountPercent());

        // 2. Update
        Discount updatedDiscount = new Discount(productId, "Discounted Product", "BrandX", "1", "buc", "Drinks", "2025-05-01", "2025-05-31", 50, testDate);
        marketDataRepository.updateDiscount(store, testDate, productId, updatedDiscount);
        
        // Verify Update
        Discount fetchedUpdatedDiscount = marketDataRepository.getActiveDiscount(store, productId, testDate);
        assertNotNull(fetchedUpdatedDiscount);
        assertEquals(50, fetchedUpdatedDiscount.getDiscountPercent(), "Discount percent should be updated to 50.");

        // 3. Delete
        marketDataRepository.deleteDiscount(store, testDate, productId);
        
        // Verify Delete
        Discount deletedDiscount = marketDataRepository.getActiveDiscount(store, productId, testDate);
        assertNull(deletedDiscount, "Discount should be null after being deleted.");
    }
}
