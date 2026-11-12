package com.ecommerce.model.valueObjects;

import com.ecommerce.model.exception.InvalidValueException;

import java.math.BigDecimal;

public record ProductPrice(Price net, Price gross, VatRate vatRate) {

    private static final BigDecimal TOLERANCE = new BigDecimal("0.01");

    public ProductPrice {
        if (net == null || gross == null || vatRate == null) {
            throw new InvalidValueException("Net price, gross price and VAT rate are required");
        }
        BigDecimal expectedGross = net.value().multiply(BigDecimal.ONE.add(vatRate.value()));
        if (gross.value().subtract(expectedGross).abs().compareTo(TOLERANCE) > 0) {
            throw new InvalidValueException(
                    "Gross price " + gross.value() + " does not match net " + net.value()
                            + " with VAT " + vatRate.value());
        }
    }

    public static ProductPrice fromNet(Price net, VatRate vatRate) {
        return new ProductPrice(net, vatRate.grossOf(net), vatRate);
    }

    public static ProductPrice fromGross(Price gross, VatRate vatRate) {
        return new ProductPrice(vatRate.netOf(gross), gross, vatRate);
    }

    public ProductPrice withVatRateKeepingGross(VatRate newRate) {
        return fromGross(gross, newRate);
    }

    public ProductPrice withVatRateKeepingNet(VatRate newRate) {
        return fromNet(net, newRate);
    }
}
