package com.ecommerce.api.dto.page;

public record PageQuery(int page, int size) {

    public static final int DEFAULT_SIZE = 20;
    public static final int MAX_SIZE = 100;

    public PageQuery {
        if (page < 0) {
            throw new IllegalArgumentException("Page number can not be negative");
        }
        if (size < 1 || size > MAX_SIZE) {
            throw new IllegalArgumentException("Page size must be between 1 and " + MAX_SIZE);
        }
    }

    public static PageQuery of(int page, int size) {
        return new PageQuery(page, size);
    }

    public static PageQuery firstPage() {
        return new PageQuery(0, DEFAULT_SIZE);
    }
}
