package com.ecommerce.product;

import com.ecommerce.model.*;
import com.ecommerce.model.valueObjects.*;
import com.ecommerce.product.entity.*;
import org.mapstruct.CollectionMappingStrategy;
import org.mapstruct.Context;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Mapper(
        componentModel = "spring",
        // używa addOption/addVariant/addAttribute/addValue, które ustawiają powiązania zwrotne
        collectionMappingStrategy = CollectionMappingStrategy.ADDER_PREFERRED,
        // niezmapowane pole encji = błąd kompilacji, a nie cichy null w bazie
        unmappedTargetPolicy = ReportingPolicy.ERROR
)
public abstract class ProductPersistenceMapper {
    @Mapping(target = "name", source = "name.value")
    @Mapping(target = "basePriceNet", source = "basePrice.net.value")
    @Mapping(target = "basePriceGross", source = "basePrice.gross.value")
    @Mapping(target = "vatRate", source = "basePrice.vatRate.value")
    @Mapping(target = "category", ignore = true) // ustawia adapter, patrz niżej
    @Mapping(target = "variants", dependsOn = "options") // najpierw opcje, potem warianty
    public abstract ProductEntity toEntity(Product product, @Context MappingContext context);

    @Mapping(target = "product", ignore = true) // ustawia ProductEntity.addOption
    protected abstract ProductOptionEntity toEntity(ProductOption option,
                                                    @Context MappingContext context);

    @Mapping(target = "priceDeltaNet", source = "priceDelta.net")
    @Mapping(target = "priceDeltaGross", source = "priceDelta.gross")
    @Mapping(target = "defaultValue", source = "default")
    @Mapping(target = "option", ignore = true) // ustawia ProductOptionEntity.addValue
    protected abstract ProductOptionValueEntity toEntity(ProductOptionValue value,
                                                         @Context MappingContext context);

    @Mapping(target = "sku", source = "sku.value")
    @Mapping(target = "priceNet", source = "price.net.value")
    @Mapping(target = "priceGross", source = "price.gross.value")
    @Mapping(target = "stock", source = "stock.quantity")
    @Mapping(target = "product", ignore = true) // ustawia ProductEntity.addVariant
    protected abstract ProductVariantEntity toEntity(ProductVariant variant,
                                                     @Context MappingContext context);

    @Mapping(target = "product", ignore = true) // ustawia ProductEntity.addAttribute
    protected abstract ProductAttributeEntity toEntity(ProductAttribute attribute,
                                                       @Context MappingContext context);

    // ===================== encja -> model (ręcznie) =====================

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

    /** Mapuje kategorię razem z przodkami. Dzieci nie są ładowane. */
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
        // te same instancje wartości, które są pod opcjami
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
