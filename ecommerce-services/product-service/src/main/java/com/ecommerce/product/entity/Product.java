package com.ecommerce.product.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "products", indexes = {
    @Index(name = "idx_sku", columnList = "sku"),
    @Index(name = "idx_category_id", columnList = "category_id"),
    @Index(name = "idx_name", columnList = "name"),
    @Index(name = "idx_active", columnList = "active")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Product {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 200)
    private String name;

    @Column(unique = true, nullable = false, length = 50)
    private String sku;

    @Column(length = 2000)
    private String description;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal price;

    @Column(name = "discount_price", precision = 10, scale = 2)
    private BigDecimal discountPrice;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id")
    private Category category;

    @Column(nullable = false)
    @Builder.Default
    private Boolean active = true;

    @Column(name = "stock_quantity")
    private Integer stockQuantity;

    @ElementCollection
    @CollectionTable(name = "product_images", joinColumns = @JoinColumn(name = "product_id"))
    @Column(name = "image_url", length = 500)
    @Builder.Default
    private List<String> images = new ArrayList<>();

    @Column(length = 100)
    private String brand;

    @Column(precision = 3, scale = 2)
    private BigDecimal weight; // in kg

    @Column(length = 50)
    private String dimensions; // e.g., "10x20x30 cm"

    @Column(name = "view_count")
    @Builder.Default
    private Long viewCount = 0L;

    @Column(name = "sold_count")
    @Builder.Default
    private Integer soldCount = 0;

    @Column(name = "average_rating", precision = 2, scale = 1)
    private BigDecimal averageRating;

    @Column(name = "review_count")
    @Builder.Default
    private Integer reviewCount = 0;

    @Column(name = "featured")
    @Builder.Default
    private Boolean featured = false;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    // Helper methods
    public void incrementViewCount() {
        this.viewCount++;
    }

    public void incrementSoldCount(int quantity) {
        this.soldCount += quantity;
    }

    public void updateRating(BigDecimal newRating) {
        if (this.reviewCount == 0) {
            this.averageRating = newRating;
            this.reviewCount = 1;
        } else {
            BigDecimal totalRating = this.averageRating.multiply(new BigDecimal(this.reviewCount));
            totalRating = totalRating.add(newRating);
            this.reviewCount++;
            this.averageRating = totalRating.divide(new BigDecimal(this.reviewCount), 1, BigDecimal.ROUND_HALF_UP);
        }
    }

    public boolean isOnSale() {
        return discountPrice != null && discountPrice.compareTo(price) < 0;
    }

    public BigDecimal getEffectivePrice() {
        return isOnSale() ? discountPrice : price;
    }

    public void addImage(String imageUrl) {
        this.images.add(imageUrl);
    }

    public void removeImage(String imageUrl) {
        this.images.remove(imageUrl);
    }
}
