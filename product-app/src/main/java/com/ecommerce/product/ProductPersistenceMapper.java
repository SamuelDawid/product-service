package com.ecommerce.product;

import com.ecommerce.model.*;
import com.ecommerce.model.valueObjects.*;
import com.ecommerce.product.entity.*;
import org.mapstruct.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Mapper(
        componentModel = "spring",
        collectionMappingStrategy = CollectionMappingStrategy.ADDER_PREFERRED,
        unmappedTargetPolicy = ReportingPolicy.ERROR
)
public abstract class ProductPersistenceMapper {
    @Mapping(target = "name", source = "name.value")
    @Mapping(target = "basePriceNet", source = "basePrice.net.value")
    @Mapping(target = "basePriceGross", source = "basePrice.gross.value")
    @Mapping(target = "vatRate", source = "basePrice.vatRate.value")
    @Mapping(target = "category", ignore = true)
    @Mapping(target = "variants", dependsOn = "options")
    public abstract ProductEntity toEntity(Product product, @Context MappingContext context);

    @Mapping(target = "product", ignore = true)
    protected abstract ProductOptionEntity toEntity(ProductOption option,
                                                    @Context MappingContext context);

    @Mapping(target = "priceDeltaNet", source = "priceDelta.net")
    @Mapping(target = "priceDeltaGross", source = "priceDelta.gross")
    @Mapping(target = "defaultValue", source = "default")
    @Mapping(target = "option", ignore = true)
    protected abstract ProductOptionValueEntity toEntity(ProductOptionValue value,
                                                         @Context MappingContext context);

    @Mapping(target = "sku", source = "sku.value")
    @Mapping(target = "priceNet", source = "price.net.value")
    @Mapping(target = "priceGross", source = "price.gross.value")
    @Mapping(target = "stock", source = "stock.quantity")
    @Mapping(target = "product", ignore = true)
    protected abstract ProductVariantEntity toEntity(ProductVariant variant,
                                                     @Context MappingContext context);

    @Mapping(target = "product", ignore = true)
    protected abstract ProductAttributeEntity toEntity(ProductAttribute attribute,
                                                       @Context MappingContext context);

    public Product toDomain(ProductEntity entity) {
        VatRate vatRate = new VatRate(entity.getVatRate());
        Map<Long, ProductOptionValue> valuesById = new HashMap<>();

        List<ProductOption> options = entity.getOptions().stream()
                .map(option -> toDomain(option, valuesById))
                .toList();
        List<ProductVariant> variants = entity.getVariants().stream()
                .map(variant -> toDomain(variant, vatRate, valuesById))
                .toList();
        List<ProductAttribute> attributes = entity.getAttributes().stream()
                .map(a -> new ProductAttribute(a.getId(), a.getName(), a.getValue()))
                .toList();

        return Product.restore(
                new ProductId(entity.getId()),
                new ProductName(entity.getName()),
                entity.getStatus(),
                new ProductPrice(
                        new Price(entity.getBasePriceNet()),
                        new Price(entity.getBasePriceGross()),
                        vatRate),
                entity.isConfigurable(),
                toDomain(entity.getCategory()),
                options,
                variants,
                attributes
        );
    }

    public Category toDomain(CategoryEntity entity) {
        Category category = new Category(entity.getId(), entity.getName());
        if (entity.getParent() != null) {
            toDomain(entity.getParent()).addChild(category);
        }
        return category;
    }

    private ProductOption toDomain(ProductOptionEntity entity,
                                   Map<Long, ProductOptionValue> valuesById) {
        List<ProductOptionValue> values = entity.getValues().stream()
                .map(v -> {
                    ProductOptionValue value = new ProductOptionValue(
                            v.getId(),
                            v.getName(),
                            new PriceDelta(v.getPriceDeltaNet(), v.getPriceDeltaGross()),
                            v.isDefaultValue());
                    valuesById.put(v.getId(), value);
                    return value;
                })
                .toList();
        return new ProductOption(entity.getId(), entity.getName(), entity.isRequired(), values);
    }

    private ProductVariant toDomain(ProductVariantEntity entity, VatRate vatRate,
                                    Map<Long, ProductOptionValue> valuesById) {
        List<ProductOptionValue> values = entity.getOptionValues().stream()
                .map(v -> valuesById.get(v.getId()))
                .toList();
        return new ProductVariant(
                entity.getId(),
                new Sku(entity.getSku()),
                new ProductPrice(
                        new Price(entity.getPriceNet()),
                        new Price(entity.getPriceGross()),
                        vatRate),
                new Stock(entity.getStock()),
                values
        );
    }
}
