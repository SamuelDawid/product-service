package com.ecommerce.model.ports;

import com.ecommerce.model.PageResult;
import com.ecommerce.model.Product;
import com.ecommerce.model.valueobjects.ProductId;

import java.util.Optional;

public interface ProductRepositoryPort {
    Product save(Product product);

    Optional<Product> findById(ProductId id);

    PageResult<Product> findAll(int page, int size);
}
