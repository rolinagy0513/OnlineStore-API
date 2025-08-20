package org.example.onlinestoreapi.exception;

public class ProductNotFoundException extends RuntimeException {
    public ProductNotFoundException(Long id) {
        super("The product is not found with the id of: " + id);
    }
}
