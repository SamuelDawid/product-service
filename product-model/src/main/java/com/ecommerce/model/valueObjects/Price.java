package com.ecommerce.model.valueObjects;

import java.math.BigDecimal;
import java.math.RoundingMode;

public record Price(BigDecimal value) {
    public Price {
        if (value == null || value.signum() < 0) {
            throw new IllegalArgumentException("Price can not be null or negative");
        }
        value = value.setScale(2, RoundingMode.HALF_UP);
    }

    public Price add(BigDecimal delta) {
        return new Price(value.add(delta));
    }
}
