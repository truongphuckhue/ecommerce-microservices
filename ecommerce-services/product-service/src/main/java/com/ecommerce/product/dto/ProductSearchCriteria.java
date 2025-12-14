package com.ecommerce.product.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProductSearchCriteria {
    
    private String keyword;
    private Long categoryId;
    private String brand;
    private BigDecimal minPrice;
    private BigDecimal maxPrice;
    private Boolean onSale;
    private Boolean featured;
    private String sortBy; // price, name, createdAt, soldCount, rating
    private String sortDirection; // asc, desc
    private Integer page;
    private Integer size;
}
