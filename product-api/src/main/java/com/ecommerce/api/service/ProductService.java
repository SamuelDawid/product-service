package com.ecommerce.api.service;

import com.ecommerce.api.dto.page.PageQuery;
import com.ecommerce.api.dto.page.PageResponse;
import com.ecommerce.api.dto.request.CreateProductRequest;
import com.ecommerce.api.dto.request.UpdateProductRequest;
import com.ecommerce.api.dto.response.ProductResponse;
import com.ecommerce.api.dto.response.ProductSummaryResponse;

public interface ProductService {

    ProductResponse createProduct(CreateProductRequest request);

    ProductResponse getProduct(Long id);

    PageResponse<ProductSummaryResponse> getProducts(PageQuery pageQuery);

    ProductResponse updateProduct(Long id, UpdateProductRequest request);

    void archiveProduct(Long id);
}
