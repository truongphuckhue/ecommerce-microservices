package com.ecommerce.inventory.kafka;

import com.ecommerce.inventory.dto.InventoryReservedEvent;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

/**
 * Kafka Event Producer for Inventory Service
 * Publishes events to notify other services about inventory changes
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class InventoryEventProducer {
    
    private final KafkaTemplate<String, String> kafkaTemplate;
    private final ObjectMapper objectMapper;
    
    private static final String INVENTORY_RESERVED_TOPIC = "inventory-reserved";
    private static final String INVENTORY_RESERVATION_FAILED_TOPIC = "inventory-reservation-failed";
    private static final String INVENTORY_RELEASED_TOPIC = "inventory-released";
    
    /**
     * Send inventory reserved success event
     * This notifies Order/Payment services that stock has been reserved
     */
    public void sendInventoryReservedEvent(Long orderId, Map<Long, Integer> reservedItems) {
        try {
            InventoryReservedEvent event = InventoryReservedEvent.builder()
                    .orderId(orderId)
                    .reservedItems(reservedItems)
                    .success(true)
                    .message("Inventory reserved successfully")
                    .timestamp(LocalDateTime.now())
                    .build();
            
            String eventJson = objectMapper.writeValueAsString(event);
            
            CompletableFuture<SendResult<String, String>> future = 
                kafkaTemplate.send(INVENTORY_RESERVED_TOPIC, orderId.toString(), eventJson);
            
            future.whenComplete((result, ex) -> {
                if (ex == null) {
                    log.info("✅ [KAFKA] Inventory reserved event sent successfully for order: {}", orderId);
                    log.debug("Event payload: {}", eventJson);
                    log.debug("Partition: {}, Offset: {}", 
                        result.getRecordMetadata().partition(),
                        result.getRecordMetadata().offset());
                } else {
                    log.error("❌ [KAFKA] Failed to send inventory reserved event for order: {}", orderId, ex);
                }
            });
            
        } catch (JsonProcessingException e) {
            log.error("❌ [KAFKA] Error serializing inventory reserved event", e);
        }
    }
    
    /**
     * Send inventory reservation failed event
     * This notifies Order service that reservation failed (e.g., insufficient stock)
     */
    public void sendInventoryReservationFailedEvent(Long orderId, String reason) {
        try {
            InventoryReservedEvent event = InventoryReservedEvent.builder()
                    .orderId(orderId)
                    .success(false)
                    .message(reason)
                    .timestamp(LocalDateTime.now())
                    .build();
            
            String eventJson = objectMapper.writeValueAsString(event);
            
            CompletableFuture<SendResult<String, String>> future = 
                kafkaTemplate.send(INVENTORY_RESERVATION_FAILED_TOPIC, orderId.toString(), eventJson);
            
            future.whenComplete((result, ex) -> {
                if (ex == null) {
                    log.warn("⚠️ [KAFKA] Inventory reservation failed event sent for order: {}", orderId);
                    log.debug("Reason: {}", reason);
                } else {
                    log.error("❌ [KAFKA] Failed to send inventory reservation failed event for order: {}", orderId, ex);
                }
            });
            
        } catch (JsonProcessingException e) {
            log.error("❌ [KAFKA] Error serializing inventory reservation failed event", e);
        }
    }
    
    /**
     * Send inventory released event (compensation after payment failure)
     * This is part of the Saga pattern compensation
     */
    public void sendInventoryReleasedEvent(Long orderId, Map<Long, Integer> releasedItems) {
        try {
            InventoryReservedEvent event = InventoryReservedEvent.builder()
                    .orderId(orderId)
                    .reservedItems(releasedItems)
                    .success(true)
                    .message("Inventory released due to payment failure (Saga compensation)")
                    .timestamp(LocalDateTime.now())
                    .build();
            
            String eventJson = objectMapper.writeValueAsString(event);
            
            CompletableFuture<SendResult<String, String>> future = 
                kafkaTemplate.send(INVENTORY_RELEASED_TOPIC, orderId.toString(), eventJson);
            
            future.whenComplete((result, ex) -> {
                if (ex == null) {
                    log.info("🔄 [KAFKA] Inventory released event sent (compensation) for order: {}", orderId);
                    log.debug("Released items: {}", releasedItems);
                } else {
                    log.error("❌ [KAFKA] Failed to send inventory released event for order: {}", orderId, ex);
                }
            });
            
        } catch (JsonProcessingException e) {
            log.error("❌ [KAFKA] Error serializing inventory released event", e);
        }
    }
    
    /**
     * Send inventory confirmed event (when payment succeeds)
     * This finalizes the inventory reduction
     */
    public void sendInventoryConfirmedEvent(Long orderId, Map<Long, Integer> confirmedItems) {
        try {
            InventoryReservedEvent event = InventoryReservedEvent.builder()
                    .orderId(orderId)
                    .reservedItems(confirmedItems)
                    .success(true)
                    .message("Inventory reservation confirmed, stock deducted")
                    .timestamp(LocalDateTime.now())
                    .build();
            
            String eventJson = objectMapper.writeValueAsString(event);
            String topic = "inventory-confirmed";
            
            CompletableFuture<SendResult<String, String>> future = 
                kafkaTemplate.send(topic, orderId.toString(), eventJson);
            
            future.whenComplete((result, ex) -> {
                if (ex == null) {
                    log.info("✅ [KAFKA] Inventory confirmed event sent for order: {}", orderId);
                } else {
                    log.error("❌ [KAFKA] Failed to send inventory confirmed event for order: {}", orderId, ex);
                }
            });
            
        } catch (JsonProcessingException e) {
            log.error("❌ [KAFKA] Error serializing inventory confirmed event", e);
        }
    }
}
