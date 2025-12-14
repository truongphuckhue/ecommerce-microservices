package com.ecommerce.payment.repository;

import com.ecommerce.payment.entity.Payment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface PaymentRepository extends JpaRepository<Payment, Long> {
    
    Optional<Payment> findByPaymentNumber(String paymentNumber);
    
    Optional<Payment> findByOrderNumber(String orderNumber);
    
    Optional<Payment> findBySagaId(String sagaId);
    
    boolean existsBySagaId(String sagaId);
    
    boolean existsByOrderNumber(String orderNumber);
    
    // Find by user
    List<Payment> findByUserIdOrderByCreatedAtDesc(Long userId);
    
    // Find by status
    List<Payment> findByStatus(Payment.PaymentStatus status);
    
    List<Payment> findByUserIdAndStatus(Long userId, Payment.PaymentStatus status);
    
    // Find by order ID
    Optional<Payment> findByOrderId(Long orderId);
    
    // Analytics queries
    @Query("SELECT SUM(p.amount) FROM Payment p " +
           "WHERE p.status = 'COMPLETED' AND p.createdAt BETWEEN :start AND :end")
    BigDecimal getTotalRevenue(@Param("start") LocalDateTime start, @Param("end") LocalDateTime end);
    
    @Query("SELECT COUNT(p) FROM Payment p " +
           "WHERE p.status = :status AND p.createdAt >= :since")
    long countByStatusSince(@Param("status") Payment.PaymentStatus status, @Param("since") LocalDateTime since);
    
    // Find failed payments for monitoring
    @Query("SELECT p FROM Payment p " +
           "WHERE p.status = 'FAILED' AND p.createdAt BETWEEN :start AND :end " +
           "ORDER BY p.createdAt DESC")
    List<Payment> findFailedPayments(
            @Param("start") LocalDateTime start, 
            @Param("end") LocalDateTime end
    );
    
    // Find refunded payments
    @Query("SELECT p FROM Payment p " +
           "WHERE p.status IN ('REFUNDED', 'PARTIALLY_REFUNDED') " +
           "AND p.createdAt >= :since " +
           "ORDER BY p.updatedAt DESC")
    List<Payment> findRefundedPayments(@Param("since") LocalDateTime since);
}
