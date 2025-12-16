package com.promox.coupon.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ValidateCouponResponse {

    private Boolean valid;
    private String message;
    private String couponCode;
    private BigDecimal discountAmount;
    private BigDecimal finalAmount;
    private String discountDescription;
}
