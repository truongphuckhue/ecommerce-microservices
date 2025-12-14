package com.ecommerce.product.kafka;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProductEvent {
    
    private String eventType; // CREATED, UPDATED, DELETED, STOCK_CHANGED
    private Long productId;
    private String sku;
    private String name;
    private BigDecimal price;
    private BigDecimal discountPrice;
    private Long categoryId;
    private String categoryName;
    private Integer stockQuantity;
    private Boolean active;
    private LocalDateTime timestamp;
}
