package org.example.onlinestoreapi.service.impl;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.onlinestoreapi.DTO.ProductDTO;
import org.example.onlinestoreapi.DTO.ProductResponseDTO;
import org.example.onlinestoreapi.DTO.ProductWithQuantityDTO;
import org.example.onlinestoreapi.DTO.ProductWithStockDTO;
import org.example.onlinestoreapi.exception.CategoryNotFoundException;
import org.example.onlinestoreapi.exception.ProductNotFoundException;
import org.example.onlinestoreapi.model.Category;
import org.example.onlinestoreapi.model.Product;
import org.example.onlinestoreapi.model.Stock;
import org.example.onlinestoreapi.repository.CategoryRepository;
import org.example.onlinestoreapi.repository.ProductRepository;
import org.example.onlinestoreapi.service.IProductService;
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
 * Service for managing products.
 * <p>
 * Provides functionality for:
 * <ul>
 *     <li>Retrieving all available products asynchronously</li>
 *     <li>Retrieving products by category asynchronously</li>
 *     <li>Retrieving single product details asynchronously</li>
 *     <li>Adding new products (Admin only)</li>
 *     <li>Editing existing products (Admin only)</li>
 *     <li>Deleting products (Admin only)</li>
 * </ul>
 * Includes caching, async processing, and retry mechanisms for concurrent operations.
 */
@Service
@Validated
@RequiredArgsConstructor
public class ProductService implements IProductService {

    private final ProductRepository repository;
    private final CategoryRepository categoryRepository;
    private final StockService stockService;

    /**
     * Retrieves all products with pagination and sorting.
     * Uses async processing and caching for better performance.
     *
     * @param page Page number (0-based)
     * @param size Number of items per page
     * @param sortBy Field to sort by
     * @param direction Sort direction (ASC/DESC)
     * @return CompletableFuture containing paginated ProductResponseDTOs
     */
    @Async("asyncExecutor")
    @Cacheable(value = "products", key = "{#page, #size, #sortBy, #direction}")
    public CompletableFuture<Page<ProductResponseDTO>> getAllProduct(Integer page, Integer size, String sortBy, String direction) {
        Sort sort = direction.equalsIgnoreCase(Sort.Direction.ASC.name()) ?
                Sort.by(sortBy).ascending() : Sort.by(sortBy).descending();

        Pageable pageable = PageRequest.of(page, size, sort);
        Page<Product> products = repository.findAll(pageable);

        Page<ProductResponseDTO> responseDTOS = products.map(ProductResponseDTO::fromProduct);
        return CompletableFuture.completedFuture(responseDTOS);
    }

    /**
     * Retrieves products by category ID with pagination and sorting.
     * Uses async processing and caching for better performance.
     * Throws CategoryNotFoundException if category doesn't exist.
     *
     * @param categoryId ID of the category to filter by
     * @param page Page number (0-based)
     * @param size Number of items per page
     * @param sortBy Field to sort by
     * @param direction Sort direction (ASC/DESC)
     * @return CompletableFuture containing paginated ProductResponseDTOs
     */
    @Async("asyncExecutor")
    @Cacheable(value = "productsByCategory", key = "{#categoryId, #page, #size, #sortBy, #direction}")
    public CompletableFuture<Page<ProductResponseDTO>> getAllProductByCategory(Long categoryId, Integer page, Integer size, String sortBy, String direction) {
        Sort sort = direction.equalsIgnoreCase(Sort.Direction.ASC.name()) ?
                Sort.by(sortBy).ascending() : Sort.by(sortBy).descending();

        Pageable pageable = PageRequest.of(page, size, sort);
        Page<Product> products = repository.findByCategoryId(categoryId, pageable);

        if(products.isEmpty()){
            throw new CategoryNotFoundException(categoryId);
        }

        Page<ProductResponseDTO> responseDTOS = products.map(ProductResponseDTO::fromProduct);
        return CompletableFuture.completedFuture(responseDTOS);
    }

    /**
     * Retrieves a single product by ID.
     * Uses async processing and caching for better performance.
     * Throws ProductNotFoundException if product doesn't exist.
     *
     * @param id ID of the product to retrieve
     * @return CompletableFuture containing ProductResponseDTO
     */
    @Async("asyncExecutor")
    @Cacheable(value = "productById", key = "#id")
    public CompletableFuture<ProductResponseDTO> getOneProduct(Long id) {
        Product product = repository.findById(id)
                .orElseThrow(() -> new ProductNotFoundException(id));
        ProductResponseDTO productResponseDTO = ProductResponseDTO.fromProduct(product);
        return CompletableFuture.completedFuture(productResponseDTO);
    }

    /**
     * Adds a new product to the system (Admin only).
     * Handles both new products and stock updates for existing products.
     * Uses retry mechanism (3 attempts) for concurrent operations.
     * Invalidates relevant caches after operation.
     *
     * @param categoryId ID of the product's category
     * @param productDTO Product data to add
     * @return ProductWithStockDTO containing product and stock information
     */
    @Transactional
    @Retryable(
            retryFor = DataIntegrityViolationException.class,
            maxAttempts = 3,
            backoff = @Backoff(delay = 100))
    @CacheEvict(value = {"products", "productsByCategory"}, allEntries = true)
    public ProductWithStockDTO addProduct(Long categoryId, ProductDTO productDTO) {
        Category category = categoryRepository.findById(categoryId)
                .orElseThrow(() -> new CategoryNotFoundException(categoryId));

        Product existingProduct = repository.findByName(productDTO.getName()).orElse(null);
        if (existingProduct != null) {
            Stock stock = stockService.increaseStock(existingProduct.getId(), productDTO.getQuantity());

            return buildProductWithStockDTO(existingProduct, stock, category);

        }
        Product newProduct = Product.builder()
                .name(productDTO.getName())
                .description(productDTO.getDescription())
                .price(productDTO.getPrice())
                .category(category)
                .build();

        Product savedProduct = repository.save(newProduct);
        Stock stock = stockService.createNewStock(savedProduct, productDTO.getQuantity());
        return buildProductWithStockDTO(savedProduct, stock, category);


    }

    /**
     * Helper method to build ProductWithStockDTO from components.
     */
    private ProductWithStockDTO buildProductWithStockDTO(Product product, Stock stock, Category category) {
        return ProductWithStockDTO.builder()
                .id(product.getId())
                .name(product.getName())
                .description(product.getDescription())
                .price(product.getPrice())
                .stockQuantity(stock.getQuantity())
                .categoryId(category.getId())
                .build();
    }

    /**
     * Edits an existing product (Admin only).
     * Uses optimistic locking with retry mechanism (3 attempts) for concurrent edits.
     * Updates relevant cache entries after operation.
     *
     * @param productId ID of the product to edit
     * @param productWithoutQuantityDTO Updated product data
     * @return ProductResponseDTO with updated product information
     */
    @Transactional
    @CachePut(value = "productById", key = "{#productId}") //Frissíti az adatot ezen név alatt ezzel a key-el
    @CacheEvict(value = {"products", "productsByCategory"}, allEntries = true)
    @Retryable(
            retryFor = OptimisticLockingFailureException.class,
            maxAttempts = 3,
            backoff = @Backoff(delay = 100)
    )
    public ProductResponseDTO editProduct(Long productId, @Valid ProductWithQuantityDTO productWithoutQuantityDTO) {

        Product existingProduct = repository.findById(productId)
                .orElseThrow(() -> new ProductNotFoundException(productId));

        if (productWithoutQuantityDTO.getName() == null || productWithoutQuantityDTO.getName().trim().isEmpty()){
        }else {
            existingProduct.setName(productWithoutQuantityDTO.getName());
        }

        if (productWithoutQuantityDTO.getDescription() == null || productWithoutQuantityDTO.getDescription().trim().isEmpty()){
        }else{
            existingProduct.setDescription(productWithoutQuantityDTO.getDescription());
        }

        existingProduct.setPrice(productWithoutQuantityDTO.getPrice());

        Product updatedProduct = repository.save(existingProduct);

        return ProductResponseDTO.fromProduct(updatedProduct);

    }

    /**
     * Deletes a product from the system (Admin only).
     * Invalidates all relevant cache entries after deletion.
     *
     * @param productId ID of the product to delete
     * @return Success message with deleted product name
     */
    @Caching(
            evict = {
                    @CacheEvict(value = {"products","productsByCategory"}, allEntries = true),
                    @CacheEvict(value = "productById", key = "{#productId}")
            }
    )
    public String deleteProduct(Long productId) {
        Product existingProduct = repository.findById(productId)
                .orElseThrow(() -> new ProductNotFoundException(productId));
        repository.deleteById(productId);
        return "The following product was deleted: " + existingProduct.getName();
    }
}