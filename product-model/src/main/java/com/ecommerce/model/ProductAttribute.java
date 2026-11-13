package com.ecommerce.model;

import lombok.Getter;
import lombok.ToString;

import java.util.Objects;

@ToString
@Getter
public class ProductAttribute {
    private final Long id;
    private final String name;
    private String value;

    public ProductAttribute(Long id, String name, String value) {
        this.id = id;
        this.name = Validate.notBlank(name, "Attribute name");
        this.value = Validate.notBlank(value, "Attribute value");
    }

    public void changeValue(String newValue) {
        this.value = Validate.notBlank(newValue, "Attribute value");
    }
}
