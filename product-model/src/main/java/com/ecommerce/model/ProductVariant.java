package com.ecommerce.model;

import com.ecommerce.model.exception.BusinessRuleException;
import com.ecommerce.model.valueObjects.*;
import lombok.Getter;
import lombok.ToString;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

@Getter
@ToString
public class ProductVariant {

    private final Long id;
    private final Sku sku;
    private ProductPrice price;
    private Stock stock;
    private final Set<ProductOptionValue> optionValues = new HashSet<>();

    public ProductVariant(Long id, Sku sku, ProductPrice price, Stock stock,
                          Collection<ProductOptionValue> optionValues) {
        this.id = id;
        this.sku = Validate.notNull(sku, "SKU");
        this.price = Validate.notNull(price, "Variant price");
        this.stock = Validate.notNull(stock, "Stock");
        if (optionValues == null || optionValues.isEmpty()) {
            throw new BusinessRuleException("Variant must have at least one option value");
        }
        this.optionValues.addAll(optionValues);
    }

    public boolean isInStock() {
        return stock.isAvailable();
    }

    public void increaseStock(int amount) {
        this.stock = stock.increase(amount);
    }

    public void decreaseStock(int amount) {
        this.stock = stock.decrease(amount);
    }

    public void changeGrossPrice(Price newGross) {
        this.price = ProductPrice.fromGross(newGross, price.vatRate());
    }

    void applyVatRate(VatRate newRate) {
        this.price = price.withVatRateKeepingGross(newRate);
    }
}
