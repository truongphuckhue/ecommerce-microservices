package com.ecommerce.notification.service;

import com.ecommerce.notification.entity.Notification;
import com.ecommerce.notification.kafka.OrderEvent;

import java.util.List;

public interface NotificationService {
    
    void sendOrderCreatedNotification(OrderEvent event);
    
    void sendOrderConfirmedNotification(OrderEvent event);
    
    void sendOrderCancelledNotification(OrderEvent event);
    
    void sendOrderFailedNotification(OrderEvent event);
    
    void sendOrderShippedNotification(OrderEvent event);
    
    void sendOrderDeliveredNotification(OrderEvent event);
    
    List<Notification> getUserNotifications(Long userId);
    
    List<Notification> getFailedNotifications();
}
