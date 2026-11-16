package com.ecommerce.product.adapter.out.persistence;

import com.ecommerce.model.Category;
import com.ecommerce.model.PageResult;
import com.ecommerce.model.Product;
import com.ecommerce.model.enums.ProductStatus;
import com.ecommerce.model.valueobjects.*;
import com.ecommerce.product.entity.CategoryEntity;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class ProductRepositoryAdapterTest {
    private static final VatRate VAT_23 = new VatRate(new BigDecimal("0.23"));

    @Autowired
    private ProductRepositoryAdapter productRepository;

    @Autowired
    private CategoryRepositoryAdapter categoryRepository;

    @Autowired
    private CategoryJpaRepository categoryJpaRepository;

    @Autowired
    private EntityManager entityManager;

    private Category laptops;

    @BeforeEach
    void setUp() {
        CategoryEntity entity = new CategoryEntity();
        entity.setName("Laptopy");
        Long categoryId = categoryJpaRepository.saveAndFlush(entity).getId();
        laptops = categoryRepository.findById(categoryId).orElseThrow();
    }

    @Test
    void givenNewProduct_whenSave_thenAssignsIdAndStoresNetAndGrossPrice() {
        //given
        Product product = laptop("Laptop Pro 14", "4999.99");
        //when
        Product saved = productRepository.save(product);
        //then
        assertThat(saved.getId()).isNotNull();
        assertThat(saved.getStatus()).isEqualTo(ProductStatus.DRAFT);
        assertThat(saved.getBasePrice().gross().value()).isEqualByComparingTo("4999.99");
        assertThat(saved.getBasePrice().net().value()).isEqualByComparingTo("4065.03");
    }

    @Test
    void givenSavedProduct_whenFindById_thenReadsProductFromDatabase() {
        // given
        ProductId id = productRepository.save(laptop("Laptop Pro 14", "4999.99")).getId();
        clearPersistenceContext();

        // when
        Optional<Product> found = productRepository.findById(id);

        // then
        assertThat(found).isPresent();
        assertThat(found.get().getName().value()).isEqualTo("Laptop Pro 14");
        assertThat(found.get().getVatRate().value()).isEqualByComparingTo("0.23");
        assertThat(found.get().getCategory().getName()).isEqualTo("Laptopy");
    }

    @Test
    void givenSavedProduct_whenSaveAfterChanges_thenUpdatesExistingProduct() {
        // given
        ProductId id = productRepository.save(laptop("Laptop Pro 14", "4999.99")).getId();
        clearPersistenceContext();
        Product product = productRepository.findById(id).orElseThrow();
        product.rename(new ProductName("Laptop Pro 16"));
        product.changeBaseGrossPrice(new Price(new BigDecimal("5999.99")));
        product.archive();

        // when
        productRepository.save(product);
        clearPersistenceContext();

        // then
        Product reloaded = productRepository.findById(id).orElseThrow();
        assertThat(reloaded.getName().value()).isEqualTo("Laptop Pro 16");
        assertThat(reloaded.getBasePrice().gross().value()).isEqualByComparingTo("5999.99");
        assertThat(reloaded.getStatus()).isEqualTo(ProductStatus.ARCHIVED);
    }

    @Test
    void givenMissingProduct_whenFindById_thenReturnsEmpty() {
        // given
        ProductId missingId = new ProductId(999_999L);

        // when
        Optional<Product> found = productRepository.findById(missingId);

        // then
        assertThat(found).isEmpty();
    }

    @Test
    void givenThreeProducts_whenFindAllWithPageSizeTwo_thenReturnsFirstTwoSortedById() {
        // given
        productRepository.save(laptop("Laptop A", "3999.99"));
        productRepository.save(laptop("Laptop B", "4999.99"));
        productRepository.save(laptop("Laptop C", "5999.99"));
        clearPersistenceContext();

        // when
        PageResult<Product> page = productRepository.findAll(0, 2);

        // then
        assertThat(page.totalElements()).isEqualTo(3);
        assertThat(page.totalPages()).isEqualTo(2);
        assertThat(page.content())
                .extracting(product -> product.getName().value())
                .containsExactly("Laptop A", "Laptop B");
    }

    private Product laptop(String name, String grossPrice) {
        return Product.standard(
                new ProductName(name),
                ProductPrice.fromGross(new Price(new BigDecimal(grossPrice)), VAT_23),
                laptops);
    }

    private void clearPersistenceContext() {
        entityManager.flush();
        entityManager.clear();
    }
}