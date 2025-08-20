package org.example.onlinestoreapi.repository;

import org.example.onlinestoreapi.model.Category;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Repository interface for managing {@link Category} entities.
 * Provides CRUD operations and custom query methods for categories.
 */
@Repository
public interface CategoryRepository extends JpaRepository<Category, Long> {

    /**
     * Finds categories by name containing the given string, case-insensitive.
     *
     * @param credential the string to search for in category names
     * @param pageable pagination information
     * @return a page of matching categories
     */
    Page<Category> findByNameContainingIgnoreCase(String credential, Pageable pageable);
}
