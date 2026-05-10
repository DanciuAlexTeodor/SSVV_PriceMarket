package com.pricecomparator;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class PriceComparatorApplication {
    public static void main(String[] args) {
        System.out.println("Starting the Price Comparator Web Server...");
        SpringApplication.run(PriceComparatorApplication.class, args);
    }
}