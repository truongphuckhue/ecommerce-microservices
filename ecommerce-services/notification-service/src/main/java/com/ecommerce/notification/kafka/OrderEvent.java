package com.ecommerce.notification.kafka;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrderEvent implements Serializable {
    
    private static final long serialVersionUID = 1L;

    private Long orderId;
    private String orderNumber;
    private Long userId;
    private String userEmail;
    private String userName;
    private BigDecimal totalAmount;
    private String status;
    private String reason;  // For cancellation/failure
    private String trackingNumber;  // For shipping
    
    @Builder.Default
    private Long timestamp = System.currentTimeMillis();
}
