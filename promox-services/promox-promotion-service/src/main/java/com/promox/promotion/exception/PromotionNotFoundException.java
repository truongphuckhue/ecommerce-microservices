package com.promox.promotion.exception;

public class PromotionNotFoundException extends RuntimeException {
    public PromotionNotFoundException(String message) {
        super(message);
    }

    public PromotionNotFoundException(Long id) {
        super("Promotion not found with id: " + id);
    }

    public PromotionNotFoundException(String field, String value) {
        super(String.format("Promotion not found with %s: %s", field, value));
    }
}
