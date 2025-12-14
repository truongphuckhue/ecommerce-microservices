package com.promox.flashsale.repository;

import com.promox.flashsale.entity.FlashSalePurchase;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface FlashSalePurchaseRepository extends JpaRepository<FlashSalePurchase, Long> {

    // Find by flash sale
    List<FlashSalePurchase> findByFlashSaleId(Long flashSaleId);

    // Find by user
    List<FlashSalePurchase> findByUserId(Long userId);

    // Find by flash sale and user
    List<FlashSalePurchase> findByFlashSaleIdAndUserId(Long flashSaleId, Long userId);

    // Count user purchases for a flash sale
    @Query("SELECT COALESCE(SUM(p.quantity), 0) FROM FlashSalePurchase p " +
           "WHERE p.flashSaleId = :flashSaleId AND p.userId = :userId")
    Integer countUserPurchaseQuantity(@Param("flashSaleId") Long flashSaleId, 
                                      @Param("userId") Long userId);
}
