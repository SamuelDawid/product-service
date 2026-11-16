package com.ecommerce.model;

import com.ecommerce.model.enums.ProductStatus;
import com.ecommerce.model.exception.BusinessRuleException;
import com.ecommerce.model.valueobjects.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;

public class ProductStatusTest {
    @Test
    void givenArchivedProduct_whenRestore_thenStatusIsDraft() {
        // given
        Product product = laptopWithStatus(ProductStatus.ARCHIVED);

        // when
        product.restore();

        // then
        assertThat(product.getStatus()).isEqualTo(ProductStatus.DRAFT);
        assertThat(product.isPurchasable()).isFalse();
    }

    @ParameterizedTest
    @EnumSource(value = ProductStatus.class, names = {"DRAFT", "ACTIVE"})
    void givenNotArchivedProduct_whenRestore_thenThrowsBusinessRuleException(ProductStatus status) {
        // given
        Product product = laptopWithStatus(status);

        // when
        Throwable thrown = catchThrowable(product::restore);

        // then
        assertThat(thrown).isInstanceOf(BusinessRuleException.class);
        assertThat(product.getStatus()).isEqualTo(status);
    }

    @Test
    void givenArchivedProduct_whenActivate_thenThrowsBusinessRuleException() {
        // given
        Product product = laptopWithStatus(ProductStatus.ARCHIVED);

        // when
        Throwable thrown = catchThrowable(product::activate);

        // then
        assertThat(thrown).isInstanceOf(BusinessRuleException.class);
        assertThat(product.getStatus()).isEqualTo(ProductStatus.ARCHIVED);
    }

    @Test
    void givenRestoredProduct_whenActivate_thenProductIsPurchasable() {
        // given
        Product product = laptopWithStatus(ProductStatus.ARCHIVED);
        product.restore();

        // when
        product.activate();

        // then
        assertThat(product.getStatus()).isEqualTo(ProductStatus.ACTIVE);
        assertThat(product.isPurchasable()).isTrue();
    }

    private static Product laptopWithStatus(ProductStatus status) {
        return Product.restore(
                new ProductId(1L),
                new ProductName("Laptop Pro 14"),
                status,
                ProductPrice.fromGross(
                        new Price(new BigDecimal("4999.99")),
                        new VatRate(new BigDecimal("0.23"))),
                false,
                new Category(5L, "Laptopy"),
                List.of(), List.of(), List.of());
    }
}
