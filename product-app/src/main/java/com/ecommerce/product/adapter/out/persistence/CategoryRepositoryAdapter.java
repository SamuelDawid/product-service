package com.ecommerce.product.adapter.out.persistence;

import com.ecommerce.domain.Category;
import com.ecommerce.domain.ports.CategoryRepositoryPort;
import com.ecommerce.product.ProductPersistenceMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
@Repository
@RequiredArgsConstructor
public class CategoryRepositoryAdapter implements CategoryRepositoryPort {
    private final CategoryJpaRepository categoryJpaRepository;
    private final ProductPersistenceMapper mapper;

    @Override
    @Transactional(readOnly = true)
    public Optional<Category> findById(Long id) {
        return categoryJpaRepository.findById(id).map(mapper::toDomain);
    }
}
