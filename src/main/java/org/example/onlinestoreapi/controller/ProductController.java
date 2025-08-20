package org.example.onlinestoreapi.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.onlinestoreapi.DTO.ProductDTO;
import org.example.onlinestoreapi.DTO.ProductResponseDTO;
import org.example.onlinestoreapi.DTO.ProductWithQuantityDTO;
import org.example.onlinestoreapi.DTO.ProductWithStockDTO;
import org.example.onlinestoreapi.service.impl.ProductService;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.concurrent.CompletableFuture;

/**
 * Controller for handling product-related operations including retrieval,
 * creation, modification, and deletion of products
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/api")
public class ProductController {

    private final ProductService service;

    /**
     * Retrieves all products with pagination and sorting
     * @param page Page number
     * @param size Page size
     * @param sortBy Field to sort by
     * @param direction Sort direction (ASC/DESC)
     * @return CompletableFuture containing ResponseEntity with paginated ProductResponseDTO
     */
    @GetMapping("/product/getAll")
    public CompletableFuture<ResponseEntity<Page<ProductResponseDTO>>> getAll(
            @RequestParam(defaultValue = "0") Integer page,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(defaultValue = "id") String sortBy,
            @RequestParam(defaultValue = "ASC") String direction
    ) {
        return service.getAllProduct(page, size, sortBy, direction)
                .thenApply(ResponseEntity::ok);
    }

    /**
     * Retrieves all products belonging to a specific category with pagination and sorting
     * @param id The category id
     * @param page Page number
     * @param size Page size
     * @param sortBy Field to sort by
     * @param direction Sort direction (ASC/DESC)
     * @return CompletableFuture containing ResponseEntity with paginated ProductResponseDTO
     */
    @GetMapping("/product/getAllByCategory/{id}")
    public CompletableFuture<ResponseEntity<Page<ProductResponseDTO>>> getAllByCategory(
            @PathVariable Long id,
            @RequestParam(defaultValue = "0") Integer page,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(defaultValue = "id") String sortBy,
            @RequestParam(defaultValue = "ASC") String direction
    ) {
        return service.getAllProductByCategory(id, page, size, sortBy, direction)
                .thenApply(ResponseEntity::ok);
    }

    /**
     * Retrieves a single product by its id
     * @param id The product id
     * @return CompletableFuture containing ResponseEntity with ProductResponseDTO
     */
    @GetMapping("/product/getOne/{id}")
    public CompletableFuture<ResponseEntity<ProductResponseDTO>> getOneProduct(
            @PathVariable Long id
    ) {
        return service.getOneProduct(id)
                .thenApply(ResponseEntity::ok);
    }

    /**
     * Adds a new product to a specific category
     * @param categoryId The category id to which the product belongs
     * @param productDTO The product data transfer object
     * @return ProductWithStockDTO containing the created product with stock information
     */
    @PostMapping("/management/product/add/{categoryId}")
    public ProductWithStockDTO addProduct(
            @PathVariable Long categoryId,
            @Valid @RequestBody ProductDTO productDTO
    ) {
        return service.addProduct(categoryId, productDTO);
    }

    /**
     * Edits an existing product
     * @param id The product id to edit
     * @param productWithoutQuantityDTO The updated product data
     * @return ProductResponseDTO containing the updated product information
     */
    @PutMapping("/management/product/edit/{id}")
    public ProductResponseDTO editProduct(
            @PathVariable Long id,
            @Valid @RequestBody ProductWithQuantityDTO productWithoutQuantityDTO
    ) {
        return service.editProduct(id, productWithoutQuantityDTO);
    }

    /**
     * Deletes a product by its id
     * @param id The product id to delete
     * @return A confirmation message string
     */
    @DeleteMapping("/management/product/delete/{id}")
    public String deleteProduct(
            @PathVariable Long id
    ) {
        return service.deleteProduct(id);
    }

}