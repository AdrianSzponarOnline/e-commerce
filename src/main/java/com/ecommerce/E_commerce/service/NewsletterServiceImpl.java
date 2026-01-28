package com.ecommerce.E_commerce.service;

import com.ecommerce.E_commerce.dto.newsletter.NewsletterSendRequestDTO;
import com.ecommerce.E_commerce.dto.newsletter.NewsletterSubscriptionDTO;
import com.ecommerce.E_commerce.dto.newsletter.NewsletterSubscribeRequestDTO;
import com.ecommerce.E_commerce.exception.DuplicateResourceException;
import com.ecommerce.E_commerce.exception.ResourceNotFoundException;
import com.ecommerce.E_commerce.model.NewsletterSubscription;
import com.ecommerce.E_commerce.repository.NewsletterSubscriptionRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;

@Service
@RequiredArgsConstructor
public class NewsletterServiceImpl implements NewsletterService {

    private static final Logger logger = LoggerFactory.getLogger(NewsletterServiceImpl.class);
    private final NewsletterSubscriptionRepository subscriptionRepository;
    private final EmailService emailService;

    @Override
    @Transactional
    public NewsletterSubscriptionDTO subscribe(NewsletterSubscribeRequestDTO request) {
        String email = request.email().trim().toLowerCase();
        if (subscriptionRepository.existsByEmailIgnoreCase(email)) {
            throw new DuplicateResourceException("Ten adres e-mail jest już zapisany do newslettera.");
        }
        NewsletterSubscription subscription = new NewsletterSubscription();
        subscription.setEmail(email);
        subscription.setSubscribedAt(Instant.now());
        NewsletterSubscription saved = subscriptionRepository.save(subscription);
        logger.info("Newsletter subscription created: email={}, id={}", email, saved.getId());
        return toDTO(saved);
    }

    @Override
    @Transactional
    public void unsubscribe(String email) {
        String normalized = email == null ? "" : email.trim().toLowerCase();
        NewsletterSubscription subscription = subscriptionRepository.findByEmailIgnoreCase(normalized)
                .orElseThrow(() -> new ResourceNotFoundException("Nie znaleziono subskrypcji dla podanego adresu e-mail."));
        subscriptionRepository.delete(subscription);
        logger.info("Newsletter subscription removed: email={}", normalized);
    }

    @Override
    @Transactional(readOnly = true)
    public List<NewsletterSubscriptionDTO> getAllSubscriptions() {
        return subscriptionRepository.findAllByOrderBySubscribedAtDesc().stream()
                .map(this::toDTO)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public int sendNewsletter(NewsletterSendRequestDTO request) {
        List<NewsletterSubscription> subscriptions = subscriptionRepository.findAllByOrderBySubscribedAtDesc();
        int sent = 0;
        for (NewsletterSubscription sub : subscriptions) {
            try {
                emailService.sendSimpleMail(sub.getEmail(), request.subject(), request.content());
                sent++;
            } catch (Exception e) {
                logger.error("Failed to send newsletter to {}: {}", sub.getEmail(), e.getMessage());
            }
        }
        logger.info("Newsletter sent: {} / {} recipients", sent, subscriptions.size());
        return sent;
    }

    private NewsletterSubscriptionDTO toDTO(NewsletterSubscription s) {
        Long userId = s.getUser() != null ? s.getUser().getId() : null;
        return new NewsletterSubscriptionDTO(
                s.getId(),
                s.getEmail(),
                s.getSubscribedAt(),
                userId
        );
    }
}
