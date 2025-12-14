package com.promox.promotion.dto;

import com.promox.promotion.entity.PromotionRule;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PromotionRuleResponse {

    private Long id;
    private Long promotionId;
    private PromotionRule.RuleType ruleType;
    private String ruleConfig;
    private Integer priority;
    private Boolean isActive;
    private LocalDateTime createdAt;
}
