package org.example.onlinestoreapi.exception;

public class OrderNotFoundException extends RuntimeException {
    public OrderNotFoundException(Long id) {
        super("The order is not found with the id of: " + id);
    }
}
