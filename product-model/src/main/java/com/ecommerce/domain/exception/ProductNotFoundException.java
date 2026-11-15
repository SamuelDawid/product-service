package com.ecommerce.domain.exception;

public class ProductNotFoundException extends NotFoundException {
    public ProductNotFoundException(Long id) {
        super("Product not found: " + id);
    }
}
