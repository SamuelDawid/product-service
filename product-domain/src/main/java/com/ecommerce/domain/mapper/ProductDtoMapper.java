package com.ecommerce.domain.mapper;

import com.ecommerce.api.dto.page.PageResponse;
import com.ecommerce.api.dto.response.CategoryResponse;
import com.ecommerce.api.dto.response.PriceResponse;
import com.ecommerce.api.dto.response.ProductResponse;
import com.ecommerce.api.dto.response.ProductSummaryResponse;
import com.ecommerce.domain.Category;
import com.ecommerce.domain.PageResult;
import com.ecommerce.domain.Product;
import com.ecommerce.domain.valueObjects.ProductPrice;

public class ProductDtoMapper {
    public ProductResponse toResponse(Product product) {
        return new ProductResponse(
                idOf(product),
                product.getName().value(),
                product.getStatus().name(),
                product.isConfigurable(),
                toPriceResponse(product.getBasePrice()),
                toCategoryResponse(product.getCategory())
        );
    }

    public ProductSummaryResponse toSummaryResponse(Product product) {
        return new ProductSummaryResponse(
                idOf(product),
                product.getName().value(),
                product.getStatus().name(),
                toPriceResponse(product.getBasePrice())
        );
    }

    public PageResponse<ProductSummaryResponse> toPageResponse(PageResult<Product> page) {
        return new PageResponse<>(
                page.content().stream().map(this::toSummaryResponse).toList(),
                page.page(),
                page.size(),
                page.totalElements(),
                page.totalPages()
        );
    }

    private PriceResponse toPriceResponse(ProductPrice price) {
        return new PriceResponse(
                price.net().value(),
                price.gross().value(),
                price.vatRate().value()
        );
    }

    private CategoryResponse toCategoryResponse(Category category) {
        return new CategoryResponse(category.getId(), category.getName());
    }

    private Long idOf(Product product) {
        return product.getId() == null ? null : product.getId().value();
    }
}
