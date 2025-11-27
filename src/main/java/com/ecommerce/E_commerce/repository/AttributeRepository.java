package com.ecommerce.E_commerce.repository;

import com.ecommerce.E_commerce.model.Attribute;
import com.ecommerce.E_commerce.model.CategoryAttributeType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

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
    
    @Query(value = "SELECT * FROM attributes WHERE id = :id AND deleted_at IS NOT NULL", nativeQuery = true)
    Optional<Attribute> findDeletedById(@Param("id") Long id);
    
    @Query(value = "SELECT * FROM attributes WHERE deleted_at IS NOT NULL", 
           countQuery = "SELECT count(*) FROM attributes WHERE deleted_at IS NOT NULL", 
           nativeQuery = true)
    Page<Attribute> findAllDeleted(Pageable pageable);

    @Query("SELECT DISTINCT a.name FROM Attribute a WHERE a.isActive = true")
    List<String> findAllActiveAttributeNames();
}
