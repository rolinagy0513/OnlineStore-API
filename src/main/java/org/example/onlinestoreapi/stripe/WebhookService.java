package org.example.onlinestoreapi.stripe;

import com.stripe.exception.SignatureVerificationException;
import com.stripe.model.Event;
import com.stripe.model.PaymentIntent;
import com.stripe.model.checkout.Session;
import com.stripe.net.Webhook;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.onlinestoreapi.ENUM.OrderStatus;
import org.example.onlinestoreapi.exception.OrderNotFoundException;
import org.example.onlinestoreapi.model.Order;
import org.example.onlinestoreapi.repository.OrderRepository;
import org.example.onlinestoreapi.security.user.User;
import org.example.onlinestoreapi.service.impl.OrderHistoryService;
import org.example.onlinestoreapi.service.impl.OrderService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

/**
 * Service class for processing Stripe webhook events.
 * Handles payment success and failure events and updates order status accordingly.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class WebhookService {

    private final OrderRepository orderRepository;
    private final OrderService orderService;
    private final OrderHistoryService orderHistoryService;

    /**
     * Processes a Stripe webhook event by verifying the signature and routing to appropriate handlers.
     *
     * @param payload the raw JSON payload from the webhook
     * @param sigHeader the Stripe-Signature header for verification
     * @param webhookSecret the secret key used to verify webhook signatures
     * @throws SignatureVerificationException if the signature verification fails
     */
    @Transactional
    public void handleWebhookEvent(String payload, String sigHeader, String webhookSecret)
            throws SignatureVerificationException {

        Event event = Webhook.constructEvent(payload, sigHeader, webhookSecret);

        switch (event.getType()) {
            case "checkout.session.completed":
                handleSuccessfulPayment(event);
                break;
            case "checkout.session.expired":
            case "payment_intent.payment_failed":
            case "checkout.session.async_payment_failed":
                handleFailedPayment(event);
                break;
            default:
        }
    }

    /**
     * Handles successful payment events from Stripe.
     * Updates the order status as PAID, records payment timestamp, and clears payment URL.
     *
     * @param event the Stripe event containing payment success information
     * @throws RuntimeException if orderId is missing, invalid, or order not found
     */
    @Transactional
    public void handleSuccessfulPayment(Event event) {
        Session session = (Session) event.getData().getObject();
        String orderIdStr = session.getMetadata().get("orderId");
        if (orderIdStr == null) {
            throw new RuntimeException("Missing orderId in metadata");
        }

        try {
            Long orderId = Long.parseLong(orderIdStr);
            Order order = orderRepository.findById(orderId)
                    .orElseThrow(() -> new OrderNotFoundException(orderId));

            if (order.getStatus() == OrderStatus.PAID) {
                return;
            }

            User user = order.getUser();
            order.setStatus(OrderStatus.PAID);
            orderService.evictOrderCache(user.getId());
            order.setPayedAt(LocalDateTime.now());
            order.setPaymentURL("");
            orderHistoryService.addToHistory(order);
            Order savedOrder = orderRepository.save(order);

        } catch (NumberFormatException e) {
            log.error("❌ Invalid orderId format: {}", orderIdStr);
            throw new RuntimeException("Invalid orderId format", e);
        } catch (OrderNotFoundException e) {
            log.error("❌ Order not found: {}", orderIdStr);
            throw new RuntimeException("Order not found", e);
        } catch (Exception e) {
            log.error("❌ Payment processing failed", e);
            throw new RuntimeException("Payment processing failed: " + e.getMessage(), e);
        }
    }

    /**
     * Handles failed payment events from Stripe.
     * Reverts the order status as CREATED and clears payment-related information.
     * Supports both Session and PaymentIntent event types.
     *
     * @param event the Stripe event containing payment failure information
     * @throws RuntimeException if processing the payment failure encounters an error
     */
    @Transactional
    void handleFailedPayment(Event event) {
        Object eventObject = event.getData().getObject();
        String orderIdStr = null;
        String sessionOrPaymentId = null;

        if (eventObject instanceof Session) {
            Session session = (Session) eventObject;
            sessionOrPaymentId = session.getId();
            orderIdStr = session.getMetadata().get("orderId");
        } else if (eventObject instanceof PaymentIntent) {
            PaymentIntent paymentIntent = (PaymentIntent) eventObject;
            sessionOrPaymentId = paymentIntent.getId();
            orderIdStr = paymentIntent.getMetadata().get("orderId");
        } else {
            log.error("❌ Unsupported event object type: {}", eventObject.getClass());
            return;
        }

        try {
            Long orderId = Long.parseLong(orderIdStr);
            Order order = orderRepository.findById(orderId)
                    .orElseThrow(() -> new OrderNotFoundException(orderId));

            if (order.getStatus() != OrderStatus.PENDING) {
                return;
            }

            order.setStatus(OrderStatus.CREATED);
            order.setSubmittedAt(null);
            order.setPaymentURL(null);
            orderRepository.save(order);
            orderService.evictOrderCache(order.getUser().getId());
        } catch (Exception e) {
            log.error("❌ Failed to process payment failure", e);
            throw new RuntimeException("Payment failure processing error", e);
        }
    }
}