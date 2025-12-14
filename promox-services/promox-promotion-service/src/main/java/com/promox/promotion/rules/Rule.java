package com.promox.promotion.rules;

import com.promox.promotion.dto.CalculateDiscountRequest;
import com.promox.promotion.entity.PromotionRule;

/**
 * Interface for all promotion rules
 */
public interface Rule {

    /**
     * Check if the rule passes for the given request
     *
     * @param rule    The promotion rule configuration
     * @param request The discount calculation request
     * @return true if rule passes, false otherwise
     */
    boolean evaluate(PromotionRule rule, CalculateDiscountRequest request);

    /**
     * Get the rule type this implementation handles
     */
    PromotionRule.RuleType getRuleType();
}
