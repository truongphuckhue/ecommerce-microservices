package com.ecommerce.notification.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "notifications", indexes = {
    @Index(name = "idx_user_id", columnList = "user_id"),
    @Index(name = "idx_type", columnList = "type"),
    @Index(name = "idx_status", columnList = "status"),
    @Index(name = "idx_created_at", columnList = "created_at")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Notification {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "recipient_email", length = 255)
    private String recipientEmail;

    @Column(name = "recipient_phone", length = 20)
    private String recipientPhone;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private NotificationType type;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private NotificationChannel channel;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private NotificationStatus status;

    @Column(nullable = false, length = 200)
    private String subject;

    @Column(columnDefinition = "TEXT")
    private String content;

    // Reference to related entity (order, payment, etc.)
    @Column(name = "reference_type", length = 50)
    private String referenceType;

    @Column(name = "reference_id", length = 100)
    private String referenceId;

    // Template information
    @Column(name = "template_name", length = 100)
    private String templateName;

    // Retry tracking
    @Column(name = "retry_count")
    @Builder.Default
    private Integer retryCount = 0;

    @Column(name = "max_retries")
    @Builder.Default
    private Integer maxRetries = 3;

    @Column(name = "next_retry_at")
    private LocalDateTime nextRetryAt;

    // Error tracking
    @Column(name = "error_message", length = 500)
    private String errorMessage;

    @Column(name = "sent_at")
    private LocalDateTime sentAt;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    // Business logic
    public void markAsSent() {
        this.status = NotificationStatus.SENT;
        this.sentAt = LocalDateTime.now();
    }

    public void markAsFailed(String errorMessage) {
        this.status = NotificationStatus.FAILED;
        this.errorMessage = errorMessage;
    }

    public void incrementRetry() {
        this.retryCount++;
        if (this.retryCount >= this.maxRetries) {
            this.status = NotificationStatus.FAILED;
        } else {
            this.status = NotificationStatus.RETRY;
            // Exponential backoff: 1min, 2min, 4min
            this.nextRetryAt = LocalDateTime.now().plusMinutes((long) Math.pow(2, retryCount - 1));
        }
    }

    public boolean canRetry() {
        return retryCount < maxRetries && 
               (status == NotificationStatus.RETRY || status == NotificationStatus.FAILED);
    }

    // Notification Type enum
    public enum NotificationType {
        ORDER_CREATED,          // Order created successfully
        ORDER_CONFIRMED,        // Payment confirmed, order confirmed
        ORDER_SHIPPED,          // Order shipped
        ORDER_DELIVERED,        // Order delivered
        ORDER_CANCELLED,        // Order cancelled
        ORDER_FAILED,           // Order creation failed
        PAYMENT_SUCCESSFUL,     // Payment successful
        PAYMENT_FAILED,         // Payment failed
        PAYMENT_REFUNDED,       // Payment refunded
        INVENTORY_LOW_STOCK,    // Low stock alert
        WELCOME,                // Welcome email
        PASSWORD_RESET,         // Password reset
        ACCOUNT_VERIFICATION    // Account verification
    }

    // Notification Channel enum
    public enum NotificationChannel {
        EMAIL,
        SMS,
        PUSH,
        IN_APP
    }

    // Notification Status enum
    public enum NotificationStatus {
        PENDING,    // Created, waiting to send
        SENT,       // Successfully sent
        FAILED,     // Failed after all retries
        RETRY       // Failed, will retry
    }
}
