package com.ecommerce.product.dto;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProductRequest {
    
    @NotBlank(message = "Product name is required")
    @Size(min = 3, max = 200, message = "Product name must be between 3 and 200 characters")
    private String name;

    @NotBlank(message = "SKU is required")
    @Size(min = 3, max = 50, message = "SKU must be between 3 and 50 characters")
    @Pattern(regexp = "^[A-Z0-9-]+$", message = "SKU must contain only uppercase letters, numbers, and hyphens")
    private String sku;

    @Size(max = 2000, message = "Description must not exceed 2000 characters")
    private String description;

    @NotNull(message = "Price is required")
    @DecimalMin(value = "0.01", message = "Price must be greater than 0")
    @Digits(integer = 8, fraction = 2, message = "Price must have at most 8 digits and 2 decimal places")
    private BigDecimal price;

    @DecimalMin(value = "0.01", message = "Discount price must be greater than 0")
    @Digits(integer = 8, fraction = 2, message = "Discount price must have at most 8 digits and 2 decimal places")
    private BigDecimal discountPrice;

    @NotNull(message = "Category is required")
    private Long categoryId;

    private Boolean active;

    @Min(value = 0, message = "Stock quantity cannot be negative")
    private Integer stockQuantity;

    private List<String> images;

    @Size(max = 100, message = "Brand name must not exceed 100 characters")
    private String brand;

    @DecimalMin(value = "0.0", message = "Weight cannot be negative")
    @Digits(integer = 3, fraction = 2, message = "Weight must have at most 3 digits and 2 decimal places")
    private BigDecimal weight;

    @Size(max = 50, message = "Dimensions must not exceed 50 characters")
    private String dimensions;

    private Boolean featured;
}
