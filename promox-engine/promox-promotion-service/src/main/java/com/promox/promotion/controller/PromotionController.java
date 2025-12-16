package com.promox.promotion.controller;

import com.promox.promotion.dto.*;
import com.promox.promotion.entity.Promotion;
import com.promox.promotion.service.PromotionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/promotions")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Promotion Management", description = "APIs for managing promotions and discounts")
public class PromotionController {

    private final PromotionService promotionService;

    @PostMapping
    @Operation(summary = "Create new promotion", description = "Create a new promotion with discount rules")
    public ResponseEntity<ApiResponse<PromotionResponse>> createPromotion(
            @Valid @RequestBody PromotionRequest request) {
        log.info("REST request to create promotion: {}", request.getName());

        PromotionResponse response = promotionService.createPromotion(request);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success("Promotion created successfully", response));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get promotion by ID", description = "Retrieve promotion details including rules")
    public ResponseEntity<ApiResponse<PromotionResponse>> getPromotionById(@PathVariable Long id) {
        log.info("REST request to get promotion: {}", id);

        PromotionResponse response = promotionService.getPromotionById(id);

        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/code/{code}")
    @Operation(summary = "Get promotion by code", description = "Retrieve promotion details by code")
    public ResponseEntity<ApiResponse<PromotionResponse>> getPromotionByCode(@PathVariable String code) {
        log.info("REST request to get promotion by code: {}", code);

        PromotionResponse response = promotionService.getPromotionByCode(code);

        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping
    @Operation(summary = "Get all promotions", description = "Retrieve all promotions with pagination")
    public ResponseEntity<ApiResponse<Page<PromotionResponse>>> getAllPromotions(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "DESC") String sortDir) {

        log.info("REST request to get all promotions, page: {}, size: {}", page, size);

        Sort sort = sortDir.equalsIgnoreCase("ASC")
                ? Sort.by(sortBy).ascending()
                : Sort.by(sortBy).descending();

        Pageable pageable = PageRequest.of(page, size, sort);
        Page<PromotionResponse> promotions = promotionService.getAllPromotions(pageable);

        return ResponseEntity.ok(ApiResponse.success(promotions));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update promotion", description = "Update existing promotion")
    public ResponseEntity<ApiResponse<PromotionResponse>> updatePromotion(
            @PathVariable Long id,
            @Valid @RequestBody PromotionRequest request) {

        log.info("REST request to update promotion: {}", id);

        PromotionResponse response = promotionService.updatePromotion(id, request);

        return ResponseEntity.ok(ApiResponse.success("Promotion updated successfully", response));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete promotion", description = "Delete a promotion and its rules")
    public ResponseEntity<ApiResponse<Void>> deletePromotion(@PathVariable Long id) {
        log.info("REST request to delete promotion: {}", id);

        promotionService.deletePromotion(id);

        return ResponseEntity.ok(ApiResponse.success("Promotion deleted successfully", null));
    }

    // Status management endpoints

    @PostMapping("/{id}/activate")
    @Operation(summary = "Activate promotion", description = "Activate a draft or paused promotion")
    public ResponseEntity<ApiResponse<PromotionResponse>> activatePromotion(@PathVariable Long id) {
        log.info("REST request to activate promotion: {}", id);

        PromotionResponse response = promotionService.activatePromotion(id);

        return ResponseEntity.ok(ApiResponse.success("Promotion activated successfully", response));
    }

    @PostMapping("/{id}/pause")
    @Operation(summary = "Pause promotion", description = "Pause an active promotion")
    public ResponseEntity<ApiResponse<PromotionResponse>> pausePromotion(@PathVariable Long id) {
        log.info("REST request to pause promotion: {}", id);

        PromotionResponse response = promotionService.pausePromotion(id);

        return ResponseEntity.ok(ApiResponse.success("Promotion paused successfully", response));
    }

    // Query endpoints

    @GetMapping("/active")
    @Operation(summary = "Get active promotions", description = "Retrieve all currently active promotions")
    public ResponseEntity<ApiResponse<List<PromotionResponse>>> getActivePromotions() {
        log.info("REST request to get active promotions");

        List<PromotionResponse> promotions = promotionService.getActivePromotions();

        return ResponseEntity.ok(ApiResponse.success(promotions));
    }

    @GetMapping("/campaign/{campaignId}")
    @Operation(summary = "Get promotions by campaign", description = "Retrieve promotions for a specific campaign")
    public ResponseEntity<ApiResponse<List<PromotionResponse>>> getPromotionsByCampaign(
            @PathVariable Long campaignId) {

        log.info("REST request to get promotions for campaign: {}", campaignId);

        List<PromotionResponse> promotions = promotionService.getPromotionsByCampaign(campaignId);

        return ResponseEntity.ok(ApiResponse.success(promotions));
    }

    @GetMapping("/status/{status}")
    @Operation(summary = "Get promotions by status", description = "Retrieve promotions by status")
    public ResponseEntity<ApiResponse<List<PromotionResponse>>> getPromotionsByStatus(
            @PathVariable Promotion.PromotionStatus status) {

        log.info("REST request to get promotions by status: {}", status);

        List<PromotionResponse> promotions = promotionService.getPromotionsByStatus(status);

        return ResponseEntity.ok(ApiResponse.success(promotions));
    }

    @GetMapping("/search")
    @Operation(summary = "Search promotions", description = "Search promotions by name or code")
    public ResponseEntity<ApiResponse<Page<PromotionResponse>>> searchPromotions(
            @RequestParam String keyword,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        log.info("REST request to search promotions with keyword: {}", keyword);

        Pageable pageable = PageRequest.of(page, size);
        Page<PromotionResponse> promotions = promotionService.searchPromotions(keyword, pageable);

        return ResponseEntity.ok(ApiResponse.success(promotions));
    }

    // Promotion rules endpoints

    @PostMapping("/{promotionId}/rules")
    @Operation(summary = "Add rule to promotion", description = "Add a validation rule to a promotion")
    public ResponseEntity<ApiResponse<PromotionRuleResponse>> addRule(
            @PathVariable Long promotionId,
            @Valid @RequestBody PromotionRuleRequest request) {

        log.info("REST request to add rule to promotion: {}", promotionId);

        PromotionRuleResponse response = promotionService.addRule(promotionId, request);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success("Rule added successfully", response));
    }

    @GetMapping("/{promotionId}/rules")
    @Operation(summary = "Get promotion rules", description = "Retrieve all rules for a promotion")
    public ResponseEntity<ApiResponse<List<PromotionRuleResponse>>> getRules(
            @PathVariable Long promotionId) {

        log.info("REST request to get rules for promotion: {}", promotionId);

        List<PromotionRuleResponse> rules = promotionService.getRules(promotionId);

        return ResponseEntity.ok(ApiResponse.success(rules));
    }

    @DeleteMapping("/rules/{ruleId}")
    @Operation(summary = "Delete rule", description = "Delete a promotion rule")
    public ResponseEntity<ApiResponse<Void>> deleteRule(@PathVariable Long ruleId) {
        log.info("REST request to delete rule: {}", ruleId);

        promotionService.deleteRule(ruleId);

        return ResponseEntity.ok(ApiResponse.success("Rule deleted successfully", null));
    }

    // Discount calculation endpoints

    @PostMapping("/calculate")
    @Operation(summary = "Calculate discount", description = "Calculate discount for an order with promotions")
    public ResponseEntity<ApiResponse<CalculateDiscountResponse>> calculateDiscount(
            @Valid @RequestBody CalculateDiscountRequest request) {

        log.info("REST request to calculate discount");

        CalculateDiscountResponse response = promotionService.calculateDiscount(request);

        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @PostMapping("/validate/{code}")
    @Operation(summary = "Validate promotion", description = "Validate if a promotion code can be applied")
    public ResponseEntity<ApiResponse<CalculateDiscountResponse>> validatePromotion(
            @PathVariable String code,
            @Valid @RequestBody CalculateDiscountRequest request) {

        log.info("REST request to validate promotion: {}", code);

        CalculateDiscountResponse response = promotionService.validatePromotion(code, request);

        return ResponseEntity.ok(ApiResponse.success(response));
    }
}
