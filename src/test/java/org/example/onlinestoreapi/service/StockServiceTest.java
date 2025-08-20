package org.example.onlinestoreapi.service;

import org.example.onlinestoreapi.exception.InsufficientStockException;
import org.example.onlinestoreapi.exception.ProductNotFoundException;
import org.example.onlinestoreapi.model.Product;
import org.example.onlinestoreapi.model.Stock;
import org.example.onlinestoreapi.repository.StockRepository;
import org.example.onlinestoreapi.service.impl.StockService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.math.BigDecimal;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Test class for the Stock service
 */
@SpringBootTest
@ExtendWith(SpringExtension.class)
public class StockServiceTest {

    @MockBean
    StockRepository repository;

    @Autowired
    StockService service;

    private Product product;
    private Stock stock;

    @BeforeEach
    void setup(){

        Product product = Product.builder()
                .id(1L)
                .name("Product")
                .description("Description")
                .build();

        Stock stock = Stock.builder()
                .id(1L)
                .product(product)
                .quantity(0)
                .build();

        product.setStock(stock);

    }

    @Test
    void createNewStock_WithValidData_ShouldReturnStock(){

        Stock newStock = Stock.builder()
                .id(1L)
                .quantity(0)
                .build();

        Integer quantity = 20;

        when(repository.save(any(Stock.class))).thenReturn(newStock);

        newStock.setQuantity(quantity);
        newStock.setProduct(product);

        Stock result = service.createNewStock(product,quantity);

        assertNotNull(result);

        assertEquals(20,result.getQuantity());
        assertEquals(product,result.getProduct());

        verify(repository).save(any(Stock.class));

    }

    @Test
    void createNewStock_WithZeroQuantity(){

        Stock zeroStock = Stock.builder()
                .id(1L)
                .quantity(0)
                .build();

        Integer quantity = 0;

        when(repository.save(any(Stock.class))).thenReturn(zeroStock);

        zeroStock.setQuantity(quantity);
        zeroStock.setProduct(product);

        Stock result = service.createNewStock(product,quantity);

        assertNotNull(result);
        assertEquals(0,result.getQuantity());
        assertEquals(product,result.getProduct());

        verify(repository).save(any(Stock.class));

    }

    @Test
    void increaseStock_WithValidData_ShouldReturnStock(){

        Product productForIncrement = Product.builder()
                .id(1L)
                .name("Product for increment")
                .description("Product fot increment")
                .price(new BigDecimal("10.00"))
                .build();

        Stock stockForIncrement = Stock.builder()
                .id(1L)
                .quantity(20)
                .product(productForIncrement)
                .build();

        productForIncrement.setStock(stockForIncrement);

        when(repository.findByProductIdForUpdate(productForIncrement.getId())).thenReturn(Optional.of(stockForIncrement));
        doNothing().when(repository).atomicIncreaseStock(eq(1L), eq(10));

        stockForIncrement.setQuantity(stockForIncrement.getQuantity() + 10);

        Stock result = service.increaseStock(productForIncrement.getId(),10);

        assertNotNull(result);
        assertEquals(productForIncrement,result.getProduct());
        assertEquals(30,result.getQuantity());

        verify(repository).atomicIncreaseStock(1L, 10);
        verify(repository).findByProductIdForUpdate(1L);

    }

    @Test
    void increaseStockWithInvalidProductId_ShouldThrowException(){

        when(repository.findByProductIdForUpdate(99L)).thenReturn(Optional.empty());

        assertThrows(IllegalStateException.class,()->{
            service.increaseStock(99L,5);
        });

        verify(repository).findByProductIdForUpdate(99L);
        verify(repository,times(0)).save(any(Stock.class));

    }

    @Test
    void decreaseStock_WithValidData_ShouldReturnStock(){

        Product productForDecrease = Product.builder()
                .id(1L)
                .name("Product for decrease")
                .description("Product for decrease")
                .price(new BigDecimal("10.00"))
                .build();

        Stock stockForDecrease = Stock.builder()
                .id(1L)
                .quantity(30)
                .build();

        productForDecrease.setStock(stockForDecrease);
        stockForDecrease.setProduct(productForDecrease);

        when(repository.atomicDecreaseStock(eq(1L), eq(10))).thenReturn(1);

        Stock updatedStock = Stock.builder()
                .id(1L)
                .quantity(20)
                .product(productForDecrease)
                .build();

        when(repository.findByProductIdForUpdate(productForDecrease.getId())).thenReturn(Optional.of(updatedStock));

        Stock result  = service.decreaseStock(productForDecrease.getId(),10);

        assertNotNull(result);
        assertEquals(productForDecrease, result.getProduct());
        assertEquals(20, result.getQuantity());

        verify(repository).atomicDecreaseStock(1L, 10);
        verify(repository).findByProductIdForUpdate(1L);

    }

    @Test
    void decreaseStock_WithTooLowStock_ShouldThrowException(){

        String finalText = "The product with the id of: 1 Has only: 30 in stock while trying to remove: 40";

        Product productForDecrease = Product.builder()
                .id(1L)
                .name("Product for decrease")
                .description("Product for decrease")
                .price(new BigDecimal("10.00"))
                .build();

        Stock stockForDecrease = Stock.builder()
                .id(1L)
                .quantity(30)
                .build();

        productForDecrease.setStock(stockForDecrease);
        stockForDecrease.setProduct(productForDecrease);

        when(repository.atomicDecreaseStock(productForDecrease.getId(),40)).thenReturn(0);

        when(repository.findByProductIdForUpdate(1L)).thenReturn(Optional.of(stockForDecrease));

        InsufficientStockException exception = assertThrows(InsufficientStockException.class, () -> {
            service.decreaseStock(1L, 40);
        });

        assertEquals(finalText, exception.getMessage());

        verify(repository).atomicDecreaseStock(1L, 40);
        verify(repository).findByProductIdForUpdate(1L);

    }

    @Test
    void decreaseStock_WithInvalidProductId_ShouldThrowException(){

        Long nonExistentProductId = 99L;

        when(repository.atomicDecreaseStock(eq(nonExistentProductId), eq(40))).thenReturn(0);

        when(repository.findByProductIdForUpdate(nonExistentProductId)).thenReturn(Optional.empty());

        assertThrows(ProductNotFoundException.class, () -> {
            service.decreaseStock(nonExistentProductId, 40);
        });

        verify(repository).atomicDecreaseStock(nonExistentProductId, 40);
        verify(repository).findByProductIdForUpdate(nonExistentProductId);

    }

}
