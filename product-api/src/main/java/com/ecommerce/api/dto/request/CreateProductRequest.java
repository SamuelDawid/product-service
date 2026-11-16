package com.ecommerce.api.dto.request;

import jakarta.validation.constraints.*;

import java.math.BigDecimal;

public record CreateProductRequest(

        @NotBlank
        @Size(max = 255)
        String name,

        @NotNull
        @DecimalMin("0.00")
        @Digits(integer = 17, fraction = 2)
        BigDecimal grossPrice,

        @NotNull
        @DecimalMin("0.00")
        @DecimalMax("1.00")
        @Digits(integer = 1, fraction = 4)
        BigDecimal vatRate,

        @NotNull
        Boolean configurable,

        @NotNull
        @Positive
        Long categoryId
) {
}
