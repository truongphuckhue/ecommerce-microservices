package com.promox.reward.repository;

import com.promox.reward.entity.RewardAccount;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface RewardAccountRepository extends JpaRepository<RewardAccount, Long> {
    Optional<RewardAccount> findByUserId(Long userId);
    boolean existsByUserId(Long userId);
    List<RewardAccount> findByTierLevel(RewardAccount.TierLevel tierLevel);
    
    @Query("SELECT r FROM RewardAccount r WHERE r.availablePoints >= :minPoints ORDER BY r.availablePoints DESC")
    List<RewardAccount> findTopAccounts(@Param("minPoints") Integer minPoints);
    
    @Query("SELECT COALESCE(SUM(r.totalPoints), 0) FROM RewardAccount r")
    Long getTotalPointsIssued();
}
