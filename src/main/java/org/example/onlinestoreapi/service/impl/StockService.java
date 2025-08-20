package org.example.onlinestoreapi.service.impl;

import lombok.RequiredArgsConstructor;
import org.example.onlinestoreapi.exception.InsufficientStockException;
import org.example.onlinestoreapi.exception.ProductNotFoundException;
import org.example.onlinestoreapi.model.Product;
import org.example.onlinestoreapi.model.Stock;
import org.example.onlinestoreapi.repository.StockRepository;
import org.example.onlinestoreapi.service.IStockService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Service for managing stocks.
 * <p>
 * Provides functionality for:
 * <ul>
 *     <li>Creating a new stock</li>
 *     <li>Increasing a stock</li>
 *     <li>Decreasing a stock</li>
 * </ul>
 */
@Service
@Transactional
@RequiredArgsConstructor
public class StockService implements IStockService {

    private final StockRepository repository;

    /**
     * This method creates a new stock
     * Used when a product is added
     * @param product The product entity
     * @param quantity The amount that is being added from that product
     * @return The saved stock
     */
    public Stock createNewStock(Product product, Integer quantity){
        Stock stock = Stock.builder()
                .product(product)
                .quantity(quantity)
                .build();
        return repository.save(stock);
    }


    /**
     * This method increases stock for an existing product
     * Used when a product is being added that is already existing in the db
     * @param productId The id of the product
     * @param quantity The amount that is being added
     * @return The updated stock
     */
    public Stock increaseStock(Long productId, Integer quantity) {
        repository.atomicIncreaseStock(productId, quantity);
        return repository.findByProductIdForUpdate(productId)
                .orElseThrow(() -> new IllegalStateException("Stock not found after update"));
    }

    /**
     * This method decreases stock for an existing product
     * Used when a product is being removed or someone bought a certain amount
     * @param productId The id of the product
     * @param quantity The amount that is being removed
     * @return The updated stock
     */
    public Stock decreaseStock(Long productId, Integer quantity){
        int updatedRows = repository.atomicDecreaseStock(productId, quantity);
        if (updatedRows == 0){
            Stock stock = repository.findByProductIdForUpdate(productId)
                    .orElseThrow(()->new ProductNotFoundException(productId));
            throw new InsufficientStockException(productId, stock.getQuantity(), quantity);
        }
        return repository.findByProductIdForUpdate(productId)
                .orElseThrow(() -> new IllegalStateException("Stock not found after update"));
    }


}
