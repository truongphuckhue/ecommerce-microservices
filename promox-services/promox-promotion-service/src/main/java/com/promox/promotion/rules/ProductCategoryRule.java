package com.promox.promotion.rules;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.promox.promotion.dto.CalculateDiscountRequest;
import com.promox.promotion.entity.PromotionRule;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * Rule: Order must contain products from specific categories
 * Config example: {"categories": ["electronics", "computers"], "includeSubcategories": true}
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class ProductCategoryRule implements Rule {

    private final ObjectMapper objectMapper;

    @Override
    public boolean evaluate(PromotionRule rule, CalculateDiscountRequest request) {
        try {
            JsonNode config = objectMapper.readTree(rule.getRuleConfig());
            List<String> requiredCategories = new ArrayList<>();

            config.get("categories").forEach(cat -> requiredCategories.add(cat.asText().toLowerCase()));

            // Check if any item in the order matches required categories
            boolean hasMatchingCategory = request.getItems().stream()
                    .anyMatch(item -> {
                        String itemCategory = item.getCategory() != null ? item.getCategory().toLowerCase() : "";
                        return requiredCategories.stream()
                                .anyMatch(itemCategory::contains);
                    });

            log.debug("ProductCategoryRule: requiredCategories={}, hasMatch={}",
                    requiredCategories, hasMatchingCategory);

            return hasMatchingCategory;

        } catch (Exception e) {
            log.error("Error evaluating ProductCategoryRule", e);
            return false;
        }
    }

    @Override
    public PromotionRule.RuleType getRuleType() {
        return PromotionRule.RuleType.PRODUCT_CATEGORY;
    }
}
