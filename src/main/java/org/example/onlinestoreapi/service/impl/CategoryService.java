package org.example.onlinestoreapi.service.impl;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.onlinestoreapi.DTO.CategoryDTO;
import org.example.onlinestoreapi.DTO.CategoryResponseDTO;
import org.example.onlinestoreapi.exception.CategoryNotFoundException;
import org.example.onlinestoreapi.model.Category;
import org.example.onlinestoreapi.repository.CategoryRepository;
import org.example.onlinestoreapi.service.ICategoryService;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import java.util.concurrent.CompletableFuture;

/**
 * Service for managing category.
 * <p>
 * Provides functionality for:
 * <ul>
 *     <li>Retrieving all the available categories asynchronously</li>
 *     <li>retrieving one category asynchronously</li>
 *     <li>Adding a category (Only ADMIN!)</li>
 *     <li>Editing a category (Only ADMIN!)</li>
 *     <li>Deleting a category (Only ADMIN!)</li>
 * </ul>
 */
@Service
@Validated
@RequiredArgsConstructor
public class CategoryService implements ICategoryService {

    private final CategoryRepository repository;

    /**
     * It gets the all the available categories
     * The method was made to use Caching Async and Pagination to improve performance
     * @param page The page
     * @param size The size of the page
     * @param sortBy Sort
     * @param direction Ascending or Descending
     * @return The paginated list of categories
     */
    @Async("asyncExecutor")
    @Cacheable(value = "categories", key = "{#page, #size, #sortBy, #direction}")
    public CompletableFuture<Page<CategoryResponseDTO>> getAllCategory(Integer page, Integer size, String sortBy, String direction) {
        Sort sort;
        if (direction.equalsIgnoreCase("ASC")) {
            sort = Sort.by(sortBy).ascending();
        } else {
            sort = Sort.by(sortBy).descending();
        }

        Pageable pageable = PageRequest.of(page, size, sort);
        Page<Category> categories = repository.findAll(pageable);

        Page<CategoryResponseDTO> responseDTOs = categories.map(CategoryResponseDTO::fromCategory);
        return CompletableFuture.completedFuture(responseDTOs);
    }

    /**
     * It gets the category that has the same id as the provided one
     * The method has Caching and Async to improve performance
     * @param id The id of the searched category
     * @return The category
     */
    @Async("asyncExecutor")
    @Cacheable(value = "categoryById", key = "#id")
    public CompletableFuture<CategoryResponseDTO> getOneCategory(Long id) {
        Category categories = repository.findById(id)
                .orElseThrow(() -> new CategoryNotFoundException(id));
        CategoryResponseDTO responseDTO = CategoryResponseDTO.fromCategory(categories);
        return CompletableFuture.completedFuture(responseDTO);
    }

    /**
     * Adds a category to the db
     * This method can only be used by a user with an ADMIN role.
     * If two admins are trying to add the same category at the same time it throws DataIntegrityViolationException
     * The Retryable annotation will ensure that if the exception occurs:
     * the operation will automatically be retried up to 3 times, with a 100ms delay between attempts.
     * This helps to mitigate transient conflicts (e.g., race conditions when inserting the same category).
     * @param categoryDTO The category that the ADMIN wants to add
     * @return The saved category
     */
    @Transactional
    @CacheEvict(value = "categories", allEntries = true)
    @Retryable(
            retryFor = DataIntegrityViolationException.class,
            maxAttempts = 3,
            backoff = @Backoff(delay = 100)
    )
    public Category addCategory(@Valid CategoryDTO categoryDTO) {
        Category newCategory = Category.builder()
                .name(categoryDTO.getName())
                .build();
        return repository.save(newCategory);
    }

    /**
     * Edits a category inside the db.
     * This method can only be used by a user with an ADMIN role.
     *  If two admins are trying to edit the same category at the same time it throws DataIntegrityViolationException
     *  The Retryable annotation will ensure that if the exception occurs:
     *  the operation will automatically be retried up to 3 times, with a 100ms delay between attempts.
     *  This helps to mitigate transient conflicts (e.g., race conditions when inserting the same category).
     * @param id The category id
     * @param categoryDTO The modified category
     * @return The new saved edited category
     */
    @CachePut(value = "categoryById", key = "#id")
    @CacheEvict(value = "categories", allEntries = true)
    @Retryable(
            retryFor = OptimisticLockingFailureException.class,
            maxAttempts = 3,
            backoff = @Backoff(delay = 100)
    )
    public CategoryResponseDTO editCategory(Long id, @Valid CategoryDTO categoryDTO) {
        Category existingCategory = repository.findById(id)
                .orElseThrow(() -> new CategoryNotFoundException(id));

        if (categoryDTO.getName() == null || categoryDTO.getName().trim().isEmpty()){
            categoryDTO.setName(existingCategory.getName());
        }

        existingCategory.setName(categoryDTO.getName());
        Category updatedCategory = repository.save(existingCategory);

        return CategoryResponseDTO.fromCategory(updatedCategory);
    }

    /**
     * It deletes a category
     * This method can only be used by a person with an ADMIN role.
     * @param id The category id
     * @return A success message
     */
    @Caching(
            evict = {
                    @CacheEvict(value = "categories", allEntries = true),
                    @CacheEvict(value = "categoryById", key = "#id")
            }
    )
    public String deleteCategory(Long id) {
        Category category = repository.findById(id)
                .orElseThrow(() -> new CategoryNotFoundException(id));
        repository.deleteById(id);
        return ("The following category was deleted successfully: " + category.getName());
    }
}