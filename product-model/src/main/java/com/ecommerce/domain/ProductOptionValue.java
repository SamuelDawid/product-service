package com.ecommerce.domain;

import com.ecommerce.domain.valueObjects.PriceDelta;
import com.ecommerce.domain.valueObjects.VatRate;
import lombok.ToString;

@ToString
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

    void applyVatRate(VatRate newRate) {
        this.priceDelta = PriceDelta.fromGross(priceDelta.gross(), newRate);
    }

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public PriceDelta getPriceDelta() {
        return priceDelta;
    }

    public boolean isDefault() {
        return isDefault;
    }
}
