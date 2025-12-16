package com.promox.coupon.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RedeemCouponResponse {

    private Boolean success;
    private String message;
    private Long redemptionId;
    private String couponCode;
    private Long userId;
    private String orderId;
    private BigDecimal orderAmount;
    private BigDecimal discountAmount;
    private BigDecimal finalAmount;
    private LocalDateTime redeemedAt;
}
