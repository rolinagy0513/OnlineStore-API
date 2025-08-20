package org.example.onlinestoreapi.exception;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * GlobalExceptionHandler.java
 * - Centralized exception handler for REST controllers.
 * - Handles various custom and database exceptions, returning
 * - meaningful HTTP responses with error details and appropriate status codes.
 * - Provides consistent error response structure for the API.
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * Internal record to standardize API error responses.
     */
    private record ApiError(String message, String details, LocalDateTime timestamp){}

    /**
     * Handles UserNotFoundException.
     * Returns HTTP 404 with error details when a user resource is not found.
     *
     * @param ex the UserNotFoundException thrown
     * @return ResponseEntity with ApiError and HTTP 404 status
     */
    @ExceptionHandler(UserNotFoundException.class)
    public ResponseEntity<ApiError> handleUserNotFoundException(UserNotFoundException ex){
        return new ResponseEntity<>(
                new ApiError(ex.getMessage(), "User resource was not found", LocalDateTime.now()),
                HttpStatus.NOT_FOUND
        );
    }

    /**
     * Handles CategoryNotFoundException.
     * Returns HTTP 404 with error details when a category resource is not found.
     *
     * @param ex the CategoryNotFoundException thrown
     * @return ResponseEntity with ApiError and HTTP 404 status
     */
    @ExceptionHandler(CategoryNotFoundException.class)
    public ResponseEntity<ApiError> handleCategoryNotFoundException(CategoryNotFoundException ex){
        return new ResponseEntity<>(
                new ApiError(ex.getMessage(), "Category resource was not found", LocalDateTime.now()),
                HttpStatus.NOT_FOUND
        );
    }

    /**
     * Handles ProductNotFoundException.
     * Returns HTTP 404 with error details when a product resource is not found.
     *
     * @param ex the ProductNotFoundException thrown
     * @return ResponseEntity with ApiError and HTTP 404 status
     */
    @ExceptionHandler(ProductNotFoundException.class)
    public ResponseEntity<ApiError> handleProductNotFoundException(ProductNotFoundException ex){
        return new ResponseEntity<>(
                new ApiError(ex.getMessage(), "Product resource was not found", LocalDateTime.now()),
                HttpStatus.NOT_FOUND
        );
    }

    /**
     * Handles OrderNotFoundException.
     * Returns HTTP 404 with error details when an order resource is not found.
     *
     * @param ex the OrderNotFoundException thrown
     * @return ResponseEntity with ApiError and HTTP 404 status
     */
    @ExceptionHandler(OrderNotFoundException.class)
    public ResponseEntity<ApiError> handleOrderNotFoundException(OrderNotFoundException ex){
        return new ResponseEntity<>(
                new ApiError(ex.getMessage(),"Order resource was not found", LocalDateTime.now()),
                HttpStatus.NOT_FOUND
        );
    }

    /**
     * Handles OrderHistoryNotFoundException.
     * Returns HTTP 404 with error details when an order history resource is not found.
     *
     * @param ex the OrderHistoryNotFoundException thrown
     * @return ResponseEntity with ApiError and HTTP 404 status
     */
    @ExceptionHandler(OrderHistoryNotFoundException.class)
    public ResponseEntity<ApiError> handelOrderHistoryNotFoundException(OrderHistoryNotFoundException ex){
        return new ResponseEntity<>(
                new ApiError(ex.getMessage(),"OrderHistory resource was not found",LocalDateTime.now()),
                HttpStatus.NOT_FOUND
        );
    }

    /**
     * Handles UsersOrderNotFoundException.
     * Returns HTTP 404 with error details when a user's order resource is not found.
     *
     * @param ex the UsersOrderNotFoundException thrown
     * @return ResponseEntity with ApiError and HTTP 404 status
     */
    @ExceptionHandler(UsersOrderNotFoundException.class)
    public ResponseEntity<ApiError> handleUsersOrderNotFoundException(UsersOrderNotFoundException ex){
        return new ResponseEntity<>(
                new ApiError(ex.getMessage(),"Users order resource was not found", LocalDateTime.now()),
                HttpStatus.NOT_FOUND
        );
    }

    /**
     * Handles InsufficientStockException.
     * Returns HTTP 400 when there's not enough stock to complete an operation.
     *
     * @param ex the InsufficientStockException thrown
     * @return ResponseEntity with ApiError and HTTP 400 status
     */
    @ExceptionHandler(InsufficientStockException.class)
    public ResponseEntity<ApiError> handleInsufficentStockException(InsufficientStockException ex){
        return new ResponseEntity<>(
                new ApiError(ex.getMessage(), "The method can not be executed" , LocalDateTime.now()),
                HttpStatus.BAD_REQUEST
        );
    }

    /**
     * Handles OrderModificationNotAllowedException.
     * Returns HTTP 400 when an order modification is attempted in an illegal state.
     *
     * @param ex the OrderModificationNotAllowedException thrown
     * @return ResponseEntity with ApiError and HTTP 400 status
     */
    @ExceptionHandler(OrderModificationNotAllowedException.class)
    public ResponseEntity<ApiError> handleOrderModificationException(OrderModificationNotAllowedException ex){
        return new ResponseEntity<>(
                new ApiError(ex.getMessage(),"Illegal state",LocalDateTime.now()),
                HttpStatus.BAD_REQUEST
        );
    }

    /**
     * Handles PaymentProcessingException.
     * Returns HTTP 417 (Expectation Failed) when payment processing fails.
     *
     * @param ex the PaymentProcessingException thrown
     * @return ResponseEntity with ApiError and HTTP 417 status
     */
    @ExceptionHandler(PaymentProcessingException.class)
    public ResponseEntity<ApiError> handlePaymentProcessingException(PaymentProcessingException ex){
        return new ResponseEntity<>(
                new ApiError(ex.getMessage(), "Payment error",LocalDateTime.now()),
                HttpStatus.EXPECTATION_FAILED
        );
    }

    /**
     * Handles DataIntegrityViolationException.
     * Returns HTTP 409 (Conflict) with specific messages for different database constraint violations.
     * Detects unique constraint, foreign key, and not-null violations.
     *
     * @param ex the DataIntegrityViolationException thrown
     * @return ResponseEntity with ApiError and HTTP 409 status
     */
    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ApiError> handleDataIntegrityViolation(DataIntegrityViolationException ex){

        String message  = "Data integrity violation";

        Throwable rootCause = ex.getRootCause();
        if (rootCause != null) {
            String rootMessage = rootCause.getMessage();
            if (rootMessage.contains("unique constraint") || rootMessage.contains("duplicate key")) {
                message = "Duplicate entry: this value already exists and must be unique";
            } else if (rootMessage.contains("foreign key constraint")) {
                message = "Reference error: related entity not found";
            } else if (rootMessage.contains("not-null constraint")) {
                message = "Missing required field";
            }
        }

        return new ResponseEntity<>(
                new ApiError(message, "Database constraint violation", LocalDateTime.now()),
                HttpStatus.CONFLICT
        );
    }

    /**
     * Handles ObjectOptimisticLockingFailureException.
     * Returns HTTP 409 (Conflict) when concurrent modification is detected.
     * Used when multiple users try to modify the same resource simultaneously.
     *
     * @param ex the ObjectOptimisticLockingFailureException thrown
     * @return ResponseEntity with ApiError and HTTP 409 status
     */
    @ExceptionHandler(ObjectOptimisticLockingFailureException.class)
    public ResponseEntity<ApiError> handleOptimisticLockingFailure(ObjectOptimisticLockingFailureException ex){
        return new ResponseEntity<>(
                new ApiError(
                        "The product was modified be another user",
                        "Please try again later.",
                        LocalDateTime.now()),
                HttpStatus.CONFLICT
        );
    }

    /**
     * Handles MethodArgumentNotValidException.
     * Returns HTTP 400 with field-level validation errors when request body validation fails.
     * Collects all validation errors from BindingResult.
     *
     * @param ex the MethodArgumentNotValidException thrown
     * @return ResponseEntity with Map of field errors and HTTP 400 status
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, String>> handleValidationExceptions(MethodArgumentNotValidException ex) {
        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getAllErrors().forEach(error -> {
            String fieldName = ((FieldError) error).getField();
            String errorMessage = error.getDefaultMessage();
            errors.put(fieldName, errorMessage);
        });
        return new ResponseEntity<>(errors, HttpStatus.BAD_REQUEST);
    }

    /**
     * Handles all uncaught exceptions.
     * Returns HTTP 500 (Internal Server Error) as a fallback for unexpected errors.
     *
     * @param ex the Exception thrown
     * @return ResponseEntity with ApiError and HTTP 500 status
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiError> handleGenericException(Exception ex){
        return new ResponseEntity<>(
                new ApiError("Internal Server Error", ex.getMessage(), LocalDateTime.now()),
                HttpStatus.INTERNAL_SERVER_ERROR
        );
    }

}
