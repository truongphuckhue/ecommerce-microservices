package com.ecommerce.notification.repository;

import com.ecommerce.notification.entity.Notification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface NotificationRepository extends JpaRepository<Notification, Long> {
    
    List<Notification> findByUserIdOrderByCreatedAtDesc(Long userId);
    
    List<Notification> findByStatus(Notification.NotificationStatus status);
    
    List<Notification> findByType(Notification.NotificationType type);
    
    List<Notification> findByUserIdAndType(Long userId, Notification.NotificationType type);
    
    List<Notification> findByUserIdAndStatus(Long userId, Notification.NotificationStatus status);
    
    // Find notifications that need retry
    @Query("SELECT n FROM Notification n WHERE n.status = 'RETRY' " +
           "AND n.nextRetryAt <= :now AND n.retryCount < n.maxRetries")
    List<Notification> findPendingRetries(@Param("now") LocalDateTime now);
    
    // Count by status
    long countByStatus(Notification.NotificationStatus status);
    
    long countByType(Notification.NotificationType type);
    
    // Find by reference
    List<Notification> findByReferenceTypeAndReferenceId(String referenceType, String referenceId);
    
    // Analytics
    @Query("SELECT n.type, COUNT(n) FROM Notification n " +
           "WHERE n.createdAt >= :since GROUP BY n.type")
    List<Object[]> countByTypeSince(@Param("since") LocalDateTime since);
    
    @Query("SELECT n.status, COUNT(n) FROM Notification n " +
           "WHERE n.createdAt >= :since GROUP BY n.status")
    List<Object[]> countByStatusSince(@Param("since") LocalDateTime since);
}
