package com.promox.promotion.rules;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.promox.promotion.dto.CalculateDiscountRequest;
import com.promox.promotion.entity.PromotionRule;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

/**
 * Rule: Order total must meet minimum value
 * Config example: {"minValue": 100.00, "currency": "USD"}
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class MinOrderValueRule implements Rule {

    private final ObjectMapper objectMapper;

    @Override
    public boolean evaluate(PromotionRule rule, CalculateDiscountRequest request) {
        try {
            JsonNode config = objectMapper.readTree(rule.getRuleConfig());
            BigDecimal minValue = new BigDecimal(config.get("minValue").asText());

            boolean passes = request.getOrderTotal().compareTo(minValue) >= 0;

            log.debug("MinOrderValueRule: orderTotal={}, minValue={}, passes={}",
                    request.getOrderTotal(), minValue, passes);

            return passes;

        } catch (Exception e) {
            log.error("Error evaluating MinOrderValueRule", e);
            return false;
        }
    }

    @Override
    public PromotionRule.RuleType getRuleType() {
        return PromotionRule.RuleType.MIN_ORDER_VALUE;
    }
}
