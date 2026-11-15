package com.ecommerce.domain.service;

import com.ecommerce.api.dto.page.PageQuery;
import com.ecommerce.api.dto.page.PageResponse;
import com.ecommerce.api.dto.request.CreateProductRequest;
import com.ecommerce.api.dto.request.UpdateProductRequest;
import com.ecommerce.api.dto.response.ProductResponse;
import com.ecommerce.api.dto.response.ProductSummaryResponse;
import com.ecommerce.api.service.ProductService;
import com.ecommerce.domain.Category;
import com.ecommerce.domain.Product;
import com.ecommerce.domain.exception.CategoryNotFoundException;
import com.ecommerce.domain.exception.ProductNotFoundException;
import com.ecommerce.domain.mapper.ProductDtoMapper;
import com.ecommerce.domain.ports.CategoryRepositoryPort;
import com.ecommerce.domain.ports.ProductRepositoryPort;
import com.ecommerce.domain.valueObjects.*;

import java.util.Objects;

public class ProductServiceImpl implements ProductService {
    private final ProductRepositoryPort productRepository;
    private final CategoryRepositoryPort categoryRepository;
    private final ProductDtoMapper mapper;

    public ProductServiceImpl(ProductRepositoryPort productRepository,
                              CategoryRepositoryPort categoryRepository,
                              ProductDtoMapper mapper) {
        this.productRepository = Objects.requireNonNull(productRepository);
        this.categoryRepository = Objects.requireNonNull(categoryRepository);
        this.mapper = Objects.requireNonNull(mapper);
    }

    @Override
    public ProductResponse createProduct(CreateProductRequest request) {
        Category category = loadCategory(request.categoryId());
        ProductName name = new ProductName(request.name());
        ProductPrice price = ProductPrice.fromGross(
                new Price(request.grossPrice()),
                new VatRate(request.vatRate()));

        Product product = Boolean.TRUE.equals(request.configurable())
                ? Product.configurable(name, price, category)
                : Product.standard(name, price, category);

        return mapper.toResponse(productRepository.save(product));
    }

    @Override
    public ProductResponse getProduct(Long id) {
        return mapper.toResponse(loadProduct(id));
    }

    @Override
    public PageResponse<ProductSummaryResponse> getProducts(PageQuery pageQuery) {
        return mapper.toPageResponse(productRepository.findAll(pageQuery.page(), pageQuery.size()));
    }

    @Override
    public ProductResponse updateProduct(Long id, UpdateProductRequest request) {
        Product product = loadProduct(id);

        product.rename(new ProductName(request.name()));
        product.changeBaseGrossPrice(new Price(request.grossPrice()));
        product.changeCategory(loadCategory(request.categoryId()));

        return mapper.toResponse(productRepository.save(product));
    }

    @Override
    public void archiveProduct(Long id) {
        Product product = loadProduct(id);
        product.archive();
        productRepository.save(product);
    }

    private Product loadProduct(Long id) {
        return productRepository.findById(new ProductId(id))
                .orElseThrow(() -> new ProductNotFoundException(id));
    }

    private Category loadCategory(Long id) {
        return categoryRepository.findById(id)
                .orElseThrow(() -> new CategoryNotFoundException(id));
    }
}
