package com.ecommerce.api.dto.response;

import java.math.BigDecimal;

public record PriceResponse(
        BigDecimal net,
        BigDecimal gross,
        BigDecimal vatRate
) {
}
