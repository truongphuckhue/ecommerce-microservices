package com.promox.analytics.repository;

import com.promox.analytics.entity.PromotionAnalytics;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface PromotionAnalyticsRepository extends JpaRepository<PromotionAnalytics, Long> {
    
    Optional<PromotionAnalytics> findByPromotionIdAndAnalyticsDate(Long promotionId, LocalDate date);
    
    List<PromotionAnalytics> findByPromotionId(Long promotionId);
    
    List<PromotionAnalytics> findByPromotionIdAndAnalyticsDateBetween(
        Long promotionId, LocalDate startDate, LocalDate endDate);
    
    List<PromotionAnalytics> findByAnalyticsDate(LocalDate date);
    
    @Query("SELECT pa FROM PromotionAnalytics pa WHERE pa.analyticsDate = :date ORDER BY pa.totalRevenue DESC")
    List<PromotionAnalytics> findTopPerformersByRevenue(@Param("date") LocalDate date);
    
    @Query("SELECT COALESCE(AVG(pa.roi), 0) FROM PromotionAnalytics pa WHERE pa.analyticsDate = :date")
    BigDecimal getAvgROI(@Param("date") LocalDate date);
    
    @Query("SELECT COALESCE(SUM(pa.totalDiscount), 0) FROM PromotionAnalytics pa WHERE pa.analyticsDate = :date")
    BigDecimal getTotalDiscountByDate(@Param("date") LocalDate date);
}
