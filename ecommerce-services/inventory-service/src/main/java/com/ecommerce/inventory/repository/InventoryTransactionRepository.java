package com.ecommerce.inventory.repository;

import com.ecommerce.inventory.entity.InventoryTransaction;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface InventoryTransactionRepository extends JpaRepository<InventoryTransaction, Long> {
    
    Page<InventoryTransaction> findByInventoryId(Long inventoryId, Pageable pageable);
    
    List<InventoryTransaction> findByReferenceId(String referenceId);
    
    @Query("SELECT t FROM InventoryTransaction t WHERE t.inventory.productId = :productId ORDER BY t.createdAt DESC")
    Page<InventoryTransaction> findByProductId(@Param("productId") Long productId, Pageable pageable);

    @Query("SELECT t FROM InventoryTransaction t WHERE t.type = :type AND t.createdAt BETWEEN :startDate AND :endDate")
    List<InventoryTransaction> findByTypeAndDateRange(
        @Param("type") InventoryTransaction.TransactionType type,
        @Param("startDate") LocalDateTime startDate,
        @Param("endDate") LocalDateTime endDate
    );

    @Query("SELECT SUM(t.quantity) FROM InventoryTransaction t WHERE t.inventory.id = :inventoryId AND t.type = :type")
    Integer sumQuantityByInventoryAndType(
        @Param("inventoryId") Long inventoryId,
        @Param("type") InventoryTransaction.TransactionType type
    );
}
