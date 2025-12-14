package com.ecommerce.order.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "orders", indexes = {
    @Index(name = "idx_user_id", columnList = "user_id"),
    @Index(name = "idx_order_number", columnList = "order_number"),
    @Index(name = "idx_status", columnList = "status"),
    @Index(name = "idx_created_at", columnList = "created_at")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Order {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "order_number", nullable = false, unique = true, length = 50)
    private String orderNumber;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private OrderStatus status;

    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<OrderItem> items = new ArrayList<>();

    @Column(name = "total_amount", nullable = false, precision = 10, scale = 2)
    private BigDecimal totalAmount;

    @Column(name = "payment_id", length = 100)
    private String paymentId;

    @Embedded
    private ShippingAddress shippingAddress;

    @Column(length = 500)
    private String notes;

    // Saga tracking
    @Column(name = "saga_id", length = 100)
    private String sagaId;

    @Enumerated(EnumType.STRING)
    @Column(name = "saga_status", length = 20)
    private SagaStatus sagaStatus;

    @Column(name = "failure_reason", length = 500)
    private String failureReason;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    // Helper methods for order items
    public void addItem(OrderItem item) {
        items.add(item);
        item.setOrder(this);
        recalculateTotal();
    }

    public void removeItem(OrderItem item) {
        items.remove(item);
        item.setOrder(null);
        recalculateTotal();
    }

    public void clearItems() {
        items.forEach(item -> item.setOrder(null));
        items.clear();
        this.totalAmount = BigDecimal.ZERO;
    }

    // Business logic
    public void recalculateTotal() {
        this.totalAmount = items.stream()
                .map(OrderItem::getSubtotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    public boolean canBeCancelled() {
        return status == OrderStatus.PENDING || 
               status == OrderStatus.CONFIRMED;
    }

    public boolean isCompleted() {
        return status == OrderStatus.COMPLETED;
    }

    public boolean isCancelled() {
        return status == OrderStatus.CANCELLED || 
               status == OrderStatus.FAILED;
    }

    // State transitions
    public void confirm() {
        validateStatusTransition(OrderStatus.CONFIRMED);
        this.status = OrderStatus.CONFIRMED;
    }

    public void ship() {
        validateStatusTransition(OrderStatus.SHIPPED);
        this.status = OrderStatus.SHIPPED;
    }

    public void deliver() {
        validateStatusTransition(OrderStatus.DELIVERED);
        this.status = OrderStatus.DELIVERED;
    }

    public void complete() {
        validateStatusTransition(OrderStatus.COMPLETED);
        this.status = OrderStatus.COMPLETED;
    }

    public void cancel(String reason) {
        if (!canBeCancelled()) {
            throw new IllegalStateException("Order cannot be cancelled in status: " + status);
        }
        this.status = OrderStatus.CANCELLED;
        this.failureReason = reason;
    }

    public void fail(String reason) {
        this.status = OrderStatus.FAILED;
        this.failureReason = reason;
    }

    private void validateStatusTransition(OrderStatus newStatus) {
        if (!isValidTransition(this.status, newStatus)) {
            throw new IllegalStateException(
                String.format("Invalid status transition from %s to %s", this.status, newStatus)
            );
        }
    }

    private boolean isValidTransition(OrderStatus from, OrderStatus to) {
        if (from == null) return to == OrderStatus.PENDING;
        
        return switch (from) {
            case PENDING -> to == OrderStatus.CONFIRMED || to == OrderStatus.CANCELLED || to == OrderStatus.FAILED;
            case CONFIRMED -> to == OrderStatus.SHIPPED || to == OrderStatus.CANCELLED;
            case SHIPPED -> to == OrderStatus.DELIVERED;
            case DELIVERED -> to == OrderStatus.COMPLETED;
            default -> false;
        };
    }

    // Order status enum
    public enum OrderStatus {
        PENDING,      // Order created, waiting for payment
        CONFIRMED,    // Payment confirmed, inventory reserved
        SHIPPED,      // Order shipped
        DELIVERED,    // Order delivered
        COMPLETED,    // Order completed
        CANCELLED,    // Order cancelled by user
        FAILED        // Order failed (payment/inventory failed)
    }

    // Saga status enum
    public enum SagaStatus {
        STARTED,           // Saga started
        INVENTORY_RESERVED, // Step 1 complete
        PAYMENT_PROCESSED,  // Step 2 complete
        COMPLETED,          // All steps complete
        COMPENSATING,       // Compensating transactions started
        COMPENSATED,        // All compensations done
        FAILED              // Saga failed
    }
}
