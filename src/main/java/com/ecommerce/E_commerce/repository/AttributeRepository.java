package com.ecommerce.E_commerce.repository;

import com.ecommerce.E_commerce.model.Attribute;
import com.ecommerce.E_commerce.model.CategoryAttributeType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface AttributeRepository extends JpaRepository<Attribute, Long> {
    Optional<Attribute> findByNameAndType(String name, CategoryAttributeType type);
    List<Attribute> findByName(String name);
    List<Attribute> findByNameAndIsActiveTrue(String name);
    Page<Attribute> findByIsActiveTrue(Pageable pageable);
    Page<Attribute> findByIsActiveFalse(Pageable pageable);
    Optional<Attribute> findByIdAndIsActiveTrue(Long id);
    Optional<Attribute> findByIdAndIsActiveFalse(Long id);
}
