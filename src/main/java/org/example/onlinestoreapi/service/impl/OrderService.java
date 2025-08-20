package org.example.onlinestoreapi.service.impl;

import lombok.RequiredArgsConstructor;
import org.example.onlinestoreapi.DTO.OrderResponseDTO;
import org.example.onlinestoreapi.DTO.SubmitDTO;
import org.example.onlinestoreapi.ENUM.OrderStatus;
import org.example.onlinestoreapi.stripe.impl.StripeService;
import org.example.onlinestoreapi.exception.OrderModificationNotAllowedException;
import org.example.onlinestoreapi.exception.OrderNotFoundException;
import org.example.onlinestoreapi.exception.UsersOrderNotFoundException;
import org.example.onlinestoreapi.model.Order;
import org.example.onlinestoreapi.repository.OrderRepository;
import org.example.onlinestoreapi.security.user.UserService;
import org.example.onlinestoreapi.service.IOrderService;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionTemplate;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

/**
 * This service is responsible for handling the operations which are related to the Order.
 * Provides functionality for:
 * <ul>
 *     <li>Getting an existing order if a user has one or creates one for them</li>
 *     <li>Updating the total price of teh order after a product was added</li>
 *     <li>Retrieving all the orders for a user</li>
 *     <li>Submitting an order for checkout</li>
 *     <li>Deleting an order</li>
 * </ul>
 */
@Service
@RequiredArgsConstructor
public class OrderService implements IOrderService {

    private final OrderRepository repository;
    private final UserService userService;
    private final TransactionTemplate transactionTemplate;
    private final StripeService stripeService;

    /**
     * This method is responsible for a check
     * If the user don't have an Order than it creates oen for them.
     * If the user has one than this method gives that back
     * The method also makes sure that it not gives back a PENDING order
     * @return The order
     */
    public Order getOrCreateAnOrder() {
        Long userId = userService.getCurrentUserId();
        Optional<Order> activeOrder = repository.findByIdForUpdate(userId);

        if (activeOrder.isEmpty() || activeOrder.get().getStatus() == OrderStatus.PENDING) {
            return createNewOrder();
        }

        return activeOrder.get();
    }

    /**
     * This method is responsible for updating the price of the order in case if a product is being added
     * @param id The orders id
     * @return The saved updated order with the correct price
     */
    @Transactional
    @CacheEvict(value = "orderCache", key = "#result.user.id")
    public Order updateTotal(Long id) {
        Order order = repository.findById(id)
                .orElseThrow(() -> new OrderNotFoundException(id));

        if (order.getStatus() == OrderStatus.PENDING) {
            throw new OrderModificationNotAllowedException(
                    id,
                    OrderStatus.PENDING
            );
        }

        BigDecimal total = order.getOrderItems().stream()
                .map(item -> item.getPrice().multiply(BigDecimal.valueOf(item.getQuantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        order.setTotalPrice(total);
        return repository.save(order);
    }

    /**
     * This method is responsible for retrieving all the orders a user has
     * The method uses async and caching to improve performance
     * @param userId The users id
     * @return The list of the Orders in a OrderResponseDTO
     */
    @Async("asyncExecutor")
    @Cacheable(value = "orderCache", key = "#userId")
    public CompletableFuture<OrderResponseDTO> getAllOrderByUser(Long userId) {
        return CompletableFuture.completedFuture(transactionTemplate.execute(status -> {
            List<Order> activeOrders = repository.findByUserId(userId);

            if (activeOrders.isEmpty()){
                throw new UsersOrderNotFoundException(userId);
            }

            activeOrders.forEach(order -> {
                BigDecimal total = order.getOrderItems().stream()
                        .map(item -> item.getPrice().multiply(BigDecimal.valueOf(item.getQuantity())))
                        .reduce(BigDecimal.ZERO, BigDecimal::add);
                order.setTotalPrice(total);
            });

            return OrderResponseDTO.fromOrders(activeOrders);
        }));
    }

    /**
     * Submits a user's order and initiates the checkout process.
     * The order status is set to PENDING, and a checkout session is created through Stripe.
     * Stripe returns a payment URL, which is stored in the order so that if the connection is lost,
     * the user can resume the payment process later. When retrieved via getAllOrdersByUser, the order
     * will include the payment URL, allowing the frontend to redirect the user to the session.
     *
     * @param id the unique identifier of the order
     * @return a SubmitDTO containing the payment URL and order details
     */

    @Transactional
    @CacheEvict(value = "orderCache", key = "#result.userId")
    public SubmitDTO submitOrder(Long id){
        Order order = repository.findByIdForFetching(id)
                .orElseThrow(()->new OrderNotFoundException(id));

        if (order.getStatus() == OrderStatus.PAID){
            throw new OrderModificationNotAllowedException(order.getId(), OrderStatus.PAID);
        }

        order.setStatus(OrderStatus.PENDING);
        order.setSubmittedAt(LocalDateTime.now());

        String uniqueKey = order.getId() + "_" + System.currentTimeMillis();

        String paymentUrl = stripeService.createCheckoutSession(
                order.getId(),
                order.getTotalPrice(),
                order.getUser().getEmail(),
                uniqueKey
        );

        order.setPaymentURL(paymentUrl);
        repository.save(order);

        SubmitDTO submitDTO = SubmitDTO.builder()
                .id(order.getId())
                .userId(order.getUser().getId())
                .price(order.getTotalPrice())
                .status(OrderStatus.PENDING)
                .paymentUrl(paymentUrl)
                .build();

        return submitDTO;
    }

    /**
     * This method is responsible for deleting an order
     * It can be used in a scenario where the user wants to cancel the whole order
     * @param orderId The orders id
     * @param userId The users id
     */
    @CacheEvict(value = "orderCache", key = "#userId")
    public void deleteOrder(Long orderId, Long userId){
        Order order = repository.findById(orderId)
                .orElseThrow(()->new OrderNotFoundException(orderId));
        if (order.getStatus() == OrderStatus.PENDING || order.getStatus() == OrderStatus.PAID){
            throw new OrderModificationNotAllowedException(orderId,order.getStatus());
        }

        repository.deleteById(orderId);

    }

    /**
     * Helper method for cache eviction
     * @param userId The users id
     */
    @CacheEvict(value = "orderCache", key = "#userId")
    public void evictOrderCache(Long userId){
    }

    /**
     * Helper method for Order entity creation
     * @return The order
     */
    public Order createNewOrder(){

        Order newOrder = Order.builder()
                .user(userService.getCurrentUser())
                .totalPrice(BigDecimal.ZERO)
                .orderItems(new ArrayList<>())
                .status(OrderStatus.CREATED)
                .createdAt(LocalDateTime.now())
                .build();

        return repository.save(newOrder);

    }

}
