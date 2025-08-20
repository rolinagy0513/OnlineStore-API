package org.example.onlinestoreapi.controller;

import lombok.RequiredArgsConstructor;
import org.example.onlinestoreapi.DTO.OrderResponseDTO;
import org.example.onlinestoreapi.DTO.SubmitDTO;
import org.example.onlinestoreapi.ENUM.OrderStatus;
import org.example.onlinestoreapi.exception.OrderNotFoundException;
import org.example.onlinestoreapi.model.Order;
import org.example.onlinestoreapi.repository.OrderRepository;
import org.example.onlinestoreapi.security.user.UserService;
import org.example.onlinestoreapi.service.impl.OrderService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.concurrent.CompletableFuture;

/**
 * Controller for handling order-related operations including retrieval, submission,
 * deletion, and payment status handling
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/order")
public class OrderController {

    private final OrderRepository repository;
    private final OrderService service;
    private final UserService userService;
    private final OrderService orderService;
    private static final Logger logger = LoggerFactory.getLogger(OrderController.class);

    /**
     * Retrieves all orders for the current authenticated user
     * @return CompletableFuture containing ResponseEntity with OrderResponseDTO
     */
    @GetMapping("/getAll")
    public CompletableFuture<ResponseEntity<OrderResponseDTO>> getAllOrder(){
        Long userId = userService.getCurrentUserId();
        return service.getAllOrderByUser(userId)
                .thenApply(ResponseEntity::ok);
    }

    /**
     * Submits an order for processing and payment
     * @param id The id of the order to submit
     * @return SubmitDTO containing submission details and payment information
     */
    @PostMapping("/submitOrder/{id}")
    public SubmitDTO submitOrder(
            @PathVariable Long id
    ){
        return service.submitOrder(id);
    }

    /**
     * Deletes a specific order
     * @param orderId The id of the order to delete
     */
    @DeleteMapping("/delete/{orderId}")
    public void deleteOrder(
            @PathVariable Long orderId
    ){
        Long userId = userService.getCurrentUserId();
        service.deleteOrder(orderId,userId);
    }

    /**
     * Handles successful payment redirection from payment gateway
     * @param orderId The id of the order that was successfully paid
     * @return ResponseEntity with success message
     */
    @GetMapping("/success")
    public ResponseEntity<String> paymentSuccess(@RequestParam Long orderId) {
        logger.info("Frontend redirected to success page for order: {}", orderId);
        Order order = repository.findById(orderId)
                .orElseThrow(() -> new OrderNotFoundException(orderId));

        return ResponseEntity.ok("Payment successful! Your order is being processed.");
    }

    /**
     * Handles canceled payment redirection from payment gateway
     * @param orderId The id of the order that had payment canceled
     * @return ResponseEntity with cancellation message
     */
    @GetMapping("/cancel")
    public ResponseEntity<String> paymentCancel(@RequestParam Long orderId) {
        logger.info("Frontend redirected to cancel page for order: {}", orderId);
        Order order = repository.findById(orderId)
                .orElseThrow(() -> new OrderNotFoundException(orderId));

        if (order.getStatus() != OrderStatus.PAID) {
            order.setStatus(OrderStatus.CREATED);
            order.setPaymentURL(null);
            order.setSubmittedAt(null);
            orderService.evictOrderCache(order.getUser().getId());
            repository.save(order);
        }

        return ResponseEntity.ok("Payment was canceled. You can try again later.");
    }
}