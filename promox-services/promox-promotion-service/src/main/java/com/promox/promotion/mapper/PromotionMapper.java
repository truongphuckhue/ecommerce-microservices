package com.promox.promotion.mapper;

import com.promox.promotion.dto.PromotionRequest;
import com.promox.promotion.dto.PromotionResponse;
import com.promox.promotion.dto.PromotionRuleRequest;
import com.promox.promotion.dto.PromotionRuleResponse;
import com.promox.promotion.entity.Promotion;
import com.promox.promotion.entity.PromotionRule;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class PromotionMapper {

    public Promotion toEntity(PromotionRequest request) {
        if (request == null) {
            return null;
        }

        return Promotion.builder()
                .campaignId(request.getCampaignId())
                .name(request.getName())
                .code(request.getCode().toUpperCase())
                .type(request.getType())
                .discountValue(request.getDiscountValue())
                .discountType(request.getDiscountType())
                .minOrderValue(request.getMinOrderValue())
                .maxDiscountAmount(request.getMaxDiscountAmount())
                .applicableProducts(request.getApplicableProducts())
                .applicableCategories(request.getApplicableCategories())
                .excludedProducts(request.getExcludedProducts())
                .usageLimit(request.getUsageLimit())
                .usageCount(0)
                .perUserLimit(request.getPerUserLimit())
                .stackable(request.getStackable() != null ? request.getStackable() : false)
                .priority(request.getPriority() != null ? request.getPriority() : 0)
                .validFrom(request.getValidFrom())
                .validTo(request.getValidTo())
                .status(Promotion.PromotionStatus.DRAFT)
                .metadata(request.getMetadata())
                .build();
    }

    public void updateEntityFromRequest(Promotion promotion, PromotionRequest request) {
        if (request.getName() != null) {
            promotion.setName(request.getName());
        }
        if (request.getCode() != null) {
            promotion.setCode(request.getCode().toUpperCase());
        }
        if (request.getType() != null) {
            promotion.setType(request.getType());
        }
        if (request.getDiscountValue() != null) {
            promotion.setDiscountValue(request.getDiscountValue());
        }
        if (request.getDiscountType() != null) {
            promotion.setDiscountType(request.getDiscountType());
        }
        if (request.getMinOrderValue() != null) {
            promotion.setMinOrderValue(request.getMinOrderValue());
        }
        if (request.getMaxDiscountAmount() != null) {
            promotion.setMaxDiscountAmount(request.getMaxDiscountAmount());
        }
        if (request.getApplicableProducts() != null) {
            promotion.setApplicableProducts(request.getApplicableProducts());
        }
        if (request.getApplicableCategories() != null) {
            promotion.setApplicableCategories(request.getApplicableCategories());
        }
        if (request.getExcludedProducts() != null) {
            promotion.setExcludedProducts(request.getExcludedProducts());
        }
        if (request.getUsageLimit() != null) {
            promotion.setUsageLimit(request.getUsageLimit());
        }
        if (request.getPerUserLimit() != null) {
            promotion.setPerUserLimit(request.getPerUserLimit());
        }
        if (request.getStackable() != null) {
            promotion.setStackable(request.getStackable());
        }
        if (request.getPriority() != null) {
            promotion.setPriority(request.getPriority());
        }
        if (request.getValidFrom() != null) {
            promotion.setValidFrom(request.getValidFrom());
        }
        if (request.getValidTo() != null) {
            promotion.setValidTo(request.getValidTo());
        }
        if (request.getMetadata() != null) {
            promotion.setMetadata(request.getMetadata());
        }
    }

    public PromotionResponse toResponse(Promotion promotion) {
        return toResponse(promotion, null);
    }

    public PromotionResponse toResponse(Promotion promotion, List<PromotionRule> rules) {
        if (promotion == null) {
            return null;
        }

        return PromotionResponse.builder()
                .id(promotion.getId())
                .campaignId(promotion.getCampaignId())
                .name(promotion.getName())
                .code(promotion.getCode())
                .type(promotion.getType())
                .discountValue(promotion.getDiscountValue())
                .discountType(promotion.getDiscountType())
                .minOrderValue(promotion.getMinOrderValue())
                .maxDiscountAmount(promotion.getMaxDiscountAmount())
                .applicableProducts(promotion.getApplicableProducts())
                .applicableCategories(promotion.getApplicableCategories())
                .excludedProducts(promotion.getExcludedProducts())
                .usageLimit(promotion.getUsageLimit())
                .usageCount(promotion.getUsageCount())
                .remainingUsage(promotion.getRemainingUsage())
                .usagePercentage(promotion.getUsagePercentage())
                .perUserLimit(promotion.getPerUserLimit())
                .stackable(promotion.getStackable())
                .priority(promotion.getPriority())
                .validFrom(promotion.getValidFrom())
                .validTo(promotion.getValidTo())
                .status(promotion.getStatus())
                .metadata(promotion.getMetadata())
                .createdAt(promotion.getCreatedAt())
                .updatedAt(promotion.getUpdatedAt())
                .isActive(promotion.isActive())
                .isExpired(promotion.isExpired())
                .isExhausted(promotion.isExhausted())
                .canBeUsed(promotion.canBeUsed())
                .rules(rules != null ? rules.stream()
                        .map(this::toRuleResponse)
                        .collect(Collectors.toList()) : null)
                .build();
    }

    public PromotionRule toRuleEntity(PromotionRuleRequest request, Long promotionId) {
        if (request == null) {
            return null;
        }

        return PromotionRule.builder()
                .promotionId(promotionId)
                .ruleType(request.getRuleType())
                .ruleConfig(request.getRuleConfig())
                .priority(request.getPriority() != null ? request.getPriority() : 0)
                .isActive(request.getIsActive() != null ? request.getIsActive() : true)
                .build();
    }

    public PromotionRuleResponse toRuleResponse(PromotionRule rule) {
        if (rule == null) {
            return null;
        }

        return PromotionRuleResponse.builder()
                .id(rule.getId())
                .promotionId(rule.getPromotionId())
                .ruleType(rule.getRuleType())
                .ruleConfig(rule.getRuleConfig())
                .priority(rule.getPriority())
                .isActive(rule.getIsActive())
                .createdAt(rule.getCreatedAt())
                .build();
    }
}
