package com.ecommerce.E_commerce.repository;

import com.ecommerce.E_commerce.model.WebsitePage;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PageRepository extends JpaRepository<WebsitePage, Long> {
    Optional<WebsitePage> findBySlug(String slug);
    
    List<WebsitePage> findAllByIsActiveTrueOrderByTitleAsc();
    
    boolean existsBySlug(String slug);
    
    Page<WebsitePage> findBySlugContainingIgnoreCase(String slug, Pageable pageable);
    
    Page<WebsitePage> findByTitleContainingIgnoreCase(String title, Pageable pageable);
    
    Page<WebsitePage> findBySlugContainingIgnoreCaseOrTitleContainingIgnoreCase(
            String slug, String title, Pageable pageable);
}
