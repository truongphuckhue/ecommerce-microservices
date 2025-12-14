package com.ecommerce.product.service;

import com.ecommerce.common.exception.BusinessException;
import com.ecommerce.common.exception.ResourceNotFoundException;
import com.ecommerce.product.dto.*;
import com.ecommerce.product.entity.Category;
import com.ecommerce.product.entity.Product;
import com.ecommerce.product.kafka.ProductEventProducer;
import com.ecommerce.product.repository.CategoryRepository;
import com.ecommerce.product.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.*;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class ProductServiceImpl implements ProductService {

    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;
    private final ProductEventProducer productEventProducer;

    @Override
    @Transactional
    @CacheEvict(value = "products", allEntries = true)
    public ProductResponse createProduct(ProductRequest request) {
        log.info("Creating new product with SKU: {}", request.getSku());

        // Validate SKU uniqueness
        if (productRepository.existsBySku(request.getSku())) {
            throw new BusinessException("PRODUCT_EXISTS", "Product with SKU " + request.getSku() + " already exists");
        }

        // Validate category exists
        Category category = categoryRepository.findById(request.getCategoryId())
                .orElseThrow(() -> new ResourceNotFoundException("Category", "id", request.getCategoryId()));

        // Validate discount price
        if (request.getDiscountPrice() != null && 
            request.getDiscountPrice().compareTo(request.getPrice()) >= 0) {
            throw new BusinessException("INVALID_DISCOUNT", "Discount price must be less than regular price");
        }

        // Create product
        Product product = Product.builder()
                .name(request.getName())
                .sku(request.getSku())
                .description(request.getDescription())
                .price(request.getPrice())
                .discountPrice(request.getDiscountPrice())
                .category(category)
                .active(request.getActive() != null ? request.getActive() : true)
                .stockQuantity(request.getStockQuantity() != null ? request.getStockQuantity() : 0)
                .images(request.getImages() != null ? request.getImages() : new ArrayList<>())
                .brand(request.getBrand())
                .weight(request.getWeight())
                .dimensions(request.getDimensions())
                .featured(request.getFeatured() != null ? request.getFeatured() : false)
                .viewCount(0L)
                .soldCount(0)
                .reviewCount(0)
                .build();

        product = productRepository.save(product);
        
        // Publish product created event
        productEventProducer.sendProductCreatedEvent(product);
        
        log.info("Product created successfully: ID={}, SKU={}", product.getId(), product.getSku());
        
        return ProductResponse.fromProduct(product);
    }

    @Override
    @Transactional
    @CachePut(value = "products", key = "#id")
    @CacheEvict(value = "productsList", allEntries = true)
    public ProductResponse updateProduct(Long id, ProductRequest request) {
        log.info("Updating product ID: {}", id);

        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product", "id", id));

        // Check SKU uniqueness if changed
        if (!product.getSku().equals(request.getSku()) && productRepository.existsBySku(request.getSku())) {
            throw new BusinessException("PRODUCT_EXISTS", "Product with SKU " + request.getSku() + " already exists");
        }

        // Validate category
        Category category = categoryRepository.findById(request.getCategoryId())
                .orElseThrow(() -> new ResourceNotFoundException("Category", "id", request.getCategoryId()));

        // Validate discount price
        if (request.getDiscountPrice() != null && 
            request.getDiscountPrice().compareTo(request.getPrice()) >= 0) {
            throw new BusinessException("INVALID_DISCOUNT", "Discount price must be less than regular price");
        }

        // Update product
        product.setName(request.getName());
        product.setSku(request.getSku());
        product.setDescription(request.getDescription());
        product.setPrice(request.getPrice());
        product.setDiscountPrice(request.getDiscountPrice());
        product.setCategory(category);
        product.setActive(request.getActive() != null ? request.getActive() : product.getActive());
        product.setStockQuantity(request.getStockQuantity());
        product.setBrand(request.getBrand());
        product.setWeight(request.getWeight());
        product.setDimensions(request.getDimensions());
        product.setFeatured(request.getFeatured() != null ? request.getFeatured() : product.getFeatured());

        if (request.getImages() != null) {
            product.setImages(request.getImages());
        }

        product = productRepository.save(product);
        
        // Publish product updated event
        productEventProducer.sendProductUpdatedEvent(product);
        
        log.info("Product updated successfully: ID={}", id);
        
        return ProductResponse.fromProduct(product);
    }

    @Override
    @Transactional
    @CacheEvict(value = {"products", "productsList"}, allEntries = true)
    public void deleteProduct(Long id) {
        log.info("Deleting product ID: {}", id);

        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product", "id", id));

        productRepository.delete(product);
        
        // Publish product deleted event
        productEventProducer.sendProductDeletedEvent(id);
        
        log.info("Product deleted successfully: ID={}", id);
    }

    @Override
    @Transactional(readOnly = true)
    @Cacheable(value = "products", key = "#id")
    public ProductResponse getProductById(Long id) {
        log.debug("Fetching product by ID: {}", id);

        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product", "id", id));

        return ProductResponse.fromProduct(product);
    }

    @Override
    @Transactional(readOnly = true)
    @Cacheable(value = "products", key = "#sku")
    public ProductResponse getProductBySku(String sku) {
        log.debug("Fetching product by SKU: {}", sku);

        Product product = productRepository.findBySku(sku)
                .orElseThrow(() -> new ResourceNotFoundException("Product", "sku", sku));

        return ProductResponse.fromProduct(product);
    }

    @Override
    @Transactional(readOnly = true)
    @Cacheable(value = "productsList", key = "#page + '-' + #size + '-' + #sortBy + '-' + #sortDirection")
    public Page<ProductResponse> getAllProducts(int page, int size, String sortBy, String sortDirection) {
        log.debug("Fetching all products: page={}, size={}, sortBy={}, sortDirection={}", 
                  page, size, sortBy, sortDirection);

        Sort.Direction direction = "desc".equalsIgnoreCase(sortDirection) 
            ? Sort.Direction.DESC 
            : Sort.Direction.ASC;
        
        Pageable pageable = PageRequest.of(page, size, Sort.by(direction, sortBy));
        
        Page<Product> products = productRepository.findByActiveTrue(pageable);
        
        return products.map(ProductResponse::fromProduct);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ProductResponse> searchProducts(ProductSearchCriteria criteria) {
        log.debug("Searching products with criteria: {}", criteria);

        Specification<Product> spec = buildSpecification(criteria);
        
        int page = criteria.getPage() != null ? criteria.getPage() : 0;
        int size = criteria.getSize() != null ? criteria.getSize() : 20;
        String sortBy = criteria.getSortBy() != null ? criteria.getSortBy() : "createdAt";
        String sortDirection = criteria.getSortDirection() != null ? criteria.getSortDirection() : "desc";
        
        Sort.Direction direction = "desc".equalsIgnoreCase(sortDirection) 
            ? Sort.Direction.DESC 
            : Sort.Direction.ASC;
        
        Pageable pageable = PageRequest.of(page, size, Sort.by(direction, sortBy));
        
        Page<Product> products = productRepository.findAll(spec, pageable);
        
        return products.map(ProductResponse::fromProduct);
    }

    @Override
    @Transactional(readOnly = true)
    @Cacheable(value = "productsByCategory", key = "#categoryId + '-' + #page + '-' + #size")
    public Page<ProductResponse> getProductsByCategory(Long categoryId, int page, int size) {
        log.debug("Fetching products by category: categoryId={}, page={}, size={}", categoryId, page, size);

        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        
        Page<Product> products = productRepository.findByCategoryIdAndActiveTrue(categoryId, pageable);
        
        return products.map(ProductResponse::fromProduct);
    }

    @Override
    @Transactional(readOnly = true)
    @Cacheable(value = "featuredProducts", key = "#limit")
    public List<ProductResponse> getFeaturedProducts(int limit) {
        log.debug("Fetching featured products: limit={}", limit);

        Pageable pageable = PageRequest.of(0, limit);
        List<Product> products = productRepository.findFeaturedProducts(pageable);
        
        return products.stream()
                .map(ProductResponse::fromProduct)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    @Cacheable(value = "bestSellingProducts", key = "#limit")
    public List<ProductResponse> getBestSellingProducts(int limit) {
        log.debug("Fetching best selling products: limit={}", limit);

        Pageable pageable = PageRequest.of(0, limit);
        List<Product> products = productRepository.findBestSellingProducts(pageable);
        
        return products.stream()
                .map(ProductResponse::fromProduct)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    @Cacheable(value = "newArrivals", key = "#limit")
    public List<ProductResponse> getNewArrivals(int limit) {
        log.debug("Fetching new arrivals: limit={}", limit);

        Pageable pageable = PageRequest.of(0, limit);
        List<Product> products = productRepository.findNewArrivals(pageable);
        
        return products.stream()
                .map(ProductResponse::fromProduct)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    @Cacheable(value = "onSaleProducts", key = "#page + '-' + #size")
    public Page<ProductResponse> getOnSaleProducts(int page, int size) {
        log.debug("Fetching on-sale products: page={}, size={}", page, size);

        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        
        Page<Product> products = productRepository.findOnSaleProducts(pageable);
        
        return products.map(ProductResponse::fromProduct);
    }

    @Override
    @Transactional(readOnly = true)
    @Cacheable(value = "allBrands")
    public List<String> getAllBrands() {
        log.debug("Fetching all brands");
        return productRepository.findAllBrands();
    }

    @Override
    @Transactional
    public void incrementViewCount(Long productId) {
        log.debug("Incrementing view count for product: {}", productId);
        productRepository.incrementViewCount(productId);
    }

    @Override
    @Transactional
    @CacheEvict(value = {"products", "bestSellingProducts"}, allEntries = true)
    public void incrementSoldCount(Long productId, int quantity) {
        log.info("Incrementing sold count for product: {} by {}", productId, quantity);
        productRepository.incrementSoldCount(productId, quantity);
    }

    // Helper method to build JPA Specification for search
    private Specification<Product> buildSpecification(ProductSearchCriteria criteria) {
        return (root, query, criteriaBuilder) -> {
            List<jakarta.persistence.criteria.Predicate> predicates = new ArrayList<>();

            // Always filter active products
            predicates.add(criteriaBuilder.isTrue(root.get("active")));

            // Keyword search
            if (criteria.getKeyword() != null && !criteria.getKeyword().isEmpty()) {
                String likePattern = "%" + criteria.getKeyword().toLowerCase() + "%";
                predicates.add(criteriaBuilder.or(
                    criteriaBuilder.like(criteriaBuilder.lower(root.get("name")), likePattern),
                    criteriaBuilder.like(criteriaBuilder.lower(root.get("description")), likePattern),
                    criteriaBuilder.like(criteriaBuilder.lower(root.get("sku")), likePattern)
                ));
            }

            // Category filter
            if (criteria.getCategoryId() != null) {
                predicates.add(criteriaBuilder.equal(root.get("category").get("id"), criteria.getCategoryId()));
            }

            // Brand filter
            if (criteria.getBrand() != null && !criteria.getBrand().isEmpty()) {
                predicates.add(criteriaBuilder.equal(root.get("brand"), criteria.getBrand()));
            }

            // Price range filter
            if (criteria.getMinPrice() != null) {
                predicates.add(criteriaBuilder.greaterThanOrEqualTo(root.get("price"), criteria.getMinPrice()));
            }
            if (criteria.getMaxPrice() != null) {
                predicates.add(criteriaBuilder.lessThanOrEqualTo(root.get("price"), criteria.getMaxPrice()));
            }

            // On sale filter
            if (criteria.getOnSale() != null && criteria.getOnSale()) {
                predicates.add(criteriaBuilder.isNotNull(root.get("discountPrice")));
                predicates.add(criteriaBuilder.lessThan(root.get("discountPrice"), root.get("price")));
            }

            // Featured filter
            if (criteria.getFeatured() != null) {
                predicates.add(criteriaBuilder.equal(root.get("featured"), criteria.getFeatured()));
            }

            return criteriaBuilder.and(predicates.toArray(new jakarta.persistence.criteria.Predicate[0]));
        };
    }
}
