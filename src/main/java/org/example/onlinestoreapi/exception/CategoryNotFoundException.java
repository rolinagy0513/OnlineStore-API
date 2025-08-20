package org.example.onlinestoreapi.exception;

public class CategoryNotFoundException extends RuntimeException {
  public CategoryNotFoundException(Long id) {
    super("Category is not found with the id of: " + id);
  }
}
