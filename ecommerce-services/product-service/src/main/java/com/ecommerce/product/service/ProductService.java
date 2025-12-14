package com.ecommerce.product.service;

import com.ecommerce.product.dto.*;
import org.springframework.data.domain.Page;

import java.util.List;

public interface ProductService {
    
    ProductResponse createProduct(ProductRequest request);
    
    ProductResponse updateProduct(Long id, ProductRequest request);
    
    void deleteProduct(Long id);
    
    ProductResponse getProductById(Long id);
    
    ProductResponse getProductBySku(String sku);
    
    Page<ProductResponse> getAllProducts(int page, int size, String sortBy, String sortDirection);
    
    Page<ProductResponse> searchProducts(ProductSearchCriteria criteria);
    
    Page<ProductResponse> getProductsByCategory(Long categoryId, int page, int size);
    
    List<ProductResponse> getFeaturedProducts(int limit);
    
    List<ProductResponse> getBestSellingProducts(int limit);
    
    List<ProductResponse> getNewArrivals(int limit);
    
    Page<ProductResponse> getOnSaleProducts(int page, int size);
    
    List<String> getAllBrands();
    
    void incrementViewCount(Long productId);
    
    void incrementSoldCount(Long productId, int quantity);
}
