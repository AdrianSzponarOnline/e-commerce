package com.ecommerce.E_commerce.controller;

import com.ecommerce.E_commerce.dto.newsletter.NewsletterSendRequestDTO;
import com.ecommerce.E_commerce.dto.newsletter.NewsletterSubscriptionDTO;
import com.ecommerce.E_commerce.dto.newsletter.NewsletterSubscribeRequestDTO;
import com.ecommerce.E_commerce.service.NewsletterService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/newsletter")
@RequiredArgsConstructor
public class NewsletterController {

    private static final Logger logger = LoggerFactory.getLogger(NewsletterController.class);
    private final NewsletterService newsletterService;

    @PostMapping("/subscribe")
    public ResponseEntity<NewsletterSubscriptionDTO> subscribe(@Valid @RequestBody NewsletterSubscribeRequestDTO request) {
        logger.info("POST /api/newsletter/subscribe - email={}", request.email());
        NewsletterSubscriptionDTO created = newsletterService.subscribe(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @PostMapping("/unsubscribe")
    public ResponseEntity<Void> unsubscribe(@Valid @RequestBody NewsletterSubscribeRequestDTO request) {
        logger.info("POST /api/newsletter/unsubscribe - email={}", request.email());
        newsletterService.unsubscribe(request.email());
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/subscriptions")
    @PreAuthorize("hasRole('OWNER')")
    public ResponseEntity<List<NewsletterSubscriptionDTO>> getAllSubscriptions() {
        logger.info("GET /api/newsletter/subscriptions");
        List<NewsletterSubscriptionDTO> list = newsletterService.getAllSubscriptions();
        return ResponseEntity.ok(list);
    }

    @PostMapping("/send")
    @PreAuthorize("hasRole('OWNER')")
    public ResponseEntity<Map<String, Object>> sendNewsletter(@Valid @RequestBody NewsletterSendRequestDTO request) {
        logger.info("POST /api/newsletter/send - subject={}", request.subject());
        int sent = newsletterService.sendNewsletter(request);
        return ResponseEntity.ok(Map.of("sentCount", sent, "message", "Wysłano do " + sent + " odbiorców."));
    }
}
