package com.ecommerce.payment.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "payments", indexes = {
    @Index(name = "idx_order_id", columnList = "order_id"),
    @Index(name = "idx_user_id", columnList = "user_id"),
    @Index(name = "idx_payment_number", columnList = "payment_number"),
    @Index(name = "idx_status", columnList = "status"),
    @Index(name = "idx_saga_id", columnList = "saga_id")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Payment {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "payment_number", nullable = false, unique = true, length = 50)
    private String paymentNumber;

    @Column(name = "order_id", nullable = false)
    private Long orderId;

    @Column(name = "order_number", nullable = false, length = 50)
    private String orderNumber;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal amount;

    @Column(length = 10)
    @Builder.Default
    private String currency = "USD";

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private PaymentStatus status;

    @Enumerated(EnumType.STRING)
    @Column(name = "payment_method", length = 20)
    private PaymentMethod paymentMethod;

    // Payment gateway details
    @Column(name = "gateway_transaction_id", length = 100)
    private String gatewayTransactionId;

    @Column(name = "gateway_response_code", length = 50)
    private String gatewayResponseCode;

    @Column(name = "gateway_response_message", length = 500)
    private String gatewayResponseMessage;

    // Card details (masked)
    @Column(name = "card_last_four", length = 4)
    private String cardLastFour;

    @Column(name = "card_brand", length = 20)
    private String cardBrand;

    // Saga tracking
    @Column(name = "saga_id", length = 100)
    private String sagaId;

    @Column(name = "failure_reason", length = 500)
    private String failureReason;

    // Refund tracking
    @Column(name = "refunded_amount", precision = 10, scale = 2)
    @Builder.Default
    private BigDecimal refundedAmount = BigDecimal.ZERO;

    @Column(name = "refund_transaction_id", length = 100)
    private String refundTransactionId;

    @Column(length = 500)
    private String notes;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Column(name = "completed_at")
    private LocalDateTime completedAt;

    // Business logic
    public void markAsProcessing() {
        validateStatusTransition(PaymentStatus.PROCESSING);
        this.status = PaymentStatus.PROCESSING;
    }

    public void markAsCompleted(String transactionId) {
        validateStatusTransition(PaymentStatus.COMPLETED);
        this.status = PaymentStatus.COMPLETED;
        this.gatewayTransactionId = transactionId;
        this.completedAt = LocalDateTime.now();
    }

    public void markAsFailed(String reason) {
        validateStatusTransition(PaymentStatus.FAILED);
        this.status = PaymentStatus.FAILED;
        this.failureReason = reason;
    }

    public void markAsRefunded(BigDecimal amount, String refundTransactionId) {
        if (this.status != PaymentStatus.COMPLETED) {
            throw new IllegalStateException("Can only refund completed payments");
        }
        if (amount.compareTo(this.amount.subtract(this.refundedAmount)) > 0) {
            throw new IllegalStateException("Refund amount exceeds available amount");
        }
        this.refundedAmount = this.refundedAmount.add(amount);
        this.refundTransactionId = refundTransactionId;
        if (this.refundedAmount.compareTo(this.amount) >= 0) {
            this.status = PaymentStatus.REFUNDED;
        } else {
            this.status = PaymentStatus.PARTIALLY_REFUNDED;
        }
    }

    public boolean canBeRefunded() {
        return status == PaymentStatus.COMPLETED || 
               status == PaymentStatus.PARTIALLY_REFUNDED;
    }

    public BigDecimal getRemainingRefundableAmount() {
        return amount.subtract(refundedAmount);
    }

    private void validateStatusTransition(PaymentStatus newStatus) {
        if (!isValidTransition(this.status, newStatus)) {
            throw new IllegalStateException(
                String.format("Invalid payment status transition from %s to %s", 
                    this.status, newStatus)
            );
        }
    }

    private boolean isValidTransition(PaymentStatus from, PaymentStatus to) {
        if (from == null) return to == PaymentStatus.PENDING;
        
        return switch (from) {
            case PENDING -> to == PaymentStatus.PROCESSING || to == PaymentStatus.CANCELLED;
            case PROCESSING -> to == PaymentStatus.COMPLETED || to == PaymentStatus.FAILED;
            case COMPLETED -> to == PaymentStatus.PARTIALLY_REFUNDED || to == PaymentStatus.REFUNDED;
            case PARTIALLY_REFUNDED -> to == PaymentStatus.REFUNDED;
            default -> false;
        };
    }

    // Payment Status enum
    public enum PaymentStatus {
        PENDING,              // Payment created, waiting to process
        PROCESSING,           // Payment being processed by gateway
        COMPLETED,            // Payment successful
        FAILED,               // Payment failed
        CANCELLED,            // Payment cancelled
        PARTIALLY_REFUNDED,   // Partial refund issued
        REFUNDED              // Full refund issued
    }

    // Payment Method enum
    public enum PaymentMethod {
        CREDIT_CARD,
        DEBIT_CARD,
        PAYPAL,
        BANK_TRANSFER,
        WALLET
    }
}
