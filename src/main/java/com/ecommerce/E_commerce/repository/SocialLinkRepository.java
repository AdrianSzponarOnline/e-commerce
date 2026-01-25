package com.ecommerce.E_commerce.repository;

import com.ecommerce.E_commerce.model.SocialLink;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SocialLinkRepository extends JpaRepository<SocialLink, Long> {
    List<SocialLink> findAllByIsActiveTrueOrderBySortOrderAsc();
    
    boolean existsByPlatformName(String platformName);
}
