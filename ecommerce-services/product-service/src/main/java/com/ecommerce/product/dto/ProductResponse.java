package com.ecommerce.product.dto;

import com.ecommerce.product.entity.Product;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProductResponse {
    
    private Long id;
    private String name;
    private String sku;
    private String description;
    private BigDecimal price;
    private BigDecimal discountPrice;
    private BigDecimal effectivePrice;
    private CategorySummary category;
    private Boolean active;
    private Integer stockQuantity;
    private List<String> images;
    private String brand;
    private BigDecimal weight;
    private String dimensions;
    private Long viewCount;
    private Integer soldCount;
    private BigDecimal averageRating;
    private Integer reviewCount;
    private Boolean featured;
    private Boolean onSale;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class CategorySummary {
        private Long id;
        private String name;
        private String slug;
    }

    public static ProductResponse fromProduct(Product product) {
        CategorySummary categorySummary = null;
        if (product.getCategory() != null) {
            categorySummary = CategorySummary.builder()
                    .id(product.getCategory().getId())
                    .name(product.getCategory().getName())
                    .slug(product.getCategory().getSlug())
                    .build();
        }

        return ProductResponse.builder()
                .id(product.getId())
                .name(product.getName())
                .sku(product.getSku())
                .description(product.getDescription())
                .price(product.getPrice())
                .discountPrice(product.getDiscountPrice())
                .effectivePrice(product.getEffectivePrice())
                .category(categorySummary)
                .active(product.getActive())
                .stockQuantity(product.getStockQuantity())
                .images(product.getImages())
                .brand(product.getBrand())
                .weight(product.getWeight())
                .dimensions(product.getDimensions())
                .viewCount(product.getViewCount())
                .soldCount(product.getSoldCount())
                .averageRating(product.getAverageRating())
                .reviewCount(product.getReviewCount())
                .featured(product.getFeatured())
                .onSale(product.isOnSale())
                .createdAt(product.getCreatedAt())
                .updatedAt(product.getUpdatedAt())
                .build();
    }
}
