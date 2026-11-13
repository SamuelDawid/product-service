package com.ecommerce.product.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "product_variants")
@Getter
@Setter
@NoArgsConstructor
public class ProductVariantEntity extends BaseEntity {

    @Column(nullable = false, unique = true, length = 64)
    private String sku;

    @Column(name = "price_net", nullable = false, precision = 19, scale = 2)
    private BigDecimal priceNet;

    @Column(name = "price_gross", nullable = false, precision = 19, scale = 2)
    private BigDecimal priceGross;

    @Column(nullable = false)
    private int stock;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "product_id", nullable = false)
    private ProductEntity product;

    @Setter(AccessLevel.NONE)
    @ManyToMany
    @JoinTable(
            name = "product_variant_option_values",
            joinColumns = @JoinColumn(name = "variant_id"),
            inverseJoinColumns = @JoinColumn(name = "option_value_id")
    )
    private Set<ProductOptionValueEntity> optionValues = new HashSet<>();
}