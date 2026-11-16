package com.ecommerce.product.config;

import com.ecommerce.api.service.ProductService;
import com.ecommerce.model.mapper.ProductDtoMapper;
import com.ecommerce.model.ports.CategoryRepositoryPort;
import com.ecommerce.model.ports.ProductRepositoryPort;
import com.ecommerce.model.service.ProductServiceImpl;
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
