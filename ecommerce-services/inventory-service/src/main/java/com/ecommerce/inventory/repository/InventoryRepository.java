package com.ecommerce.inventory.repository;

import com.ecommerce.inventory.entity.Inventory;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface InventoryRepository extends JpaRepository<Inventory, Long> {
    
    Optional<Inventory> findByProductId(Long productId);
    
    Optional<Inventory> findBySku(String sku);
    
    boolean existsByProductId(Long productId);
    
    boolean existsBySku(String sku);

    // CRITICAL: Pessimistic Locking for write operations
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT i FROM Inventory i WHERE i.productId = :productId")
    Optional<Inventory> findByProductIdWithLock(@Param("productId") Long productId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT i FROM Inventory i WHERE i.sku = :sku")
    Optional<Inventory> findBySkuWithLock(@Param("sku") String sku);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT i FROM Inventory i WHERE i.id = :id")
    Optional<Inventory> findByIdWithLock(@Param("id") Long id);

    // Optimistic Locking approach - no explicit lock
    @Query("SELECT i FROM Inventory i WHERE i.productId = :productId")
    Optional<Inventory> findByProductIdForUpdate(@Param("productId") Long productId);

    // Batch operations
    @Query("SELECT i FROM Inventory i WHERE i.productId IN :productIds")
    List<Inventory> findByProductIdIn(@Param("productIds") List<Long> productIds);

    // Low stock alert
    @Query("SELECT i FROM Inventory i WHERE i.quantity <= i.reorderPoint AND i.reorderPoint IS NOT NULL")
    List<Inventory> findLowStockItems();

    // Out of stock
    @Query("SELECT i FROM Inventory i WHERE i.availableQuantity <= 0")
    List<Inventory> findOutOfStockItems();

    // Available stock check without locking (for reads)
    @Query("SELECT CASE WHEN i.availableQuantity >= :quantity THEN true ELSE false END " +
           "FROM Inventory i WHERE i.productId = :productId")
    boolean hasAvailableStock(@Param("productId") Long productId, @Param("quantity") Integer quantity);

    // Update available quantity (for batch operations)
    @Modifying
    @Query("UPDATE Inventory i SET i.availableQuantity = i.quantity - i.reservedQuantity WHERE i.id = :id")
    void updateAvailableQuantity(@Param("id") Long id);
}
