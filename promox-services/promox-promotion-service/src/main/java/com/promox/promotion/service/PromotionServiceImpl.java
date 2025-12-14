package com.promox.promotion.service;

import com.promox.promotion.dto.*;
import com.promox.promotion.entity.Promotion;
import com.promox.promotion.entity.PromotionRule;
import com.promox.promotion.exception.PromotionNotFoundException;
import com.promox.promotion.mapper.PromotionMapper;
import com.promox.promotion.repository.PromotionRepository;
import com.promox.promotion.repository.PromotionRuleRepository;
import com.promox.promotion.rules.RuleEngine;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class PromotionServiceImpl implements PromotionService {

    private final PromotionRepository promotionRepository;
    private final PromotionRuleRepository promotionRuleRepository;
    private final PromotionMapper promotionMapper;
    private final RuleEngine ruleEngine;

    @Override
    @Transactional
    public PromotionResponse createPromotion(PromotionRequest request) {
        log.info("Creating new promotion: {}", request.getName());

        // Check if code already exists
        if (promotionRepository.existsByCode(request.getCode())) {
            throw new IllegalArgumentException("Promotion code '" + request.getCode() + "' already exists");
        }

        // Validate dates
        if (request.getValidTo().isBefore(request.getValidFrom())) {
            throw new IllegalArgumentException("Valid to must be after valid from");
        }

        Promotion promotion = promotionMapper.toEntity(request);

        // Auto-activate if valid from is now or in the past
        if (promotion.getValidFrom().isBefore(LocalDateTime.now()) ||
                promotion.getValidFrom().isEqual(LocalDateTime.now())) {
            promotion.setStatus(Promotion.PromotionStatus.ACTIVE);
        }

        Promotion savedPromotion = promotionRepository.save(promotion);

        log.info("Promotion created successfully with id: {}", savedPromotion.getId());

        return promotionMapper.toResponse(savedPromotion);
    }

    @Override
    @Transactional(readOnly = true)
    public PromotionResponse getPromotionById(Long id) {
        log.info("Fetching promotion with id: {}", id);

        Promotion promotion = promotionRepository.findById(id)
                .orElseThrow(() -> new PromotionNotFoundException(id));

        // Load rules
        List<PromotionRule> rules = promotionRuleRepository.findByPromotionId(id);

        return promotionMapper.toResponse(promotion, rules);
    }

    @Override
    @Transactional(readOnly = true)
    public PromotionResponse getPromotionByCode(String code) {
        log.info("Fetching promotion with code: {}", code);

        Promotion promotion = promotionRepository.findByCode(code.toUpperCase())
                .orElseThrow(() -> new PromotionNotFoundException("code", code));

        List<PromotionRule> rules = promotionRuleRepository.findByPromotionId(promotion.getId());

        return promotionMapper.toResponse(promotion, rules);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<PromotionResponse> getAllPromotions(Pageable pageable) {
        log.info("Fetching all promotions, page: {}, size: {}",
                pageable.getPageNumber(), pageable.getPageSize());

        return promotionRepository.findAll(pageable)
                .map(promotionMapper::toResponse);
    }

    @Override
    @Transactional
    public PromotionResponse updatePromotion(Long id, PromotionRequest request) {
        log.info("Updating promotion with id: {}", id);

        Promotion promotion = promotionRepository.findById(id)
                .orElseThrow(() -> new PromotionNotFoundException(id));

        // Check code uniqueness if code is being changed
        if (request.getCode() != null && !request.getCode().equalsIgnoreCase(promotion.getCode())) {
            if (promotionRepository.existsByCodeAndIdNot(request.getCode().toUpperCase(), id)) {
                throw new IllegalArgumentException("Promotion code '" + request.getCode() + "' already exists");
            }
        }

        // Validate if promotion can be updated
        if (promotion.getStatus() == Promotion.PromotionStatus.EXHAUSTED) {
            throw new IllegalArgumentException("Cannot update exhausted promotion");
        }

        promotionMapper.updateEntityFromRequest(promotion, request);

        Promotion updatedPromotion = promotionRepository.save(promotion);

        log.info("Promotion updated successfully: {}", updatedPromotion.getId());

        List<PromotionRule> rules = promotionRuleRepository.findByPromotionId(id);

        return promotionMapper.toResponse(updatedPromotion, rules);
    }

    @Override
    @Transactional
    public void deletePromotion(Long id) {
        log.info("Deleting promotion with id: {}", id);

        Promotion promotion = promotionRepository.findById(id)
                .orElseThrow(() -> new PromotionNotFoundException(id));

        // Delete associated rules first
        promotionRuleRepository.deleteByPromotionId(id);

        // Delete promotion
        promotionRepository.delete(promotion);

        log.info("Promotion deleted successfully: {}", id);
    }

    @Override
    @Transactional
    public PromotionResponse activatePromotion(Long id) {
        log.info("Activating promotion with id: {}", id);

        Promotion promotion = promotionRepository.findById(id)
                .orElseThrow(() -> new PromotionNotFoundException(id));

        // Validate status transition
        if (promotion.getStatus() != Promotion.PromotionStatus.DRAFT &&
                promotion.getStatus() != Promotion.PromotionStatus.PAUSED) {
            throw new IllegalArgumentException("Can only activate DRAFT or PAUSED promotions");
        }

        // Check if dates are valid
        LocalDateTime now = LocalDateTime.now();
        if (promotion.getValidFrom().isAfter(now)) {
            throw new IllegalArgumentException("Cannot activate promotion before its valid from date");
        }
        if (promotion.getValidTo().isBefore(now)) {
            throw new IllegalArgumentException("Cannot activate expired promotion");
        }

        promotion.setStatus(Promotion.PromotionStatus.ACTIVE);
        Promotion activatedPromotion = promotionRepository.save(promotion);

        log.info("Promotion activated successfully: {}", id);

        return promotionMapper.toResponse(activatedPromotion);
    }

    @Override
    @Transactional
    public PromotionResponse pausePromotion(Long id) {
        log.info("Pausing promotion with id: {}", id);

        Promotion promotion = promotionRepository.findById(id)
                .orElseThrow(() -> new PromotionNotFoundException(id));

        if (promotion.getStatus() != Promotion.PromotionStatus.ACTIVE) {
            throw new IllegalArgumentException("Only active promotions can be paused");
        }

        promotion.setStatus(Promotion.PromotionStatus.PAUSED);
        Promotion pausedPromotion = promotionRepository.save(promotion);

        log.info("Promotion paused successfully: {}", id);

        return promotionMapper.toResponse(pausedPromotion);
    }

    @Override
    @Transactional(readOnly = true)
    public List<PromotionResponse> getActivePromotions() {
        log.info("Fetching active promotions");

        return promotionRepository.findActivePromotions(LocalDateTime.now())
                .stream()
                .map(promotionMapper::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<PromotionResponse> getPromotionsByCampaign(Long campaignId) {
        log.info("Fetching promotions for campaign: {}", campaignId);

        return promotionRepository.findByCampaignId(campaignId)
                .stream()
                .map(promotionMapper::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<PromotionResponse> getPromotionsByStatus(Promotion.PromotionStatus status) {
        log.info("Fetching promotions by status: {}", status);

        return promotionRepository.findByStatus(status)
                .stream()
                .map(promotionMapper::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public Page<PromotionResponse> searchPromotions(String keyword, Pageable pageable) {
        log.info("Searching promotions with keyword: {}", keyword);

        return promotionRepository.searchByNameOrCode(keyword, pageable)
                .map(promotionMapper::toResponse);
    }

    @Override
    @Transactional
    public PromotionRuleResponse addRule(Long promotionId, PromotionRuleRequest request) {
        log.info("Adding rule to promotion: {}", promotionId);

        // Verify promotion exists
        Promotion promotion = promotionRepository.findById(promotionId)
                .orElseThrow(() -> new PromotionNotFoundException(promotionId));

        PromotionRule rule = promotionMapper.toRuleEntity(request, promotionId);
        PromotionRule savedRule = promotionRuleRepository.save(rule);

        log.info("Rule added successfully to promotion: {}", promotionId);

        return promotionMapper.toRuleResponse(savedRule);
    }

    @Override
    @Transactional(readOnly = true)
    public List<PromotionRuleResponse> getRules(Long promotionId) {
        log.info("Fetching rules for promotion: {}", promotionId);

        return promotionRuleRepository.findByPromotionIdOrderByPriorityDesc(promotionId)
                .stream()
                .map(promotionMapper::toRuleResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public void deleteRule(Long ruleId) {
        log.info("Deleting rule: {}", ruleId);
        promotionRuleRepository.deleteById(ruleId);
        log.info("Rule deleted successfully: {}", ruleId);
    }

    @Override
    @Transactional(readOnly = true)
    public CalculateDiscountResponse calculateDiscount(CalculateDiscountRequest request) {
        log.info("Calculating discount for codes: {}", request.getPromotionCodes());

        List<CalculateDiscountResponse.AppliedPromotion> appliedPromotions = new ArrayList<>();
        BigDecimal totalDiscount = BigDecimal.ZERO;

        List<String> codes = request.getPromotionCodes() != null ?
                request.getPromotionCodes() :
                (request.getPromotionCode() != null ? List.of(request.getPromotionCode()) : List.of());

        for (String code : codes) {
            Promotion promotion = promotionRepository.findByCodeAndStatus(
                    code.toUpperCase(), Promotion.PromotionStatus.ACTIVE)
                    .orElse(null);

            if (promotion == null || !promotion.canBeUsed()) {
                log.warn("Promotion {} not available", code);
                continue;
            }

            // Load and evaluate rules
            List<PromotionRule> rules = promotionRuleRepository
                    .findByPromotionIdAndIsActiveTrueOrderByPriorityDesc(promotion.getId());

            if (!ruleEngine.evaluateAll(rules, request)) {
                log.info("Promotion {} rules not satisfied", code);
                continue;
            }

            // Calculate discount
            BigDecimal discount = calculatePromotionDiscount(promotion, request);

            appliedPromotions.add(CalculateDiscountResponse.AppliedPromotion.builder()
                    .promotionId(promotion.getId())
                    .promotionCode(promotion.getCode())
                    .promotionName(promotion.getName())
                    .discountAmount(discount)
                    .discountDescription(getDiscountDescription(promotion, discount))
                    .build());

            totalDiscount = totalDiscount.add(discount);
        }

        BigDecimal finalAmount = request.getOrderTotal().subtract(totalDiscount);

        return CalculateDiscountResponse.builder()
                .applicable(!appliedPromotions.isEmpty())
                .message(appliedPromotions.isEmpty() ? "No promotions applied" : "Promotions applied successfully")
                .originalAmount(request.getOrderTotal())
                .discountAmount(totalDiscount)
                .finalAmount(finalAmount.max(BigDecimal.ZERO))
                .appliedPromotions(appliedPromotions)
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public CalculateDiscountResponse validatePromotion(String code, CalculateDiscountRequest request) {
        log.info("Validating promotion: {}", code);

        request.setPromotionCode(code);
        return calculateDiscount(request);
    }

    private BigDecimal calculatePromotionDiscount(Promotion promotion, CalculateDiscountRequest request) {
        BigDecimal discount = BigDecimal.ZERO;

        switch (promotion.getType()) {
            case PERCENTAGE:
                discount = request.getOrderTotal()
                        .multiply(promotion.getDiscountValue())
                        .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
                break;

            case FIXED_AMOUNT:
                discount = promotion.getDiscountValue();
                break;

            case FREE_SHIPPING:
                // Assuming shipping cost is in metadata or fixed
                discount = BigDecimal.valueOf(10.00); // Example
                break;

            default:
                discount = BigDecimal.ZERO;
        }

        // Apply max discount limit
        if (promotion.getMaxDiscountAmount() != null) {
            discount = discount.min(promotion.getMaxDiscountAmount());
        }

        // Don't exceed order total
        discount = discount.min(request.getOrderTotal());

        return discount;
    }

    private String getDiscountDescription(Promotion promotion, BigDecimal discount) {
        switch (promotion.getType()) {
            case PERCENTAGE:
                return String.format("%s%% off (saved $%s)", 
                    promotion.getDiscountValue(), discount);
            case FIXED_AMOUNT:
                return String.format("$%s off", discount);
            case FREE_SHIPPING:
                return "Free shipping applied";
            default:
                return promotion.getName();
        }
    }
}
