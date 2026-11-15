package com.ecommerce.api.dto.response;

public record ProductSummaryResponse(
        Long id,
        String name,
        String status,
        PriceResponse price
) {
}
