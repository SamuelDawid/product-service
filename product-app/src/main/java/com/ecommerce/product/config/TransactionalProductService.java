package com.ecommerce.product.config;

import com.ecommerce.api.dto.page.PageQuery;
import com.ecommerce.api.dto.page.PageResponse;
import com.ecommerce.api.dto.request.CreateProductRequest;
import com.ecommerce.api.dto.request.UpdateProductRequest;
import com.ecommerce.api.dto.response.ProductResponse;
import com.ecommerce.api.dto.response.ProductSummaryResponse;
import com.ecommerce.api.service.ProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.transaction.annotation.Transactional;

@Transactional
@RequiredArgsConstructor
public class TransactionalProductService implements ProductService {

    private final ProductService delegate;

    @Override
    public ProductResponse createProduct(CreateProductRequest request) {
        return delegate.createProduct(request);
    }

    @Override
    @Transactional(readOnly = true)
    public ProductResponse getProduct(Long id) {
        return delegate.getProduct(id);
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<ProductSummaryResponse> getProducts(PageQuery pageQuery) {
        return delegate.getProducts(pageQuery);
    }

    @Override
    public ProductResponse updateProduct(Long id, UpdateProductRequest request) {
        return delegate.updateProduct(id, request);
    }

    @Override
    public void archiveProduct(Long id) {
        delegate.archiveProduct(id);
    }
}
