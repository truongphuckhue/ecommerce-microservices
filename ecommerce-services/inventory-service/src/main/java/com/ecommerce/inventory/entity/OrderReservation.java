package com.ecommerce.inventory.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * Entity to track inventory reservations for orders
 * Replaces in-memory Map for better reliability and scalability
 */
@Entity
@Table(name = "order_reservations", indexes = {
    @Index(name = "idx_order_id", columnList = "order_id"),
    @Index(name = "idx_status", columnList = "status"),
    @Index(name = "idx_created_at", columnList = "created_at")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrderReservation {
    
    @Id
    private Long orderId;
    
    @ElementCollection
    @CollectionTable(
        name = "order_reservation_items",
        joinColumns = @JoinColumn(name = "order_id")
    )
    @MapKeyColumn(name = "product_id")
    @Column(name = "quantity")
    private Map<Long, Integer> reservedItems;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Builder.Default
    private ReservationStatus status = ReservationStatus.PENDING;
    
    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;
    
    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    @Column(name = "expires_at")
    private LocalDateTime expiresAt;
    
    public enum ReservationStatus {
        PENDING,      // Just reserved, waiting for payment
        CONFIRMED,    // Payment succeeded, inventory deducted
        RELEASED,     // Payment failed or order cancelled, inventory restored
        EXPIRED       // Timeout without confirmation
    }
    
    /**
     * Mark reservation as confirmed
     */
    public void confirm() {
        this.status = ReservationStatus.CONFIRMED;
    }
    
    /**
     * Mark reservation as released
     */
    public void release() {
        this.status = ReservationStatus.RELEASED;
    }
    
    /**
     * Mark reservation as expired
     */
    public void expire() {
        this.status = ReservationStatus.EXPIRED;
    }
    
    /**
     * Check if reservation is active (pending)
     */
    public boolean isActive() {
        return this.status == ReservationStatus.PENDING;
    }
    
    /**
     * Check if reservation has expired
     */
    public boolean isExpired() {
        return this.expiresAt != null && LocalDateTime.now().isAfter(this.expiresAt);
    }
}
