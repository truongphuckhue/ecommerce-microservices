package com.ecommerce.cart.dto;
import lombok.*;

@Data
@Builder
public class CartItemResponse {
    private Long id;
    private Long productId;
    private String productName;
    private Double price;
    private Integer quantity;
    private String imageUrl;
    private Double subtotal;
}
