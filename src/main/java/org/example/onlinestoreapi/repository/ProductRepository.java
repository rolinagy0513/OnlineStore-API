package org.example.onlinestoreapi.repository;

import org.example.onlinestoreapi.model.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.Optional;

/**
 * Repository interface for managing {@link Product} entities.
 * Provides CRUD operations and custom query methods for products,
 * including paginated searches and category-based queries.
 */
@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {

    /**
     * Finds a product by exact name match.
     *
     * @param name the name of the product
     * @return an Optional containing the product if found
     */
    Optional<Product> findByName(String name);

    /**
     * Finds products by category ID with pagination.
     *
     * @param categoryId the ID of the category
     * @param pageable pagination information
     * @return a page of products in the specified category
     */
    Page<Product> findByCategoryId(Long categoryId, Pageable pageable);

    /**
     * Finds a product by category ID.
     *
     * @param categoryId the ID of the category
     * @return the product in the specified category
     */
    Product findByCategoryId(Long categoryId);

    /**
     * Finds products by name containing the given string, case-insensitive, with pagination.
     *
     * @param name the string to search for in product names
     * @param pageable pagination information
     * @return a page of matching products
     */
    Page<Product> findByNameContainingIgnoreCase(String name,Pageable pageable);

    /**
     * Finds products within a price range with pagination.
     *
     * @param minPrice the minimum price
     * @param maxPrice the maximum price
     * @param pageable pagination information
     * @return a page of products within the specified price range
     */
    Page<Product> findByPriceBetween(BigDecimal minPrice, BigDecimal maxPrice, Pageable pageable);
}
