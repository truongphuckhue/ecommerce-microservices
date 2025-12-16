package com.promox.reward.entity;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Entity
@Table(name = "achievements")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EntityListeners(AuditingEntityListener.class)
public class Achievement {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false, length = 100)
    private String code;

    @Column(nullable = false, length = 200)
    private String name;

    @Column(columnDefinition = "text")
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(name = "achievement_type", nullable = false, length = 30)
    private AchievementType achievementType;

    @Column(name = "icon_url", length = 500)
    private String iconUrl;

    @Column(name = "points_reward", nullable = false)
    @Builder.Default
    private Integer pointsReward = 0;

    @Column(name = "required_count")
    private Integer requiredCount;

    @Column(nullable = false)
    @Builder.Default
    private Boolean active = true;

    @CreatedDate
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    // Enums
    public enum AchievementType {
        FIRST_PURCHASE,         // First order
        PURCHASE_MILESTONE,     // X orders completed
        SPENDING_MILESTONE,     // $X spent
        REVIEW_MILESTONE,       // X reviews written
        REFERRAL_MILESTONE,     // X referrals
        LOYALTY_MILESTONE,      // X years member
        SOCIAL_SHARE,          // Share on social media
        COMPLETE_PROFILE,      // Profile 100% complete
        BIRTHDAY_MONTH         // Birthday month bonus
    }
}
