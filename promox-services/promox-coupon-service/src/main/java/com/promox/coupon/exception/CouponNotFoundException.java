package com.promox.coupon.exception;

public class CouponNotFoundException extends RuntimeException {
    public CouponNotFoundException(String message) {
        super(message);
    }

    public CouponNotFoundException(Long id) {
        super("Coupon not found with id: " + id);
    }
}
