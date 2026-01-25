package com.ecommerce.E_commerce.repository;

import com.ecommerce.E_commerce.model.FaqItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface FaqItemRepository extends JpaRepository<FaqItem, Long> {
    List<FaqItem> findAllByIsActiveTrueOrderBySortOrderAsc();
    
    boolean existsByQuestion(String question);
}
