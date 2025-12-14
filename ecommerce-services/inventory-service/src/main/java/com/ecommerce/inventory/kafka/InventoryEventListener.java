package com.ecommerce.inventory.kafka;

import com.ecommerce.inventory.dto.OrderCreatedEvent;
import com.ecommerce.inventory.dto.PaymentFailedEvent;
import com.ecommerce.inventory.dto.ReservationRequest;
import com.ecommerce.inventory.entity.OrderReservation;
import com.ecommerce.inventory.repository.OrderReservationRepository;
import com.ecommerce.inventory.service.OptimisticLockingInventoryService;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * Kafka Event Listener for Inventory Service
 * Implements Saga Pattern for distributed transactions
 * 
 * Event Flow:
 * 1. ORDER CREATED → Reserve inventory
 * 2. PAYMENT FAILED → Release inventory (compensation)
 * 3. PAYMENT SUCCEEDED → Confirm reservation
 * 4. ORDER CANCELLED → Release inventory
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class InventoryEventListener {
    
    private final OptimisticLockingInventoryService inventoryService;
    private final InventoryEventProducer eventProducer;
    private final ObjectMapper objectMapper;
    private final OrderReservationRepository orderReservationRepository;    
    /**
     * Listen to order-created events and reserve inventory
     * This is Step 2 in the Saga pattern (after Order Service creates order)
     */
    @KafkaListener(
        topics = "order-created",
        groupId = "inventory-service-group",
        containerFactory = "kafkaListenerContainerFactory"
    )
    @Transactional
    public void handleOrderCreated(String message, Acknowledgment acknowledgment) {
        try {
            log.info("📥 [KAFKA] Received order-created event");
            log.debug("Event payload: {}", message);
            
            // Deserialize event
            OrderCreatedEvent event = objectMapper.readValue(message, OrderCreatedEvent.class);
            Long orderId = event.getOrderId();
            
            log.info("🔄 [SAGA] Processing order creation for order ID: {}", orderId);
            
            // Prepare reservation request
            Map<Long, Integer> itemsToReserve = new HashMap<>();
            for (OrderCreatedEvent.OrderItemDto item : event.getItems()) {
                itemsToReserve.put(item.getProductId(), item.getQuantity());
                log.debug("  - Product {}: {} units", item.getProductId(), item.getQuantity());
            }
            
            // Attempt to reserve inventory for all items
            log.info("🔄 [INVENTORY] Attempting to reserve inventory for order: {}", orderId);
            boolean allReserved = reserveInventoryForOrder(orderId, itemsToReserve);
            
            if (allReserved) {
                // SUCCESS: All items reserved
                log.info("✅ [INVENTORY] Successfully reserved all items for order: {}", orderId);
                
                // Store reservation in database for potential compensation
                OrderReservation reservation = OrderReservation.builder()
                        .orderId(orderId)
                        .reservedItems(itemsToReserve)
                        .status(OrderReservation.ReservationStatus.PENDING)
                        .expiresAt(LocalDateTime.now().plusMinutes(30)) // 30 minutes expiry
                        .build();
                orderReservationRepository.save(reservation);
                
                // Publish success event → Payment Service will process next
                eventProducer.sendInventoryReservedEvent(orderId, itemsToReserve);
                log.info("✅ [SAGA] Inventory reservation completed. Next: Payment processing");
                
            } else {
                // FAILURE: Insufficient stock
                log.warn("⚠️ [INVENTORY] Failed to reserve inventory for order: {}", orderId);
                log.warn("⚠️ [SAGA] Saga will be compensated (order will be cancelled)");
                
                // Publish failure event → Order Service will cancel order
                eventProducer.sendInventoryReservationFailedEvent(
                    orderId, 
                    "Insufficient stock for one or more items"
                );
            }
            
            // Manual commit after successful processing
            acknowledgment.acknowledge();
            log.debug("✓ [KAFKA] Message acknowledged");
            
        } catch (Exception e) {
            log.error("❌ [KAFKA] Error processing order-created event", e);
            // Don't acknowledge - message will be reprocessed
            // In production, implement DLQ (Dead Letter Queue) after max retries
        }
    }
    
    /**
     * Listen to payment-failed events and release reserved inventory
     * This is the COMPENSATION step in the Saga pattern
     */
    @KafkaListener(
        topics = "payment-failed",
        groupId = "inventory-service-group",
        containerFactory = "kafkaListenerContainerFactory"
    )
    @Transactional
    public void handlePaymentFailed(String message, Acknowledgment acknowledgment) {
        try {
            log.info("📥 [KAFKA] Received payment-failed event");
            log.debug("Event payload: {}", message);
            
            // Deserialize event
            PaymentFailedEvent event = objectMapper.readValue(message, PaymentFailedEvent.class);
            Long orderId = event.getOrderId();
            
            log.warn("🔄 [SAGA] COMPENSATION: Payment failed for order: {}", orderId);
            log.warn("Reason: {}", event.getReason());
            
            // Get the reservation from database
            OrderReservation reservation = orderReservationRepository.findById(orderId).orElse(null);
            
            if (reservation != null && reservation.isActive()) {
                Map<Long, Integer> reservedItems = reservation.getReservedItems();
                log.info("🔄 [INVENTORY] Releasing reserved inventory for order: {}", orderId);
                
                // Release the reserved inventory (compensation)
                boolean released = releaseReservationForOrder(orderId, reservedItems);
                
                if (released) {
                    log.info("✅ [INVENTORY] Successfully released inventory for order: {}", orderId);
                    
                    // Update reservation status in database
                    reservation.release();
                    orderReservationRepository.save(reservation);
                    
                    // Publish inventory released event
                    eventProducer.sendInventoryReleasedEvent(orderId, reservedItems);
                    log.info("✅ [SAGA] Compensation completed successfully");
                    
                } else {
                    log.error("❌ [INVENTORY] Failed to release inventory for order: {}", orderId);
                    log.error("❌ [SAGA] Manual intervention may be required!");
                }
                
            } else {
                log.warn("⚠️ [INVENTORY] No reservation found for order: {}", orderId);
                log.warn("Possibly already released or never reserved");
            }
            
            // Manual commit
            acknowledgment.acknowledge();
            log.debug("✓ [KAFKA] Message acknowledged");
            
        } catch (Exception e) {
            log.error("❌ [KAFKA] Error processing payment-failed event", e);
            // Don't acknowledge - will be reprocessed
        }
    }
    
    /**
     * Listen to payment-completed events and confirm reservation
     * This finalizes the inventory reduction
     */
    @KafkaListener(
        topics = "payment-completed",
        groupId = "inventory-service-group",
        containerFactory = "kafkaListenerContainerFactory"
    )
    @Transactional
    public void handlePaymentCompleted(String message, Acknowledgment acknowledgment) {
        try {
            log.info("📥 [KAFKA] Received payment-completed event");
            
            // Parse orderId from message
            Map<String, Object> eventData = objectMapper.readValue(message, Map.class);
            Long orderId = Long.valueOf(eventData.get("orderId").toString());
            
            log.info("🔄 [SAGA] Payment completed for order: {}", orderId);
            
            // Get the reservation from database
            OrderReservation reservation = orderReservationRepository.findById(orderId).orElse(null);
            
            if (reservation != null && reservation.isActive()) {
                Map<Long, Integer> reservedItems = reservation.getReservedItems();
                log.info("🔄 [INVENTORY] Confirming reservation for order: {}", orderId);
                
                // Confirm reservation (deduct from total stock)
                boolean confirmed = confirmReservationForOrder(orderId, reservedItems);
                
                if (confirmed) {
                    log.info("✅ [INVENTORY] Reservation confirmed for order: {}", orderId);
                    
                    // Update reservation status in database
                    reservation.confirm();
                    orderReservationRepository.save(reservation);
                    
                    // Publish confirmation event
                    eventProducer.sendInventoryConfirmedEvent(orderId, reservedItems);
                    log.info("✅ [SAGA] Order fulfillment completed successfully");
                } else {
                    log.error("❌ [INVENTORY] Failed to confirm reservation for order: {}", orderId);
                }
            }
            
            acknowledgment.acknowledge();
            
        } catch (Exception e) {
            log.error("❌ [KAFKA] Error processing payment-completed event", e);
        }
    }
    
    /**
     * Listen to order-cancelled events and release reserved inventory
     */
    @KafkaListener(
        topics = "order-cancelled",
        groupId = "inventory-service-group",
        containerFactory = "kafkaListenerContainerFactory"
    )
    @Transactional
    public void handleOrderCancelled(String message, Acknowledgment acknowledgment) {
        try {
            log.info("📥 [KAFKA] Received order-cancelled event");
            
            // Parse orderId
            Map<String, Object> eventData = objectMapper.readValue(message, Map.class);
            Long orderId = Long.valueOf(eventData.get("orderId").toString());
            
            log.info("🔄 [SAGA] Order cancelled: {}", orderId);
            
            // Get reservation from database
            OrderReservation reservation = orderReservationRepository.findById(orderId).orElse(null);
            
            if (reservation != null && reservation.isActive()) {
                Map<Long, Integer> reservedItems = reservation.getReservedItems();
                log.info("🔄 [INVENTORY] Releasing inventory for cancelled order: {}", orderId);
                
                boolean released = releaseReservationForOrder(orderId, reservedItems);
                
                if (released) {
                    log.info("✅ [INVENTORY] Inventory released for cancelled order: {}", orderId);
                    
                    // Update reservation status
                    reservation.release();
                    orderReservationRepository.save(reservation);
                    
                    eventProducer.sendInventoryReleasedEvent(orderId, reservedItems);
                }
            }
            
            acknowledgment.acknowledge();
            
        } catch (Exception e) {
            log.error("❌ [KAFKA] Error processing order-cancelled event", e);
        }
    }
    
    // ==================== HELPER METHODS ====================
    
    /**
     * Reserve inventory for all items in an order
     * Returns true if ALL items successfully reserved, false otherwise
     */
    private boolean reserveInventoryForOrder(Long orderId, Map<Long, Integer> items) {
        try {
            for (Map.Entry<Long, Integer> entry : items.entrySet()) {
                Long productId = entry.getKey();
                Integer quantity = entry.getValue();
                
                log.debug("Reserving {} units of product {}", quantity, productId);
                
                // Use Builder pattern with correct types
                ReservationRequest request = ReservationRequest.builder()
                        .quantity(quantity)
                        .referenceId(orderId.toString())  // Convert Long to String
                        .referenceType("ORDER")
                        .build();
                
                inventoryService.reserveStock(productId, request);
            }
            return true;
            
        } catch (Exception e) {
            log.error("Failed to reserve inventory for order {}: {}", orderId, e.getMessage());
            
            // Rollback any partial reservations
            rollbackPartialReservations(orderId, items);
            
            return false;
        }
    }
    
    /**
     * Release reservation for all items in an order (compensation)
     */
    private boolean releaseReservationForOrder(Long orderId, Map<Long, Integer> items) {
        try {
            for (Map.Entry<Long, Integer> entry : items.entrySet()) {
                Long productId = entry.getKey();
                Integer quantity = entry.getValue();
                
                log.debug("Releasing {} units of product {}", quantity, productId);
                
                ReservationRequest request = ReservationRequest.builder()
                        .quantity(quantity)
                        .referenceId(orderId.toString())
                        .referenceType("ORDER")
                        .build();
                
                inventoryService.releaseReservation(productId, request);
            }
            return true;
            
        } catch (Exception e) {
            log.error("Failed to release reservation for order {}: {}", orderId, e.getMessage());
            return false;
        }
    }
    
    /**
     * Confirm reservation (deduct from total stock)
     */
    private boolean confirmReservationForOrder(Long orderId, Map<Long, Integer> items) {
        try {
            for (Map.Entry<Long, Integer> entry : items.entrySet()) {
                Long productId = entry.getKey();
                Integer quantity = entry.getValue();
                
                log.debug("Confirming {} units of product {}", quantity, productId);
                
                ReservationRequest request = ReservationRequest.builder()
                        .quantity(quantity)
                        .referenceId(orderId.toString())
                        .referenceType("ORDER")
                        .build();
                
                inventoryService.confirmReservation(productId, request);
            }
            return true;
            
        } catch (Exception e) {
            log.error("Failed to confirm reservation for order {}: {}", orderId, e.getMessage());
            return false;
        }
    }
    
    /**
     * Rollback partial reservations if any item fails
     */
    private void rollbackPartialReservations(Long orderId, Map<Long, Integer> items) {
        log.warn("🔄 Rolling back partial reservations for order: {}", orderId);
        
        for (Map.Entry<Long, Integer> entry : items.entrySet()) {
            try {
                Long productId = entry.getKey();
                Integer quantity = entry.getValue();
                
                ReservationRequest request = ReservationRequest.builder()
                        .quantity(quantity)
                        .referenceId(orderId.toString())
                        .referenceType("ORDER")
                        .build();
                
                inventoryService.releaseReservation(productId, request);
                
                log.debug("✓ Rolled back reservation for product {}", productId);
                
            } catch (Exception e) {
                // Log but continue - some items may not have been reserved
                log.debug("Could not rollback product {} (may not have been reserved): {}", 
                    entry.getKey(), e.getMessage());
            }
        }
    }
}
