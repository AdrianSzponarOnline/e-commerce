package com.ecommerce.E_commerce.repository;

import com.ecommerce.E_commerce.model.ShopSetting;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ShopSettingRepository extends JpaRepository<ShopSetting, Long> {
    Optional<ShopSetting> findByKey(String key);
    
    boolean existsByKey(String key);
}
