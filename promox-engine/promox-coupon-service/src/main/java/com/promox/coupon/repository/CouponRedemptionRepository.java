package com.promox.coupon.repository;

import com.promox.coupon.entity.CouponRedemption;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface CouponRedemptionRepository extends JpaRepository<CouponRedemption, Long> {

    // Find by coupon
    List<CouponRedemption> findByCouponId(Long couponId);
    
    Page<CouponRedemption> findByCouponId(Long couponId, Pageable pageable);

    // Find by user
    List<CouponRedemption> findByUserId(Long userId);
    
    Page<CouponRedemption> findByUserId(Long userId, Pageable pageable);

    // Find by coupon and user
    List<CouponRedemption> findByCouponIdAndUserId(Long couponId, Long userId);

    // Count user redemptions for specific coupon
    long countByCouponIdAndUserId(Long couponId, Long userId);

    // Find by order
    List<CouponRedemption> findByOrderId(String orderId);

    // Find by status
    List<CouponRedemption> findByStatus(CouponRedemption.RedemptionStatus status);

    // Find recent redemptions
    @Query("SELECT r FROM CouponRedemption r WHERE r.redeemedAt >= :fromDate " +
           "ORDER BY r.redeemedAt DESC")
    List<CouponRedemption> findRecentRedemptions(@Param("fromDate") LocalDateTime fromDate);

    // Get total discount given
    @Query("SELECT COALESCE(SUM(r.discountAmount), 0) FROM CouponRedemption r " +
           "WHERE r.status = 'SUCCESS'")
    Double getTotalDiscountGiven();

    // Get total discount for campaign
    @Query("SELECT COALESCE(SUM(r.discountAmount), 0) FROM CouponRedemption r " +
           "JOIN Coupon c ON r.couponId = c.id " +
           "WHERE c.campaignId = :campaignId AND r.status = 'SUCCESS'")
    Double getTotalDiscountByCampaign(@Param("campaignId") Long campaignId);
}
