package com.ecommerce.inventory.repository;

import com.ecommerce.inventory.entity.OrderReservation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface OrderReservationRepository extends JpaRepository<OrderReservation, Long> {
    
    /**
     * Find reservations by status
     */
    List<OrderReservation> findByStatus(OrderReservation.ReservationStatus status);
    
    /**
     * Find active (pending) reservations
     */
    @Query("SELECT r FROM OrderReservation r WHERE r.status = 'PENDING'")
    List<OrderReservation> findActiveReservations();
    
    /**
     * Find expired reservations that need cleanup
     */
    @Query("SELECT r FROM OrderReservation r WHERE r.status = 'PENDING' AND r.expiresAt < :now")
    List<OrderReservation> findExpiredReservations(@Param("now") LocalDateTime now);
    
    /**
     * Find reservations created within a date range
     */
    @Query("SELECT r FROM OrderReservation r WHERE r.createdAt BETWEEN :startDate AND :endDate")
    List<OrderReservation> findByDateRange(
        @Param("startDate") LocalDateTime startDate,
        @Param("endDate") LocalDateTime endDate
    );
    
    /**
     * Count reservations by status
     */
    long countByStatus(OrderReservation.ReservationStatus status);
}
