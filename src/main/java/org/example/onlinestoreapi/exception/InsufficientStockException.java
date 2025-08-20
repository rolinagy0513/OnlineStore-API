package org.example.onlinestoreapi.exception;

public class InsufficientStockException extends RuntimeException {
    public InsufficientStockException(Long productId, Integer existingQuantity, Integer newQuantity)
    {
        super("The product with the id of: " + productId + " Has only: " + existingQuantity + " in stock while trying to remove: " + newQuantity);
    }
}
