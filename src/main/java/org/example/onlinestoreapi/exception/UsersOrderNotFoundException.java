package org.example.onlinestoreapi.exception;

public class UsersOrderNotFoundException extends RuntimeException {
    public UsersOrderNotFoundException(Long id) {
        super("The user doesn't have an order with the id of: " + id);
    }
}
