package org.example.onlinestoreapi.search;

import lombok.RequiredArgsConstructor;
import org.example.onlinestoreapi.DTO.CategoryResponseDTO;
import org.example.onlinestoreapi.DTO.ProductResponseDTO;
import org.example.onlinestoreapi.DTO.SearchByPriceDTO;
import org.example.onlinestoreapi.DTO.SearchDTO;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.concurrent.CompletableFuture;

/**
 * SearchController.java
 * - REST controller for handling search operations in the online store API.
 * - Provides asynchronous endpoints for searching products by name, categories by name,
 *   and products by price range.
 * - Supports pagination, sorting, and direction parameters for all search operations.
 * - Returns results wrapped in CompletableFuture for non-blocking operations.
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/search")
public class SearchController {

    private final SearchService service;

    /**
     * Searches products by name with pagination and sorting.
     *
     * @param searchDTO contains the search credential (name or partial name)
     * @param page page number (default: 0)
     * @param size page size (default: 10, max: 100)
     * @param sortBy field to sort by (default: "id")
     * @param direction sort direction (ASC or DESC, default: "ASC")
     * @return CompletableFuture containing ResponseEntity with paginated ProductResponseDTO results
     */
    @PostMapping("/searchByName")
    public CompletableFuture<ResponseEntity<Page<ProductResponseDTO>>> searchProductByName(
            @RequestBody SearchDTO searchDTO,
            @RequestParam(defaultValue = "0") Integer page,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(defaultValue = "id") String sortBy,
            @RequestParam(defaultValue = "ASC") String direction
    ){
        return service.searchProductsByName(searchDTO,page,size,sortBy,direction)
                .thenApply(ResponseEntity::ok);
    }

    /**
     * Searches categories by name with pagination and sorting.
     *
     * @param searchDTO contains the search credential (name or partial name)
     * @param page page number (default: 0)
     * @param size page size (default: 10, max: 100)
     * @param sortBy field to sort by (default: "id")
     * @param direction sort direction (ASC or DESC, default: "ASC")
     * @return CompletableFuture containing ResponseEntity with paginated CategoryResponseDTO results
     */
    @PostMapping("/searchByCategory")
    public CompletableFuture<ResponseEntity<Page<CategoryResponseDTO>>> searchCategoryByName(
            @RequestBody SearchDTO searchDTO,
            @RequestParam(defaultValue = "0") Integer page,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(defaultValue = "id") String sortBy,
            @RequestParam(defaultValue = "ASC") String direction
    ){
        return service.searchCategoryByName(searchDTO,page,size,sortBy,direction)
                .thenApply(ResponseEntity::ok);
    }

    /**
     * Searches products by price range with pagination and sorting.
     *
     * @param searchByPriceDTO contains min and max price values
     * @param page page number (default: 0)
     * @param size page size (default: 10, max: 100)
     * @param sortBy field to sort by (default: "id")
     * @param direction sort direction (ASC or DESC, default: "ASC")
     * @return CompletableFuture containing ResponseEntity with paginated ProductResponseDTO results
     * @throws IllegalArgumentException if minPrice > maxPrice
     */
    @PostMapping("/searchByPrice")
    public CompletableFuture<ResponseEntity<Page<ProductResponseDTO>>> searchProductByPrice(
            @RequestBody SearchByPriceDTO searchByPriceDTO,
            @RequestParam(defaultValue = "0") Integer page,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(defaultValue = "id") String sortBy,
            @RequestParam(defaultValue = "ASC") String direction
    ){
        return service.searchProductsByPrice(searchByPriceDTO, page,size,sortBy,direction)
                .thenApply(ResponseEntity::ok);
    }

}
