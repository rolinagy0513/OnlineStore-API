package org.example.onlinestoreapi.repository;

import jakarta.persistence.LockModeType;
import org.example.onlinestoreapi.model.Order;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository interface for managing {@link Order} entities.
 * Provides CRUD operations and custom query methods for orders, including
 * specialized methods for handling order statuses with locking mechanisms.
 */
@Repository
public interface OrderRepository extends JpaRepository<Order,Long> {

    /**
     * Finds an order by user ID with status 'CREATED' or 'PENDING' using pessimistic lock.
     *
     * @param id the ID of the user
     * @return an Optional containing the locked order if found
     */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT o FROM Order o WHERE o.user.id = :userId AND o.status IN ('CREATED', 'PENDING')")
    Optional<Order> findByIdForUpdate(@Param("userId") Long id);

    /**
     * Finds all orders for a user with status 'CREATED' or 'PENDING'.
     *
     * @param userId the ID of the user
     * @return a list of matching orders
     */
    @Query("SELECT o FROM Order o WHERE o.user.id = :userId AND o.status IN ('CREATED', 'PENDING')")
    List<Order> findByUserId(Long userId);

    /**
     * Finds a created order by user ID.
     *
     * @param userId the ID of the user
     * @return an Optional containing the order if found with 'CREATED' status
     */
    @Query("SELECT o FROM Order o WHERE o.user.id = :userId AND o.status IN ( 'CREATED')")
    Optional<Order> findCreatedByUserId(Long userId);

    /**
     * Finds an order by ID with pessimistic lock for fetching.
     *
     * @param orderId the ID of the order
     * @return an Optional containing the locked order if found
     */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT o FROM Order o WHERE o.id = :orderId")
    Optional<Order> findByIdForFetching(@Param("orderId") Long orderId);

}
