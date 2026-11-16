package com.ecommerce.model.service;

import com.ecommerce.api.dto.page.PageQuery;
import com.ecommerce.api.dto.page.PageResponse;
import com.ecommerce.api.dto.request.CreateProductRequest;
import com.ecommerce.api.dto.request.UpdateProductRequest;
import com.ecommerce.api.dto.response.ProductResponse;
import com.ecommerce.api.dto.response.ProductSummaryResponse;
import com.ecommerce.model.Category;
import com.ecommerce.model.PageResult;
import com.ecommerce.model.Product;
import com.ecommerce.model.enums.ProductStatus;
import com.ecommerce.model.exception.CategoryNotFoundException;
import com.ecommerce.model.exception.ProductNotFoundException;
import com.ecommerce.model.mapper.ProductDtoMapper;
import com.ecommerce.model.ports.CategoryRepositoryPort;
import com.ecommerce.model.ports.ProductRepositoryPort;
import com.ecommerce.model.valueobjects.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProductServiceImplTest {
    private static final VatRate VAT_23 = new VatRate(new BigDecimal("0.23"));
    private static final Category LAPTOPS = new Category(5L, "Laptopy");
    private static final Category DESKTOPS = new Category(6L, "Komputery stacjonarne");

    @Mock
    private ProductRepositoryPort productRepository;

    @Mock
    private CategoryRepositoryPort categoryRepository;

    private ProductServiceImpl productService;

    @BeforeEach
    void setUp() {
        productService = new ProductServiceImpl(productRepository, categoryRepository, new ProductDtoMapper());
    }

    @Test
    void givenExistingCategory_whenCreateProduct_thenSavesDraftWithNetCalculatedFromGross() {
        // given
        CreateProductRequest request = new CreateProductRequest(
                "Laptop Pro 14", new BigDecimal("4999.99"), new BigDecimal("0.23"), false, 5L);
        when(categoryRepository.findById(5L)).thenReturn(Optional.of(LAPTOPS));
        when(productRepository.save(any())).thenAnswer(inv -> savedWithId(inv.getArgument(0), 1L));

        // when
        ProductResponse response = productService.createProduct(request);

        // then
        assertThat(response.id()).isEqualTo(1L);
        assertThat(response.status()).isEqualTo("DRAFT");
        assertThat(response.configurable()).isFalse();
        assertThat(response.basePrice().gross()).isEqualByComparingTo("4999.99");
        assertThat(response.basePrice().net()).isEqualByComparingTo("4065.03");
        assertThat(response.category().name()).isEqualTo("Laptopy");
    }

    @Test
    void givenMissingCategory_whenCreateProduct_thenThrowsCategoryNotFoundAndDoesNotSave() {
        // given
        CreateProductRequest request = new CreateProductRequest(
                "Laptop Pro 14", new BigDecimal("4999.99"), new BigDecimal("0.23"), false, 99L);
        when(categoryRepository.findById(99L)).thenReturn(Optional.empty());

        // when
        Throwable thrown = catchThrowable(() -> productService.createProduct(request));

        // then
        assertThat(thrown).isInstanceOf(CategoryNotFoundException.class);
        verify(productRepository, never()).save(any());
    }

    @Test
    void givenExistingProduct_whenGetProduct_thenReturnsProduct() {
        // given
        when(productRepository.findById(new ProductId(1L))).thenReturn(Optional.of(draftLaptop()));

        // when
        ProductResponse response = productService.getProduct(1L);

        // then
        assertThat(response.id()).isEqualTo(1L);
        assertThat(response.name()).isEqualTo("Laptop Pro 14");
        assertThat(response.basePrice().gross()).isEqualByComparingTo("4999.99");
    }

    @Test
    void givenMissingProduct_whenGetProduct_thenThrowsProductNotFound() {
        // given
        when(productRepository.findById(new ProductId(42L))).thenReturn(Optional.empty());

        // when
        Throwable thrown = catchThrowable(() -> productService.getProduct(42L));

        // then
        assertThat(thrown).isInstanceOf(ProductNotFoundException.class);
    }

    @Test
    void givenOneProductInRepository_whenGetProducts_thenReturnsSinglePage() {
        // given
        when(productRepository.findAll(0, 20))
                .thenReturn(new PageResult<>(List.of(draftLaptop()), 0, 20, 1));

        // when
        PageResponse<ProductSummaryResponse> page = productService.getProducts(PageQuery.firstPage());

        // then
        assertThat(page.content()).hasSize(1);
        assertThat(page.content().get(0).name()).isEqualTo("Laptop Pro 14");
        assertThat(page.totalElements()).isEqualTo(1);
        assertThat(page.totalPages()).isEqualTo(1);
    }

    @Test
    void givenExistingProduct_whenUpdateProduct_thenChangesNamePriceAndCategory() {
        // given
        UpdateProductRequest request = new UpdateProductRequest(
                "Laptop Pro 16", new BigDecimal("5999.99"), 6L);
        when(productRepository.findById(new ProductId(1L))).thenReturn(Optional.of(draftLaptop()));
        when(categoryRepository.findById(6L)).thenReturn(Optional.of(DESKTOPS));
        when(productRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        // when
        ProductResponse response = productService.updateProduct(1L, request);

        // then
        assertThat(response.name()).isEqualTo("Laptop Pro 16");
        assertThat(response.basePrice().gross()).isEqualByComparingTo("5999.99");
        assertThat(response.basePrice().vatRate()).isEqualByComparingTo("0.23");
        assertThat(response.category().id()).isEqualTo(6L);
    }

    @Test
    void givenMissingProduct_whenUpdateProduct_thenThrowsProductNotFoundAndDoesNotSave() {
        // given
        UpdateProductRequest request = new UpdateProductRequest(
                "Laptop Pro 16", new BigDecimal("5999.99"), 6L);
        when(productRepository.findById(new ProductId(42L))).thenReturn(Optional.empty());

        // when
        Throwable thrown = catchThrowable(() -> productService.updateProduct(42L, request));

        // then
        assertThat(thrown).isInstanceOf(ProductNotFoundException.class);
        verify(productRepository, never()).save(any());
    }

    @Test
    void givenExistingProduct_whenArchiveProduct_thenSavesWithArchivedStatus() {
        // given
        when(productRepository.findById(new ProductId(1L))).thenReturn(Optional.of(draftLaptop()));
        ArgumentCaptor<Product> savedProduct = ArgumentCaptor.forClass(Product.class);

        // when
        productService.archiveProduct(1L);

        // then
        verify(productRepository).save(savedProduct.capture());
        assertThat(savedProduct.getValue().getStatus()).isEqualTo(ProductStatus.ARCHIVED);
    }

    private static Product draftLaptop() {
        return Product.restore(
                new ProductId(1L),
                new ProductName("Laptop Pro 14"),
                ProductStatus.DRAFT,
                ProductPrice.fromGross(new Price(new BigDecimal("4999.99")), VAT_23),
                false,
                LAPTOPS,
                List.of(), List.of(), List.of());
    }

    private static Product savedWithId(Product product, long id) {
        return Product.restore(
                new ProductId(id),
                product.getName(),
                product.getStatus(),
                product.getBasePrice(),
                product.isConfigurable(),
                product.getCategory(),
                product.getOptions(),
                product.getVariants(),
                product.getAttributes());
    }
}