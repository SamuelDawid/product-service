package com.ecommerce.domain.ports;

import com.ecommerce.domain.PageResult;
import com.ecommerce.domain.Product;
import com.ecommerce.domain.valueObjects.ProductId;

import java.util.Optional;

public interface ProductRepositoryPort {
    Product save(Product product);

    Optional<Product> findById(ProductId id);

    PageResult<Product> findAll(int page, int size);
}
