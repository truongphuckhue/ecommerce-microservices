package com.ecommerce.product.dto;

import com.ecommerce.product.entity.Category;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CategoryResponse {
    
    private Long id;
    private String name;
    private String slug;
    private String description;
    private Long parentId;
    private String parentName;
    private List<CategoryResponse> children;
    private String imageUrl;
    private Boolean active;
    private Integer displayOrder;
    private Integer productCount;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public static CategoryResponse fromCategory(Category category) {
        return fromCategory(category, false);
    }

    public static CategoryResponse fromCategory(Category category, boolean includeChildren) {
        CategoryResponse response = CategoryResponse.builder()
                .id(category.getId())
                .name(category.getName())
                .slug(category.getSlug())
                .description(category.getDescription())
                .parentId(category.getParent() != null ? category.getParent().getId() : null)
                .parentName(category.getParent() != null ? category.getParent().getName() : null)
                .imageUrl(category.getImageUrl())
                .active(category.getActive())
                .displayOrder(category.getDisplayOrder())
                .createdAt(category.getCreatedAt())
                .updatedAt(category.getUpdatedAt())
                .build();

        if (includeChildren && category.getChildren() != null && !category.getChildren().isEmpty()) {
            response.setChildren(
                category.getChildren().stream()
                    .map(child -> CategoryResponse.fromCategory(child, false))
                    .collect(Collectors.toList())
            );
        }

        return response;
    }
}
