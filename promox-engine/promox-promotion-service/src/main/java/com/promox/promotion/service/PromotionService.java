package com.promox.promotion.service;

import com.promox.promotion.dto.*;
import com.promox.promotion.entity.Promotion;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface PromotionService {

    // CRUD operations
    PromotionResponse createPromotion(PromotionRequest request);

    PromotionResponse getPromotionById(Long id);

    PromotionResponse getPromotionByCode(String code);

    Page<PromotionResponse> getAllPromotions(Pageable pageable);

    PromotionResponse updatePromotion(Long id, PromotionRequest request);

    void deletePromotion(Long id);

    // Status management
    PromotionResponse activatePromotion(Long id);

    PromotionResponse pausePromotion(Long id);

    // Query operations
    List<PromotionResponse> getActivePromotions();

    List<PromotionResponse> getPromotionsByCampaign(Long campaignId);

    List<PromotionResponse> getPromotionsByStatus(Promotion.PromotionStatus status);

    Page<PromotionResponse> searchPromotions(String keyword, Pageable pageable);

    // Promotion rules
    PromotionRuleResponse addRule(Long promotionId, PromotionRuleRequest request);

    List<PromotionRuleResponse> getRules(Long promotionId);

    void deleteRule(Long ruleId);

    // Discount calculation
    CalculateDiscountResponse calculateDiscount(CalculateDiscountRequest request);

    CalculateDiscountResponse validatePromotion(String code, CalculateDiscountRequest request);
}
