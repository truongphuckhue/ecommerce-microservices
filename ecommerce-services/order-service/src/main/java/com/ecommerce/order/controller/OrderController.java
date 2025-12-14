package com.ecommerce.order.controller;

import com.ecommerce.common.dto.ApiResponse;
import com.ecommerce.order.dto.OrderRequest;
import com.ecommerce.order.dto.OrderResponse;
import com.ecommerce.order.entity.Order;
import com.ecommerce.order.service.OrderService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
@Slf4j
public class OrderController {

    private final OrderService orderService;

    /**
     * Create new order
     * POST /api/orders
     */
    @PostMapping
    public ResponseEntity<ApiResponse<OrderResponse>> createOrder(@Valid @RequestBody OrderRequest request) {
        log.info("REST: Creating order for user {}", request.getUserId());
        OrderResponse response = orderService.createOrder(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Order created successfully. Saga started.", response));
    }

    /**
     * Get order by ID
     * GET /api/orders/{id}
     */
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<OrderResponse>> getOrderById(@PathVariable Long id) {
        log.info("REST: Getting order by ID: {}", id);
        OrderResponse response = orderService.getOrderById(id);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    /**
     * Get order by order number
     * GET /api/orders/number/{orderNumber}
     */
    @GetMapping("/number/{orderNumber}")
    public ResponseEntity<ApiResponse<OrderResponse>> getOrderByOrderNumber(@PathVariable String orderNumber) {
        log.info("REST: Getting order by order number: {}", orderNumber);
        OrderResponse response = orderService.getOrderByOrderNumber(orderNumber);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    /**
     * Get all orders for a user
     * GET /api/orders/user/{userId}
     */
    @GetMapping("/user/{userId}")
    public ResponseEntity<ApiResponse<List<OrderResponse>>> getUserOrders(@PathVariable Long userId) {
        log.info("REST: Getting orders for user: {}", userId);
        List<OrderResponse> responses = orderService.getUserOrders(userId);
        return ResponseEntity.ok(ApiResponse.success(responses));
    }

    /**
     * Get orders by user and status
     * GET /api/orders/user/{userId}/status/{status}
     */
    @GetMapping("/user/{userId}/status/{status}")
    public ResponseEntity<ApiResponse<List<OrderResponse>>> getUserOrdersByStatus(
            @PathVariable Long userId,
            @PathVariable String status) {
        log.info("REST: Getting orders for user {} with status {}", userId, status);
        Order.OrderStatus orderStatus = Order.OrderStatus.valueOf(status.toUpperCase());
        List<OrderResponse> responses = orderService.getUserOrdersByStatus(userId, orderStatus);
        return ResponseEntity.ok(ApiResponse.success(responses));
    }

    /**
     * Cancel order
     * PUT /api/orders/{id}/cancel
     */
    @PutMapping("/{id}/cancel")
    public ResponseEntity<ApiResponse<OrderResponse>> cancelOrder(
            @PathVariable Long id,
            @RequestParam(required = false, defaultValue = "User cancelled") String reason) {
        log.info("REST: Cancelling order ID: {}, reason: {}", id, reason);
        OrderResponse response = orderService.cancelOrder(id, reason);
        return ResponseEntity.ok(ApiResponse.success("Order cancelled successfully", response));
    }

    /**
     * Get order status
     * GET /api/orders/{id}/status
     */
    @GetMapping("/{id}/status")
    public ResponseEntity<ApiResponse<String>> getOrderStatus(@PathVariable Long id) {
        log.info("REST: Getting status for order: {}", id);
        Order.OrderStatus status = orderService.getOrderStatus(id);
        return ResponseEntity.ok(ApiResponse.success(status.name()));
    }

    /**
     * Get saga status (for debugging)
     * GET /api/orders/{id}/saga-status
     */
    @GetMapping("/{id}/saga-status")
    public ResponseEntity<ApiResponse<String>> getSagaStatus(@PathVariable Long id) {
        log.info("REST: Getting saga status for order: {}", id);
        Order.SagaStatus status = orderService.getSagaStatus(id);
        return ResponseEntity.ok(ApiResponse.success(status != null ? status.name() : "N/A"));
    }

    /**
     * Get all orders (admin)
     * GET /api/orders
     */
    @GetMapping
    public ResponseEntity<ApiResponse<List<OrderResponse>>> getAllOrders() {
        log.info("REST: Getting all orders");
        List<OrderResponse> responses = orderService.getAllOrders();
        return ResponseEntity.ok(ApiResponse.success(responses));
    }

    /**
     * Get failed orders (monitoring)
     * GET /api/orders/failed
     */
    @GetMapping("/failed")
    public ResponseEntity<ApiResponse<List<OrderResponse>>> getFailedOrders() {
        log.info("REST: Getting failed orders");
        List<OrderResponse> responses = orderService.getFailedOrders();
        return ResponseEntity.ok(ApiResponse.success(responses));
    }
}
