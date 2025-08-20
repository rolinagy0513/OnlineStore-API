package org.example.onlinestoreapi.exception;

public class PaymentProcessingException extends RuntimeException {
    public PaymentProcessingException() {
        super("Failed to create payment section");
    }
}
