package com.ecommerce.model;

import java.math.BigDecimal;
import java.util.Objects;

public class ValidateProduct {
    private ValidateProduct(){};
    public static String validateName(String name){
        if(Objects.isNull(name) || name.isBlank()){
            throw new IllegalArgumentException("name can not be null or blank");
        }
        return name;
    }
    public static long validateId(Long id){
        if(Objects.isNull(id) || id <= 0){
            throw new IllegalArgumentException("id can not be null or negative");
        }
        return id;
    }
    public static BigDecimal validatePrice(BigDecimal price){
        if(Objects.isNull(price) || price.signum() < 0){
            throw new IllegalArgumentException("price can not be null or negative");
        }
        return price;
    }
}
