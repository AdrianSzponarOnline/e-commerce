package com.ecommerce.E_commerce.repository;

import com.ecommerce.E_commerce.model.ERole;
import com.ecommerce.E_commerce.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Repository;

import java.util.Optional;


@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByEmail(String email);

    boolean existsByEmail(String email);
    Optional<User> findById(@NonNull Long id);

    @Query("SELECT u FROM User u JOIN u.roles r WHERE r.role = :roleName AND u.deletedAt IS NULL AND u.enabled = true")
    Optional<User> findByRoleName(@Param("roleName") ERole roleName);
}
