package com.ecommerce.product.adapter.in.web;

import com.ecommerce.api.dto.page.PageQuery;
import com.ecommerce.api.dto.page.PageResponse;
import com.ecommerce.api.dto.request.CreateProductRequest;
import com.ecommerce.api.dto.response.CategoryResponse;
import com.ecommerce.api.dto.response.PriceResponse;
import com.ecommerce.api.dto.response.ProductResponse;
import com.ecommerce.api.dto.response.ProductSummaryResponse;
import com.ecommerce.api.service.ProductService;
import com.ecommerce.model.exception.BusinessRuleException;
import com.ecommerce.model.exception.InvalidValueException;
import com.ecommerce.model.exception.ProductNotFoundException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.List;

import static org.hamcrest.Matchers.endsWith;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ProductController.class)
@AutoConfigureMockMvc(addFilters = false)
class ProductControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ProductService productService;

    @Test
    void givenValidRequest_whenCreateProduct_thenReturns201WithLocation() throws Exception {
        // given
        when(productService.createProduct(any(CreateProductRequest.class))).thenReturn(laptopResponse());
        String body = """
                {
                  "name": "Laptop Pro 14",
                  "grossPrice": 4999.99,
                  "vatRate": 0.23,
                  "configurable": false,
                  "categoryId": 5
                }
                """;

        // when / then
        mockMvc.perform(post("/api/products")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", endsWith("/api/products/1")))
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.status").value("DRAFT"))
                .andExpect(jsonPath("$.basePrice.gross").value(4999.99));
    }

    @Test
    void givenBlankName_whenCreateProduct_thenReturns400WithFieldError() throws Exception {
        // given
        String body = """
                {
                  "name": "",
                  "grossPrice": 4999.99,
                  "vatRate": 0.23,
                  "configurable": false,
                  "categoryId": 5
                }
                """;

        // when / then
        mockMvc.perform(post("/api/products")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.title").value("Validation failed"))
                .andExpect(jsonPath("$.errors.name").exists());

        verify(productService, never()).createProduct(any());
    }


    @Test
    void givenExistingProduct_whenGetProduct_thenReturns200() throws Exception {
        // given
        when(productService.getProduct(1L)).thenReturn(laptopResponse());

        // when / then
        mockMvc.perform(get("/api/products/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Laptop Pro 14"))
                .andExpect(jsonPath("$.category.name").value("Laptopy"));
    }

    @Test
    void givenMissingProduct_whenGetProduct_thenReturns404ProblemDetail() throws Exception {
        // given
        when(productService.getProduct(42L)).thenThrow(new ProductNotFoundException(42L));

        // when / then
        mockMvc.perform(get("/api/products/42"))
                .andExpect(status().isNotFound())
                .andExpect(header().string("Content-Type", "application/problem+json"))
                .andExpect(jsonPath("$.title").value("Resource not found"));
    }


    @Test
    void givenProducts_whenGetProductsWithDefaults_thenReturnsFirstPage() throws Exception {
        // given
        ProductSummaryResponse summary = new ProductSummaryResponse(
                1L, "Laptop Pro 14", "DRAFT", laptopPrice());
        when(productService.getProducts(PageQuery.of(0, 20)))
                .thenReturn(new PageResponse<>(List.of(summary), 0, 20, 1, 1));

        // when / then
        mockMvc.perform(get("/api/products"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].name").value("Laptop Pro 14"))
                .andExpect(jsonPath("$.totalElements").value(1));
    }

    @Test
    void givenPageSizeAboveLimit_whenGetProducts_thenReturns400() throws Exception {
        // when / then
        mockMvc.perform(get("/api/products").param("size", "1000"))
                .andExpect(status().isBadRequest());

        verify(productService, never()).getProducts(any());
    }


    @Test
    void givenExistingProduct_whenArchiveProduct_thenReturns204() throws Exception {
        // when / then
        mockMvc.perform(post("/api/products/1/archive"))
                .andExpect(status().isNoContent());

        verify(productService).archiveProduct(1L);
    }

    @Test
    void givenInvalidValueInService_whenCreateProduct_thenReturns400() throws Exception {
        // given
        when(productService.createProduct(any(CreateProductRequest.class)))
                .thenThrow(new InvalidValueException("Price can not be null or negative"));
        String body = """
                {
                  "name": "Laptop Pro 14",
                  "grossPrice": 4999.99,
                  "vatRate": 0.23,
                  "configurable": false,
                  "categoryId": 5
                }
                """;

        // when / then
        mockMvc.perform(post("/api/products")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.title").value("Invalid value"));
    }

    @Test
    void givenBusinessRuleViolation_whenArchiveProduct_thenReturns409() throws Exception {
        // given
        doThrow(new BusinessRuleException("Archived product can not be activated"))
                .when(productService).archiveProduct(1L);

        // when / then
        mockMvc.perform(post("/api/products/1/archive"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.title").value("Business rule violated"));
    }

    private static ProductResponse laptopResponse() {
        return new ProductResponse(1L, "Laptop Pro 14", "DRAFT", false,
                laptopPrice(), new CategoryResponse(5L, "Laptopy"));
    }

    private static PriceResponse laptopPrice() {
        return new PriceResponse(
                new BigDecimal("4065.03"), new BigDecimal("4999.99"), new BigDecimal("0.23"));
    }
}