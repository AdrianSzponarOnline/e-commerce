package com.ecommerce.E_commerce.repository;

import com.ecommerce.E_commerce.model.CategoryAttribute;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CategoryAttributeRepository extends JpaRepository<CategoryAttribute, Long> {
    List<CategoryAttribute> findAllByCategory_Id(Long categoryId);
    boolean existsByCategory_IdAndName(Long categoryId, String name);
}


