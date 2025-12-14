package com.ecommerce.order.dto;

import com.ecommerce.order.entity.Order;
import com.ecommerce.order.entity.OrderItem;
import com.ecommerce.order.entity.ShippingAddress;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrderResponse {
    
    private Long id;
    private String orderNumber;
    private Long userId;
    private String status;
    private List<OrderItemResponse> items;
    private BigDecimal totalAmount;
    private String paymentId;
    private ShippingAddress shippingAddress;
    private String notes;
    private String sagaId;
    private String sagaStatus;
    private String failureReason;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public static OrderResponse fromOrder(Order order) {
        return OrderResponse.builder()
                .id(order.getId())
                .orderNumber(order.getOrderNumber())
                .userId(order.getUserId())
                .status(order.getStatus().name())
                .items(order.getItems().stream()
                        .map(OrderItemResponse::fromOrderItem)
                        .collect(Collectors.toList()))
                .totalAmount(order.getTotalAmount())
                .paymentId(order.getPaymentId())
                .shippingAddress(order.getShippingAddress())
                .notes(order.getNotes())
                .sagaId(order.getSagaId())
                .sagaStatus(order.getSagaStatus() != null ? order.getSagaStatus().name() : null)
                .failureReason(order.getFailureReason())
                .createdAt(order.getCreatedAt())
                .updatedAt(order.getUpdatedAt())
                .build();
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class OrderItemResponse {
        private Long id;
        private Long productId;
        private String productName;
        private String sku;
        private BigDecimal price;
        private Integer quantity;
        private BigDecimal subtotal;

        public static OrderItemResponse fromOrderItem(OrderItem item) {
            return OrderItemResponse.builder()
                    .id(item.getId())
                    .productId(item.getProductId())
                    .productName(item.getProductName())
                    .sku(item.getSku())
                    .price(item.getPrice())
                    .quantity(item.getQuantity())
                    .subtotal(item.getSubtotal())
                    .build();
        }
    }
}
