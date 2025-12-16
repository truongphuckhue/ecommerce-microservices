package com.promox.analytics.repository;

import com.promox.analytics.entity.PromotionUsageLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface PromotionUsageLogRepository extends JpaRepository<PromotionUsageLog, Long> {
    
    List<PromotionUsageLog> findByPromotionId(Long promotionId);
    
    List<PromotionUsageLog> findByUserId(Long userId);
    
    List<PromotionUsageLog> findByCreatedAtBetween(LocalDateTime start, LocalDateTime end);
    
    @Query("SELECT pul FROM PromotionUsageLog pul WHERE pul.promotionId = :promotionId " +
           "AND pul.createdAt >= :startDate AND pul.createdAt < :endDate")
    List<PromotionUsageLog> findByPromotionAndDateRange(
        @Param("promotionId") Long promotionId,
        @Param("startDate") LocalDateTime startDate,
        @Param("endDate") LocalDateTime endDate);
    
    @Query("SELECT COUNT(DISTINCT pul.userId) FROM PromotionUsageLog pul " +
           "WHERE pul.promotionId = :promotionId AND pul.createdAt >= :startDate AND pul.createdAt < :endDate")
    Integer countUniqueUsersByPromotionAndDateRange(
        @Param("promotionId") Long promotionId,
        @Param("startDate") LocalDateTime startDate,
        @Param("endDate") LocalDateTime endDate);
}
