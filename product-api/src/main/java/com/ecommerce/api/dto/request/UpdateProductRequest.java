package com.ecommerce.api.dto.request;

import jakarta.validation.constraints.*;

import java.math.BigDecimal;

public record UpdateProductRequest(

        @NotBlank
        @Size(max = 255)
        String name,

        @NotNull
        @DecimalMin("0.00")
        @Digits(integer = 17, fraction = 2)
        BigDecimal grossPrice,

        @NotNull
        @Positive
        Long categoryId
) {
}
