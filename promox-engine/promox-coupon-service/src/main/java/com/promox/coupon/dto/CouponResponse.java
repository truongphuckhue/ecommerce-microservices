package com.promox.coupon.dto;

import com.promox.coupon.entity.Coupon;
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
public class CouponResponse {

    private Long id;
    private String code;
    private Long campaignId;
    private Coupon.CouponType couponType;
    private Coupon.DiscountType discountType;
    private BigDecimal discountValue;
    private BigDecimal maxDiscountAmount;
    private BigDecimal minOrderValue;
    private LocalDateTime validFrom;
    private LocalDateTime validTo;
    private Coupon.CouponStatus status;
    private Integer usageLimit;
    private Integer usageCount;
    private Integer remainingUsage;
    private BigDecimal usagePercentage;
    private Integer perUserLimit;
    private Long assignedUserId;
    private String metadata;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private String createdBy;
    private String batchId;

    // Helper flags
    private Boolean isValid;
    private Boolean isExpired;
    private Boolean isExhausted;
    private Boolean canBeUsed;
}
