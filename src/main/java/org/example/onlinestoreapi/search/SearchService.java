package org.example.onlinestoreapi.search;

import lombok.RequiredArgsConstructor;
import org.example.onlinestoreapi.DTO.CategoryResponseDTO;
import org.example.onlinestoreapi.DTO.ProductResponseDTO;
import org.example.onlinestoreapi.DTO.SearchByPriceDTO;
import org.example.onlinestoreapi.DTO.SearchDTO;
import org.example.onlinestoreapi.model.Category;
import org.example.onlinestoreapi.model.Product;
import org.example.onlinestoreapi.repository.CategoryRepository;
import org.example.onlinestoreapi.repository.ProductRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.concurrent.CompletableFuture;

/**
 * SearchService.java
 * - Service layer for handling search operations in the online store API.
 * - Implements asynchronous search functionality using CompletableFuture.
 * - Provides methods for searching products by name, categories by name,
 *   and products by price range.
 * - Validates pagination parameters and price ranges before executing searches.
 * - Converts entity results to DTOs for response.
 */
@Service
@RequiredArgsConstructor
public class SearchService {

    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;

    /**
     * Asynchronously searches products by name with pagination and sorting.
     *
     * @param searchDTO contains the search credential (name or partial name)
     * @param page page number (0-based)
     * @param size page size (1-100)
     * @param sortBy field to sort by
     * @param direction sort direction (ASC or DESC)
     * @return CompletableFuture containing paginated ProductResponseDTO results
     * @throws IllegalArgumentException if page or size parameters are invalid
     */
    @Async("asyncExecutor")
    public CompletableFuture<Page<ProductResponseDTO>> searchProductsByName(SearchDTO searchDTO, Integer page, Integer size, String sortBy, String direction){

        validateParams(page, size);

        Sort sort = direction.equalsIgnoreCase(Sort.Direction.ASC.name()) ?
                Sort.by(sortBy).ascending() : Sort.by(sortBy).descending();

        Pageable pageable = PageRequest.of(page, size, sort);

        Page<Product> productPage = productRepository.findByNameContainingIgnoreCase(
                searchDTO.getCredential(),
                pageable
        );

        Page<ProductResponseDTO> responseDTO = productPage.map(ProductResponseDTO::fromProduct);

        return CompletableFuture.completedFuture(responseDTO);
    }

    /**
     * Asynchronously searches categories by name with pagination and sorting.
     *
     * @param searchDTO contains the search credential (name or partial name)
     * @param page page number (0-based)
     * @param size page size (1-100)
     * @param sortBy field to sort by
     * @param direction sort direction (ASC or DESC)
     * @return CompletableFuture containing paginated CategoryResponseDTO results
     * @throws IllegalArgumentException if page or size parameters are invalid
     */
    @Async("asyncExecutor")
    public CompletableFuture<Page<CategoryResponseDTO>> searchCategoryByName(SearchDTO searchDTO, Integer page, Integer size, String sortBy, String direction){

        validateParams(page, size);

        Sort sort = direction.equalsIgnoreCase(Sort.Direction.ASC.name()) ?
                Sort.by(sortBy).ascending(): Sort.by(sortBy).descending();

        Pageable pageable = PageRequest.of(page,size,sort);

        Page<Category> categoryPage  = categoryRepository.findByNameContainingIgnoreCase(
                searchDTO.getCredential(),
                pageable
        );

        Page<CategoryResponseDTO> responseDTOS = categoryPage.map(CategoryResponseDTO::fromCategory);

        return CompletableFuture.completedFuture(responseDTOS);

    }

    /**
     * Asynchronously searches products by price range with pagination and sorting.
     *
     * @param searchByPriceDTO contains min and max price values
     * @param page page number (0-based)
     * @param size page size (1-100)
     * @param sortBy field to sort by
     * @param direction sort direction (ASC or DESC)
     * @return CompletableFuture containing paginated ProductResponseDTO results
     * @throws IllegalArgumentException if page/size parameters are invalid or minPrice > maxPrice
     */
    @Async("asyncExecutor")
    public CompletableFuture<Page<ProductResponseDTO>> searchProductsByPrice(SearchByPriceDTO searchByPriceDTO, Integer page, Integer size, String sortBy, String direction){

        validateParams(page, size);

        BigDecimal minPrice = searchByPriceDTO.getMinPrice();
        BigDecimal maxPrice = searchByPriceDTO.getMaxPrice();

        if (minPrice == null){
            minPrice = BigDecimal.ZERO;
        }

        if (maxPrice == null) {
            maxPrice = BigDecimal.valueOf(Long.MAX_VALUE);
        }

        if (minPrice.compareTo(maxPrice) > 0) {
            throw new IllegalArgumentException("Minimum price cannot be greater than maximum price");
        }

        Sort sort = direction.equalsIgnoreCase(Sort.Direction.ASC.name()) ?
                Sort.by(sortBy).ascending(): Sort.by(sortBy).descending();


        Pageable pageable = PageRequest.of(page,size,sort);

        Page<Product> productPage = productRepository.findByPriceBetween(
                minPrice,
                maxPrice,
                pageable
        );

        Page<ProductResponseDTO> responseDTOS = productPage.map(ProductResponseDTO::fromProduct);

        return CompletableFuture.completedFuture(responseDTOS);

    }

    /**
     * Validates pagination parameters.
     *
     * @param page page number (must be >= 0)
     * @param size page size (must be between 1 and 100)
     * @throws IllegalArgumentException if parameters are out of valid range
     */
    public void validateParams(Integer page, Integer size){

        if (size < 0 || size > 100){
            throw new IllegalArgumentException("The size must be higher than 0 and lower than 100");
        }else if (page < 0 ){
            throw new IllegalArgumentException("The page must be 0 or higher");
        }
    }

}
