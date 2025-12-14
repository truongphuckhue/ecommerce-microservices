package com.ecommerce.product.service;

import com.ecommerce.product.dto.CategoryRequest;
import com.ecommerce.product.dto.CategoryResponse;

import java.util.List;

public interface CategoryService {
    
    CategoryResponse createCategory(CategoryRequest request);
    
    CategoryResponse updateCategory(Long id, CategoryRequest request);
    
    void deleteCategory(Long id);
    
    CategoryResponse getCategoryById(Long id);
    
    CategoryResponse getCategoryBySlug(String slug);
    
    List<CategoryResponse> getAllCategories();
    
    List<CategoryResponse> getRootCategories();
    
    List<CategoryResponse> getChildCategories(Long parentId);
    
    List<CategoryResponse> getCategoryTree();
}
