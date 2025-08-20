package org.example.onlinestoreapi.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.onlinestoreapi.DTO.CategoryDTO;
import org.example.onlinestoreapi.DTO.CategoryResponseDTO;
import org.example.onlinestoreapi.model.Category;
import org.example.onlinestoreapi.service.impl.CategoryService;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.concurrent.CompletableFuture;

/**
 * Controller for the Category operations
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/api")
public class CategoryController {

    private final CategoryService service;

    /**
     * Retrieves all the categories
     * @param page Page
     * @param size The size of the page
     * @param sortBy The sorting
     * @param direction ASC or DESC order
     */
    @GetMapping("/category/getAll")
    public CompletableFuture<ResponseEntity<Page<CategoryResponseDTO>>> getAll(
            @RequestParam(defaultValue = "0") Integer page,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(defaultValue = "id") String sortBy,
            @RequestParam(defaultValue = "ASC") String direction
    ){
        return service.getAllCategory(page, size, sortBy, direction)
                .thenApply(ResponseEntity::ok);
    }

    /**
     * Get one category according to the given id
     * @param id The id of the category
     */
    @GetMapping("/category/getOne/{id}")
    public CompletableFuture<ResponseEntity<CategoryResponseDTO>> getOne(
            @PathVariable Long id
    ){
        return service.getOneCategory(id)
                .thenApply(ResponseEntity::ok);
    }

    /**
     * Adds a new category
     * @param categoryDTO The category data transfer object containing category details
     * @return The created Category entity
     */
    @PostMapping("/management/category/add")
    public Category addCategory(
            @Valid @RequestBody CategoryDTO categoryDTO
    ){
        return service.addCategory(categoryDTO);
    }

    /**
     * Edits an existing category
     * @param id The id of the category to edit
     * @param categoryDTO The updated category data
     * @return The updated CategoryResponseDTO
     */
    @PutMapping("/management/category/edit/{id}")
    public CategoryResponseDTO editCategory(
            @PathVariable Long id,
            @Valid @RequestBody CategoryDTO categoryDTO
    ){
        return service.editCategory(id,categoryDTO);
    }

    /**
     * Deletes a category by id
     * @param id The id of the category to delete
     * @return A confirmation message string
     */
    @DeleteMapping("/management/category/delete/{id}")
    public String deleteCategory(
            @PathVariable Long id
    ){
        return service.deleteCategory(id);
    }

}