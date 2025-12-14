package com.promox.coupon.service;

import com.promox.coupon.dto.*;
import com.promox.coupon.entity.Coupon;
import com.promox.coupon.entity.CouponRedemption;
import com.promox.coupon.exception.CouponNotFoundException;
import com.promox.coupon.generator.CouponCodeGenerator;
import com.promox.coupon.mapper.CouponMapper;
import com.promox.coupon.repository.CouponRedemptionRepository;
import com.promox.coupon.repository.CouponRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class CouponServiceImpl implements CouponService {

    private final CouponRepository couponRepository;
    private final CouponRedemptionRepository redemptionRepository;
    private final CouponMapper couponMapper;
    private final CouponCodeGenerator codeGenerator;

    @Override
    @Transactional
    public CouponResponse createCoupon(CouponRequest request) {
        log.info("Creating new coupon: {}", request.getCode());

        // Check if code already exists
        if (couponRepository.existsByCode(request.getCode())) {
            throw new IllegalArgumentException("Coupon code already exists: " + request.getCode());
        }

        // Validate code format
        if (!codeGenerator.isValidFormat(request.getCode())) {
            throw new IllegalArgumentException("Invalid coupon code format. Use uppercase letters, numbers, and hyphens only.");
        }

        Coupon coupon = couponMapper.toEntity(request);
        Coupon savedCoupon = couponRepository.save(coupon);

        log.info("Coupon created successfully: {}", savedCoupon.getCode());

        return couponMapper.toResponse(savedCoupon);
    }

    @Override
    @Transactional(readOnly = true)
    public CouponResponse getCouponById(Long id) {
        log.info("Fetching coupon with id: {}", id);

        Coupon coupon = couponRepository.findById(id)
                .orElseThrow(() -> new CouponNotFoundException(id));

        return couponMapper.toResponse(coupon);
    }

    @Override
    @Transactional(readOnly = true)
    public CouponResponse getCouponByCode(String code) {
        log.info("Fetching coupon with code: {}", code);

        Coupon coupon = couponRepository.findByCode(code)
                .orElseThrow(() -> new CouponNotFoundException("Coupon not found with code: " + code));

        return couponMapper.toResponse(coupon);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<CouponResponse> getAllCoupons(Pageable pageable) {
        log.info("Fetching all coupons, page: {}, size: {}",
                pageable.getPageNumber(), pageable.getPageSize());

        return couponRepository.findAll(pageable)
                .map(couponMapper::toResponse);
    }

    @Override
    @Transactional
    public CouponResponse updateCoupon(Long id, CouponRequest request) {
        log.info("Updating coupon with id: {}", id);

        Coupon coupon = couponRepository.findById(id)
                .orElseThrow(() -> new CouponNotFoundException(id));

        // Check if code changed and new code exists
        if (!coupon.getCode().equals(request.getCode())) {
            if (couponRepository.existsByCode(request.getCode())) {
                throw new IllegalArgumentException("Coupon code already exists: " + request.getCode());
            }
        }

        couponMapper.updateEntityFromRequest(coupon, request);
        Coupon updatedCoupon = couponRepository.save(coupon);

        log.info("Coupon updated successfully: {}", updatedCoupon.getCode());

        return couponMapper.toResponse(updatedCoupon);
    }

    @Override
    @Transactional
    public void deleteCoupon(Long id) {
        log.info("Deleting coupon with id: {}", id);

        Coupon coupon = couponRepository.findById(id)
                .orElseThrow(() -> new CouponNotFoundException(id));

        // Soft delete: change status to REVOKED
        coupon.setStatus(Coupon.CouponStatus.REVOKED);
        couponRepository.save(coupon);

        log.info("Coupon revoked successfully: {}", coupon.getCode());
    }

    @Override
    @Transactional
    public BulkCouponResponse generateBulkCoupons(BulkCouponRequest request) {
        log.info("Generating {} bulk coupons with prefix: {}", request.getQuantity(), request.getPrefix());

        // Generate unique batch ID
        String batchId = UUID.randomUUID().toString().substring(0, 8).toUpperCase();

        // Generate unique codes
        Set<String> generatedCodes = codeGenerator.generateUniqueCodes(
                request.getPrefix(),
                request.getCodeLength(),
                request.getQuantity()
        );

        List<Coupon> coupons = new ArrayList<>();
        List<String> couponCodes = new ArrayList<>();

        for (String code : generatedCodes) {
            // Double-check code doesn't exist
            if (couponRepository.existsByCode(code)) {
                log.warn("Skipping duplicate code: {}", code);
                continue;
            }

            Coupon coupon = Coupon.builder()
                    .code(code)
                    .campaignId(request.getCampaignId())
                    .couponType(request.getCouponType())
                    .discountType(request.getDiscountType())
                    .discountValue(request.getDiscountValue())
                    .maxDiscountAmount(request.getMaxDiscountAmount())
                    .minOrderValue(request.getMinOrderValue())
                    .validFrom(request.getValidFrom())
                    .validTo(request.getValidTo())
                    .status(Coupon.CouponStatus.ACTIVE)
                    .usageLimit(request.getUsageLimit())
                    .usageCount(0)
                    .perUserLimit(request.getPerUserLimit())
                    .metadata(request.getMetadata())
                    .createdBy(request.getCreatedBy())
                    .batchId(batchId)
                    .build();

            coupons.add(coupon);
            couponCodes.add(code);
        }

        // Batch insert
        List<Coupon> savedCoupons = couponRepository.saveAll(coupons);

        log.info("Successfully generated {} coupons in batch: {}", savedCoupons.size(), batchId);

        return BulkCouponResponse.builder()
                .batchId(batchId)
                .totalGenerated(savedCoupons.size())
                .couponCodes(couponCodes)
                .message("Successfully generated " + savedCoupons.size() + " coupons")
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public List<CouponResponse> getCouponsByBatch(String batchId) {
        log.info("Fetching coupons for batch: {}", batchId);

        return couponRepository.findByBatchId(batchId)
                .stream()
                .map(couponMapper::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public ValidateCouponResponse validateCoupon(ValidateCouponRequest request) {
        log.info("Validating coupon: {} for user: {}", request.getCouponCode(), request.getUserId());

        Optional<Coupon> couponOpt = couponRepository.findByCode(request.getCouponCode());

        if (couponOpt.isEmpty()) {
            return ValidateCouponResponse.builder()
                    .valid(false)
                    .message("Coupon code not found")
                    .couponCode(request.getCouponCode())
                    .build();
        }

        Coupon coupon = couponOpt.get();

        // Check if coupon is valid
        if (!coupon.isValid()) {
            String reason = coupon.getStatus() != Coupon.CouponStatus.ACTIVE ? "Coupon is " + coupon.getStatus().name().toLowerCase()
                    : coupon.isExpired() ? "Coupon has expired"
                    : "Coupon is not yet valid";

            return ValidateCouponResponse.builder()
                    .valid(false)
                    .message(reason)
                    .couponCode(request.getCouponCode())
                    .build();
        }

        // Check usage limit
        if (coupon.isExhausted()) {
            return ValidateCouponResponse.builder()
                    .valid(false)
                    .message("Coupon usage limit reached")
                    .couponCode(request.getCouponCode())
                    .build();
        }

        // Check if assigned to specific user
        if (coupon.getAssignedUserId() != null && !coupon.isAssignedTo(request.getUserId())) {
            return ValidateCouponResponse.builder()
                    .valid(false)
                    .message("This coupon is not assigned to you")
                    .couponCode(request.getCouponCode())
                    .build();
        }

        // Check per-user limit
        if (coupon.getPerUserLimit() != null) {
            long userRedemptions = redemptionRepository.countByCouponIdAndUserId(
                    coupon.getId(), request.getUserId());

            if (userRedemptions >= coupon.getPerUserLimit()) {
                return ValidateCouponResponse.builder()
                        .valid(false)
                        .message("You have already used this coupon " + coupon.getPerUserLimit() + " times")
                        .couponCode(request.getCouponCode())
                        .build();
            }
        }

        // Check minimum order value
        if (coupon.getMinOrderValue() != null 
                && request.getOrderAmount().compareTo(coupon.getMinOrderValue()) < 0) {
            return ValidateCouponResponse.builder()
                    .valid(false)
                    .message("Minimum order value of $" + coupon.getMinOrderValue() + " required")
                    .couponCode(request.getCouponCode())
                    .build();
        }

        // Calculate discount
        BigDecimal discountAmount = calculateDiscount(coupon, request.getOrderAmount());
        BigDecimal finalAmount = request.getOrderAmount().subtract(discountAmount);

        String description = coupon.getDiscountType() == Coupon.DiscountType.PERCENTAGE
                ? coupon.getDiscountValue() + "% off"
                : "$" + coupon.getDiscountValue() + " off";

        return ValidateCouponResponse.builder()
                .valid(true)
                .message("Coupon is valid!")
                .couponCode(request.getCouponCode())
                .discountAmount(discountAmount)
                .finalAmount(finalAmount)
                .discountDescription(description)
                .build();
    }

    @Override
    @Transactional
    public RedeemCouponResponse redeemCoupon(RedeemCouponRequest request) {
        log.info("Redeeming coupon: {} for user: {} on order: {}",
                request.getCouponCode(), request.getUserId(), request.getOrderId());

        // Validate first
        ValidateCouponRequest validateRequest = ValidateCouponRequest.builder()
                .couponCode(request.getCouponCode())
                .userId(request.getUserId())
                .orderAmount(request.getOrderAmount())
                .build();

        ValidateCouponResponse validation = validateCoupon(validateRequest);

        if (!validation.getValid()) {
            return RedeemCouponResponse.builder()
                    .success(false)
                    .message(validation.getMessage())
                    .couponCode(request.getCouponCode())
                    .userId(request.getUserId())
                    .orderId(request.getOrderId())
                    .build();
        }

        // Get coupon
        Coupon coupon = couponRepository.findByCode(request.getCouponCode())
                .orElseThrow(() -> new CouponNotFoundException("Coupon not found: " + request.getCouponCode()));

        // Calculate discount
        BigDecimal discountAmount = calculateDiscount(coupon, request.getOrderAmount());
        BigDecimal finalAmount = request.getOrderAmount().subtract(discountAmount);

        // Create redemption record
        CouponRedemption redemption = CouponRedemption.builder()
                .couponId(coupon.getId())
                .couponCode(coupon.getCode())
                .userId(request.getUserId())
                .orderId(request.getOrderId())
                .orderAmount(request.getOrderAmount())
                .discountAmount(discountAmount)
                .finalAmount(finalAmount)
                .status(CouponRedemption.RedemptionStatus.SUCCESS)
                .ipAddress(request.getIpAddress())
                .metadata(request.getMetadata())
                .build();

        CouponRedemption savedRedemption = redemptionRepository.save(redemption);

        // Increment usage count
        coupon.setUsageCount(coupon.getUsageCount() + 1);

        // Check if exhausted
        if (coupon.isExhausted()) {
            coupon.setStatus(Coupon.CouponStatus.EXHAUSTED);
        }

        couponRepository.save(coupon);

        log.info("Coupon redeemed successfully. Redemption ID: {}", savedRedemption.getId());

        return RedeemCouponResponse.builder()
                .success(true)
                .message("Coupon redeemed successfully!")
                .redemptionId(savedRedemption.getId())
                .couponCode(coupon.getCode())
                .userId(request.getUserId())
                .orderId(request.getOrderId())
                .orderAmount(request.getOrderAmount())
                .discountAmount(discountAmount)
                .finalAmount(finalAmount)
                .redeemedAt(savedRedemption.getRedeemedAt())
                .build();
    }

    @Override
    @Transactional
    public CouponResponse activateCoupon(Long id) {
        log.info("Activating coupon with id: {}", id);

        Coupon coupon = couponRepository.findById(id)
                .orElseThrow(() -> new CouponNotFoundException(id));

        coupon.setStatus(Coupon.CouponStatus.ACTIVE);
        Coupon activatedCoupon = couponRepository.save(coupon);

        log.info("Coupon activated: {}", activatedCoupon.getCode());

        return couponMapper.toResponse(activatedCoupon);
    }

    @Override
    @Transactional
    public CouponResponse deactivateCoupon(Long id) {
        log.info("Deactivating coupon with id: {}", id);

        Coupon coupon = couponRepository.findById(id)
                .orElseThrow(() -> new CouponNotFoundException(id));

        coupon.setStatus(Coupon.CouponStatus.INACTIVE);
        Coupon deactivatedCoupon = couponRepository.save(coupon);

        log.info("Coupon deactivated: {}", deactivatedCoupon.getCode());

        return couponMapper.toResponse(deactivatedCoupon);
    }

    @Override
    @Transactional
    public CouponResponse revokeCoupon(Long id) {
        log.info("Revoking coupon with id: {}", id);

        Coupon coupon = couponRepository.findById(id)
                .orElseThrow(() -> new CouponNotFoundException(id));

        coupon.setStatus(Coupon.CouponStatus.REVOKED);
        Coupon revokedCoupon = couponRepository.save(coupon);

        log.info("Coupon revoked: {}", revokedCoupon.getCode());

        return couponMapper.toResponse(revokedCoupon);
    }

    @Override
    @Transactional(readOnly = true)
    public List<CouponResponse> getActiveCoupons() {
        log.info("Fetching active coupons");

        return couponRepository.findActiveCoupons(LocalDateTime.now())
                .stream()
                .map(couponMapper::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<CouponResponse> getCouponsByCampaign(Long campaignId) {
        log.info("Fetching coupons for campaign: {}", campaignId);

        return couponRepository.findByCampaignId(campaignId)
                .stream()
                .map(couponMapper::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<CouponResponse> getCouponsByStatus(Coupon.CouponStatus status) {
        log.info("Fetching coupons by status: {}", status);

        return couponRepository.findByStatus(status)
                .stream()
                .map(couponMapper::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<CouponResponse> getCouponsForUser(Long userId) {
        log.info("Fetching coupons for user: {}", userId);

        return couponRepository.findValidCouponsForUser(userId, LocalDateTime.now())
                .stream()
                .map(couponMapper::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public Page<CouponResponse> searchCoupons(String keyword, Pageable pageable) {
        log.info("Searching coupons with keyword: {}", keyword);

        return couponRepository.searchByCode(keyword, pageable)
                .map(couponMapper::toResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<CouponRedemption> getRedemptionHistory(Long couponId, Pageable pageable) {
        log.info("Fetching redemption history for coupon: {}", couponId);

        return redemptionRepository.findByCouponId(couponId, pageable);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<CouponRedemption> getUserRedemptions(Long userId, Pageable pageable) {
        log.info("Fetching redemptions for user: {}", userId);

        return redemptionRepository.findByUserId(userId, pageable);
    }

    @Override
    @Transactional
    public void updateExpiredCoupons() {
        log.info("Running scheduled task to update expired coupons");

        List<Coupon> expiredCoupons = couponRepository.findExpiredCoupons(LocalDateTime.now());

        for (Coupon coupon : expiredCoupons) {
            coupon.setStatus(Coupon.CouponStatus.EXPIRED);
        }

        couponRepository.saveAll(expiredCoupons);

        log.info("Updated {} expired coupons", expiredCoupons.size());
    }

    @Override
    @Transactional
    public void updateExhaustedCoupons() {
        log.info("Running scheduled task to update exhausted coupons");

        List<Coupon> exhaustedCoupons = couponRepository.findExhaustedCoupons();

        for (Coupon coupon : exhaustedCoupons) {
            coupon.setStatus(Coupon.CouponStatus.EXHAUSTED);
        }

        couponRepository.saveAll(exhaustedCoupons);

        log.info("Updated {} exhausted coupons", exhaustedCoupons.size());
    }

    // Helper method to calculate discount
    private BigDecimal calculateDiscount(Coupon coupon, BigDecimal orderAmount) {
        BigDecimal discount;

        if (coupon.getDiscountType() == Coupon.DiscountType.PERCENTAGE) {
            discount = orderAmount
                    .multiply(coupon.getDiscountValue())
                    .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
        } else {
            discount = coupon.getDiscountValue();
        }

        // Apply max discount limit
        if (coupon.getMaxDiscountAmount() != null 
                && discount.compareTo(coupon.getMaxDiscountAmount()) > 0) {
            discount = coupon.getMaxDiscountAmount();
        }

        // Discount cannot exceed order amount
        if (discount.compareTo(orderAmount) > 0) {
            discount = orderAmount;
        }

        return discount.setScale(2, RoundingMode.HALF_UP);
    }
}
