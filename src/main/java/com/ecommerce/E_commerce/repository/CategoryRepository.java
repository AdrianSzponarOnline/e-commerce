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

    @Query(value =
            "WITH RECURSIVE CategorySubtree AS ( " +
                    "    SELECT * FROM categories WHERE id = :rootId AND deleted_at IS NULL " +
                    "    UNION ALL " +
                    "    SELECT c.* FROM categories c " +
                    "    INNER JOIN CategorySubtree cs ON c.parent_id = cs.id " +
                    "    WHERE c.deleted_at IS NULL " +
                    ") " +
                    "SELECT * FROM CategorySubtree",
            nativeQuery = true
    )
    List<Category> findSubtreeByRootId(@Param("rootId") Long rootId);


    List<Category> findAllByIsActiveTrue();

    List<Category> findAllByParent_Id(Long parentId);
}


