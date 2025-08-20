package org.example.onlinestoreapi.service;

import org.example.onlinestoreapi.DTO.OrderItemDTO;

/**
 * OrderItem interface for managing orderItem related logic.
 * <p>
 * Defines operations for adding or removing a product from an order.
 * </p>
 * <p>
 *      For more information look at {@link org.example.onlinestoreapi.service.impl.OrderItemService}
 * </p>
 */
public interface IOrderItemService {
    OrderItemDTO addToOrder(Long productId, Integer quantity);

    void removeProductFromOrder(Long userId, Long productId);

    void removeQuantityOfProductFromOrder(Long userId, Long productId, Integer quantity);
}
