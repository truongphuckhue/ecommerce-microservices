package com.ecommerce.order.repository;

import com.ecommerce.order.entity.Order;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {
    
    Optional<Order> findByOrderNumber(String orderNumber);
    
    boolean existsByOrderNumber(String orderNumber);
    
    // Find by user
    Page<Order> findByUserId(Long userId, Pageable pageable);
    
    List<Order> findByUserIdOrderByCreatedAtDesc(Long userId);
    
    // Find by status
    Page<Order> findByStatus(Order.OrderStatus status, Pageable pageable);
    
    // Find by user and status
    Page<Order> findByUserIdAndStatus(Long userId, Order.OrderStatus status, Pageable pageable);
    
    // Find by saga status (for monitoring)
    @Query("SELECT o FROM Order o WHERE o.sagaStatus = :sagaStatus")
    List<Order> findBySagaStatus(@Param("sagaStatus") Order.SagaStatus sagaStatus);
    
    // Find stuck sagas (started more than X hours ago but not completed)
    @Query("SELECT o FROM Order o WHERE o.sagaStatus = :sagaStatus " +
           "AND o.createdAt < :threshold")
    List<Order> findStuckSagas(
        @Param("sagaStatus") Order.SagaStatus sagaStatus,
        @Param("threshold") LocalDateTime threshold
    );
    
    // Find failed orders
    @Query("SELECT o FROM Order o WHERE o.status IN ('FAILED', 'CANCELLED') " +
           "AND o.createdAt BETWEEN :startDate AND :endDate")
    List<Order> findFailedOrders(
        @Param("startDate") LocalDateTime startDate,
        @Param("endDate") LocalDateTime endDate
    );
    
    // Count orders by status
    long countByStatus(Order.OrderStatus status);
    
    long countByUserIdAndStatus(Long userId, Order.OrderStatus status);
    
    // Find recent orders
    @Query("SELECT o FROM Order o WHERE o.createdAt >= :since ORDER BY o.createdAt DESC")
    List<Order> findRecentOrders(@Param("since") LocalDateTime since);
    
    // Find by payment ID
    Optional<Order> findByPaymentId(String paymentId);
    
    // Find by saga ID (for idempotency check)
    Optional<Order> findBySagaId(String sagaId);
}
