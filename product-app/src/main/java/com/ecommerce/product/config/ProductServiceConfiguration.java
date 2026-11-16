package com.ecommerce.product.config;

import com.ecommerce.api.service.ProductService;
import com.ecommerce.domain.mapper.ProductDtoMapper;
import com.ecommerce.domain.ports.CategoryRepositoryPort;
import com.ecommerce.domain.ports.ProductRepositoryPort;
import com.ecommerce.domain.service.ProductServiceImpl;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ProductServiceConfiguration {

    @Bean
    public ProductService productService(ProductRepositoryPort productRepository,
                                         CategoryRepositoryPort categoryRepository) {
        ProductService domainService = new ProductServiceImpl(
                productRepository, categoryRepository, new ProductDtoMapper());
        return new TransactionalProductService(domainService);
    }
}
