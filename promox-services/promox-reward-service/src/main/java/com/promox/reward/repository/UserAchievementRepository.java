package com.promox.reward.repository;

import com.promox.reward.entity.UserAchievement;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface UserAchievementRepository extends JpaRepository<UserAchievement, Long> {
    List<UserAchievement> findByUserId(Long userId);
    List<UserAchievement> findByUserIdAndIsCompletedTrue(Long userId);
    Optional<UserAchievement> findByUserIdAndAchievementId(Long userId, Long achievementId);
    boolean existsByUserIdAndAchievementId(Long userId, Long achievementId);
}
