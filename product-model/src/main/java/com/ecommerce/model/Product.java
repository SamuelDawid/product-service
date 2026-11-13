package com.ecommerce.model;

import com.ecommerce.model.enums.ProductStatus;
import com.ecommerce.model.exception.BusinessRuleException;
import com.ecommerce.model.valueObjects.*;
import lombok.Getter;
import lombok.ToString;

import java.math.BigDecimal;
import java.util.*;

@Getter
@ToString
public class Product {
        private final ProductId id; // null = jeszcze niezapisany
        private ProductName name;
        private ProductStatus status;
        private ProductPrice basePrice; // zawiera też stawkę VAT
        private final boolean configurable;
        private Category category;
        private final List<ProductOption> options = new ArrayList<>();
        private final List<ProductVariant> variants = new ArrayList<>();
        private final List<ProductAttribute> attributes = new ArrayList<>();

        private Product(ProductId id, ProductName name, ProductStatus status,
                        ProductPrice basePrice, boolean configurable, Category category) {
            this.id = id;
            this.name = Validate.notNull(name, "Product name");
            this.status = Validate.notNull(status, "Product status");
            this.basePrice = Validate.notNull(basePrice, "Base price");
            this.configurable = configurable;
            this.category = Validate.notNull(category, "Category");
        }


        public static Product standard(ProductName name, ProductPrice basePrice, Category category) {
            return new Product(null, name, ProductStatus.DRAFT, basePrice, false, category);
        }

        public static Product configurable(ProductName name, ProductPrice basePrice, Category category) {
            return new Product(null, name, ProductStatus.DRAFT, basePrice, true, category);
        }

        public static Product restore(ProductId id, ProductName name, ProductStatus status,
                                      ProductPrice basePrice, boolean configurable, Category category,
                                      List<ProductOption> options, List<ProductVariant> variants,
                                      List<ProductAttribute> attributes) {
            Product product = new Product(Validate.notNull(id, "Product id"),
                    name, status, basePrice, configurable, category);
            options.forEach(product::addOption);
            variants.forEach(product::addVariant);
            attributes.forEach(product::addAttribute);
            return product;
        }

        public ProductPrice calculatePrice(List<ProductOptionValue> selected) {
            List<ProductOptionValue> selection = selected == null ? List.of() : selected;
            if (configurable) {
                return priceForConfiguration(selection);
            }
            if (!variants.isEmpty()) {
                return findVariant(selection).getPrice();
            }
            return basePrice;
        }

        private ProductPrice priceForConfiguration(List<ProductOptionValue> selection) {
            requireOwnValues(selection);
            BigDecimal gross = basePrice.gross().value();
            for (ProductOption option : options) {
                Optional<ProductOptionValue> chosen = chosenFor(option, selection);
                if (chosen.isEmpty() && option.isRequired()) {
                    throw new BusinessRuleException("Required option not selected: " + option.getName());
                }
                if (chosen.isPresent()) {
                    gross = gross.add(chosen.get().getPriceDelta().gross());
                }
            }
            return ProductPrice.fromGross(new Price(gross), basePrice.vatRate());
        }

        private Optional<ProductOptionValue> chosenFor(ProductOption option,
                                                       List<ProductOptionValue> selection) {
            List<ProductOptionValue> chosen = selection.stream()
                    .filter(option::hasValue)
                    .distinct()
                    .toList();
            if (chosen.size() > 1) {
                throw new BusinessRuleException(
                        "More than one value selected for option: " + option.getName());
            }
            return chosen.isEmpty() ? option.getDefaultValue() : Optional.of(chosen.get(0));
        }

        private ProductVariant findVariant(List<ProductOptionValue> selection) {
            HashSet<ProductOptionValue> wanted = new HashSet<>(selection);
            return variants.stream()
                    .filter(v -> v.getOptionValues().equals(wanted))
                    .findFirst()
                    .orElseThrow(() -> new BusinessRuleException(
                            "No variant for the selected combination"));
        }

        public void changeVatRate(VatRate newRate) {
            Validate.notNull(newRate, "VAT rate");
            this.basePrice = basePrice.withVatRateKeepingGross(newRate);
            variants.forEach(v -> v.applyVatRate(newRate));
            options.forEach(o -> o.getValues().forEach(v -> v.applyVatRate(newRate)));
        }

        public void changeBaseGrossPrice(Price newGross) {
            this.basePrice = ProductPrice.fromGross(newGross, basePrice.vatRate());
        }

        public boolean isPurchasable() {
            return status == ProductStatus.ACTIVE;
        }

        public void activate() {
            if (status == ProductStatus.ARCHIVED) {
                throw new BusinessRuleException("Archived product can not be activated");
            }
            this.status = ProductStatus.ACTIVE;
        }

        public void archive() {
            this.status = ProductStatus.ARCHIVED;
        }

        public void addOption(ProductOption option) {
            Validate.notNull(option, "Option");
            boolean nameTaken = options.stream().anyMatch(o -> o.getName().equals(option.getName()));
            if (nameTaken) {
                throw new BusinessRuleException("Product already has option '" + option.getName() + "'");
            }
            options.add(option);
        }

        public void removeOption(ProductOption option) {
            boolean usedByVariant = variants.stream()
                    .anyMatch(v -> v.getOptionValues().stream().anyMatch(option::hasValue));
            if (usedByVariant) {
                throw new BusinessRuleException(
                        "Option '" + option.getName() + "' is used by a variant");
            }
            options.remove(option);
        }

        public void removeOptionValue(ProductOption option, ProductOptionValue value) {
            boolean usedByVariant = variants.stream().anyMatch(v -> v.getOptionValues().contains(value));
            if (usedByVariant) {
                throw new BusinessRuleException(
                        "Option value '" + value.getName() + "' is used by a variant");
            }
            option.removeValue(value);
        }

        public void addVariant(ProductVariant variant) {
            Validate.notNull(variant, "Variant");
            if (configurable) {
                throw new BusinessRuleException("Configurable product can not have variants");
            }
            if (!variant.getPrice().vatRate().equals(basePrice.vatRate())) {
                throw new BusinessRuleException("Variant VAT rate must match product VAT rate");
            }
            requireOwnValues(variant.getOptionValues());
            for (ProductOption option : options) {
                long valuesOfOption = variant.getOptionValues().stream().filter(option::hasValue).count();
                if (valuesOfOption > 1) {
                    throw new BusinessRuleException(
                            "Variant has more than one value of option: " + option.getName());
                }
            }
            boolean combinationTaken = variants.stream()
                    .anyMatch(v -> v.getOptionValues().equals(variant.getOptionValues()));
            if (combinationTaken) {
                throw new BusinessRuleException("Variant with this combination already exists");
            }
            boolean skuTaken = variants.stream().anyMatch(v -> v.getSku().equals(variant.getSku()));
            if (skuTaken) {
                throw new BusinessRuleException("Product already has variant " + variant.getSku().value());
            }
            variants.add(variant);
        }

        public void removeVariant(ProductVariant variant) {
            variants.remove(variant);
        }

        public void addAttribute(ProductAttribute attribute) {
            Validate.notNull(attribute, "Attribute");
            boolean nameTaken = attributes.stream()
                    .anyMatch(a -> a.getName().equals(attribute.getName()));
            if (nameTaken) {
                throw new BusinessRuleException(
                        "Product already has attribute '" + attribute.getName() + "'");
            }
            attributes.add(attribute);
        }

        public void removeAttribute(ProductAttribute attribute) {
            attributes.remove(attribute);
        }

        private void requireOwnValues(Collection<ProductOptionValue> values) {
            boolean allOwn = values.stream()
                    .allMatch(v -> options.stream().anyMatch(o -> o.hasValue(v)));
            if (!allOwn) {
                throw new BusinessRuleException("Option values do not belong to this product");
            }
        }

        public void rename(ProductName newName) {
            this.name = Validate.notNull(newName, "Product name");
        }

        public void changeCategory(Category newCategory) {
            this.category = Validate.notNull(newCategory, "Category");
        }
}
