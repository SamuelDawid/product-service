package com.ecommerce.model;

import com.ecommerce.model.exception.InvalidValueException;

import java.math.BigDecimal;
import java.util.Objects;

final class Validate {

    private Validate() {
    }

    static String notBlank(String value, String field) {
        if (value == null || value.isBlank()) {
            throw new InvalidValueException(field + " can not be null or blank");
        }
        return value.trim();
    }

    static <T> T notNull(T value, String field) {
        if (value == null) {
            throw new InvalidValueException(field + " can not be null");
        }
        return value;
    }
}
