package com.promox.promotion.repository;

import com.promox.promotion.entity.PromotionRule;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PromotionRuleRepository extends JpaRepository<PromotionRule, Long> {

    // Find by promotion
    List<PromotionRule> findByPromotionId(Long promotionId);

    List<PromotionRule> findByPromotionIdOrderByPriorityDesc(Long promotionId);

    // Find active rules
    List<PromotionRule> findByPromotionIdAndIsActiveTrue(Long promotionId);

    List<PromotionRule> findByPromotionIdAndIsActiveTrueOrderByPriorityDesc(Long promotionId);

    // Find by rule type
    List<PromotionRule> findByRuleType(PromotionRule.RuleType ruleType);

    List<PromotionRule> findByPromotionIdAndRuleType(Long promotionId, PromotionRule.RuleType ruleType);

    // Delete by promotion
    void deleteByPromotionId(Long promotionId);
}
