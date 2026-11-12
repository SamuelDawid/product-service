package com.ecommerce.model.valueObjects;

public record ProductId(Long value) {
    public ProductId {
        if (value == null || value <= 0) {
            throw new IllegalArgumentException("Product id must be positive");
        }
    }
}
