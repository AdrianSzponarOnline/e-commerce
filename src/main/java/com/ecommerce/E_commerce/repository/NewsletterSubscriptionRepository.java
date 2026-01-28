package com.ecommerce.E_commerce.repository;

import com.ecommerce.E_commerce.model.NewsletterSubscription;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface NewsletterSubscriptionRepository extends JpaRepository<NewsletterSubscription, Integer> {

    Optional<NewsletterSubscription> findByEmailIgnoreCase(String email);

    boolean existsByEmailIgnoreCase(String email);

    List<NewsletterSubscription> findAllByOrderBySubscribedAtDesc();
}
