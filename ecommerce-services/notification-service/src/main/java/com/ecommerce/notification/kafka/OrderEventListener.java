package com.ecommerce.notification.kafka;

import com.ecommerce.notification.service.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

/**
 * Listens to order-related events and sends notifications
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class OrderEventListener {

    private final NotificationService notificationService;

    /**
     * Order created event
     */
    @KafkaListener(
        topics = "order-created",
        groupId = "notification-service"
    )
    public void handleOrderCreated(OrderEvent event) {
        log.info("Received order created event: orderNumber={}, userId={}", 
                event.getOrderNumber(), event.getUserId());

        try {
            notificationService.sendOrderCreatedNotification(event);
        } catch (Exception e) {
            log.error("Failed to send order created notification: {}", e.getMessage(), e);
        }
    }

    /**
     * Order confirmed event (payment successful)
     */
    @KafkaListener(
        topics = "order-confirmed",
        groupId = "notification-service"
    )
    public void handleOrderConfirmed(OrderEvent event) {
        log.info("Received order confirmed event: orderNumber={}", event.getOrderNumber());

        try {
            notificationService.sendOrderConfirmedNotification(event);
        } catch (Exception e) {
            log.error("Failed to send order confirmed notification: {}", e.getMessage(), e);
        }
    }

    /**
     * Order cancelled event
     */
    @KafkaListener(
        topics = "order-cancelled",
        groupId = "notification-service"
    )
    public void handleOrderCancelled(OrderEvent event) {
        log.info("Received order cancelled event: orderNumber={}", event.getOrderNumber());

        try {
            notificationService.sendOrderCancelledNotification(event);
        } catch (Exception e) {
            log.error("Failed to send order cancelled notification: {}", e.getMessage(), e);
        }
    }

    /**
     * Order failed event
     */
    @KafkaListener(
        topics = "order-failed",
        groupId = "notification-service"
    )
    public void handleOrderFailed(OrderEvent event) {
        log.info("Received order failed event: orderNumber={}, reason={}", 
                event.getOrderNumber(), event.getReason());

        try {
            notificationService.sendOrderFailedNotification(event);
        } catch (Exception e) {
            log.error("Failed to send order failed notification: {}", e.getMessage(), e);
        }
    }

    /**
     * Order shipped event
     */
    @KafkaListener(
        topics = "order-shipped",
        groupId = "notification-service"
    )
    public void handleOrderShipped(OrderEvent event) {
        log.info("Received order shipped event: orderNumber={}", event.getOrderNumber());

        try {
            notificationService.sendOrderShippedNotification(event);
        } catch (Exception e) {
            log.error("Failed to send order shipped notification: {}", e.getMessage(), e);
        }
    }

    /**
     * Order delivered event
     */
    @KafkaListener(
        topics = "order-delivered",
        groupId = "notification-service"
    )
    public void handleOrderDelivered(OrderEvent event) {
        log.info("Received order delivered event: orderNumber={}", event.getOrderNumber());

        try {
            notificationService.sendOrderDeliveredNotification(event);
        } catch (Exception e) {
            log.error("Failed to send order delivered notification: {}", e.getMessage(), e);
        }
    }
}
