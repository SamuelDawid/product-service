package com.ecommerce.domain.exception;

public class CategoryNotFoundException extends NotFoundException {
    public CategoryNotFoundException(Long id) {
        super("Category not found: " + id);
    }
}
