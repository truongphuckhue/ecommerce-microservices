package com.ecommerce.product.controller;

import com.ecommerce.common.dto.ApiResponse;
import com.ecommerce.product.dto.*;
import com.ecommerce.product.service.ProductService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/products")
@RequiredArgsConstructor
@Slf4j
public class ProductController {

    private final ProductService productService;

    @PostMapping
    public ResponseEntity<ApiResponse<ProductResponse>> createProduct(@Valid @RequestBody ProductRequest request) {
        log.info("POST /products - Create new product: {}", request.getName());
        
        ProductResponse response = productService.createProduct(request);
        
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success("Product created successfully", response));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<ProductResponse>> updateProduct(
            @PathVariable Long id,
            @Valid @RequestBody ProductRequest request) {
        log.info("PUT /products/{} - Update product", id);
        
        ProductResponse response = productService.updateProduct(id, request);
        
        return ResponseEntity.ok(ApiResponse.success("Product updated successfully", response));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteProduct(@PathVariable Long id) {
        log.info("DELETE /products/{} - Delete product", id);
        
        productService.deleteProduct(id);
        
        return ResponseEntity.ok(ApiResponse.success("Product deleted successfully", null));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<ProductResponse>> getProductById(@PathVariable Long id) {
        log.info("GET /products/{} - Get product by ID", id);
        
        ProductResponse response = productService.getProductById(id);
        
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/sku/{sku}")
    public ResponseEntity<ApiResponse<ProductResponse>> getProductBySku(@PathVariable String sku) {
        log.info("GET /products/sku/{} - Get product by SKU", sku);
        
        ProductResponse response = productService.getProductBySku(sku);
        
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<Page<ProductResponse>>> getAllProducts(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDirection) {
        log.info("GET /products - Get all products: page={}, size={}", page, size);
        
        Page<ProductResponse> response = productService.getAllProducts(page, size, sortBy, sortDirection);
        
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @PostMapping("/search")
    public ResponseEntity<ApiResponse<Page<ProductResponse>>> searchProducts(
            @RequestBody ProductSearchCriteria criteria) {
        log.info("POST /products/search - Search products with criteria");
        
        Page<ProductResponse> response = productService.searchProducts(criteria);
        
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/category/{categoryId}")
    public ResponseEntity<ApiResponse<Page<ProductResponse>>> getProductsByCategory(
            @PathVariable Long categoryId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        log.info("GET /products/category/{} - Get products by category", categoryId);
        
        Page<ProductResponse> response = productService.getProductsByCategory(categoryId, page, size);
        
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/featured")
    public ResponseEntity<ApiResponse<List<ProductResponse>>> getFeaturedProducts(
            @RequestParam(defaultValue = "10") int limit) {
        log.info("GET /products/featured - Get featured products");
        
        List<ProductResponse> response = productService.getFeaturedProducts(limit);
        
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/best-selling")
    public ResponseEntity<ApiResponse<List<ProductResponse>>> getBestSellingProducts(
            @RequestParam(defaultValue = "10") int limit) {
        log.info("GET /products/best-selling - Get best selling products");
        
        List<ProductResponse> response = productService.getBestSellingProducts(limit);
        
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/new-arrivals")
    public ResponseEntity<ApiResponse<List<ProductResponse>>> getNewArrivals(
            @RequestParam(defaultValue = "10") int limit) {
        log.info("GET /products/new-arrivals - Get new arrivals");
        
        List<ProductResponse> response = productService.getNewArrivals(limit);
        
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/on-sale")
    public ResponseEntity<ApiResponse<Page<ProductResponse>>> getOnSaleProducts(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        log.info("GET /products/on-sale - Get on-sale products");
        
        Page<ProductResponse> response = productService.getOnSaleProducts(page, size);
        
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/brands")
    public ResponseEntity<ApiResponse<List<String>>> getAllBrands() {
        log.info("GET /products/brands - Get all brands");
        
        List<String> response = productService.getAllBrands();
        
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @PostMapping("/{id}/view")
    public ResponseEntity<ApiResponse<Void>> incrementViewCount(@PathVariable Long id) {
        log.debug("POST /products/{}/view - Increment view count", id);
        
        productService.incrementViewCount(id);
        
        return ResponseEntity.ok(ApiResponse.success("View count incremented", null));
    }
}
