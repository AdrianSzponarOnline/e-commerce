package com.ecommerce.E_commerce.repository;

import com.ecommerce.E_commerce.model.Address;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AddressRepository extends JpaRepository<Address, Long> {

    List<Address> findByUserId(Long userId);

    List<Address> findByUserIdAndIsActive(Long userId, Boolean isActive);

    long countByUserId(Long userId);
}

