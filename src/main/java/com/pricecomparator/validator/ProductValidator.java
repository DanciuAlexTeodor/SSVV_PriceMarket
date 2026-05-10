package com.pricecomparator.validator;

import com.pricecomparator.model.Product;

public class ProductValidator implements Validator<Product> {
    public void validate(Product p) {
        if (p.getName() == null || p.getName().isBlank())
            throw new ValidationException("Product name is missing");

        if (p.getQuantity() <= 0)
            throw new ValidationException("Invalid quantity: " + p.getQuantity());

        if (p.getPrice() < 0)
            throw new ValidationException("Price cannot be negative");
    }
}
