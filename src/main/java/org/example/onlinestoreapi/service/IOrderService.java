package org.example.onlinestoreapi.service;

import org.example.onlinestoreapi.DTO.OrderResponseDTO;
import org.example.onlinestoreapi.DTO.SubmitDTO;
import org.example.onlinestoreapi.model.Order;

import java.util.concurrent.CompletableFuture;

/**
 * Order interface for managing order related logic.
 * <p>
 * Defines operations for submitting, deleting, getting the order.
 * Methods that are checking that a user created an order or not yet.
 * </p>
 * <p>
 *      For more information look at {@link org.example.onlinestoreapi.service.impl.OrderService}
 * </p>
 */
public interface IOrderService {
    Order getOrCreateAnOrder();

    Order updateTotal(Long id);

    CompletableFuture<OrderResponseDTO> getAllOrderByUser(Long userId);

    SubmitDTO submitOrder(Long id);

    void deleteOrder(Long orderId,Long userId);
}
