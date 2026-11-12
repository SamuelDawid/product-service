package com.ecommerce.model.valueObjects;

import com.ecommerce.model.exception.InvalidValueException;

import java.util.regex.Pattern;

public record Sku(String value) {

    private static final int MAX_LENGTH = 64;
    private static final Pattern FORMAT = Pattern.compile("^[A-Z0-9]+(-[A-Z0-9]+)*$");

    public Sku {
        if (value == null || value.isBlank()) {
            throw new InvalidValueException("SKU can not be null or blank");
        }
        value = value.trim().toUpperCase();
        if (value.length() > MAX_LENGTH) {
            throw new InvalidValueException("SKU can not be longer than " + MAX_LENGTH + " characters");
        }
        if (!FORMAT.matcher(value).matches()) {
            throw new InvalidValueException(
                    "SKU may contain only letters, digits and single hyphens, got: " + value);
        }
    }
}
