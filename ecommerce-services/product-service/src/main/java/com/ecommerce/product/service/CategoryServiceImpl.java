package com.ecommerce.product.service;

import com.ecommerce.common.exception.BusinessException;
import com.ecommerce.common.exception.ResourceNotFoundException;
import com.ecommerce.product.dto.CategoryRequest;
import com.ecommerce.product.dto.CategoryResponse;
import com.ecommerce.product.entity.Category;
import com.ecommerce.product.repository.CategoryRepository;
import com.ecommerce.product.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class CategoryServiceImpl implements CategoryService {

    private final CategoryRepository categoryRepository;
    private final ProductRepository productRepository;

    @Override
    @Transactional
    @CacheEvict(value = {"categories", "categoryTree"}, allEntries = true)
    public CategoryResponse createCategory(CategoryRequest request) {
        log.info("Creating new category: {}", request.getName());

        // Validate slug uniqueness
        if (categoryRepository.existsBySlug(request.getSlug())) {
            throw new BusinessException("CATEGORY_EXISTS", "Category with slug " + request.getSlug() + " already exists");
        }

        // Validate parent category if specified
        Category parent = null;
        if (request.getParentId() != null) {
            parent = categoryRepository.findById(request.getParentId())
                    .orElseThrow(() -> new ResourceNotFoundException("Category", "id", request.getParentId()));
        }

        // Create category
        Category category = Category.builder()
                .name(request.getName())
                .slug(request.getSlug())
                .description(request.getDescription())
                .parent(parent)
                .imageUrl(request.getImageUrl())
                .active(request.getActive() != null ? request.getActive() : true)
                .displayOrder(request.getDisplayOrder() != null ? request.getDisplayOrder() : 0)
                .build();

        category = categoryRepository.save(category);
        
        log.info("Category created successfully: ID={}, Slug={}", category.getId(), category.getSlug());
        
        return CategoryResponse.fromCategory(category);
    }

    @Override
    @Transactional
    @CacheEvict(value = {"categories", "categoryTree"}, allEntries = true)
    public CategoryResponse updateCategory(Long id, CategoryRequest request) {
        log.info("Updating category ID: {}", id);

        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Category", "id", id));

        // Check slug uniqueness if changed
        if (!category.getSlug().equals(request.getSlug()) && categoryRepository.existsBySlug(request.getSlug())) {
            throw new BusinessException("CATEGORY_EXISTS", "Category with slug " + request.getSlug() + " already exists");
        }

        // Validate parent category if specified
        Category parent = null;
        if (request.getParentId() != null) {
            // Cannot set self as parent
            if (request.getParentId().equals(id)) {
                throw new BusinessException("INVALID_PARENT", "Category cannot be its own parent");
            }
            
            parent = categoryRepository.findById(request.getParentId())
                    .orElseThrow(() -> new ResourceNotFoundException("Category", "id", request.getParentId()));
            
            // Check for circular reference
            if (isCircularReference(id, parent)) {
                throw new BusinessException("CIRCULAR_REFERENCE", "Circular reference detected in category hierarchy");
            }
        }

        // Update category
        category.setName(request.getName());
        category.setSlug(request.getSlug());
        category.setDescription(request.getDescription());
        category.setParent(parent);
        category.setImageUrl(request.getImageUrl());
        category.setActive(request.getActive() != null ? request.getActive() : category.getActive());
        category.setDisplayOrder(request.getDisplayOrder() != null ? request.getDisplayOrder() : category.getDisplayOrder());

        category = categoryRepository.save(category);
        
        log.info("Category updated successfully: ID={}", id);
        
        return CategoryResponse.fromCategory(category);
    }

    @Override
    @Transactional
    @CacheEvict(value = {"categories", "categoryTree"}, allEntries = true)
    public void deleteCategory(Long id) {
        log.info("Deleting category ID: {}", id);

        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Category", "id", id));

        // Check if category has products
        long productCount = productRepository.countByCategoryId(id);
        if (productCount > 0) {
            throw new BusinessException("CATEGORY_HAS_PRODUCTS", 
                "Cannot delete category with " + productCount + " products");
        }

        // Check if category has children
        long childCount = categoryRepository.countByParentId(id);
        if (childCount > 0) {
            throw new BusinessException("CATEGORY_HAS_CHILDREN", 
                "Cannot delete category with " + childCount + " sub-categories");
        }

        categoryRepository.delete(category);
        
        log.info("Category deleted successfully: ID={}", id);
    }

    @Override
    @Transactional(readOnly = true)
    @Cacheable(value = "categories", key = "#id")
    public CategoryResponse getCategoryById(Long id) {
        log.debug("Fetching category by ID: {}", id);

        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Category", "id", id));

        return CategoryResponse.fromCategory(category, true);
    }

    @Override
    @Transactional(readOnly = true)
    @Cacheable(value = "categories", key = "#slug")
    public CategoryResponse getCategoryBySlug(String slug) {
        log.debug("Fetching category by slug: {}", slug);

        Category category = categoryRepository.findBySlug(slug)
                .orElseThrow(() -> new ResourceNotFoundException("Category", "slug", slug));

        return CategoryResponse.fromCategory(category, true);
    }

    @Override
    @Transactional(readOnly = true)
    @Cacheable(value = "categories", key = "'all'")
    public List<CategoryResponse> getAllCategories() {
        log.debug("Fetching all categories");

        return categoryRepository.findByActiveTrueOrderByDisplayOrderAscNameAsc()
                .stream()
                .map(category -> CategoryResponse.fromCategory(category, false))
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    @Cacheable(value = "categories", key = "'root'")
    public List<CategoryResponse> getRootCategories() {
        log.debug("Fetching root categories");

        return categoryRepository.findRootCategories()
                .stream()
                .map(category -> CategoryResponse.fromCategory(category, false))
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    @Cacheable(value = "categories", key = "'children-' + #parentId")
    public List<CategoryResponse> getChildCategories(Long parentId) {
        log.debug("Fetching child categories for parent ID: {}", parentId);

        return categoryRepository.findChildCategories(parentId)
                .stream()
                .map(category -> CategoryResponse.fromCategory(category, false))
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    @Cacheable(value = "categoryTree")
    public List<CategoryResponse> getCategoryTree() {
        log.debug("Fetching category tree");

        return categoryRepository.findRootCategories()
                .stream()
                .map(category -> CategoryResponse.fromCategory(category, true))
                .collect(Collectors.toList());
    }

    // Helper method to check circular reference
    private boolean isCircularReference(Long categoryId, Category parent) {
        Category current = parent;
        while (current != null) {
            if (current.getId().equals(categoryId)) {
                return true;
            }
            current = current.getParent();
        }
        return false;
    }
}
