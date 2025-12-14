package com.ecommerce.order.service;

import com.ecommerce.common.exception.ResourceNotFoundException;
import com.ecommerce.order.dto.OrderItemRequest;
import com.ecommerce.order.dto.OrderRequest;
import com.ecommerce.order.dto.OrderResponse;
import com.ecommerce.order.entity.Order;
import com.ecommerce.order.entity.OrderItem;
import com.ecommerce.order.repository.OrderRepository;
import com.ecommerce.order.saga.OrderSaga;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class OrderServiceImpl implements OrderService {

    private final OrderRepository orderRepository;
    private final OrderSaga orderSaga;
    // In real implementation, you'd inject ProductService to fetch product details

    @Override
    @Transactional
    public OrderResponse createOrder(OrderRequest request) {
        log.info("Creating order for user {}", request.getUserId());

        // Generate unique order number
        String orderNumber = generateOrderNumber();

        // Create order entity
        Order order = Order.builder()
                .orderNumber(orderNumber)
                .userId(request.getUserId())
                .status(Order.OrderStatus.PENDING)
                .shippingAddress(request.getShippingAddress())
                .notes(request.getNotes())
                .totalAmount(BigDecimal.ZERO)
                .build();

        // Add order items
        // In real implementation, fetch product details from Product Service
        request.getItems().forEach(itemRequest -> {
            OrderItem item = createOrderItem(itemRequest);
            order.addItem(item);
        });

        // Save order
        order = orderRepository.save(order);

        log.info("Order created: {}", orderNumber);

        // Start saga asynchronously
        orderSaga.startSaga(order);

        return OrderResponse.fromOrder(order);
    }

    private OrderItem createOrderItem(OrderItemRequest request) {
        // In real implementation, call Product Service to get details
        // For now, using mock data
        return OrderItem.builder()
                .productId(request.getProductId())
                .productName("Product " + request.getProductId()) // Mock
                .sku("SKU-" + request.getProductId()) // Mock
                .price(BigDecimal.valueOf(99.99)) // Mock
                .quantity(request.getQuantity())
                .subtotal(BigDecimal.valueOf(99.99).multiply(BigDecimal.valueOf(request.getQuantity())))
                .build();
    }

    private String generateOrderNumber() {
        // Format: ORD-YYYYMMDD-UUID (first 8 chars)
        String date = LocalDateTime.now().toString().substring(0, 10).replace("-", "");
        String uuid = UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        return "ORD-" + date + "-" + uuid;
    }

    @Override
    @Transactional(readOnly = true)
    public OrderResponse getOrderById(Long id) {
        log.info("Getting order by ID: {}", id);
        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Order", "id", id));
        return OrderResponse.fromOrder(order);
    }

    @Override
    @Transactional(readOnly = true)
    public OrderResponse getOrderByOrderNumber(String orderNumber) {
        log.info("Getting order by order number: {}", orderNumber);
        Order order = orderRepository.findByOrderNumber(orderNumber)
                .orElseThrow(() -> new ResourceNotFoundException("Order", "orderNumber", orderNumber));
        return OrderResponse.fromOrder(order);
    }

    @Override
    @Transactional(readOnly = true)
    public List<OrderResponse> getUserOrders(Long userId) {
        log.info("Getting orders for user: {}", userId);
        return orderRepository.findByUserIdOrderByCreatedAtDesc(userId).stream()
                .map(OrderResponse::fromOrder)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<OrderResponse> getUserOrdersByStatus(Long userId, Order.OrderStatus status) {
        log.info("Getting orders for user {} with status {}", userId, status);
        return orderRepository.findByUserIdAndStatus(userId, status, null).stream()
                .map(OrderResponse::fromOrder)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public OrderResponse cancelOrder(Long id, String reason) {
        log.info("Cancelling order ID: {}", id);

        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Order", "id", id));

        // Delegate to saga for proper compensation
        orderSaga.cancelOrder(order, reason);

        return OrderResponse.fromOrder(order);
    }

    @Override
    @Transactional(readOnly = true)
    public Order.OrderStatus getOrderStatus(Long id) {
        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Order", "id", id));
        return order.getStatus();
    }

    @Override
    @Transactional(readOnly = true)
    public Order.SagaStatus getSagaStatus(Long id) {
        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Order", "id", id));
        return order.getSagaStatus();
    }

    @Override
    @Transactional(readOnly = true)
    public List<OrderResponse> getAllOrders() {
        log.info("Getting all orders");
        return orderRepository.findAll().stream()
                .map(OrderResponse::fromOrder)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<OrderResponse> getFailedOrders() {
        log.info("Getting failed orders");
        LocalDateTime startDate = LocalDateTime.now().minusDays(7);
        LocalDateTime endDate = LocalDateTime.now();
        return orderRepository.findFailedOrders(startDate, endDate).stream()
                .map(OrderResponse::fromOrder)
                .collect(Collectors.toList());
    }
}
