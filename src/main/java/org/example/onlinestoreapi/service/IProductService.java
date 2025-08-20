package org.example.onlinestoreapi.service;

import jakarta.validation.Valid;
import org.example.onlinestoreapi.DTO.ProductDTO;
import org.example.onlinestoreapi.DTO.ProductResponseDTO;
import org.example.onlinestoreapi.DTO.ProductWithQuantityDTO;
import org.example.onlinestoreapi.DTO.ProductWithStockDTO;
import org.springframework.data.domain.Page;

import java.util.concurrent.CompletableFuture;

/**
 * Product interface for managing product related logic.
 * <p>
 * Defines operations for adding removing editing products
 * </p>
 * <p>
 *      For more information look at {@link org.example.onlinestoreapi.service.impl.ProductService}
 * </p>
 */
public interface IProductService {
    CompletableFuture<Page<ProductResponseDTO>> getAllProduct(Integer page, Integer size, String sortBy, String direction);

    CompletableFuture<Page<ProductResponseDTO>> getAllProductByCategory(Long categoryId, Integer page, Integer size, String sortBy, String direction);

    CompletableFuture<ProductResponseDTO> getOneProduct(Long id);

    ProductWithStockDTO addProduct(Long categoryId, ProductDTO productDTO);

    ProductResponseDTO editProduct(Long productId, @Valid ProductWithQuantityDTO productWithoutQuantityDTO);

    String deleteProduct(Long productId);
}
