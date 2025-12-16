package com.promox.reward.entity;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Entity
@Table(name = "user_achievements", indexes = {
    @Index(name = "idx_user_id", columnList = "user_id"),
    @Index(name = "idx_achievement_id", columnList = "achievement_id")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EntityListeners(AuditingEntityListener.class)
public class UserAchievement {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "achievement_id", nullable = false)
    private Long achievementId;

    @Column(name = "achievement_code", length = 100)
    private String achievementCode;

    @Column(nullable = false)
    @Builder.Default
    private Integer progress = 0;

    @Column(name = "required_count")
    private Integer requiredCount;

    @Column(name = "is_completed", nullable = false)
    @Builder.Default
    private Boolean isCompleted = false;

    @Column(name = "points_earned")
    private Integer pointsEarned;

    @CreatedDate
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "completed_at")
    private LocalDateTime completedAt;

    // Business methods
    public void incrementProgress(int amount) {
        this.progress += amount;
        if (this.requiredCount != null && this.progress >= this.requiredCount) {
            this.isCompleted = true;
            this.completedAt = LocalDateTime.now();
        }
    }

    public Integer getRemainingProgress() {
        if (requiredCount == null) return null;
        return Math.max(0, requiredCount - progress);
    }

    public Integer getProgressPercentage() {
        if (requiredCount == null || requiredCount == 0) return 0;
        return Math.min(100, (progress * 100) / requiredCount);
    }
}
