package org.example.onlinestoreapi.repository;

import jakarta.persistence.LockModeType;
import org.example.onlinestoreapi.model.Stock;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Repository interface for managing {@link Stock} entities.
 * Provides CRUD operations and custom query methods for stock management,
 * including atomic operations with pessimistic locking.
 */
@Repository
public interface StockRepository extends JpaRepository<Stock, Long> {

    /**
     * Finds stock by product ID with pessimistic lock for update.
     *
     * @param productId the ID of the product
     * @return an Optional containing the locked stock if found
     */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT s FROM Stock s WHERE s.product.id = :productId")
    Optional<Stock> findByProductIdForUpdate(Long productId);

    /**
     * Atomically increases stock quantity for a product.
     *
     * @param productId the ID of the product
     * @param increment the amount to increase the stock by
     */
    @Modifying
    @Query("UPDATE Stock s SET s.quantity = s.quantity + :increment WHERE s.product.id = :productId")
    void atomicIncreaseStock(@Param("productId") Long productId, @Param("increment") Integer increment);

    /**
     * Atomically decreases stock quantity for a product if sufficient stock exists.
     *
     * @param productId the ID of the product
     * @param decrement the amount to decrease the stock by
     * @return the number of affected rows (1 if successful, 0 if insufficient stock)
     */
    @Modifying
    @Query("UPDATE Stock s SET s.quantity = s.quantity - :decrement WHERE s.product.id = :productId AND s.quantity >= :decrement")
    int atomicDecreaseStock(@Param("productId") Long productId, @Param("decrement") Integer decrement);

}
