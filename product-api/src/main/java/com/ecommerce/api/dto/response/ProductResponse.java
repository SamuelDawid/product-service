package com.ecommerce.api.dto.response;

public record ProductResponse(
        Long id,
        String name,
        String status,
        boolean configurable,
        PriceResponse basePrice,
        CategoryResponse category
        // opcje, warianty i atrybuty dojdą w kolejnych etapach
) {
}