package com.ecommerce.E_commerce.service;

import com.ecommerce.E_commerce.model.*;
import com.ecommerce.E_commerce.repository.OrderRepository;
import com.ecommerce.E_commerce.repository.UserRepository;
import org.springframework.transaction.annotation.Transactional;
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
    @Transactional
    public void sendOrderConfirmation(Long orderId) {
        orderRepository.findByIdWithDetails(orderId).ifPresentOrElse(order -> {
            try {
                String customerEmail;
                String customerName;
                
                if (order.getUser() != null) {
                    // Registered user order
                    customerEmail = order.getUser().getEmail();
                    customerName = getUserName(order.getUser());
                } else {
                    // Guest order
                    customerEmail = order.getGuestEmail();
                    customerName = getGuestName(order);
                    if (customerEmail == null) {
                        logger.warn("Cannot send confirmation email for guest order: orderId={}, no email provided", order.getId());
                        return;
                    }
                }
                
                logger.info("Sending order confirmation email: orderId={}, email={}", order.getId(), customerEmail);

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
                        customerName,
                        order.getId(),
                        order.getCreatedAt(),
                        order.getStatus(),
                        buildItemsList(order),
                        buildAddressString(order),
                        order.getTotalAmount()
                );

                emailService.sendSimpleMail(customerEmail, "Potwierdzenie zamówienia #" + order.getId(), content);
                logger.info("Order confirmation email sent successfully: orderId={}", order.getId());

            } catch (Exception e) {
                logger.error("Failed to send order confirmation email: orderId={}", order.getId(), e);
            }
        }, () -> logger.warn("Order not found for confirmation email: orderId={}", orderId));
    }

    @Async
    @Transactional
    public void sendOrderShipped(Long orderId) {
        orderRepository.findByIdWithDetails(orderId).ifPresentOrElse(order -> {
            try {
                String customerEmail;
                String customerName;
                
                if (order.getUser() != null) {
                    // Registered user order
                    customerEmail = order.getUser().getEmail();
                    customerName = getUserName(order.getUser());
                } else {
                    // Guest order
                    customerEmail = order.getGuestEmail();
                    customerName = getGuestName(order);
                    if (customerEmail == null) {
                        logger.warn("Cannot send shipped email for guest order: orderId={}, no email provided", order.getId());
                        return;
                    }
                }
                
                logger.info("Sending order shipped email: orderId={}, email={}", order.getId(), customerEmail);
                String content = String.format(
                        """
                        Witaj %s,
                        
                        Dobra wiadomość! Twoje zamówienie #%d zostało wysłane.
                        
                        Adres dostawy:
                        %s
                        
                        Produkty w paczce:
                        %s
                        
                        %s
                        
                        Pozdrawiamy,
                        Zespół E-commerce
                        """,
                        customerName,
                        order.getId(),
                        buildAddressString(order),
                        buildItemsList(order),
                        order.getUser() != null ? "Śledź status swojego zamówienia w panelu użytkownika." : ""
                );

                emailService.sendSimpleMail(customerEmail, "Zamówienie #" + order.getId() + " zostało wysłane", content);
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
            orderRepository.findByIdWithDetails(orderId).ifPresent(order -> {
                try {
                    String clientName;
                    String clientEmail;
                    String clientInfo;
                    
                    if (order.getUser() != null) {
                        // Registered user order
                        User client = order.getUser();
                        clientName = getUserName(client);
                        clientEmail = client.getEmail();
                        clientInfo = String.format("%s (%s) - Zarejestrowany użytkownik", clientName, clientEmail);
                    } else {
                        // Guest order
                        clientName = getGuestName(order);
                        clientEmail = order.getGuestEmail();
                        String phone = order.getGuestPhone() != null ? ", tel: " + order.getGuestPhone() : "";
                        clientInfo = String.format("%s (%s%s) - Zamówienie gościa", clientName, clientEmail, phone);
                    }
                    
                    logger.info("Sending order confirmation to owner: orderId={}, ownerEmail={}", order.getId(), owner.getEmail());
                    String content = String.format(
                            """
                            Nowe zamówienie w systemie!
                            
                            Dane Klienta:
                            %s
                            
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
                            clientInfo,
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

    private String getGuestName(Order order) {
        if (order.getGuestFirstName() != null && order.getGuestLastName() != null) {
            return order.getGuestFirstName() + " " + order.getGuestLastName();
        } else if (order.getGuestFirstName() != null) {
            return order.getGuestFirstName();
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
