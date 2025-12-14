package com.ecommerce.inventory.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "inventory_transactions", indexes = {
    @Index(name = "idx_inventory_id", columnList = "inventory_id"),
    @Index(name = "idx_reference_id", columnList = "reference_id"),
    @Index(name = "idx_created_at", columnList = "created_at")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class InventoryTransaction {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "inventory_id", nullable = false)
    private Inventory inventory;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private TransactionType type;

    @Column(nullable = false)
    private Integer quantity;

    @Column(name = "quantity_before", nullable = false)
    private Integer quantityBefore;

    @Column(name = "quantity_after", nullable = false)
    private Integer quantityAfter;

    @Column(name = "reference_id", length = 100)
    private String referenceId; // Order ID, Purchase ID, etc.

    @Column(name = "reference_type", length = 50)
    private String referenceType; // ORDER, PURCHASE, ADJUSTMENT, etc.

    @Column(length = 500)
    private String notes;

    @Column(name = "performed_by", length = 100)
    private String performedBy;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    public enum TransactionType {
        STOCK_IN,           // Adding stock
        STOCK_OUT,          // Removing stock
        RESERVATION,        // Reserve for order
        RELEASE_RESERVATION, // Cancel reservation
        CONFIRM_RESERVATION, // Confirm and deduct
        ADJUSTMENT,         // Manual adjustment
        REORDER            // Automatic reorder
    }
}
