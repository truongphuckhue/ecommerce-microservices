package com.ecommerce.notification.service;

import com.ecommerce.notification.entity.Notification;
import com.ecommerce.notification.kafka.OrderEvent;
import com.ecommerce.notification.repository.NotificationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class NotificationServiceImpl implements NotificationService {

    private final NotificationRepository notificationRepository;
    private final EmailService emailService;

    @Override
    @Async
    @Transactional
    public void sendOrderCreatedNotification(OrderEvent event) {
        log.info("Sending order created notification for order: {}", event.getOrderNumber());

        Notification notification = Notification.builder()
                .userId(event.getUserId())
                .recipientEmail(event.getUserEmail())
                .type(Notification.NotificationType.ORDER_CREATED)
                .channel(Notification.NotificationChannel.EMAIL)
                .status(Notification.NotificationStatus.PENDING)
                .subject("Order Created - " + event.getOrderNumber())
                .content(buildOrderCreatedContent(event))
                .referenceType("ORDER")
                .referenceId(event.getOrderNumber())
                .templateName("order-created")
                .build();

        notification = notificationRepository.save(notification);

        try {
            // Send email
            Map<String, Object> variables = new HashMap<>();
            variables.put("userName", event.getUserName());
            variables.put("orderNumber", event.getOrderNumber());
            variables.put("totalAmount", event.getTotalAmount());

            emailService.sendHtmlEmail(
                    event.getUserEmail(),
                    notification.getSubject(),
                    "order-created",
                    variables
            );

            notification.markAsSent();
            notificationRepository.save(notification);

        } catch (Exception e) {
            log.error("Failed to send order created notification: {}", e.getMessage());
            notification.markAsFailed(e.getMessage());
            notificationRepository.save(notification);
        }
    }

    @Override
    @Async
    @Transactional
    public void sendOrderConfirmedNotification(OrderEvent event) {
        log.info("Sending order confirmed notification for order: {}", event.getOrderNumber());

        Notification notification = Notification.builder()
                .userId(event.getUserId())
                .recipientEmail(event.getUserEmail())
                .type(Notification.NotificationType.ORDER_CONFIRMED)
                .channel(Notification.NotificationChannel.EMAIL)
                .status(Notification.NotificationStatus.PENDING)
                .subject("Order Confirmed - " + event.getOrderNumber())
                .content(buildOrderConfirmedContent(event))
                .referenceType("ORDER")
                .referenceId(event.getOrderNumber())
                .templateName("order-confirmed")
                .build();

        notification = notificationRepository.save(notification);

        try {
            Map<String, Object> variables = new HashMap<>();
            variables.put("userName", event.getUserName());
            variables.put("orderNumber", event.getOrderNumber());
            variables.put("totalAmount", event.getTotalAmount());

            emailService.sendHtmlEmail(
                    event.getUserEmail(),
                    notification.getSubject(),
                    "order-confirmed",
                    variables
            );

            notification.markAsSent();
            notificationRepository.save(notification);

        } catch (Exception e) {
            log.error("Failed to send order confirmed notification: {}", e.getMessage());
            notification.incrementRetry();
            notificationRepository.save(notification);
        }
    }

    @Override
    @Async
    @Transactional
    public void sendOrderCancelledNotification(OrderEvent event) {
        log.info("Sending order cancelled notification for order: {}", event.getOrderNumber());

        Notification notification = Notification.builder()
                .userId(event.getUserId())
                .recipientEmail(event.getUserEmail())
                .type(Notification.NotificationType.ORDER_CANCELLED)
                .channel(Notification.NotificationChannel.EMAIL)
                .status(Notification.NotificationStatus.PENDING)
                .subject("Order Cancelled - " + event.getOrderNumber())
                .content(buildOrderCancelledContent(event))
                .referenceType("ORDER")
                .referenceId(event.getOrderNumber())
                .build();

        notification = notificationRepository.save(notification);

        try {
            Map<String, Object> variables = new HashMap<>();
            variables.put("userName", event.getUserName());
            variables.put("orderNumber", event.getOrderNumber());
            variables.put("reason", event.getReason());

            emailService.sendHtmlEmail(
                    event.getUserEmail(),
                    notification.getSubject(),
                    "order-cancelled",
                    variables
            );

            notification.markAsSent();
            notificationRepository.save(notification);

        } catch (Exception e) {
            log.error("Failed to send order cancelled notification: {}", e.getMessage());
            notification.markAsFailed(e.getMessage());
            notificationRepository.save(notification);
        }
    }

    @Override
    @Async
    @Transactional
    public void sendOrderFailedNotification(OrderEvent event) {
        log.info("Sending order failed notification for order: {}", event.getOrderNumber());

        Notification notification = Notification.builder()
                .userId(event.getUserId())
                .recipientEmail(event.getUserEmail())
                .type(Notification.NotificationType.ORDER_FAILED)
                .channel(Notification.NotificationChannel.EMAIL)
                .status(Notification.NotificationStatus.PENDING)
                .subject("Order Failed - " + event.getOrderNumber())
                .content(buildOrderFailedContent(event))
                .referenceType("ORDER")
                .referenceId(event.getOrderNumber())
                .build();

        notification = notificationRepository.save(notification);

        try {
            String content = String.format(
                    "Dear %s,\n\nWe're sorry, but your order %s could not be completed.\n\nReason: %s\n\nPlease try again or contact support.\n\nBest regards,\nE-commerce Team",
                    event.getUserName(),
                    event.getOrderNumber(),
                    event.getReason()
            );

            emailService.sendSimpleEmail(
                    event.getUserEmail(),
                    notification.getSubject(),
                    content
            );

            notification.markAsSent();
            notificationRepository.save(notification);

        } catch (Exception e) {
            log.error("Failed to send order failed notification: {}", e.getMessage());
            notification.markAsFailed(e.getMessage());
            notificationRepository.save(notification);
        }
    }

    @Override
    @Async
    @Transactional
    public void sendOrderShippedNotification(OrderEvent event) {
        log.info("Sending order shipped notification for order: {}", event.getOrderNumber());

        Notification notification = Notification.builder()
                .userId(event.getUserId())
                .recipientEmail(event.getUserEmail())
                .type(Notification.NotificationType.ORDER_SHIPPED)
                .channel(Notification.NotificationChannel.EMAIL)
                .status(Notification.NotificationStatus.PENDING)
                .subject("Order Shipped - " + event.getOrderNumber())
                .content(buildOrderShippedContent(event))
                .referenceType("ORDER")
                .referenceId(event.getOrderNumber())
                .build();

        notification = notificationRepository.save(notification);

        try {
            Map<String, Object> variables = new HashMap<>();
            variables.put("userName", event.getUserName());
            variables.put("orderNumber", event.getOrderNumber());
            variables.put("trackingNumber", event.getTrackingNumber());

            emailService.sendHtmlEmail(
                    event.getUserEmail(),
                    notification.getSubject(),
                    "order-shipped",
                    variables
            );

            notification.markAsSent();
            notificationRepository.save(notification);

        } catch (Exception e) {
            log.error("Failed to send order shipped notification: {}", e.getMessage());
            notification.markAsFailed(e.getMessage());
            notificationRepository.save(notification);
        }
    }

    @Override
    @Async
    @Transactional
    public void sendOrderDeliveredNotification(OrderEvent event) {
        log.info("Sending order delivered notification for order: {}", event.getOrderNumber());

        Notification notification = Notification.builder()
                .userId(event.getUserId())
                .recipientEmail(event.getUserEmail())
                .type(Notification.NotificationType.ORDER_DELIVERED)
                .channel(Notification.NotificationChannel.EMAIL)
                .status(Notification.NotificationStatus.PENDING)
                .subject("Order Delivered - " + event.getOrderNumber())
                .content(buildOrderDeliveredContent(event))
                .referenceType("ORDER")
                .referenceId(event.getOrderNumber())
                .build();

        notification = notificationRepository.save(notification);

        try {
            String content = String.format(
                    "Dear %s,\n\nGreat news! Your order %s has been delivered.\n\nThank you for shopping with us!\n\nBest regards,\nE-commerce Team",
                    event.getUserName(),
                    event.getOrderNumber()
            );

            emailService.sendSimpleEmail(
                    event.getUserEmail(),
                    notification.getSubject(),
                    content
            );

            notification.markAsSent();
            notificationRepository.save(notification);

        } catch (Exception e) {
            log.error("Failed to send order delivered notification: {}", e.getMessage());
            notification.markAsFailed(e.getMessage());
            notificationRepository.save(notification);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public List<Notification> getUserNotifications(Long userId) {
        return notificationRepository.findByUserIdOrderByCreatedAtDesc(userId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Notification> getFailedNotifications() {
        return notificationRepository.findByStatus(Notification.NotificationStatus.FAILED);
    }

    // Helper methods for building content
    private String buildOrderCreatedContent(OrderEvent event) {
        return String.format(
                "Order %s has been created successfully. Total amount: $%.2f. We will notify you once payment is confirmed.",
                event.getOrderNumber(),
                event.getTotalAmount()
        );
    }

    private String buildOrderConfirmedContent(OrderEvent event) {
        return String.format(
                "Your order %s has been confirmed! Total amount: $%.2f. We're preparing your items for shipment.",
                event.getOrderNumber(),
                event.getTotalAmount()
        );
    }

    private String buildOrderCancelledContent(OrderEvent event) {
        return String.format(
                "Your order %s has been cancelled. Reason: %s. If this was a mistake, please contact support.",
                event.getOrderNumber(),
                event.getReason()
        );
    }

    private String buildOrderFailedContent(OrderEvent event) {
        return String.format(
                "Order %s failed. Reason: %s. Please try again or contact support for assistance.",
                event.getOrderNumber(),
                event.getReason()
        );
    }

    private String buildOrderShippedContent(OrderEvent event) {
        return String.format(
                "Your order %s has been shipped! Tracking number: %s. Expected delivery in 3-5 business days.",
                event.getOrderNumber(),
                event.getTrackingNumber()
        );
    }

    private String buildOrderDeliveredContent(OrderEvent event) {
        return String.format(
                "Your order %s has been delivered! Thank you for shopping with us. We hope you enjoy your purchase!",
                event.getOrderNumber()
        );
    }
}
