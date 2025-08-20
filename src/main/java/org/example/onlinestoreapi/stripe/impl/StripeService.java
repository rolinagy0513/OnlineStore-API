package org.example.onlinestoreapi.stripe.impl;

import com.stripe.Stripe;
import com.stripe.exception.StripeException;
import com.stripe.model.checkout.Session;
import com.stripe.net.RequestOptions;
import com.stripe.param.checkout.SessionCreateParams;
import jakarta.annotation.PostConstruct;
import org.example.onlinestoreapi.stripe.IStripeService;
import org.example.onlinestoreapi.exception.PaymentProcessingException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

/**
 * Service implementation that handles all Stripe-related logic for order payments.
 *
 * <p>This service is responsible for creating checkout sessions in Stripe, generating
 * idempotency keys, attaching metadata, and returning a payment URL for the client.
 * The payment URL can later be used to resume the checkout process if interrupted.
 */
@Service
public class StripeService implements IStripeService {

    @Value("${stripe.api-key}")
    private String stripeApiKey;

    @Value("${app-domain}")
    private String appDomain;

    @Value("${stripe.currency}")
    private String currency;

    /**
     * Initializes the Stripe API key after bean construction.
     */
    @PostConstruct
    public void init() {
        Stripe.apiKey = stripeApiKey;
    }

    /**
     * Creates a Stripe Checkout session for the given order and returns the payment URL.
     *
     * <p>The session is initialized with metadata, an idempotency key, and order details.
     * If successful, Stripe provides a payment URL which the client can use to complete
     * the payment process.
     *
     * @param orderId       the ID of the order
     * @param amount        the total order amount
     * @param customerEmail the email address of the customer
     * @param uniqueKey     a unique key generated for idempotency
     * @return the Stripe Checkout session URL
     * @throws PaymentProcessingException if session creation fails or invalid data is provided
     */
    public String createCheckoutSession(Long orderId, BigDecimal amount, String customerEmail, String uniqueKey) {
        validateInputs(orderId, amount, customerEmail);

        String idempotencyKey = generateIdempotencyKey(uniqueKey,orderId);
        Map<String, String> metadata = createMetadata(orderId, uniqueKey);
        try {
            SessionCreateParams params = buildSessionParams(orderId, amount, customerEmail, metadata);
            RequestOptions options = buildRequestOptions(idempotencyKey);

            Session session = createSession(params, options);

            return session.getUrl();
        } catch (StripeException e) {
            throw new PaymentProcessingException();
        } catch (IllegalArgumentException e) {
            throw new PaymentProcessingException();
        }
    }

    /**
     * Validates the inputs required for session creation.
     *
     * @param orderId       the order ID
     * @param amount        the total amount (must be > 0)
     * @param customerEmail the customer's email (must not be null or empty)
     * @throws IllegalArgumentException if validation fails
     */
    private void validateInputs(Long orderId, BigDecimal amount, String customerEmail) {
        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("The price must be bigger than 0");
        }

        if (customerEmail == null || customerEmail.trim().isEmpty()) {
            throw new IllegalArgumentException("The customer's email can not be null");
        }
    }

    /**
     * Generates an idempotency key for the session request.
     *
     * @param uniqueKey generated from the order ID and timestamp
     * @param orderId   the order ID
     * @return an idempotency key string
     */
    private String generateIdempotencyKey(String uniqueKey, Long orderId) {
        return "order_" + orderId + "_" + uniqueKey;
    }

    /**
     * Creates metadata to attach to the Stripe session.
     *
     * @param orderId   the order ID
     * @param uniqueKey a unique session key
     * @return a map containing metadata
     */
    private Map<String, String> createMetadata(Long orderId, String uniqueKey) {
        Map<String, String> metadata = new HashMap<>();
        metadata.put("orderId", orderId.toString());
        metadata.put("sessionKey", uniqueKey);
        return metadata;
    }


    /**
     * Builds the Stripe {@link SessionCreateParams} for the checkout session.
     *
     * @param orderId       the order ID
     * @param amount        the order amount
     * @param customerEmail the customer's email
     * @param metadata      metadata to attach to the session
     * @return the configured {@link SessionCreateParams}
     */
    private SessionCreateParams buildSessionParams(Long orderId, BigDecimal amount,String customerEmail, Map<String, String> metadata) {

        SessionCreateParams.PaymentIntentData paymentIntentData = SessionCreateParams.PaymentIntentData.builder()
                .putAllMetadata(metadata)
                .build();

        return SessionCreateParams.builder()
                .setMode(SessionCreateParams.Mode.PAYMENT)
                .setSuccessUrl(appDomain + "/api/order/success?orderId=" + orderId)
                .setCancelUrl(appDomain + "/api/order/cancel?orderId=" + orderId)
                .setCustomerEmail(customerEmail)
                .setClientReferenceId(orderId.toString())
                .putAllMetadata(metadata)
                .setPaymentIntentData(paymentIntentData)
                .addLineItem(buildLineItem(orderId, amount))
                .build();
    }

    /**
     * Builds the {@link SessionCreateParams.LineItem} representing the order item.
     *
     * @param orderId the order ID
     * @param amount  the order amount
     * @return the configured {@link SessionCreateParams.LineItem}
     */
    private SessionCreateParams.LineItem buildLineItem(Long orderId, BigDecimal amount) {
        return SessionCreateParams.LineItem.builder()
                .setPriceData(
                        SessionCreateParams.LineItem.PriceData.builder()
                                .setCurrency(getCurrency())
                                .setUnitAmount(convertAmountToCents(amount))
                                .setProductData(
                                        SessionCreateParams.LineItem.PriceData.ProductData.builder()
                                                .setName("Order #" + orderId)
                                                .build()
                                )
                                .build()
                )
                .setQuantity(1L)
                .build();
    }

    /**
     * Returns the configured currency for the Stripe session.
     *
     * @return the currency code (e.g. "usd", "eur")
     */
    private String getCurrency() {
        return currency;
    }

    /**
     * Converts the given amount into cents (Stripe requires amounts in the smallest currency unit).
     *
     * @param amount the amount in standard currency units
     * @return the amount in cents
     */
    private Long convertAmountToCents(BigDecimal amount) {
        return amount.setScale(2, RoundingMode.HALF_UP)
                .multiply(new BigDecimal("100")).longValueExact();
    }

    /**
     * Builds Stripe {@link RequestOptions} with the given idempotency key.
     *
     * @param idempotencyKey the key ensuring request uniqueness
     * @return the configured {@link RequestOptions}
     */
    private RequestOptions buildRequestOptions(String idempotencyKey) {
        return RequestOptions.builder()
                .setIdempotencyKey(idempotencyKey)
                .build();
    }

    /**
     * Creates a new Stripe {@link Session}.
     *
     * @param params  the session creation parameters
     * @param options the request options including the idempotency key
     * @return the created {@link Session}
     * @throws StripeException if session creation fails
     */
    private Session createSession(SessionCreateParams params, RequestOptions options) throws StripeException {
        return Session.create(params, options);
    }
}
