package com.ecommerce.domain.ports;

import com.ecommerce.domain.Category;

import java.util.Optional;

public interface CategoryRepositoryPort {
    Optional<Category> findById(Long id);
}
