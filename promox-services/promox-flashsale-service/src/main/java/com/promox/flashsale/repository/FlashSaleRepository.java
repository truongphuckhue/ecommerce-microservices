package com.promox.flashsale.repository;

import com.promox.flashsale.entity.FlashSale;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface FlashSaleRepository extends JpaRepository<FlashSale, Long> {

    // Find by status
    List<FlashSale> findByStatus(FlashSale.FlashSaleStatus status);
    
    Page<FlashSale> findByStatus(FlashSale.FlashSaleStatus status, Pageable pageable);

    // Find active flash sales
    @Query("SELECT f FROM FlashSale f WHERE f.status = 'ACTIVE' " +
           "AND f.startTime <= :now AND f.endTime >= :now")
    List<FlashSale> findActiveFlashSales(@Param("now") LocalDateTime now);

    // Find by campaign
    List<FlashSale> findByCampaignId(Long campaignId);

    // Find scheduled sales that should start
    @Query("SELECT f FROM FlashSale f WHERE f.status = 'SCHEDULED' " +
           "AND f.startTime <= :now")
    List<FlashSale> findFlashSalesToStart(@Param("now") LocalDateTime now);

    // Find active sales that should end
    @Query("SELECT f FROM FlashSale f WHERE f.status = 'ACTIVE' " +
           "AND f.endTime <= :now")
    List<FlashSale> findFlashSalesToEnd(@Param("now") LocalDateTime now);

    // Find by product
    List<FlashSale> findByProductId(Long productId);

    @Query("SELECT f FROM FlashSale f WHERE f.productId = :productId " +
           "AND f.status = 'ACTIVE' AND f.startTime <= :now AND f.endTime >= :now")
    List<FlashSale> findActiveFlashSalesByProduct(@Param("productId") Long productId, 
                                                   @Param("now") LocalDateTime now);

    // Search by product name
    @Query("SELECT f FROM FlashSale f WHERE LOWER(f.productName) " +
           "LIKE LOWER(CONCAT('%', :keyword, '%'))")
    List<FlashSale> searchByProductName(@Param("keyword") String keyword);
    
    Page<FlashSale> searchByProductName(@Param("keyword") String keyword, Pageable pageable);

    // Find upcoming sales
    @Query("SELECT f FROM FlashSale f WHERE f.status = 'SCHEDULED' " +
           "AND f.startTime > :now ORDER BY f.startTime ASC")
    List<FlashSale> findUpcomingSales(@Param("now") LocalDateTime now, Pageable pageable);

    // Count by status
    long countByStatus(FlashSale.FlashSaleStatus status);
}
