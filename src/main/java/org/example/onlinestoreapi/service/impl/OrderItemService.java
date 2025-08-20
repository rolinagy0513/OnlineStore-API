package org.example.onlinestoreapi.service.impl;

import lombok.RequiredArgsConstructor;
import org.example.onlinestoreapi.DTO.OrderItemDTO;
import org.example.onlinestoreapi.ENUM.OrderStatus;
import org.example.onlinestoreapi.exception.OrderModificationNotAllowedException;
import org.example.onlinestoreapi.exception.InsufficientStockException;
import org.example.onlinestoreapi.exception.ProductNotFoundException;
import org.example.onlinestoreapi.exception.UsersOrderNotFoundException;
import org.example.onlinestoreapi.model.Order;
import org.example.onlinestoreapi.model.OrderItem;
import org.example.onlinestoreapi.model.Product;
import org.example.onlinestoreapi.repository.OrderItemRepository;
import org.example.onlinestoreapi.repository.OrderRepository;
import org.example.onlinestoreapi.repository.ProductRepository;
import org.example.onlinestoreapi.service.IOrderItemService;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Caching;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

/**
 * This service is responsible for managing the OrderItem related logic
 * The OrderItem is acting as a bridge between Product and Order
 * Provides functionality for:
 * <ul>
 *     <li>Adding a product to an order</li>
 *     <li>Removing a product from an order</li>
 *     <li>Removing a certain amount of products from an order</li>
 * </ul>
 */
@Service
@RequiredArgsConstructor
public class OrderItemService implements IOrderItemService {

    private final OrderItemRepository repository;
    private final ProductRepository productRepository;
    private final OrderService orderService;
    private final StockService stockService;
    private final OrderRepository orderRepository;

    /**
     * Adds a product to the current user's order with specified quantity.
     * <p>
     * Handles both new items and quantity updates for existing items in the order.
     * Manages stock levels and order totals automatically.
     *
     * @param productId ID of the product to add to order (must exist)
     * @param quantity Quantity to add (must be positive)
     * @return OrderItemDTO containing details of the added/updated order item
     */

    @Transactional
    public OrderItemDTO addToOrder(Long productId, Integer quantity) {
        Order order = orderService.getOrCreateAnOrder();
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ProductNotFoundException(productId));

        Optional<OrderItem> existingItem = order.getOrderItems().stream()
                .filter(item -> item.getProduct().getId().equals(productId))
                .findFirst();

        OrderItem orderItem;
        int quantityToDecrease = quantity;

        if (existingItem.isPresent()) {
            orderItem = existingItem.get();
            quantityToDecrease = quantity;
            orderItem.setQuantity(orderItem.getQuantity() + quantity);
        } else {
            orderItem = OrderItem.builder()
                    .order(order)
                    .product(product)
                    .name(product.getName())
                    .description(product.getDescription())
                    .price(product.getPrice())
                    .quantity(quantity)
                    .build();
            order.getOrderItems().add(orderItem);
        }

        try {
            stockService.decreaseStock(productId, quantityToDecrease);

            OrderItem savedItem = repository.save(orderItem);
            orderService.updateTotal(order.getId());

            evictOrderCache(productId,order.getUser().getId());

            return OrderItemDTO.fromOrderItem(savedItem);
        } catch (InsufficientStockException e) {
            if (!existingItem.isPresent()) {
                order.getOrderItems().remove(orderItem);
            } else {
                orderItem.setQuantity(orderItem.getQuantity() - quantity);
            }
            throw e;
        }
    }

    /**
     * Removes a product completely from the user's order.
     * Restores the product quantity to stock and updates order total.
     *
     * @param userId ID of the user whose order should be modified
     * @param productId ID of the product to remove from the order
     */
    @Transactional
    @Caching(
            evict = {
                    @CacheEvict(value = {"products","productsByCategory"}, allEntries = true),
                    @CacheEvict(value = "productById", key = "#productId"),
                    @CacheEvict(value = "orderCache", key = "#userId")
            }
    )
    public void removeProductFromOrder(Long userId, Long productId){
        Order order = orderRepository.findCreatedByUserId(userId)
                .orElseThrow(()->new UsersOrderNotFoundException(userId));

        if (order.getStatus() != OrderStatus.CREATED){
            throw new OrderModificationNotAllowedException(order.getId(),order.getStatus());
        }

        OrderItem orderItem = order.getOrderItems().stream()
                .filter(item -> item.getProduct().getId().equals(productId))
                .findFirst()
                .orElseThrow(() -> new ProductNotFoundException(productId));

        stockService.increaseStock(productId,orderItem.getQuantity());

        order.getOrderItems().remove(orderItem);
        repository.delete(orderItem);

        orderService.updateTotal(order.getId());
    }

    /**
     * Reduces the quantity of a product in the user's order.
     * Partially restores stock if quantity is reduced but not fully removed.
     *
     * @param userId ID of the user whose order should be modified
     * @param productId ID of the product to reduce quantity for
     * @param quantity Amount to reduce by (must be positive)
     */
    @Transactional
    @Caching(
            evict = {
                    @CacheEvict(value = {"products","productsByCategory"}, allEntries = true),
                    @CacheEvict(value = "productById", key = "#productId"),
                    @CacheEvict(value = "orderCache", key = "#userId")
            }
    )
    public void removeQuantityOfProductFromOrder(Long userId, Long productId, Integer quantity){

        if (quantity == null || quantity <= 0) {
            throw new IllegalArgumentException("Quantity must be a non-null, positive number.");
        }

        Order order = orderRepository.findCreatedByUserId(userId)
                .orElseThrow(()->new UsersOrderNotFoundException(userId));

        OrderItem orderItem = repository.findByOrderIdAndProductId(order.getId(), productId)
                .orElseThrow(() -> new ProductNotFoundException(productId));

        if(orderItem.getQuantity() < quantity){
            throw new InsufficientStockException(productId, orderItem.getQuantity(), quantity);
        }

        if (quantity < 0){
            throw new IllegalArgumentException("Quantity must be a positive number.");
        }

        orderItem.setQuantity(orderItem.getQuantity() - quantity);
        stockService.increaseStock(productId,quantity);

        if (orderItem.getQuantity() == 0){
            order.getOrderItems().remove(orderItem);
            repository.delete(orderItem);
        }

        orderService.updateTotal(order.getId());

    }

    /**
     * Helper method for cache eviction
     * @param productId The products id
     * @param userId The users id
     */
    @Caching(
            evict = {
                    @CacheEvict(value = {"products","productsByCategory"}, allEntries = true),
                    @CacheEvict(value = "productById", key = "#productId"),
                    @CacheEvict(value = "orderCache", key = "#userId")
            }
    )
    public void evictOrderCache(Long productId, Long userId){
    }

}
