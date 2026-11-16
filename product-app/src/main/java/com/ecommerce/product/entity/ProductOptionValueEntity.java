package com.ecommerce.product.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@Entity
@Table(name = "product_option_values")
@Getter
@Setter
@NoArgsConstructor
public class ProductOptionValueEntity extends BaseEntity {

    @Column(nullable = false)
    private String name;

    @Column(name = "price_delta_net", nullable = false, precision = 19, scale = 2)
    private BigDecimal priceDeltaNet;

    @Column(name = "price_delta_gross", nullable = false, precision = 19, scale = 2)
    private BigDecimal priceDeltaGross;

    @Column(name = "is_default", nullable = false)
    private boolean defaultValue;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "option_id", nullable = false)
    private ProductOptionEntity option;
}