package com.ecommerce.model.valueObjects;

import com.ecommerce.model.exception.InvalidValueException;

public record Stock(int quantity) {

    public static final Stock EMPTY = new Stock(0);

    public Stock {
        if (quantity < 0) {
            throw new InvalidValueException("Stock can not be negative, got: " + quantity);
        }
    }

    public boolean isAvailable() {
        return quantity > 0;
    }

    public boolean canFulfill(int requested) {
        return requested > 0 && requested <= quantity;
    }

    public Stock increase(int amount) {
        requirePositive(amount);
        return new Stock(Math.addExact(quantity, amount));
    }

    public Stock decrease(int amount) {
        requirePositive(amount);
        if (amount > quantity) {
            throw new InvalidValueException(
                    "Insufficient stock: requested " + amount + ", available " + quantity);
        }
        return new Stock(quantity - amount);
    }

    private static void requirePositive(int amount) {
        if (amount <= 0) {
            throw new InvalidValueException("Amount must be positive, got: " + amount);
        }
    }
}
