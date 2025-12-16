package com.promox.coupon.controller;

import com.promox.coupon.dto.*;
import com.promox.coupon.entity.Coupon;
import com.promox.coupon.entity.CouponRedemption;
import com.promox.coupon.service.CouponService;
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
@RequestMapping("/api/coupons")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Coupon Management", description = "APIs for managing coupons with bulk generation and redemption tracking")
public class CouponController {

    private final CouponService couponService;

    @PostMapping
    @Operation(summary = "Create coupon", description = "Create a single coupon")
    public ResponseEntity<ApiResponse<CouponResponse>> createCoupon(
            @Valid @RequestBody CouponRequest request) {
        log.info("REST request to create coupon: {}", request.getCode());

        CouponResponse response = couponService.createCoupon(request);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success("Coupon created successfully", response));
    }

    @PostMapping("/bulk")
    @Operation(summary = "Generate bulk coupons", description = "Generate multiple coupons with unique codes")
    public ResponseEntity<ApiResponse<BulkCouponResponse>> generateBulkCoupons(
            @Valid @RequestBody BulkCouponRequest request) {
        log.info("REST request to generate {} bulk coupons", request.getQuantity());

        BulkCouponResponse response = couponService.generateBulkCoupons(request);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success("Bulk coupons generated successfully", response));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get coupon by ID", description = "Retrieve coupon details by ID")
    public ResponseEntity<ApiResponse<CouponResponse>> getCouponById(@PathVariable Long id) {
        log.info("REST request to get coupon: {}", id);

        CouponResponse response = couponService.getCouponById(id);

        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/code/{code}")
    @Operation(summary = "Get coupon by code", description = "Retrieve coupon details by code")
    public ResponseEntity<ApiResponse<CouponResponse>> getCouponByCode(@PathVariable String code) {
        log.info("REST request to get coupon by code: {}", code);

        CouponResponse response = couponService.getCouponByCode(code);

        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping
    @Operation(summary = "Get all coupons", description = "Retrieve all coupons with pagination")
    public ResponseEntity<ApiResponse<Page<CouponResponse>>> getAllCoupons(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "DESC") String sortDir) {

        log.info("REST request to get all coupons, page: {}, size: {}", page, size);

        Sort sort = sortDir.equalsIgnoreCase("ASC")
                ? Sort.by(sortBy).ascending()
                : Sort.by(sortBy).descending();

        Pageable pageable = PageRequest.of(page, size, sort);
        Page<CouponResponse> coupons = couponService.getAllCoupons(pageable);

        return ResponseEntity.ok(ApiResponse.success(coupons));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update coupon", description = "Update existing coupon")
    public ResponseEntity<ApiResponse<CouponResponse>> updateCoupon(
            @PathVariable Long id,
            @Valid @RequestBody CouponRequest request) {

        log.info("REST request to update coupon: {}", id);

        CouponResponse response = couponService.updateCoupon(id, request);

        return ResponseEntity.ok(ApiResponse.success("Coupon updated successfully", response));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete coupon", description = "Revoke a coupon")
    public ResponseEntity<ApiResponse<Void>> deleteCoupon(@PathVariable Long id) {
        log.info("REST request to delete coupon: {}", id);

        couponService.deleteCoupon(id);

        return ResponseEntity.ok(ApiResponse.success("Coupon revoked successfully", null));
    }

    // Validation & Redemption

    @PostMapping("/validate")
    @Operation(summary = "Validate coupon", description = "Validate if coupon can be used")
    public ResponseEntity<ApiResponse<ValidateCouponResponse>> validateCoupon(
            @Valid @RequestBody ValidateCouponRequest request) {

        log.info("REST request to validate coupon: {}", request.getCouponCode());

        ValidateCouponResponse response = couponService.validateCoupon(request);

        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @PostMapping("/redeem")
    @Operation(summary = "Redeem coupon", description = "Redeem coupon on an order")
    public ResponseEntity<ApiResponse<RedeemCouponResponse>> redeemCoupon(
            @Valid @RequestBody RedeemCouponRequest request) {

        log.info("REST request to redeem coupon: {}", request.getCouponCode());

        RedeemCouponResponse response = couponService.redeemCoupon(request);

        if (response.getSuccess()) {
            return ResponseEntity.ok(ApiResponse.success(response.getMessage(), response));
        } else {
            return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.<RedeemCouponResponse>builder()
                            .success(false)
                            .message(response.getMessage())
                            .data(response)
                            .build());
        }
    }

    // Status management

    @PostMapping("/{id}/activate")
    @Operation(summary = "Activate coupon", description = "Activate a coupon")
    public ResponseEntity<ApiResponse<CouponResponse>> activateCoupon(@PathVariable Long id) {
        log.info("REST request to activate coupon: {}", id);

        CouponResponse response = couponService.activateCoupon(id);

        return ResponseEntity.ok(ApiResponse.success("Coupon activated successfully", response));
    }

    @PostMapping("/{id}/deactivate")
    @Operation(summary = "Deactivate coupon", description = "Deactivate a coupon")
    public ResponseEntity<ApiResponse<CouponResponse>> deactivateCoupon(@PathVariable Long id) {
        log.info("REST request to deactivate coupon: {}", id);

        CouponResponse response = couponService.deactivateCoupon(id);

        return ResponseEntity.ok(ApiResponse.success("Coupon deactivated successfully", response));
    }

    @PostMapping("/{id}/revoke")
    @Operation(summary = "Revoke coupon", description = "Revoke a coupon permanently")
    public ResponseEntity<ApiResponse<CouponResponse>> revokeCoupon(@PathVariable Long id) {
        log.info("REST request to revoke coupon: {}", id);

        CouponResponse response = couponService.revokeCoupon(id);

        return ResponseEntity.ok(ApiResponse.success("Coupon revoked successfully", response));
    }

    // Query operations

    @GetMapping("/active")
    @Operation(summary = "Get active coupons", description = "Retrieve all currently active coupons")
    public ResponseEntity<ApiResponse<List<CouponResponse>>> getActiveCoupons() {
        log.info("REST request to get active coupons");

        List<CouponResponse> coupons = couponService.getActiveCoupons();

        return ResponseEntity.ok(ApiResponse.success(coupons));
    }

    @GetMapping("/campaign/{campaignId}")
    @Operation(summary = "Get coupons by campaign", description = "Retrieve coupons for a specific campaign")
    public ResponseEntity<ApiResponse<List<CouponResponse>>> getCouponsByCampaign(
            @PathVariable Long campaignId) {

        log.info("REST request to get coupons for campaign: {}", campaignId);

        List<CouponResponse> coupons = couponService.getCouponsByCampaign(campaignId);

        return ResponseEntity.ok(ApiResponse.success(coupons));
    }

    @GetMapping("/status/{status}")
    @Operation(summary = "Get coupons by status", description = "Retrieve coupons by status")
    public ResponseEntity<ApiResponse<List<CouponResponse>>> getCouponsByStatus(
            @PathVariable Coupon.CouponStatus status) {

        log.info("REST request to get coupons by status: {}", status);

        List<CouponResponse> coupons = couponService.getCouponsByStatus(status);

        return ResponseEntity.ok(ApiResponse.success(coupons));
    }

    @GetMapping("/user/{userId}")
    @Operation(summary = "Get coupons for user", description = "Retrieve valid coupons for a specific user")
    public ResponseEntity<ApiResponse<List<CouponResponse>>> getCouponsForUser(
            @PathVariable Long userId) {

        log.info("REST request to get coupons for user: {}", userId);

        List<CouponResponse> coupons = couponService.getCouponsForUser(userId);

        return ResponseEntity.ok(ApiResponse.success(coupons));
    }

    @GetMapping("/batch/{batchId}")
    @Operation(summary = "Get coupons by batch", description = "Retrieve all coupons from a bulk generation batch")
    public ResponseEntity<ApiResponse<List<CouponResponse>>> getCouponsByBatch(
            @PathVariable String batchId) {

        log.info("REST request to get coupons for batch: {}", batchId);

        List<CouponResponse> coupons = couponService.getCouponsByBatch(batchId);

        return ResponseEntity.ok(ApiResponse.success(coupons));
    }

    @GetMapping("/search")
    @Operation(summary = "Search coupons", description = "Search coupons by code")
    public ResponseEntity<ApiResponse<Page<CouponResponse>>> searchCoupons(
            @RequestParam String keyword,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        log.info("REST request to search coupons with keyword: {}", keyword);

        Pageable pageable = PageRequest.of(page, size);
        Page<CouponResponse> coupons = couponService.searchCoupons(keyword, pageable);

        return ResponseEntity.ok(ApiResponse.success(coupons));
    }

    // Redemption history

    @GetMapping("/{couponId}/redemptions")
    @Operation(summary = "Get redemption history", description = "Get redemption history for a coupon")
    public ResponseEntity<ApiResponse<Page<CouponRedemption>>> getRedemptionHistory(
            @PathVariable Long couponId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        log.info("REST request to get redemption history for coupon: {}", couponId);

        Pageable pageable = PageRequest.of(page, size, Sort.by("redeemedAt").descending());
        Page<CouponRedemption> redemptions = couponService.getRedemptionHistory(couponId, pageable);

        return ResponseEntity.ok(ApiResponse.success(redemptions));
    }

    @GetMapping("/user/{userId}/redemptions")
    @Operation(summary = "Get user redemptions", description = "Get all redemptions for a user")
    public ResponseEntity<ApiResponse<Page<CouponRedemption>>> getUserRedemptions(
            @PathVariable Long userId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        log.info("REST request to get redemptions for user: {}", userId);

        Pageable pageable = PageRequest.of(page, size, Sort.by("redeemedAt").descending());
        Page<CouponRedemption> redemptions = couponService.getUserRedemptions(userId, pageable);

        return ResponseEntity.ok(ApiResponse.success(redemptions));
    }
}
