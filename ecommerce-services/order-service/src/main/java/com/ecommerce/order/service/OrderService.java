package com.ecommerce.order.service;

import com.ecommerce.order.dto.OrderRequest;
import com.ecommerce.order.dto.OrderResponse;
import com.ecommerce.order.entity.Order;

import java.util.List;

public interface OrderService {
    
    /**
     * Create new order and start saga
     */
    OrderResponse createOrder(OrderRequest request);
    
    /**
     * Get order by ID
     */
    OrderResponse getOrderById(Long id);
    
    /**
     * Get order by order number
     */
    OrderResponse getOrderByOrderNumber(String orderNumber);
    
    /**
     * Get all orders for a user
     */
    List<OrderResponse> getUserOrders(Long userId);
    
    /**
     * Get orders by user and status
     */
    List<OrderResponse> getUserOrdersByStatus(Long userId, Order.OrderStatus status);
    
    /**
     * Cancel order (user-initiated)
     */
    OrderResponse cancelOrder(Long id, String reason);
    
    /**
     * Get order status
     */
    Order.OrderStatus getOrderStatus(Long id);
    
    /**
     * Get saga status (for debugging)
     */
    Order.SagaStatus getSagaStatus(Long id);
    
    /**
     * Get all orders (admin)
     */
    List<OrderResponse> getAllOrders();
    
    /**
     * Get failed orders (monitoring)
     */
    List<OrderResponse> getFailedOrders();
}
