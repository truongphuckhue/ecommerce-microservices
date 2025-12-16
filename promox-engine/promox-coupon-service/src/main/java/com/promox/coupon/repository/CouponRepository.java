package com.promox.coupon.repository;

import com.promox.coupon.entity.Coupon;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface CouponRepository extends JpaRepository<Coupon, Long> {

    // Find by code
    Optional<Coupon> findByCode(String code);

    boolean existsByCode(String code);

    // Find by status
    List<Coupon> findByStatus(Coupon.CouponStatus status);
    
    Page<Coupon> findByStatus(Coupon.CouponStatus status, Pageable pageable);

    // Find by campaign
    List<Coupon> findByCampaignId(Long campaignId);
    
    Page<Coupon> findByCampaignId(Long campaignId, Pageable pageable);

    // Find by batch
    List<Coupon> findByBatchId(String batchId);

    // Find by assigned user
    List<Coupon> findByAssignedUserId(Long userId);
    
    Page<Coupon> findByAssignedUserId(Long userId, Pageable pageable);

    // Find active coupons
    @Query("SELECT c FROM Coupon c WHERE c.status = 'ACTIVE' " +
           "AND c.validFrom <= :now AND c.validTo >= :now")
    List<Coupon> findActiveCoupons(@Param("now") LocalDateTime now);

    // Find valid coupons for user
    @Query("SELECT c FROM Coupon c WHERE c.status = 'ACTIVE' " +
           "AND c.validFrom <= :now AND c.validTo >= :now " +
           "AND (c.assignedUserId IS NULL OR c.assignedUserId = :userId) " +
           "AND (c.usageLimit IS NULL OR c.usageCount < c.usageLimit)")
    List<Coupon> findValidCouponsForUser(@Param("userId") Long userId, 
                                          @Param("now") LocalDateTime now);

    // Find expiring soon
    @Query("SELECT c FROM Coupon c WHERE c.status = 'ACTIVE' " +
           "AND c.validTo BETWEEN :now AND :expiryDate " +
           "ORDER BY c.validTo ASC")
    List<Coupon> findExpiringSoon(@Param("now") LocalDateTime now, 
                                   @Param("expiryDate") LocalDateTime expiryDate);

    // Find expired coupons to update status
    @Query("SELECT c FROM Coupon c WHERE c.status = 'ACTIVE' " +
           "AND c.validTo < :now")
    List<Coupon> findExpiredCoupons(@Param("now") LocalDateTime now);

    // Find exhausted coupons
    @Query("SELECT c FROM Coupon c WHERE c.status = 'ACTIVE' " +
           "AND c.usageLimit IS NOT NULL AND c.usageCount >= c.usageLimit")
    List<Coupon> findExhaustedCoupons();

    // Search by code
    @Query("SELECT c FROM Coupon c WHERE LOWER(c.code) LIKE LOWER(CONCAT('%', :keyword, '%'))")
    Page<Coupon> searchByCode(@Param("keyword") String keyword, Pageable pageable);

    // Count by status
    long countByStatus(Coupon.CouponStatus status);

    // Count by campaign
    long countByCampaignId(Long campaignId);

    // Count by batch
    long countByBatchId(String batchId);
}
