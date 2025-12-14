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
public class BulkCouponRequest {

    @NotBlank(message = "Prefix is required")
    @Size(min = 2, max = 20, message = "Prefix must be between 2 and 20 characters")
    @Pattern(regexp = "^[A-Z0-9]+$", message = "Prefix must contain only uppercase letters and numbers")
    private String prefix;

    @NotNull(message = "Quantity is required")
    @Min(value = 1, message = "Quantity must be at least 1")
    @Max(value = 10000, message = "Quantity cannot exceed 10000")
    private Integer quantity;

    @NotNull(message = "Code length is required")
    @Min(value = 4, message = "Code length must be at least 4")
    @Max(value = 20, message = "Code length cannot exceed 20")
    private Integer codeLength;

    private Long campaignId;

    @NotNull(message = "Coupon type is required")
    private Coupon.CouponType couponType;

    @NotNull(message = "Discount type is required")
    private Coupon.DiscountType discountType;

    @NotNull(message = "Discount value is required")
    @DecimalMin(value = "0.01", message = "Discount value must be greater than 0")
    private BigDecimal discountValue;

    private BigDecimal maxDiscountAmount;

    private BigDecimal minOrderValue;

    @NotNull(message = "Valid from is required")
    private LocalDateTime validFrom;

    @NotNull(message = "Valid to is required")
    private LocalDateTime validTo;

    @Min(value = 1, message = "Usage limit must be at least 1")
    private Integer usageLimit;

    @Min(value = 1, message = "Per user limit must be at least 1")
    private Integer perUserLimit;

    private String metadata;

    private String createdBy;
}
