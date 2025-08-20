package org.example.onlinestoreapi.repository;

import org.example.onlinestoreapi.model.OrderHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Repository interface for managing {@link OrderHistory} entities.
 * Provides CRUD operations and custom query methods for order histories.
 */
@Repository
public interface OrderHistoryRepository extends JpaRepository<OrderHistory,Long> {

    /**
     * Finds an order history by user ID.
     *
     * @param userId the ID of the user
     * @return an Optional containing the order history if found
     */
    Optional<OrderHistory> findByUserId(Long userId);

}
