package com.promox.promotion.dto;

import com.promox.promotion.entity.PromotionRule;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PromotionRuleRequest {

    @NotNull(message = "Rule type is required")
    private PromotionRule.RuleType ruleType;

    @NotBlank(message = "Rule configuration is required")
    private String ruleConfig;

    @Min(value = 0, message = "Priority cannot be negative")
    @Max(value = 100, message = "Priority cannot exceed 100")
    private Integer priority;

    private Boolean isActive;
}
