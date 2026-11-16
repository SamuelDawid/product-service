package com.ecommerce.product.adapter.out.persistence;


import com.ecommerce.model.Category;
import com.ecommerce.product.entity.CategoryEntity;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class CategoryRepositoryAdapterTest {
    @Autowired
    private CategoryRepositoryAdapter categoryRepository;

    @Autowired
    private CategoryJpaRepository categoryJpaRepository;

    @Autowired
    private EntityManager entityManager;

    @Test
    void givenCategoryWithParent_whenFindById_thenLoadsCategoryWithParent() {
        // given
        CategoryEntity computers = new CategoryEntity();
        computers.setName("Komputery");
        CategoryEntity laptops = new CategoryEntity();
        laptops.setName("Laptopy");
        laptops.setParent(computers);
        categoryJpaRepository.save(computers);
        Long laptopsId = categoryJpaRepository.save(laptops).getId();
        entityManager.flush();
        entityManager.clear();

        // when
        Optional<Category> found = categoryRepository.findById(laptopsId);

        // then
        assertThat(found).isPresent();
        assertThat(found.get().getName()).isEqualTo("Laptopy");
        assertThat(found.get().getParent().getName()).isEqualTo("Komputery");
    }

    @Test
    void givenMissingCategory_whenFindById_thenReturnsEmpty() {
        // given
        Long missingId = 999_999L;

        // when
        Optional<Category> found = categoryRepository.findById(missingId);

        // then
        assertThat(found).isEmpty();
    }
}