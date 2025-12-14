package com.promox.promotion.repository;

import com.promox.promotion.entity.Promotion;
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
public interface PromotionRepository extends JpaRepository<Promotion, Long> {

    // Find by code
    Optional<Promotion> findByCode(String code);

    Optional<Promotion> findByCodeAndStatus(String code, Promotion.PromotionStatus status);

    // Find by campaign
    List<Promotion> findByCampaignId(Long campaignId);

    Page<Promotion> findByCampaignId(Long campaignId, Pageable pageable);

    // Find by status
    List<Promotion> findByStatus(Promotion.PromotionStatus status);

    Page<Promotion> findByStatus(Promotion.PromotionStatus status, Pageable pageable);

    // Find active promotions
    @Query("SELECT p FROM Promotion p WHERE p.status = 'ACTIVE' " +
           "AND p.validFrom <= :now AND p.validTo >= :now")
    List<Promotion> findActivePromotions(@Param("now") LocalDateTime now);

    // Find by campaign and status
    List<Promotion> findByCampaignIdAndStatus(Long campaignId, Promotion.PromotionStatus status);

    // Find stackable promotions
    List<Promotion> findByStackableTrue();

    @Query("SELECT p FROM Promotion p WHERE p.stackable = true " +
           "AND p.status = 'ACTIVE' AND p.validFrom <= :now AND p.validTo >= :now")
    List<Promotion> findActiveStackablePromotions(@Param("now") LocalDateTime now);

    // Find by type
    List<Promotion> findByType(Promotion.PromotionType type);

    // Search by name or code
    @Query("SELECT p FROM Promotion p WHERE " +
           "LOWER(p.name) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(p.code) LIKE LOWER(CONCAT('%', :keyword, '%'))")
    List<Promotion> searchByNameOrCode(@Param("keyword") String keyword);

    Page<Promotion> searchByNameOrCode(@Param("keyword") String keyword, Pageable pageable);

    // Check existence
    boolean existsByCode(String code);

    boolean existsByCodeAndIdNot(String code, Long id);

    // Find expiring soon
    @Query("SELECT p FROM Promotion p WHERE p.status = 'ACTIVE' " +
           "AND p.validTo BETWEEN :now AND :expiryTime")
    List<Promotion> findExpiringSoon(@Param("now") LocalDateTime now, 
                                     @Param("expiryTime") LocalDateTime expiryTime);

    // Count by status
    long countByStatus(Promotion.PromotionStatus status);

    long countByCampaignIdAndStatus(Long campaignId, Promotion.PromotionStatus status);
}
