package com.ecommerce.E_commerce.repository;

import com.ecommerce.E_commerce.model.CategoryAttribute;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CategoryAttributeRepository extends JpaRepository<CategoryAttribute, Long> {
    List<CategoryAttribute> findAllByCategory_Id(Long categoryId);
    Optional<CategoryAttribute> findByCategoryIdAndAttributeId(Long categoryId, Long attributeId);
    
    @Query("SELECT COUNT(ca) > 0 FROM CategoryAttribute ca WHERE ca.category.id = :categoryId AND ca.attribute.name = :name")
    boolean existsByCategoryIdAndAttributeName(@Param("categoryId") Long categoryId, @Param("name") String name);
}


