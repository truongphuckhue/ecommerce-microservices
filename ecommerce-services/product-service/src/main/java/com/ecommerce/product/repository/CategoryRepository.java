package com.ecommerce.product.repository;

import com.ecommerce.product.entity.Category;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CategoryRepository extends JpaRepository<Category, Long> {
    
    Optional<Category> findBySlug(String slug);
    
    boolean existsBySlug(String slug);

    List<Category> findByParentIsNullAndActiveTrue();

    List<Category> findByParentIdAndActiveTrue(Long parentId);

    @Query("SELECT c FROM Category c WHERE c.parent.id = :parentId AND c.active = true ORDER BY c.displayOrder, c.name")
    List<Category> findChildCategories(@Param("parentId") Long parentId);

    @Query("SELECT c FROM Category c WHERE c.parent IS NULL AND c.active = true ORDER BY c.displayOrder, c.name")
    List<Category> findRootCategories();

    @Query("SELECT COUNT(c) FROM Category c WHERE c.parent.id = :parentId")
    long countByParentId(@Param("parentId") Long parentId);

    List<Category> findByActiveTrueOrderByDisplayOrderAscNameAsc();
}
