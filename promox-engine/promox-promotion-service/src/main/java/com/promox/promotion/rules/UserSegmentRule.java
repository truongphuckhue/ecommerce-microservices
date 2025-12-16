package com.promox.promotion.rules;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.promox.promotion.dto.CalculateDiscountRequest;
import com.promox.promotion.entity.PromotionRule;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * Rule: User must belong to specific segment
 * Config example: {"segment": "VIP", "tierLevel": "GOLD"}
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class UserSegmentRule implements Rule {

    private final ObjectMapper objectMapper;

    @Override
    public boolean evaluate(PromotionRule rule, CalculateDiscountRequest request) {
        try {
            JsonNode config = objectMapper.readTree(rule.getRuleConfig());
            String requiredSegment = config.get("segment").asText().toUpperCase();

            String userSegment = request.getUserSegment() != null 
                    ? request.getUserSegment().toUpperCase() 
                    : "REGULAR";

            boolean passes = userSegment.equals(requiredSegment);

            log.debug("UserSegmentRule: requiredSegment={}, userSegment={}, passes={}",
                    requiredSegment, userSegment, passes);

            return passes;

        } catch (Exception e) {
            log.error("Error evaluating UserSegmentRule", e);
            return false;
        }
    }

    @Override
    public PromotionRule.RuleType getRuleType() {
        return PromotionRule.RuleType.USER_SEGMENT;
    }
}
