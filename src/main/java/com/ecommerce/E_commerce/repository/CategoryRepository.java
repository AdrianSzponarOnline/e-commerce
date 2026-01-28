package com.ecommerce.E_commerce.repository;

import com.ecommerce.E_commerce.model.Category;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CategoryRepository extends JpaRepository<Category, Long> {

    boolean existsBySeoSlug(String seoSlug);

    @Override
    Optional<Category> findById(Long id);

    Optional<Category> findBySeoSlug(String seoSlug);

    @Query(value = """
    WITH RECURSIVE category_tree AS (
        SELECT id FROM categories WHERE id = :categoryId
        UNION ALL
        SELECT c.id FROM categories c
        INNER JOIN category_tree ct ON c.parent_id = ct.id
        )
        SELECT id FROM category_tree
    """, nativeQuery = true)
    List<Long> findAllSubcategoryIds(@Param("categoryId") Long categoryId);


    List<Category> findAllByIsActiveTrue();

    List<Category> findAllByParent_Id(Long parentId);
}


