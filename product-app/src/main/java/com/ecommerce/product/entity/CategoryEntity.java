package com.ecommerce.product.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "categories")
@Getter
@Setter
@NoArgsConstructor
public class CategoryEntity extends BaseEntity {

    @Column(nullable = false)
    private String name;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_id")
    private CategoryEntity parent;

    @Setter(AccessLevel.NONE)
    @OneToMany(mappedBy = "parent")
    private List<CategoryEntity> children = new ArrayList<>();

    public void addChild(CategoryEntity child) {
        children.add(child);
        child.setParent(this);
    }
}