package org.example.onlinestoreapi.stripe;

import java.math.BigDecimal;

/**
 * Stripe interface for managing stirpe related logic.
 * <p>
 * Defines an operation to create a checkout session for stripe
 * </p>
 * <p>
 *      For more information look at {@link org.example.onlinestoreapi.stripe.impl.StripeService}
 * </p>
 */
public interface IStripeService {
    String createCheckoutSession(Long orderId, BigDecimal amount, String customerEmail, String uniqueKey);

}
