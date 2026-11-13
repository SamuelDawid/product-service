package com.ecommerce.model;

import com.ecommerce.model.exception.BusinessRuleException;
import lombok.Getter;
import lombok.ToString;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@ToString
@Getter
public class ProductOption {

    private final Long id;
    private String name;
    private boolean required;
    private final List<ProductOptionValue> values = new ArrayList<>();

    public ProductOption(Long id, String name, boolean required, List<ProductOptionValue> values) {
        this.id = id;
        this.name = Validate.notBlank(name, "Option name");
        this.required = required;
        if (values == null || values.isEmpty()) {
            throw new BusinessRuleException("Option '" + this.name + "' must have at least one value");
        }
        values.forEach(this::addValue);
    }

    public Optional<ProductOptionValue> getDefaultValue() {
        return values.stream().filter(ProductOptionValue::isDefault).findFirst();
    }

    public void addValue(ProductOptionValue value) {
        Validate.notNull(value, "Option value");
        if (value.isDefault() && getDefaultValue().isPresent()) {
            throw new BusinessRuleException("Option '" + name + "' already has a default value");
        }
        boolean nameTaken = values.stream().anyMatch(v -> v.getName().equals(value.getName()));
        if (nameTaken) {
            throw new BusinessRuleException(
                    "Option '" + name + "' already has value '" + value.getName() + "'");
        }
        values.add(value);
    }

    void removeValue(ProductOptionValue value) {
        if (values.size() == 1 && values.contains(value)) {
            throw new BusinessRuleException("Option '" + name + "' must have at least one value");
        }
        values.remove(value);
    }

    public boolean hasValue(ProductOptionValue value) {
        return values.contains(value);
    }

    public void rename(String newName) {
        this.name = Validate.notBlank(newName, "Option name");
    }

    public void changeRequired(boolean required) {
        this.required = required;
    }
}
