package com.promox.reward.repository;

import com.promox.reward.entity.PointTransaction;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface PointTransactionRepository extends JpaRepository<PointTransaction, Long> {
    List<PointTransaction> findByUserId(Long userId);
    Page<PointTransaction> findByUserId(Long userId, Pageable pageable);
    List<PointTransaction> findByUserIdAndTransactionType(Long userId, PointTransaction.TransactionType type);
    
    @Query("SELECT t FROM PointTransaction t WHERE t.status = 'PENDING' AND t.expiresAt < :now")
    List<PointTransaction> findExpiredPendingTransactions(@Param("now") LocalDateTime now);
    
    @Query("SELECT COALESCE(SUM(t.points), 0) FROM PointTransaction t WHERE t.userId = :userId AND t.transactionType = :type AND t.status = 'CONFIRMED'")
    Integer sumPointsByUserAndType(@Param("userId") Long userId, @Param("type") PointTransaction.TransactionType type);
}
