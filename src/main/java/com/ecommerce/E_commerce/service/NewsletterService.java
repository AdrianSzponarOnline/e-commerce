package com.ecommerce.E_commerce.service;

import com.ecommerce.E_commerce.dto.newsletter.NewsletterSendRequestDTO;
import com.ecommerce.E_commerce.dto.newsletter.NewsletterSubscriptionDTO;
import com.ecommerce.E_commerce.dto.newsletter.NewsletterSubscribeRequestDTO;

import java.util.List;

public interface NewsletterService {

    /**
     * Subskrybuje podany adres e-mail do newslettera. Rzuca DuplicateResourceException, jeśli e-mail jest już zapisany.
     */
    NewsletterSubscriptionDTO subscribe(NewsletterSubscribeRequestDTO request);

    /**
     * Usuwa subskrypcję po adresie e-mail. Rzuca ResourceNotFoundException, jeśli subskrypcja nie istnieje.
     */
    void unsubscribe(String email);

    /**
     * Zwraca listę wszystkich subskrypcji (dla administratora).
     */
    List<NewsletterSubscriptionDTO> getAllSubscriptions();

    /**
     * Wysyła newsletter do wszystkich zapisanych adresów.
     */
    int sendNewsletter(NewsletterSendRequestDTO request);
}
