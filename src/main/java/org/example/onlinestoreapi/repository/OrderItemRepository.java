package org.example.onlinestoreapi.repository;

import jakarta.persistence.LockModeType;
import org.example.onlinestoreapi.model.OrderItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Repository interface for managing {@link OrderItem} entities.
 * Provides CRUD operations and custom query methods for order items.
 */
@Repository
public interface OrderItemRepository extends JpaRepository<OrderItem,Long> {

    /**
     * Finds an order item by order ID and product ID with pessimistic lock.
     *
     * @param orderId the ID of the order
     * @param productId the ID of the product
     * @return an Optional containing the order item if found
     */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    Optional<OrderItem> findByOrderIdAndProductId(Long orderId, Long productId);

}
