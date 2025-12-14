package com.ecommerce.cart.dto;
import jakarta.validation.constraints.*;
import lombok.*;

@Data
public class UpdateCartItemRequest {
    @NotNull @Positive private Integer quantity;
}
