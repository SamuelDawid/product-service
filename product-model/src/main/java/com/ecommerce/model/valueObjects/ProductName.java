package com.ecommerce.model.valueObjects;

public record ProductName(String value) {
    public ProductName {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("Product name can not be null or blank");
        }
    }
}
