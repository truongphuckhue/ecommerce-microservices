package com.ecommerce.cart.dto;
import jakarta.validation.constraints.*;
import lombok.*;

@Data
public class AddToCartRequest {
    @NotNull private Long productId;
    @NotBlank private String productName;
    @NotNull @Positive private Double price;
    @NotNull @Positive private Integer quantity;
    private String imageUrl;
}
