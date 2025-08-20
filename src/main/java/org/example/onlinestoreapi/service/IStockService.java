package org.example.onlinestoreapi.service;

import org.example.onlinestoreapi.model.Product;
import org.example.onlinestoreapi.model.Stock;

/**
 * Stock interface for managing stock related logic.
 * <p>
 * Defines operations for creating, increasing, decreasing stock.
 * </p>
 * <p>
 *      For more information look at {@link org.example.onlinestoreapi.service.impl.StockService}
 * </p>
 */
public interface IStockService {
    Stock createNewStock(Product product, Integer quantity);

    Stock increaseStock(Long productId, Integer quantity);

    Stock decreaseStock(Long productId, Integer quantity);
}
