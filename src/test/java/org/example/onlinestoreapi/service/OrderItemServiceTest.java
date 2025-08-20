package org.example.onlinestoreapi.service;


import org.example.onlinestoreapi.DTO.OrderItemDTO;
import org.example.onlinestoreapi.ENUM.OrderStatus;
import org.example.onlinestoreapi.exception.InsufficientStockException;
import org.example.onlinestoreapi.exception.OrderModificationNotAllowedException;
import org.example.onlinestoreapi.exception.ProductNotFoundException;
import org.example.onlinestoreapi.exception.UsersOrderNotFoundException;
import org.example.onlinestoreapi.model.*;
import org.example.onlinestoreapi.repository.OrderItemRepository;
import org.example.onlinestoreapi.repository.OrderRepository;
import org.example.onlinestoreapi.repository.ProductRepository;
import org.example.onlinestoreapi.security.user.Role;
import org.example.onlinestoreapi.security.user.User;
import org.example.onlinestoreapi.service.impl.OrderItemService;
import org.example.onlinestoreapi.service.impl.OrderService;
import org.example.onlinestoreapi.service.impl.StockService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Test class for the OrderItem service
 */
@SpringBootTest
@ExtendWith(SpringExtension.class)
public class OrderItemServiceTest {

    @MockBean
    private OrderItemRepository orderItemRepository;

    @MockBean
    private OrderRepository orderRepository;

    @MockBean
    private ProductRepository productRepository;

    @MockBean
    private OrderService orderService;

    @MockBean
    private StockService stockService;

    @Autowired
    private OrderItemService orderItemService;

    private Order order;
    private User testUser;
    private Product product;
    private Stock stock;
    private Category category;

    @BeforeEach
    void setup() {

        testUser = User.builder()
                .id(1L)
                .email("admin@admin.com")
                .orders(new ArrayList<>())
                .firstname("Admin")
                .lastname("Admin")
                .role(Role.ADMIN)
                .build();

        order = Order.builder()
                .user(testUser)
                .id(1L)
                .status(OrderStatus.CREATED)
                .totalPrice(new BigDecimal("100.00"))
                .orderItems(new ArrayList<>())
                .build();

        testUser.getOrders().add(order);

        category = Category.builder()
                .name("Category")
                .products(new ArrayList<>())
                .build();

        product = Product.builder()
                .id(1L)
                .name("Product")
                .description("Description")
                .price(new BigDecimal("10.00"))
                .category(category)
                .build();

        category.getProducts().add(product);

        stock = Stock.builder()
                .id(1L)
                .product(product)
                .quantity(20)
                .build();

        product.setStock(stock);

    }

    @Test
    void addToOrder_WhenProductExistsInOrder_ShouldReturnOrderItem() {

        Integer quantityToAdd = 10;
        Long productId = 1L;

        OrderItem existingOrderItem = OrderItem.builder()
                .id(1L)
                .order(order)
                .product(product)
                .name(product.getName())
                .description(product.getDescription())
                .price(product.getPrice())
                .quantity(10)
                .build();

        when(orderService.getOrCreateAnOrder()).thenReturn(order);
        when(productRepository.findById(productId)).thenReturn(Optional.of(product));
        when(stockService.decreaseStock(productId, quantityToAdd)).thenReturn(stock);
        when(orderItemRepository.save(any(OrderItem.class))).thenReturn(existingOrderItem);

        existingOrderItem.setQuantity(existingOrderItem.getQuantity() + quantityToAdd);

        OrderItemDTO result = orderItemService.addToOrder(productId, quantityToAdd);

        assertNotNull(result);
        assertEquals(20, result.getQuantity());
        assertEquals(productId, result.getProductId());
        assertEquals("Product", result.getProductName());

        verify(orderService).getOrCreateAnOrder();
        verify(productRepository).findById(productId);
        verify(stockService).decreaseStock(productId, quantityToAdd);
        verify(orderItemRepository).save(any(OrderItem.class));
        verify(orderService).updateTotal(order.getId());
    }

    @Test
    void addToOrder_WhenProductNotExistsInOrder_ShouldReturnOrderItem(){

        Integer quantityToAdd = 10;
        Long productId = 1L;

        OrderItem newOrderItem = OrderItem.builder()
                .id(1L)
                .order(order)
                .product(product)
                .name(product.getName())
                .description(product.getDescription())
                .price(product.getPrice())
                .quantity(quantityToAdd)
                .build();

        when(orderService.getOrCreateAnOrder()).thenReturn(order);
        when(productRepository.findById(productId)).thenReturn(Optional.of(product));
        when(stockService.decreaseStock(productId, quantityToAdd)).thenReturn(stock);
        when(orderItemRepository.save(any(OrderItem.class))).thenReturn(newOrderItem);

        OrderItemDTO result = orderItemService.addToOrder(productId, quantityToAdd);

        assertNotNull(result);
        assertEquals(10, result.getQuantity());
        assertEquals(productId, result.getProductId());
        assertEquals("Product", result.getProductName());

        verify(orderService).getOrCreateAnOrder();
        verify(productRepository).findById(productId);
        verify(stockService).decreaseStock(productId, quantityToAdd);
        verify(orderItemRepository).save(any(OrderItem.class));
        verify(orderService).updateTotal(order.getId());

    }

    @Test
    void addToOrder_WithInvalidProductId(){

        when(orderService.getOrCreateAnOrder()).thenReturn(order);
        when(productRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(ProductNotFoundException.class,()->{
            orderItemService.addToOrder(99L,10);
        });

        verify(orderService).getOrCreateAnOrder();
        verify(productRepository).findById(99L);
        verify(stockService, never()).decreaseStock(anyLong(), anyInt());
        verify(orderItemRepository, never()).save(any());
    }

    @Test
    void addToOrder_WithExistingOrder_NotEnoughOnStock_ShouldThrowException(){

        Integer quantityToAdd = 50;
        Long productId = 1L;

        OrderItem newOrderItem = OrderItem.builder()
                .id(1L)
                .order(order)
                .product(product)
                .name(product.getName())
                .description(product.getDescription())
                .price(product.getPrice())
                .quantity(10)
                .build();

        when(orderService.getOrCreateAnOrder()).thenReturn(order);
        when(productRepository.findById(productId)).thenReturn(Optional.of(product));

        when(stockService.decreaseStock(productId, quantityToAdd)).thenThrow(new InsufficientStockException(productId,product.getStock().getQuantity(),quantityToAdd));

        assertThrows(InsufficientStockException.class,()->{
            orderItemService.addToOrder(productId,quantityToAdd);
        });

        assertTrue(order.getOrderItems().isEmpty(), "Order items should be empty after rollback due to insufficient stock");
        assertEquals(10,newOrderItem.getQuantity());

        verify(orderService).getOrCreateAnOrder();
        verify(productRepository).findById(productId);
        verify(stockService).decreaseStock(productId,quantityToAdd);
        verify(orderItemRepository,never()).save(any(OrderItem.class));
        verify(orderService,never()).updateTotal(order.getId());

    }

    @Test
    void addToOrder_WithNewOrder_NotEnoughOnStock_ShouldThrowException() {

        Long productId = 1L;
        int originalQuantity = 5;
        int quantityToAdd = 50;

        OrderItem existingItem = OrderItem.builder()
                .order(order)
                .product(product)
                .quantity(originalQuantity)
                .name(product.getName())
                .description(product.getDescription())
                .price(product.getPrice())
                .build();

        order.getOrderItems().add(existingItem);

        when(orderService.getOrCreateAnOrder()).thenReturn(order);
        when(productRepository.findById(productId)).thenReturn(Optional.of(product));
        when(stockService.decreaseStock(productId, quantityToAdd))
                .thenThrow(new InsufficientStockException(productId, product.getStock().getQuantity(), quantityToAdd));

        assertThrows(InsufficientStockException.class, () -> {
            orderItemService.addToOrder(productId, quantityToAdd);
        });

        assertEquals(originalQuantity, existingItem.getQuantity(), "Quantity should be restored after failed stock operation");

        verify(orderItemRepository, never()).save(any());
        verify(orderService, never()).updateTotal(order.getId());

    }

    @Test
    void removeProductFromOrder_ShouldDeleteProduct(){

        OrderItem existingItem = OrderItem.builder()
                .order(order)
                .product(product)
                .quantity(10)
                .name(product.getName())
                .description(product.getDescription())
                .price(product.getPrice())
                .build();

        order.getOrderItems().add(existingItem);

        when(orderRepository.findCreatedByUserId(testUser.getId())).thenReturn(Optional.of(order));
        when(stockService.increaseStock(product.getId(),existingItem.getQuantity())).thenReturn(stock);

        stock.setQuantity(stock.getQuantity() + existingItem.getQuantity());

        doNothing().when(orderItemRepository).delete(existingItem);
        when(orderService.updateTotal(order.getId())).thenReturn(order);

        orderItemService.removeProductFromOrder(testUser.getId(), product.getId());

        assertFalse(order.getOrderItems().contains(existingItem), "Item should be removed from order");

        verify(orderRepository).findCreatedByUserId(testUser.getId());
        verify(stockService).increaseStock(product.getId(), existingItem.getQuantity());
        verify(orderItemRepository).delete(existingItem);
        verify(orderService).updateTotal(order.getId());

    }

    @Test
    void removeProductFromOrder_WithInvalidUserId_ShouldThrowException(){

        OrderItem existingItem = OrderItem.builder()
                .order(order)
                .product(product)
                .quantity(10)
                .name(product.getName())
                .description(product.getDescription())
                .price(product.getPrice())
                .build();

        when(orderRepository.findCreatedByUserId(5L)).thenThrow(new UsersOrderNotFoundException(5L));

        assertThrows(UsersOrderNotFoundException.class,()->{
            orderItemService.removeProductFromOrder(5L,product.getId());
        });

        assertEquals(10, existingItem.getQuantity());

        verify(orderRepository).findCreatedByUserId(5L);
        verify(stockService,never()).increaseStock(product.getId(),existingItem.getQuantity());
        verify(orderItemRepository,never()).delete(existingItem);
        verify(orderService,never()).updateTotal(order.getId());

    }

    @Test
    void removeProductFromOrder_WithIncorrectStatus_ShouldThrowException(){

        OrderItem existingItem = OrderItem.builder()
                .order(order)
                .product(product)
                .quantity(10)
                .name(product.getName())
                .description(product.getDescription())
                .price(product.getPrice())
                .build();

        order.setStatus(OrderStatus.PENDING);

        when(orderRepository.findCreatedByUserId(testUser.getId())).thenReturn(Optional.of(order));

        assertThrows(OrderModificationNotAllowedException.class,()->{
            orderItemService.removeProductFromOrder(testUser.getId(), product.getId());
        });

        assertEquals(10,existingItem.getQuantity());

        verify(orderRepository).findCreatedByUserId(testUser.getId());
        verify(stockService,never()).increaseStock(product.getId(),existingItem.getQuantity());
        verify(orderItemRepository,never()).delete(existingItem);
        verify(orderService,never()).updateTotal(order.getId());

    }

    @Test
    void removeProductFromOrder_WithInvalidProductId_ShouldThrowException(){

        OrderItem existingItem = OrderItem.builder()
                .order(order)
                .product(product)
                .quantity(10)
                .name(product.getName())
                .description(product.getDescription())
                .price(product.getPrice())
                .build();

        when(orderRepository.findCreatedByUserId(testUser.getId())).thenReturn(Optional.of(order));

        assertThrows(ProductNotFoundException.class,()->{
            orderItemService.removeProductFromOrder(testUser.getId(), 5L);
        });

        assertEquals(10,existingItem.getQuantity());

        verify(orderRepository).findCreatedByUserId(testUser.getId());
        verify(stockService,never()).increaseStock(product.getId(),existingItem.getQuantity());
        verify(orderItemRepository,never()).delete(existingItem);
        verify(orderService,never()).updateTotal(order.getId());

    }

    @Test
    void removeQuantityOfProductFromOrder_WithValidData_ShouldDeleteTheProductWithQuantity(){

        Integer quantityToRemove = 5;

        OrderItem existingItem = OrderItem.builder()
                .order(order)
                .product(product)
                .quantity(10)
                .name(product.getName())
                .description(product.getDescription())
                .price(product.getPrice())
                .build();

        when(orderRepository.findCreatedByUserId(testUser.getId())).thenReturn(Optional.of(order));
        when(orderItemRepository.findByOrderIdAndProductId(order.getId(),product.getId())).thenReturn(Optional.of(existingItem));
        when(stockService.increaseStock(product.getId(),quantityToRemove)).thenReturn(stock);
        when(orderService.updateTotal(order.getId())).thenReturn(order);

        orderItemService.removeQuantityOfProductFromOrder(testUser.getId(), product.getId(),quantityToRemove);

        assertEquals(5,existingItem.getQuantity());

        verify(orderRepository).findCreatedByUserId(testUser.getId());
        verify(orderItemRepository).findByOrderIdAndProductId(order.getId(),product.getId());
        verify(stockService).increaseStock(product.getId(),quantityToRemove);
        verify(orderService).updateTotal(order.getId());

    }

    @Test
    void removeQuantityOfProductFromOrder_WithValidData_ShouldDeleteTheProductEntirelyWhileZero(){

        Integer quantityToRemove = 10;

        OrderItem existingItem = OrderItem.builder()
                .order(order)
                .product(product)
                .quantity(10)
                .name(product.getName())
                .description(product.getDescription())
                .price(product.getPrice())
                .build();

        order.getOrderItems().add(existingItem);

        when(orderRepository.findCreatedByUserId(testUser.getId())).thenReturn(Optional.of(order));
        when(orderItemRepository.findByOrderIdAndProductId(order.getId(), product.getId())).thenReturn(Optional.of(existingItem));
        when(stockService.increaseStock(product.getId(),quantityToRemove)).thenReturn(stock);
        doNothing().when(orderItemRepository).delete(existingItem);
        when(orderService.updateTotal(order.getId())).thenReturn(order);

        orderItemService.removeQuantityOfProductFromOrder(testUser.getId(), product.getId(), quantityToRemove);

        assertEquals(0,existingItem.getQuantity());
        assertFalse(order.getOrderItems().contains(existingItem));

        verify(orderRepository).findCreatedByUserId(testUser.getId());
        verify(stockService).increaseStock(product.getId(), quantityToRemove);
        verify(orderItemRepository).delete(existingItem);
        verify(orderService).updateTotal(order.getId());

    }

    @Test
    void removeQuantityOfProductFromOrder_WithInvalidUserId_ShouldThrowException(){

        Integer quantityToRemove = 5;

        OrderItem existingItem = OrderItem.builder()
                .order(order)
                .product(product)
                .quantity(10)
                .name(product.getName())
                .description(product.getDescription())
                .price(product.getPrice())
                .build();

        order.getOrderItems().add(existingItem);

        when(orderRepository.findCreatedByUserId(5L)).thenReturn(Optional.empty());

        assertThrows(UsersOrderNotFoundException.class,()->{
            orderItemService.removeQuantityOfProductFromOrder(5L,existingItem.getProduct().getId(),quantityToRemove);
        });

        assertEquals(10,existingItem.getQuantity());

        verify(orderRepository).findCreatedByUserId(5L);

        verify(orderItemRepository,never()).findByOrderIdAndProductId(existingItem.getOrder().getId(),existingItem.getProduct().getId());
        verify(stockService,never()).increaseStock(existingItem.getProduct().getId(),quantityToRemove);
        verify(orderService,never()).updateTotal(existingItem.getOrder().getId());

    }

    @Test
    void removeQuantityOfProductFromOrder_WithInvalidProductId_ShouldThrowException(){

        Integer quantityToRemove = 5;

        OrderItem existingItem = OrderItem.builder()
                .order(order)
                .product(product)
                .quantity(10)
                .name(product.getName())
                .description(product.getDescription())
                .price(product.getPrice())
                .build();

        when(orderRepository.findCreatedByUserId(testUser.getId())).thenReturn(Optional.of(order));
        when(orderItemRepository.findByOrderIdAndProductId(order.getId(), 99L)).thenReturn(Optional.empty());

        assertThrows(ProductNotFoundException.class,()->{
            orderItemService.removeQuantityOfProductFromOrder(testUser.getId(), 99L,quantityToRemove);
        });

        assertEquals(10, existingItem.getQuantity());

        verify(orderRepository).findCreatedByUserId(testUser.getId());
        verify(orderItemRepository).findByOrderIdAndProductId(order.getId(), 99L);

        verify(stockService,never()).increaseStock(existingItem.getProduct().getId(),quantityToRemove);
        verify(orderService,never()).updateTotal(existingItem.getOrder().getId());

    }

    @Test
    void removeQuantityOfProductFromOrder_WithQuantityGreaterThanAvailable_ShouldThrowInsufficientStockException(){

        Integer quantityToRemove = 20;

        OrderItem existingItem = OrderItem.builder()
                .order(order)
                .product(product)
                .quantity(10)
                .name(product.getName())
                .description(product.getDescription())
                .price(product.getPrice())
                .build();

        when(orderRepository.findCreatedByUserId(testUser.getId())).thenReturn(Optional.of(order));
        when(orderItemRepository.findByOrderIdAndProductId(order.getId(), product.getId())).thenReturn(Optional.of(existingItem));

        assertThrows(InsufficientStockException.class,()->{
            orderItemService.removeQuantityOfProductFromOrder(testUser.getId(), existingItem.getProduct().getId(),quantityToRemove);
        });

        assertEquals(10,existingItem.getQuantity());

        verify(orderRepository).findCreatedByUserId(testUser.getId());
        verify(orderItemRepository).findByOrderIdAndProductId(order.getId(),product.getId());

        verify(stockService,never()).increaseStock(product.getId(),quantityToRemove);
        verify(orderService,never()).updateTotal(order.getId());

    }

    @Test
    void removeQuantityOfProductFromOrder_WithNegativeQuantity_ShouldThrowException(){

        Integer quantityToRemove = -5;

        OrderItem existingItem = OrderItem.builder()
                .order(order)
                .product(product)
                .quantity(10)
                .name(product.getName())
                .description(product.getDescription())
                .price(product.getPrice())
                .build();

        assertThrows(IllegalArgumentException.class,()->{
            orderItemService.removeQuantityOfProductFromOrder(testUser.getId(), product.getId(),quantityToRemove);
        });

        assertEquals(10,existingItem.getQuantity());

        verify(orderRepository,never()).findCreatedByUserId(testUser.getId());
        verify(orderItemRepository,never()).findByOrderIdAndProductId(order.getId(), product.getId());
        verify(stockService,never()).increaseStock(product.getId(),quantityToRemove);
        verify(orderService,never()).updateTotal(order.getId());

    }

    @Test
    void removeQuantityOfProductFromOrder_WithNullQuantity_ShouldThrowException(){

        Integer quantityToRemove = null;

        OrderItem existingItem = OrderItem.builder()
                .order(order)
                .product(product)
                .quantity(10)
                .name(product.getName())
                .description(product.getDescription())
                .price(product.getPrice())
                .build();

        assertThrows(IllegalArgumentException.class,()->{
            orderItemService.removeQuantityOfProductFromOrder(testUser.getId(), product.getId(),quantityToRemove);
        });

        assertEquals(10,existingItem.getQuantity());

        verify(orderRepository,never()).findCreatedByUserId(testUser.getId());
        verify(orderItemRepository,never()).findByOrderIdAndProductId(order.getId(), product.getId());
        verify(stockService,never()).increaseStock(product.getId(),quantityToRemove);
        verify(orderService,never()).updateTotal(order.getId());

    }


}
