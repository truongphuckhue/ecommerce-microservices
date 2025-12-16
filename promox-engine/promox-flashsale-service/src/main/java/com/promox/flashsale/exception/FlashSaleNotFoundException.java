package com.promox.flashsale.exception;

public class FlashSaleNotFoundException extends RuntimeException {
    public FlashSaleNotFoundException(String message) {
        super(message);
    }

    public FlashSaleNotFoundException(Long id) {
        super("Flash sale not found with id: " + id);
    }
}
