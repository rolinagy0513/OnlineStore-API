package org.example.onlinestoreapi.exception;

public class UserNotFoundException extends RuntimeException {
    public UserNotFoundException(String email) {
        super("The user is not found with the id of : " + email);
    }
}
