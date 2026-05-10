package com.pricecomparator.loader;

import com.opencsv.CSVReader;
import com.opencsv.CSVReaderBuilder;
import com.opencsv.CSVParserBuilder;
import com.opencsv.exceptions.CsvException;
import com.pricecomparator.model.Product;
import com.pricecomparator.validator.ProductValidator;

import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class ProductLoader {

    public static List<Product> loadFromCSV(String filePath) {
        List<Product> products = new ArrayList<>();
        ProductValidator validator = new ProductValidator();

        try (CSVReader reader = new CSVReaderBuilder(new FileReader(filePath))
                .withCSVParser(new CSVParserBuilder().withSeparator(';').build())
                .build()) {

            List<String[]> lines = reader.readAll();
            lines.remove(0); 

            for (String[] line : lines) {
                if (line.length < 8) continue;

                try {
                    Product p = new Product(line[0], line[1], line[2], line[3], 
                        Double.parseDouble(line[4]), line[5], Double.parseDouble(line[6]), line[7]);


                    validator.validate(p); 
                    products.add(p);
                } catch (NumberFormatException e) {
                    System.err.println("Failed to parse number in line: " + String.join(";", line));
                }
            }

        } catch (IOException | CsvException e) {
            e.printStackTrace();
        }

        return products;
    }
}
