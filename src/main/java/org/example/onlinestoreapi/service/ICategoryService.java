package org.example.onlinestoreapi.service;

import jakarta.validation.Valid;
import org.example.onlinestoreapi.DTO.CategoryDTO;
import org.example.onlinestoreapi.DTO.CategoryResponseDTO;
import org.example.onlinestoreapi.model.Category;
import org.springframework.data.domain.Page;

import java.util.concurrent.CompletableFuture;

/**
 * Category interface for managing category related logic.
 * <p>
 * Defines operations for adding removing editing categories
 * </p>
 * <p>
 *      For more information look at {@link org.example.onlinestoreapi.service.impl.CategoryService}
 * </p>
 */
public interface ICategoryService {

    CompletableFuture<Page<CategoryResponseDTO>> getAllCategory(Integer page, Integer size, String sortBy, String direction);

    CompletableFuture<CategoryResponseDTO> getOneCategory(Long id);

    Category addCategory(@Valid CategoryDTO categoryDTO);

    CategoryResponseDTO editCategory(Long id, @Valid CategoryDTO categoryDTO);

    String deleteCategory(Long id);

}
