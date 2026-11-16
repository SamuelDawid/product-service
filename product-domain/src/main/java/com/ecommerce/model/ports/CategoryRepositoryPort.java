package com.ecommerce.model.ports;

import com.ecommerce.model.Category;

import java.util.Optional;

public interface CategoryRepositoryPort {
    Optional<Category> findById(Long id);
}
