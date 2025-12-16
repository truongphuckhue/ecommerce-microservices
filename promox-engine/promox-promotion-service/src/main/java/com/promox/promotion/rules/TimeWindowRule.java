package com.promox.promotion.rules;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.promox.promotion.dto.CalculateDiscountRequest;
import com.promox.promotion.entity.PromotionRule;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.LocalTime;

/**
 * Rule: Promotion valid only during specific time windows
 * Config example: {"startTime": "09:00", "endTime": "17:00", "daysOfWeek": [1,2,3,4,5]}
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class TimeWindowRule implements Rule {

    private final ObjectMapper objectMapper;

    @Override
    public boolean evaluate(PromotionRule rule, CalculateDiscountRequest request) {
        try {
            JsonNode config = objectMapper.readTree(rule.getRuleConfig());
            
            LocalTime startTime = LocalTime.parse(config.get("startTime").asText());
            LocalTime endTime = LocalTime.parse(config.get("endTime").asText());
            LocalTime now = LocalTime.now();

            boolean withinTimeWindow = !now.isBefore(startTime) && !now.isAfter(endTime);

            log.debug("TimeWindowRule: now={}, startTime={}, endTime={}, passes={}",
                    now, startTime, endTime, withinTimeWindow);

            return withinTimeWindow;

        } catch (Exception e) {
            log.error("Error evaluating TimeWindowRule", e);
            return false;
        }
    }

    @Override
    public PromotionRule.RuleType getRuleType() {
        return PromotionRule.RuleType.TIME_WINDOW;
    }
}
