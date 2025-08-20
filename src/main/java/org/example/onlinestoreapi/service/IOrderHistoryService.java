package org.example.onlinestoreapi.service;

import org.example.onlinestoreapi.DTO.OrderHistoryDTO;
import org.example.onlinestoreapi.model.Order;
import org.example.onlinestoreapi.model.OrderHistory;

import java.util.concurrent.CompletableFuture;

/**
 * OrderHistory interface for managing history related logic.
 * <p>
 * Defines operations for creating a history, adding to it or retrieving it.
 * </p>
 * <p>
 *      For more information look at {@link org.example.onlinestoreapi.service.impl.OrderHistoryService}
 * </p>
 */
public interface IOrderHistoryService {
    OrderHistory createHistory(Long userId);

    void addToHistory(Order order);

    CompletableFuture<OrderHistoryDTO> getAllHistory(Long userId);
}
