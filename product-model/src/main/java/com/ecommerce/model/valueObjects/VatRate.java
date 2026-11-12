package com.ecommerce.model.valueObjects;

import com.ecommerce.model.exception.InvalidValueException;

import java.math.BigDecimal;
import java.math.RoundingMode;

public record VatRate(BigDecimal value) {

    private static final int SCALE = 4;

    public VatRate {
        if (value == null) {
            throw new InvalidValueException("VAT rate can not be null");
        }
        if (value.signum() < 0 || value.compareTo(BigDecimal.ONE) > 0) {
            throw new InvalidValueException("VAT rate must be between 0 and 1, got: " + value);
        }
        value = value.setScale(SCALE, RoundingMode.HALF_UP);
    }

    public static VatRate of(String value) {
        return new VatRate(new BigDecimal(value));
    }


    public Price grossOf(Price net) {
        return new Price(net.value().multiply(BigDecimal.ONE.add(value)));
    }

    public Price netOf(Price gross) {
        return new Price(gross.value().divide(BigDecimal.ONE.add(value), 2, RoundingMode.HALF_UP));
    }
}
