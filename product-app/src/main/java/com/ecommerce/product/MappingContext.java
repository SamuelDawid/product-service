package com.ecommerce.product;

import org.mapstruct.BeforeMapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.TargetType;

import java.util.IdentityHashMap;
import java.util.Map;

public class MappingContext {
    private final Map<Object, Object> mapped = new IdentityHashMap<>();

    @BeforeMapping
    @SuppressWarnings("unchecked")
    public <T> T getMappedInstance(Object source, @TargetType Class<T> targetType) {
        return (T) mapped.get(source);
    }

    @BeforeMapping
    public void storeMappedInstance(Object source, @MappingTarget Object target) {
        mapped.put(source, target);
    }
}
