package org.example.onlinestoreapi.exception;

public class OrderHistoryNotFoundException extends RuntimeException {
    public OrderHistoryNotFoundException(Long id) {
        super("OrderHistory is not found with the id of: " + id);
    }
}
