package com.ecommerce.model;

import com.ecommerce.model.exception.BusinessRuleException;
import lombok.Getter;
import lombok.ToString;

import java.util.ArrayList;
import java.util.List;

@Getter
@ToString
public class Category {

    private final Long id;
    private String name;
    private Category parent;
    private final List<Category> children = new ArrayList<>();

    public Category(Long id, String name) {
        this.id = id;
        this.name = Validate.notBlank(name, "Category name");
    }

    public void addChild(Category child) {
        Validate.notNull(child, "Child category");
        if (child == this || isDescendantOf(child)) {
            throw new BusinessRuleException("Category tree can not contain cycles");
        }
        if (child.parent != null) {
            child.parent.children.remove(child);
        }
        children.add(child);
        child.parent = this;
    }

    public void rename(String newName) {
        this.name = Validate.notBlank(newName, "Category name");
    }

    public boolean isRoot() {
        return parent == null;
    }

    private boolean isDescendantOf(Category candidate) {
        for (Category c = parent; c != null; c = c.parent) {
            if (c == candidate) return true;
        }
        return false;
    }
}
