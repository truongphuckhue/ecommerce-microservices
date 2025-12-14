package com.ecommerce.product.controller;

import com.ecommerce.common.dto.ApiResponse;
import com.ecommerce.product.dto.CategoryRequest;
import com.ecommerce.product.dto.CategoryResponse;
import com.ecommerce.product.service.CategoryService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/categories")
@RequiredArgsConstructor
@Slf4j
public class CategoryController {

    private final CategoryService categoryService;

    @PostMapping
    public ResponseEntity<ApiResponse<CategoryResponse>> createCategory(@Valid @RequestBody CategoryRequest request) {
        log.info("POST /categories - Create new category: {}", request.getName());
        
        CategoryResponse response = categoryService.createCategory(request);
        
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success("Category created successfully", response));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<CategoryResponse>> updateCategory(
            @PathVariable Long id,
            @Valid @RequestBody CategoryRequest request) {
        log.info("PUT /categories/{} - Update category", id);
        
        CategoryResponse response = categoryService.updateCategory(id, request);
        
        return ResponseEntity.ok(ApiResponse.success("Category updated successfully", response));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteCategory(@PathVariable Long id) {
        log.info("DELETE /categories/{} - Delete category", id);
        
        categoryService.deleteCategory(id);
        
        return ResponseEntity.ok(ApiResponse.success("Category deleted successfully", null));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<CategoryResponse>> getCategoryById(@PathVariable Long id) {
        log.info("GET /categories/{} - Get category by ID", id);
        
        CategoryResponse response = categoryService.getCategoryById(id);
        
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/slug/{slug}")
    public ResponseEntity<ApiResponse<CategoryResponse>> getCategoryBySlug(@PathVariable String slug) {
        log.info("GET /categories/slug/{} - Get category by slug", slug);
        
        CategoryResponse response = categoryService.getCategoryBySlug(slug);
        
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<CategoryResponse>>> getAllCategories() {
        log.info("GET /categories - Get all categories");
        
        List<CategoryResponse> response = categoryService.getAllCategories();
        
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/root")
    public ResponseEntity<ApiResponse<List<CategoryResponse>>> getRootCategories() {
        log.info("GET /categories/root - Get root categories");
        
        List<CategoryResponse> response = categoryService.getRootCategories();
        
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/{parentId}/children")
    public ResponseEntity<ApiResponse<List<CategoryResponse>>> getChildCategories(@PathVariable Long parentId) {
        log.info("GET /categories/{}/children - Get child categories", parentId);
        
        List<CategoryResponse> response = categoryService.getChildCategories(parentId);
        
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/tree")
    public ResponseEntity<ApiResponse<List<CategoryResponse>>> getCategoryTree() {
        log.info("GET /categories/tree - Get category tree");
        
        List<CategoryResponse> response = categoryService.getCategoryTree();
        
        return ResponseEntity.ok(ApiResponse.success(response));
    }
}
