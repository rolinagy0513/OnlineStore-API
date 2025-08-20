package org.example.onlinestoreapi.service.impl;

import lombok.RequiredArgsConstructor;
import org.example.onlinestoreapi.DTO.OrderHistoryDTO;
import org.example.onlinestoreapi.exception.OrderHistoryNotFoundException;
import org.example.onlinestoreapi.exception.UserNotFoundException;
import org.example.onlinestoreapi.model.Order;
import org.example.onlinestoreapi.model.OrderHistory;
import org.example.onlinestoreapi.repository.OrderHistoryRepository;
import org.example.onlinestoreapi.security.user.User;
import org.example.onlinestoreapi.security.user.UserRepository;
import org.example.onlinestoreapi.service.IOrderHistoryService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionTemplate;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.concurrent.CompletableFuture;

/**
 * This service is responsible for managing the order history logic
 * Provides functionality for:
 * <ul>
 *     <li>Creating an order history after the first order for a user</li>
 *     <li>Adding to an existing history if the user has already bought something in the past</li>
 *     <li>Retrieving the users whole history</li>
 * </ul>
 */
@Service
@RequiredArgsConstructor
public class OrderHistoryService implements IOrderHistoryService {

    private final OrderHistoryRepository repository;
    private final UserRepository userRepository;

    private static final Logger logger = LoggerFactory.getLogger(OrderHistoryService.class);
    private final TransactionTemplate transactionTemplate;

    /**
     * This method creates the history for a user
     * It is called when the user buys something for the first time
     * @param userId The users id
     * @return The saved history
     */
    public OrderHistory createHistory(Long userId){
        User user = userRepository.findById(userId);
        if (user == null) {
            throw new UserNotFoundException("User not found with ID: " + userId);
        }

        OrderHistory orderHistory = OrderHistory.builder()
                .user(user)
                .payedAt(LocalDateTime.now())
                .totalAmount(BigDecimal.ZERO)
                .orders(new ArrayList<>())
                .build();

        return repository.save(orderHistory);
    }

    /**
     * This method adds to an existing history
     * Is used when the user has already bought something and has a history
     * @param order The order
     */
    @CacheEvict(value = "orderHistoryCache", key = "#order.user.id")
    @Retryable(
            value = OptimisticLockingFailureException.class,
            maxAttempts = 3,
            backoff = @Backoff(delay = 100)
    )
    public void addToHistory(Order order) {
        Long userId = order.getUser().getId();
        OrderHistory history = repository.findByUserId(userId)
                .orElseGet(() -> createHistory(userId));

        order.setOrderHistory(history);
        history.getOrders().add(order);

        repository.save(history);
    }

    /**
     * This method gives back the users history containing the orders
     * The method has caching and async to improve performance
     * @param userId The users id
     * @return The history with th orders in a OrderHistoryDTO
     */
    @Async("asyncExecutor")
    @Cacheable(value = "orderHistoryCache", key = "#userId")
    public CompletableFuture<OrderHistoryDTO> getAllHistory(Long userId) {
        return CompletableFuture.completedFuture(transactionTemplate.execute(status -> {
            OrderHistory history = repository.findByUserId(userId)
                    .orElseThrow(() -> new OrderHistoryNotFoundException(userId));
            return OrderHistoryDTO.fromHistory(history);
        }));
    }

}
