package com.pricecomparator.loader;

import com.opencsv.*;
import com.opencsv.exceptions.CsvException;
import com.pricecomparator.model.Discount;
import com.pricecomparator.validator.DiscountValidator;

import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class DiscountLoader {

    public static List<Discount> loadFromCSV(String filePath) {
        List<Discount> discounts = new ArrayList<>();
        DiscountValidator validator = new DiscountValidator();

        try (CSVReader reader = new CSVReaderBuilder(new FileReader(filePath))
                                     .withCSVParser(new CSVParserBuilder().withSeparator(';').build())
                                     .build()) {
            List<String[]> lines = reader.readAll();
            lines.remove(0); 

            for (String[] line : lines) {
                if (line.length < 9) continue;

                Discount d = new Discount(line[0], line[1], line[2], line[3], line[4], 
                    line[5], line[6], line[7], Integer.parseInt(line[8]));

                validator.validate(d);
                discounts.add(d);
            }

        } catch (IOException | CsvException e) {
            e.printStackTrace();
        }

        return discounts;
    }
}
