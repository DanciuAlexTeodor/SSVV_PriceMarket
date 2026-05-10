package com.pricecomparator.validator;

import com.pricecomparator.model.Discount;

public class DiscountValidator implements Validator<Discount> {
    
    public void validate(Discount discount) {
        if (discount.getDiscountPercent() < 0 || discount.getDiscountPercent() > 100) {
            throw new ValidationException("Invalid discount percentage: " + discount.getDiscountPercent());
        }
        if (discount.getProductName() == null || discount.getProductName().isBlank()) {
            throw new ValidationException("Product name is missing");
        }
        if (discount.getBrand() == null || discount.getBrand().isBlank()) {
            throw new ValidationException("Brand name is missing");
        }
    }
    
}
