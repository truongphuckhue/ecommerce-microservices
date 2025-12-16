package com.promox.campaign.repository;

import com.promox.campaign.entity.Campaign;
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
public interface CampaignRepository extends JpaRepository<Campaign, Long> {

    // Find by status
    List<Campaign> findByStatus(Campaign.CampaignStatus status);
    
    Page<Campaign> findByStatus(Campaign.CampaignStatus status, Pageable pageable);

    // Find by type
    List<Campaign> findByType(Campaign.CampaignType type);

    // Find active campaigns
    @Query("SELECT c FROM Campaign c WHERE c.status = 'ACTIVE' " +
           "AND c.startDate <= :now AND c.endDate >= :now")
    List<Campaign> findActiveCampaigns(@Param("now") LocalDateTime now);

    // Find scheduled campaigns
    @Query("SELECT c FROM Campaign c WHERE c.status = 'SCHEDULED' " +
           "AND c.startDate > :now")
    List<Campaign> findScheduledCampaigns(@Param("now") LocalDateTime now);

    // Find campaigns that should start
    @Query("SELECT c FROM Campaign c WHERE c.status = 'SCHEDULED' " +
           "AND c.startDate <= :now")
    List<Campaign> findCampaignsToStart(@Param("now") LocalDateTime now);

    // Find campaigns that should end
    @Query("SELECT c FROM Campaign c WHERE c.status = 'ACTIVE' " +
           "AND c.endDate <= :now")
    List<Campaign> findCampaignsToEnd(@Param("now") LocalDateTime now);

    // Find by merchant
    List<Campaign> findByMerchantId(Long merchantId);
    
    Page<Campaign> findByMerchantId(Long merchantId, Pageable pageable);

    // Find by merchant and status
    List<Campaign> findByMerchantIdAndStatus(Long merchantId, Campaign.CampaignStatus status);

    // Search by name
    @Query("SELECT c FROM Campaign c WHERE LOWER(c.name) LIKE LOWER(CONCAT('%', :keyword, '%'))")
    List<Campaign> searchByName(@Param("keyword") String keyword);
    
    Page<Campaign> searchByName(@Param("keyword") String keyword, Pageable pageable);

    // Check if name exists
    boolean existsByName(String name);
    
    boolean existsByNameAndIdNot(String name, Long id);

    // Count by status
    long countByStatus(Campaign.CampaignStatus status);
    
    long countByMerchantIdAndStatus(Long merchantId, Campaign.CampaignStatus status);
}
