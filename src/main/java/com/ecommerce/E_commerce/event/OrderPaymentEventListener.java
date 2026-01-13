package com.ecommerce.E_commerce.event;

import com.ecommerce.E_commerce.model.PaymentStatus;
import com.ecommerce.E_commerce.service.OrderService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
@RequiredArgsConstructor
public class OrderPaymentEventListener {
    private final OrderService orderService;
    private static final Logger logger = LoggerFactory.getLogger(OrderPaymentEventListener.class);

    @TransactionalEventListener
    public void handlePaymentChange(PaymentStatusChangedEvent event) {
        logger.info("Received PaymentStatusChangedEvent: paymentId={}, orderId={}, status={}",
                event.paymentId(), event.orderId(), event.newStatus());

        if (event.newStatus() == PaymentStatus.COMPLETED) {
            logger.debug("Initiating order confirmation for orderId={}", event.orderId());
            orderService.confirmOrderPayment(event.orderId());
        }
        else if (event.newStatus() == PaymentStatus.FAILED || event.newStatus() == PaymentStatus.CANCELLED) {
            if (event.oldStatus() == PaymentStatus.PENDING) {
                logger.warn("Payment failed/cancelled for pending orderId={}. Triggering handler.", event.orderId());
                orderService.handlePaymentFailure(event.orderId(), event.newStatus().toString());
            } else {
                logger.debug("Ignoring payment failure for non-pending transaction. OldStatus={}", event.oldStatus());
            }
        }
    }
}
