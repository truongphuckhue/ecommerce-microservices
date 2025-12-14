package com.promox.promotion.dto;

import com.promox.promotion.entity.Promotion;
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
public class PromotionRequest {

    @NotNull(message = "Campaign ID is required")
    private Long campaignId;

    @NotBlank(message = "Promotion name is required")
    @Size(min = 3, max = 255, message = "Name must be between 3 and 255 characters")
    private String name;

    @NotBlank(message = "Promotion code is required")
    @Pattern(regexp = "^[A-Z0-9-]+$", message = "Code must contain only uppercase letters, numbers, and hyphens")
    @Size(min = 3, max = 50, message = "Code must be between 3 and 50 characters")
    private String code;

    @NotNull(message = "Promotion type is required")
    private Promotion.PromotionType type;

    @DecimalMin(value = "0.0", inclusive = false, message = "Discount value must be greater than 0")
    private BigDecimal discountValue;

    private Promotion.DiscountType discountType;

    @DecimalMin(value = "0.0", message = "Minimum order value cannot be negative")
    private BigDecimal minOrderValue;

    @DecimalMin(value = "0.0", message = "Max discount amount cannot be negative")
    private BigDecimal maxDiscountAmount;

    private String applicableProducts;

    private String applicableCategories;

    private String excludedProducts;

    @Min(value = 1, message = "Usage limit must be at least 1")
    private Integer usageLimit;

    @Min(value = 1, message = "Per user limit must be at least 1")
    private Integer perUserLimit;

    private Boolean stackable;

    @Min(value = 0, message = "Priority cannot be negative")
    @Max(value = 100, message = "Priority cannot exceed 100")
    private Integer priority;

    @NotNull(message = "Valid from date is required")
    private LocalDateTime validFrom;

    @NotNull(message = "Valid to date is required")
    private LocalDateTime validTo;

    private String metadata;

    @AssertTrue(message = "Valid to must be after valid from")
    public boolean isValidToAfterValidFrom() {
        if (validFrom == null || validTo == null) {
            return true;
        }
        return validTo.isAfter(validFrom);
    }
}
