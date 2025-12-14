package com.promox.coupon.dto;

import com.promox.coupon.entity.Coupon;
import jakarta.validation.constraints.*;
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
public class CouponRequest {

    @NotBlank(message = "Code is required")
    @Size(min = 4, max = 50, message = "Code must be between 4 and 50 characters")
    @Pattern(regexp = "^[A-Z0-9-]+$", message = "Code must contain only uppercase letters, numbers, and hyphens")
    private String code;

    private Long campaignId;

    @NotNull(message = "Coupon type is required")
    private Coupon.CouponType couponType;

    @NotNull(message = "Discount type is required")
    private Coupon.DiscountType discountType;

    @NotNull(message = "Discount value is required")
    @DecimalMin(value = "0.01", message = "Discount value must be greater than 0")
    private BigDecimal discountValue;

    @DecimalMin(value = "0.01", message = "Max discount must be greater than 0")
    private BigDecimal maxDiscountAmount;

    @DecimalMin(value = "0.01", message = "Min order value must be greater than 0")
    private BigDecimal minOrderValue;

    @NotNull(message = "Valid from is required")
    private LocalDateTime validFrom;

    @NotNull(message = "Valid to is required")
    private LocalDateTime validTo;

    @Min(value = 1, message = "Usage limit must be at least 1")
    private Integer usageLimit;

    @Min(value = 1, message = "Per user limit must be at least 1")
    private Integer perUserLimit;

    private Long assignedUserId;

    private String metadata;

    @AssertTrue(message = "Valid to must be after valid from")
    public boolean isValidToAfterValidFrom() {
        if (validFrom == null || validTo == null) {
            return true;
        }
        return validTo.isAfter(validFrom);
    }

    @AssertTrue(message = "Percentage discount must be between 1 and 100")
    public boolean isPercentageValueValid() {
        if (discountType == null || discountType != Coupon.DiscountType.PERCENTAGE) {
            return true;
        }
        if (discountValue == null) {
            return true;
        }
        return discountValue.compareTo(BigDecimal.ONE) >= 0 
                && discountValue.compareTo(BigDecimal.valueOf(100)) <= 0;
    }
}
