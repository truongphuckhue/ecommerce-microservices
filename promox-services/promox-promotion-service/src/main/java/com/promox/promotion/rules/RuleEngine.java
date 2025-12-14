package com.promox.promotion.rules;

import com.promox.promotion.dto.CalculateDiscountRequest;
import com.promox.promotion.entity.PromotionRule;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Rule Engine - evaluates all rules for a promotion
 */
@Component
@Slf4j
public class RuleEngine {

    private final Map<PromotionRule.RuleType, Rule> rules = new HashMap<>();

    public RuleEngine(List<Rule> ruleList) {
        // Register all rule implementations
        ruleList.forEach(rule -> rules.put(rule.getRuleType(), rule));
        log.info("Registered {} rules: {}", rules.size(), rules.keySet());
    }

    /**
     * Evaluate all rules for a promotion
     * 
     * @param promotionRules List of rules to evaluate
     * @param request        Discount calculation request
     * @return true if ALL rules pass, false otherwise
     */
    public boolean evaluateAll(List<PromotionRule> promotionRules, CalculateDiscountRequest request) {
        if (promotionRules == null || promotionRules.isEmpty()) {
            log.debug("No rules to evaluate, allowing promotion");
            return true;
        }

        for (PromotionRule promotionRule : promotionRules) {
            if (!promotionRule.getIsActive()) {
                log.debug("Skipping inactive rule: {}", promotionRule.getRuleType());
                continue;
            }

            Rule rule = rules.get(promotionRule.getRuleType());
            if (rule == null) {
                log.warn("No implementation found for rule type: {}", promotionRule.getRuleType());
                return false;
            }

            boolean passes = rule.evaluate(promotionRule, request);
            log.debug("Rule {} evaluation: {}", promotionRule.getRuleType(), passes);

            if (!passes) {
                log.info("Rule {} failed, promotion not applicable", promotionRule.getRuleType());
                return false;
            }
        }

        log.info("All {} rules passed", promotionRules.size());
        return true;
    }

    /**
     * Get specific rule implementation
     */
    public Rule getRule(PromotionRule.RuleType ruleType) {
        return rules.get(ruleType);
    }
}
