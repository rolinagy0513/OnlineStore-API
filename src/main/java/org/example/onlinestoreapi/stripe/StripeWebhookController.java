package org.example.onlinestoreapi.stripe;

import com.stripe.exception.SignatureVerificationException;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.logging.Logger;

/**
 * REST Controller for handling Stripe webhook events.
 * Provides endpoints for receiving and processing payment-related webhook notifications from Stripe.
 */
@RestController
@RequestMapping("/api/webhook")
@RequiredArgsConstructor
public class StripeWebhookController {

    private final WebhookService webhookService;

    @Value("${stripe.webhook-secret}")
    private String webhookSecret;

    /**
     * Handles incoming Stripe webhook events.
     * Validates the webhook signature and processes the event payload.
     *
     * @param payload the raw JSON payload from Stripe webhook
     * @param sigHeader the Stripe-Signature header for signature verification
     * @return ResponseEntity with HTTP status 200 if successful,
     *         400 for invalid signature, or 500 for processing errors
     */
    @PostMapping("/stripe")
    public ResponseEntity<String> handleStripeWebhook(
            @RequestBody String payload,
            @RequestHeader("Stripe-Signature") String sigHeader) {
        try {
            webhookService.handleWebhookEvent(payload, sigHeader, webhookSecret);
            return ResponseEntity.ok().build();

        } catch (SignatureVerificationException e) {
            return ResponseEntity.badRequest().body("Invalid signature");
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("Error processing webhook");
        }
    }
}