package com.promox.analytics.repository;

import com.promox.analytics.entity.CampaignAnalytics;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface CampaignAnalyticsRepository extends JpaRepository<CampaignAnalytics, Long> {
    
    Optional<CampaignAnalytics> findByCampaignIdAndAnalyticsDate(Long campaignId, LocalDate date);
    
    List<CampaignAnalytics> findByCampaignId(Long campaignId);
    
    List<CampaignAnalytics> findByCampaignIdAndAnalyticsDateBetween(
        Long campaignId, LocalDate startDate, LocalDate endDate);
    
    List<CampaignAnalytics> findByAnalyticsDate(LocalDate date);
    
    @Query("SELECT ca FROM CampaignAnalytics ca WHERE ca.analyticsDate = :date ORDER BY ca.totalRevenue DESC")
    List<CampaignAnalytics> findTopPerformersByRevenue(@Param("date") LocalDate date);
    
    @Query("SELECT COALESCE(SUM(ca.totalOrders), 0) FROM CampaignAnalytics ca WHERE ca.analyticsDate = :date")
    Integer getTotalOrdersByDate(@Param("date") LocalDate date);
    
    @Query("SELECT COALESCE(SUM(ca.totalRevenue), 0) FROM CampaignAnalytics ca WHERE ca.analyticsDate = :date")
    BigDecimal getTotalRevenueByDate(@Param("date") LocalDate date);
    
    @Query("SELECT COALESCE(AVG(ca.conversionRate), 0) FROM CampaignAnalytics ca WHERE ca.analyticsDate = :date")
    BigDecimal getAvgConversionRate(@Param("date") LocalDate date);
}
