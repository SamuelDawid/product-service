package com.ecommerce.product.adapter.out.persistence;

import com.ecommerce.domain.PageResult;
import com.ecommerce.domain.Product;
import com.ecommerce.domain.exception.ProductNotFoundException;
import com.ecommerce.domain.ports.ProductRepositoryPort;
import com.ecommerce.domain.valueObjects.ProductId;
import com.ecommerce.product.MappingContext;
import com.ecommerce.product.ProductPersistenceMapper;
import com.ecommerce.product.entity.CategoryEntity;
import com.ecommerce.product.entity.ProductEntity;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class ProductRepositoryAdapter implements ProductRepositoryPort {
    private final ProductJpaRepository productJpaRepository;
    private final CategoryJpaRepository categoryJpaRepository;
    private final ProductPersistenceMapper mapper;

    @Override
    @Transactional
    public Product save(Product product) {
        ProductEntity entity = product.getId() == null
                ? toNewEntity(product)
                : updateExistingEntity(product);
        ProductEntity saved = productJpaRepository.saveAndFlush(entity);
        return mapper.toDomain(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Product> findById(ProductId id) {
        return productJpaRepository.findById(id.value()).map(mapper::toDomain);
    }

    @Override
    public PageResult<Product> findAll(int page, int size) {
        Page<ProductEntity> result = productJpaRepository.findAll(
                PageRequest.of(page, size, Sort.by("id")));
        return new PageResult<>(
                result.getContent().stream().map(mapper::toDomain).toList(),
                page,
                size,
                result.getTotalElements()
        );
    }

    private ProductEntity toNewEntity(Product product) {
        ProductEntity entity = mapper.toEntity(product, new MappingContext());
        entity.setCategory(categoryReference(product));
        return entity;
    }

    private ProductEntity updateExistingEntity(Product product) {
        Long id = product.getId().value();
        ProductEntity entity = productJpaRepository.findById(id)
                .orElseThrow(() -> new ProductNotFoundException(id));

        entity.setName(product.getName().value());
        entity.setStatus(product.getStatus());
        entity.setBasePriceNet(product.getBasePrice().net().value());
        entity.setBasePriceGross(product.getBasePrice().gross().value());
        entity.setVatRate(product.getBasePrice().vatRate().value());
        entity.setCategory(categoryReference(product));
        return entity;
    }

    private CategoryEntity categoryReference(Product product) {
        return categoryJpaRepository.getReferenceById(product.getCategory().getId());
    }
}
