package com.ecommerce.model;

import com.ecommerce.model.valueObjects.PriceDelta;
import com.ecommerce.model.valueObjects.VatRate;
import lombok.Getter;
import lombok.ToString;

import java.math.BigDecimal;
import java.util.Objects;
@ToString
@Getter
public class ProductOptionValue {

    private final Long id;
    private String name;
    private PriceDelta priceDelta;
    private final boolean isDefault;

    public ProductOptionValue(Long id, String name, PriceDelta priceDelta, boolean isDefault) {
        this.id = id;
        this.name = Validate.notBlank(name, "Option value name");
        this.priceDelta = Validate.notNull(priceDelta, "Price delta");
        this.isDefault = isDefault;
    }

    public void rename(String newName) {
        this.name = Validate.notBlank(newName, "Option value name");
    }

    public void changePriceDelta(PriceDelta newDelta) {
        this.priceDelta = Validate.notNull(newDelta, "Price delta");
    }

    /** Wołane tylko przez Product przy zmianie stawki VAT: brutto zostaje. */
    void applyVatRate(VatRate newRate) {
        this.priceDelta = PriceDelta.fromGross(priceDelta.gross(), newRate);
    }
}
