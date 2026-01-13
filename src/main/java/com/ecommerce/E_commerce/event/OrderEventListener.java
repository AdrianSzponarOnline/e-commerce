package com.ecommerce.E_commerce.event;

import com.ecommerce.E_commerce.service.OrderNotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
@RequiredArgsConstructor
@Slf4j
public class OrderEventListener {
    private final OrderNotificationService notificationService;

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleOrderCreated(OrderCreatedEvent event) {
        log.info("Received order created event for orderId={}. Sending notification...", event.orderId());
        try {
            notificationService.sendOrderConfirmation(event.orderId());
        } catch (Exception e) {
            log.error("Failed to send order confirmation email", e);
        }
    }
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleOrderConfirmedEvent(OrderConfirmedEvent event) {
        log.info("Received order confirmed event for orderId={}. Sending notification...", event.orderId());
        try {
            notificationService.sendOrderConfirmedToOwner(event.orderId());
        }catch (Exception e) {
            log.error("Failed to send order confirmed email", e);
        }
    }
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleOrderShippedEvent(OrderShippedEvent event) {
        log.info("Received order shipped event for orderId={}. Sending notification...", event.orderId());
        try {
            notificationService.sendOrderShipped(event.orderId());
        }catch (Exception e) {
            log.error("Failed to send order confirmed email", e);
        }
    }
}
