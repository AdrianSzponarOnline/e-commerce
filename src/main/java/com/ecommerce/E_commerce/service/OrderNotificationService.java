package com.ecommerce.E_commerce.service;

import com.ecommerce.E_commerce.model.*;
import com.ecommerce.E_commerce.repository.OrderRepository;
import com.ecommerce.E_commerce.repository.UserRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@Service
@RequiredArgsConstructor
public class OrderNotificationService {

    private static final Logger logger = LoggerFactory.getLogger(OrderNotificationService.class);
    private final EmailService emailService;
    private final UserRepository userRepository;
    private final OrderRepository orderRepository;

    @Async
    public void sendOrderConfirmation(Long orderId) {
        orderRepository.findById(orderId).ifPresentOrElse(order -> {
            try {
                User user = order.getUser();
                logger.info("Sending order confirmation email: orderId={}, userEmail={}", order.getId(), user.getEmail());

                String content = String.format(
                        """
                        Witaj %s,
                        
                        Dziękujemy za złożenie zamówienia!
                        
                        Szczegóły zamówienia:
                        ---------------------
                        Numer: #%d
                        Data: %s
                        Status: %s
                        
                        Produkty:
                        %s
                        Adres dostawy:
                        %s
                        
                        Łączna kwota: %.2f PLN
                        
                        Pozdrawiamy,
                        Zespół E-commerce
                        """,
                        getUserName(user),
                        order.getId(),
                        order.getCreatedAt(),
                        order.getStatus(),
                        buildItemsList(order),
                        buildAddressString(order),
                        order.getTotalAmount()
                );

                emailService.sendSimpleMail(user.getEmail(), "Potwierdzenie zamówienia #" + order.getId(), content);
                logger.info("Order confirmation email sent successfully: orderId={}", order.getId());

            } catch (Exception e) {
                logger.error("Failed to send order confirmation email: orderId={}", order.getId(), e);
            }
        }, () -> logger.warn("Order not found for confirmation email: orderId={}", orderId));
    }

    @Async
    @Transactional
    public void sendOrderShipped(Long orderId) {
        orderRepository.findById(orderId).ifPresentOrElse(order -> {
            try {
                User user = order.getUser();
                logger.info("Sending order shipped email: orderId={}, userEmail={}", order.getId(), user.getEmail());
                String content = String.format(
                        """
                        Witaj %s,
                        
                        Dobra wiadomość! Twoje zamówienie #%d zostało wysłane.
                        
                        Adres dostawy:
                        %s
                        
                        Produkty w paczce:
                        %s
                        
                        Śledź status swojego zamówienia w panelu użytkownika.
                        
                        Pozdrawiamy,
                        Zespół E-commerce
                        """,
                        getUserName(user),
                        order.getId(),
                        buildAddressString(order),
                        buildItemsList(order)
                );

                emailService.sendSimpleMail(user.getEmail(), "Zamówienie #" + order.getId() + " zostało wysłane", content);
                logger.info("Order shipped email sent successfully: orderId={}", order.getId());

            } catch (Exception e) {
                logger.error("Failed to send order shipped email: orderId={}", order.getId(), e);
            }
        }, () -> logger.warn("Order not found for shipped email: orderId={}", orderId));
    }

    @Async
    @Transactional
    public void sendOrderConfirmedToOwner(Long orderId) {
        userRepository.findByRoleName(ERole.ROLE_OWNER).ifPresent(owner -> {
            orderRepository.findById(orderId).ifPresent(order -> {
                try {
                    User client = order.getUser();
                    logger.info("Sending order confirmation to owner: orderId={}, ownerEmail={}", order.getId(), owner.getEmail());
                    String content = String.format(
                            """
                            Nowe zamówienie w systemie!
                            
                            Dane Klienta:
                            %s (%s)
                            
                            Szczegóły:
                            Numer: #%d
                            Data: %s
                            Status: %s
                            
                            Produkty:
                            %s
                            Adres dostawy:
                            %s
                            
                            Wartość zamówienia: %.2f PLN
                            """,
                            getUserName(client),
                            client.getEmail(),
                            order.getId(),
                            order.getCreatedAt(),
                            order.getStatus(),
                            buildItemsList(order),
                            buildAddressString(order),
                            order.getTotalAmount()
                    );

                    emailService.sendSimpleMail(owner.getEmail(), "ADMIN: Nowe zamówienie #" + order.getId(), content);
                    logger.info("Order confirmation email sent to owner successfully: orderId={}", order.getId());

                } catch (Exception e) {
                    logger.error("Failed to send order confirmation email to owner: orderId={}", order.getId(), e);
                }
            });
        });
    }


    private String getUserName(User user) {
        if (user.getFirstName() != null && user.getLastName() != null) {
            return user.getFirstName() + " " + user.getLastName();
        } else if (user.getFirstName() != null) {
            return user.getFirstName();
        }
        return "Klient";
    }

    private String buildItemsList(Order order) {
        StringBuilder sb = new StringBuilder();
        for (OrderItem item : order.getItems()) {
            BigDecimal totalItemPrice = item.getPrice().multiply(BigDecimal.valueOf(item.getQuantity()));
            sb.append(String.format("- %s x%d - %.2f PLN\n",
                    item.getProduct().getName(),
                    item.getQuantity(),
                    totalItemPrice));
        }
        return sb.toString();
    }

    private String buildAddressString(Order order) {
        if (order.getAddress() == null) {
            return "Brak danych adresowych (Odbiór osobisty lub błąd)";
        }
        Address addr = order.getAddress();
        String line2 = addr.getLine2() != null && !addr.getLine2().isEmpty() ? "\n" + addr.getLine2() : "";

        return String.format("%s%s\n%s %s\n%s",
                addr.getLine1(),
                line2,
                addr.getPostalCode(),
                addr.getCity(),
                addr.getCountry());
    }
}
