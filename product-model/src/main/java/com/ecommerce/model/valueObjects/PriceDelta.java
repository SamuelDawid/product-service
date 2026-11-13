package com.ecommerce.model.valueObjects;

import com.ecommerce.model.exception.InvalidValueException;

import java.math.BigDecimal;
import java.math.RoundingMode;

public record PriceDelta(BigDecimal net, BigDecimal gross) {

    public PriceDelta {
        if (net == null || gross == null) {
            throw new InvalidValueException("Price delta net and gross are required");
        }
        net = net.setScale(2, RoundingMode.HALF_UP);
        gross = gross.setScale(2, RoundingMode.HALF_UP);
        // ta sama reguła co CHECK w bazie: ten sam znak, |brutto| >= |netto|
        if (net.signum() * gross.signum() < 0 || gross.abs().compareTo(net.abs()) < 0) {
            throw new InvalidValueException(
                    "Inconsistent price delta: net " + net + ", gross " + gross);
        }
    }

    public static PriceDelta fromGross(BigDecimal gross, VatRate vatRate) {
        if (gross == null || vatRate == null) {
            throw new InvalidValueException("Gross delta and VAT rate are required");
        }
        BigDecimal net = gross.divide(BigDecimal.ONE.add(vatRate.value()), 2, RoundingMode.HALF_UP);
        return new PriceDelta(net, gross);
    }
}
