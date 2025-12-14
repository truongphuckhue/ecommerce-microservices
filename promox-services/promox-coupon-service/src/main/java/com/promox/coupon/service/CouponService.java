package com.promox.coupon.service;

import com.promox.coupon.dto.*;
import com.promox.coupon.entity.Coupon;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface CouponService {

    // CRUD operations
    CouponResponse createCoupon(CouponRequest request);
    
    CouponResponse getCouponById(Long id);
    
    CouponResponse getCouponByCode(String code);
    
    Page<CouponResponse> getAllCoupons(Pageable pageable);
    
    CouponResponse updateCoupon(Long id, CouponRequest request);
    
    void deleteCoupon(Long id);

    // Bulk operations
    BulkCouponResponse generateBulkCoupons(BulkCouponRequest request);
    
    List<CouponResponse> getCouponsByBatch(String batchId);

    // Validation & Redemption
    ValidateCouponResponse validateCoupon(ValidateCouponRequest request);
    
    RedeemCouponResponse redeemCoupon(RedeemCouponRequest request);

    // Status management
    CouponResponse activateCoupon(Long id);
    
    CouponResponse deactivateCoupon(Long id);
    
    CouponResponse revokeCoupon(Long id);

    // Query operations
    List<CouponResponse> getActiveCoupons();
    
    List<CouponResponse> getCouponsByCampaign(Long campaignId);
    
    List<CouponResponse> getCouponsByStatus(Coupon.CouponStatus status);
    
    List<CouponResponse> getCouponsForUser(Long userId);
    
    Page<CouponResponse> searchCoupons(String keyword, Pageable pageable);

    // Redemption history
    Page<CouponRedemption> getRedemptionHistory(Long couponId, Pageable pageable);
    
    Page<CouponRedemption> getUserRedemptions(Long userId, Pageable pageable);

    // Scheduled tasks
    void updateExpiredCoupons();
    
    void updateExhaustedCoupons();
}
