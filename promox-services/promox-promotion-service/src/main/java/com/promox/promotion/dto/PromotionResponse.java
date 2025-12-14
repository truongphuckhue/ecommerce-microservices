package com.promox.promotion.dto;

import com.promox.promotion.entity.Promotion;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PromotionResponse {

    private Long id;
    private Long campaignId;
    private String campaignName;
    private String name;
    private String code;
    private Promotion.PromotionType type;
    private BigDecimal discountValue;
    private Promotion.DiscountType discountType;
    private BigDecimal minOrderValue;
    private BigDecimal maxDiscountAmount;
    private String applicableProducts;
    private String applicableCategories;
    private String excludedProducts;
    private Integer usageLimit;
    private Integer usageCount;
    private Integer remainingUsage;
    private BigDecimal usagePercentage;
    private Integer perUserLimit;
    private Boolean stackable;
    private Integer priority;
    private LocalDateTime validFrom;
    private LocalDateTime validTo;
    private Promotion.PromotionStatus status;
    private String metadata;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // Helper flags
    private Boolean isActive;
    private Boolean isExpired;
    private Boolean isExhausted;
    private Boolean canBeUsed;

    // Associated rules
    private List<PromotionRuleResponse> rules;
}
