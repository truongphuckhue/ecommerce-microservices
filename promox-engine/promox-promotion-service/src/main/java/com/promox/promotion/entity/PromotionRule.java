package com.promox.promotion.entity;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Entity
@Table(name = "promotion_rules")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EntityListeners(AuditingEntityListener.class)
public class PromotionRule {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "promotion_id", nullable = false)
    private Long promotionId;

    @Enumerated(EnumType.STRING)
    @Column(name = "rule_type", nullable = false, length = 50)
    private RuleType ruleType;

    @Column(name = "rule_config", nullable = false, columnDefinition = "jsonb")
    private String ruleConfig;

    @Column
    @Builder.Default
    private Integer priority = 0;

    @Column(name = "is_active")
    @Builder.Default
    private Boolean isActive = true;

    @CreatedDate
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    // Enums
    public enum RuleType {
        MIN_ORDER_VALUE,      // Minimum order value
        PRODUCT_CATEGORY,     // Specific product categories
        USER_SEGMENT,         // User segment (VIP, NEW, etc.)
        TIME_WINDOW,          // Time-based (happy hour)
        FIRST_ORDER,          // First-time purchase
        LOCATION,             // Geographic location
        PAYMENT_METHOD,       // Specific payment method
        QUANTITY_BASED        // Buy X items
    }
}
