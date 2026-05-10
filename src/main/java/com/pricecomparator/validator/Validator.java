package com.pricecomparator.validator;

public interface Validator<T> {
    void validate(T input) throws ValidationException;
}

