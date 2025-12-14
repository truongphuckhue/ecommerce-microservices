package com.ecommerce.inventory.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "inventory", indexes = {
    @Index(name = "idx_product_id", columnList = "product_id"),
    @Index(name = "idx_sku", columnList = "sku")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Inventory {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "product_id", nullable = false, unique = true)
    private Long productId;

    @Column(nullable = false, unique = true, length = 50)
    private String sku;

    @Column(nullable = false)
    private Integer quantity;

    @Column(name = "reserved_quantity", nullable = false)
    @Builder.Default
    private Integer reservedQuantity = 0;

    @Column(name = "available_quantity", nullable = false)
    @Builder.Default
    private Integer availableQuantity = 0;

    @Column(name = "reorder_point")
    private Integer reorderPoint;

    @Column(name = "reorder_quantity")
    private Integer reorderQuantity;

    @Column(length = 50)
    private String location;

    // CRITICAL: Optimistic Locking
    @Version
    @Column(nullable = false)
    private Long version;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    // Helper methods with validation
    public void addStock(int amount) {
        if (amount < 0) {
            throw new IllegalArgumentException("Amount must be positive");
        }
        this.quantity += amount;
        this.availableQuantity += amount;
    }

    public void removeStock(int amount) {
        if (amount < 0) {
            throw new IllegalArgumentException("Amount must be positive");
        }
        if (this.quantity < amount) {
            throw new IllegalStateException("Insufficient stock");
        }
        this.quantity -= amount;
        this.availableQuantity -= amount;
    }

    public boolean canReserve(int amount) {
        return this.availableQuantity >= amount;
    }

    public void reserve(int amount) {
        if (!canReserve(amount)) {
            throw new IllegalStateException("Insufficient available quantity");
        }
        this.availableQuantity -= amount;
        this.reservedQuantity += amount;
    }

    public void releaseReservation(int amount) {
        if (this.reservedQuantity < amount) {
            throw new IllegalStateException("Invalid release amount");
        }
        this.reservedQuantity -= amount;
        this.availableQuantity += amount;
    }

    public void confirmReservation(int amount) {
        if (this.reservedQuantity < amount) {
            throw new IllegalStateException("Invalid confirm amount");
        }
        this.reservedQuantity -= amount;
        this.quantity -= amount;
    }

    public boolean needsReorder() {
        return this.reorderPoint != null && this.quantity <= this.reorderPoint;
    }

    public void updateAvailableQuantity() {
        this.availableQuantity = this.quantity - this.reservedQuantity;
    }
}
