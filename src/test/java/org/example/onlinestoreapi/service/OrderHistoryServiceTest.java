package org.example.onlinestoreapi.service;

import org.example.onlinestoreapi.DTO.OrderHistoryDTO;
import org.example.onlinestoreapi.ENUM.OrderStatus;
import org.example.onlinestoreapi.exception.OrderHistoryNotFoundException;
import org.example.onlinestoreapi.exception.UserNotFoundException;
import org.example.onlinestoreapi.model.*;
import org.example.onlinestoreapi.repository.OrderHistoryRepository;
import org.example.onlinestoreapi.security.user.Role;
import org.example.onlinestoreapi.security.user.User;
import org.example.onlinestoreapi.security.user.UserRepository;
import org.example.onlinestoreapi.service.impl.OrderHistoryService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Test class for the OrderHistory service
 */
@SpringBootTest
@ExtendWith(SpringExtension.class)
public class OrderHistoryServiceTest {

    @MockBean
    OrderHistoryRepository orderHistoryRepository;

    @MockBean
    UserRepository userRepository;

    @Autowired
    OrderHistoryService orderHistoryService;

    private User testUser;
    private Order order;
    private OrderItem orderItem;
    private Product product;
    private Stock stock;
    private Category category;


    @BeforeEach
    void setup(){

        testUser = User.builder()
                .id(1L)
                .email("admin@admin.com")
                .orders(new ArrayList<>())
                .firstname("Admin")
                .lastname("Admin")
                .role(Role.ADMIN)
                .build();

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

        stock = Stock.builder()
                .id(1L)
                .product(product)
                .quantity(20)
                .build();

        stock.setProduct(product);
        category.getProducts().add(product);

        order = Order.builder()
                .user(testUser)
                .id(1L)
                .status(OrderStatus.CREATED)
                .totalPrice(new BigDecimal("100.00"))
                .orderItems(new ArrayList<>())
                .build();

        testUser.getOrders().add(order);

        orderItem = OrderItem.builder()
                .id(1L)
                .order(order)
                .product(product)
                .name(product.getName())
                .description(product.getDescription())
                .price(product.getPrice())
                .quantity(10)
                .build();

        order.getOrderItems().add(orderItem);

    }

    @Test
    void createHistory_WithValidData_ShouldReturnOrderHistory(){

        OrderHistory newHistory = OrderHistory.builder()
                .user(testUser)
                .payedAt(LocalDateTime.now())
                .totalAmount(BigDecimal.ZERO)
                .orders(new ArrayList<>())
                .build();

        when(userRepository.findById(testUser.getId())).thenReturn(testUser);
        when(orderHistoryRepository.save(any(OrderHistory.class))).thenReturn(newHistory);

        OrderHistory result  = orderHistoryService.createHistory(testUser.getId());

        assertNotNull(result);
        assertTrue(result.getOrders().isEmpty());
        assertEquals(testUser,result.getUser());
        assertEquals(BigDecimal.ZERO,result.getTotalAmount());

        verify(userRepository).findById(testUser.getId());
        verify(orderHistoryRepository).save(any(OrderHistory.class));

    }

    @Test
    void createHistory_WithInvalidUserId_ShouldThrowException(){

        Long invalidUserId = 99L;

        when(userRepository.findById(invalidUserId)).thenReturn(null);

        assertThrows(UserNotFoundException.class,()->{
            orderHistoryService.createHistory(invalidUserId);
        });

        verify(userRepository).findById(invalidUserId);
        verify(orderHistoryRepository,never()).save(any(OrderHistory.class));

    }

    @Test
    void addToHistory_WithExistingHistory_ShouldAddToHistory(){

        OrderHistory existingHistory = OrderHistory.builder()
                .user(testUser)
                .payedAt(LocalDateTime.now())
                .totalAmount(BigDecimal.ZERO)
                .orders(new ArrayList<>())
                .build();

        when(orderHistoryRepository.findByUserId(testUser.getId())).thenReturn(Optional.of(existingHistory));
        when(orderHistoryRepository.save(any(OrderHistory.class))).thenReturn(existingHistory);

        orderHistoryService.addToHistory(order);

        assertEquals(existingHistory, order.getOrderHistory());
        assertTrue(existingHistory.getOrders().contains(order));

        verify(orderHistoryRepository).findByUserId(testUser.getId());
        verify(orderHistoryRepository).save(any(OrderHistory.class));

    }

    @Test
    void addToHistory_WithNewHistory_ShouldAddToHistory(){

        OrderHistory newHistory = OrderHistory.builder()
                .user(testUser)
                .payedAt(LocalDateTime.now())
                .totalAmount(BigDecimal.ZERO)
                .orders(new ArrayList<>())
                .build();

        when(orderHistoryRepository.findByUserId(testUser.getId())).thenReturn(Optional.empty());

        when(userRepository.findById(testUser.getId())).thenReturn(testUser);
        when(orderHistoryRepository.save(any(OrderHistory.class))).thenReturn(newHistory);

        orderHistoryService.addToHistory(order);

        assertEquals(newHistory, order.getOrderHistory());
        assertTrue(newHistory.getOrders().contains(order));
        assertEquals(1, newHistory.getOrders().size());


        verify(orderHistoryRepository).findByUserId(testUser.getId());
        verify(userRepository).findById(testUser.getId());
        verify(orderHistoryRepository, times(2)).save(any(OrderHistory.class));

    }

    @Test
    void addToHistory_ShouldSucceedAfterRetry(){

        OrderHistory existingHistory = OrderHistory.builder()
                .user(testUser)
                .payedAt(LocalDateTime.now())
                .totalAmount(BigDecimal.ZERO)
                .orders(new ArrayList<>())
                .build();

        when(orderHistoryRepository.findByUserId(testUser.getId())).thenReturn(Optional.of(existingHistory));
        when(orderHistoryRepository.save(any(OrderHistory.class)))
                .thenThrow(OptimisticLockingFailureException.class)
                .thenReturn(existingHistory);

        orderHistoryService.addToHistory(order);

        assertEquals(existingHistory, order.getOrderHistory());
        assertTrue(existingHistory.getOrders().contains(order));

        verify(orderHistoryRepository,times(2)).findByUserId(testUser.getId());
        verify(orderHistoryRepository,times(2)).save(any(OrderHistory.class));

    }

    @Test
    void addToHistory_ShouldRespectBackoffDelay(){

        OrderHistory existingHistory = OrderHistory.builder()
                .user(testUser)
                .payedAt(LocalDateTime.now())
                .totalAmount(BigDecimal.ZERO)
                .orders(new ArrayList<>())
                .build();

        when(orderHistoryRepository.findByUserId(testUser.getId())).thenReturn(Optional.of(existingHistory));
        when(orderHistoryRepository.save(any(OrderHistory.class))).thenThrow(OptimisticLockingFailureException.class);

        Long startTime  = System.currentTimeMillis();

        assertThrows(OptimisticLockingFailureException.class,()->{
            orderHistoryService.addToHistory(order);
        });

        Long endTime = System.currentTimeMillis();
        Long duration = endTime - startTime;

        assertTrue(duration >= 180, "Backoff delay not respected, duration: " + duration);

        verify(orderHistoryRepository,times(3)).findByUserId(testUser.getId());
        verify(orderHistoryRepository,times(3)).save(any(OrderHistory.class));

    }

    @Test
    void getAllHistory_WithValidData_ShouldReturnOrderHistoryDTO() throws ExecutionException,InterruptedException {

        LocalDateTime expectedPayedAt = LocalDateTime.of(2025, 1, 1, 12, 0);

        OrderHistory existingHistory = OrderHistory.builder()
                .user(testUser)
                .payedAt(expectedPayedAt)
                .totalAmount(BigDecimal.ZERO)
                .orders(new ArrayList<>())
                .build();

        existingHistory.getOrders().add(order);
        order.setPayedAt(expectedPayedAt);

        when(orderHistoryRepository.findByUserId(testUser.getId())).thenReturn(Optional.of(existingHistory));

        CompletableFuture<OrderHistoryDTO> future = orderHistoryService.getAllHistory(testUser.getId());
        OrderHistoryDTO result = future.get();

        assertNotNull(result);
        assertEquals(testUser.getId(),result.getUserId());
        assertEquals(1,result.getItems().size());
        assertEquals(new BigDecimal("100.00"),result.getItems().get(0).getTotalPrice());
        assertEquals(expectedPayedAt,result.getItems().get(0).getPayedAt());

        verify(orderHistoryRepository).findByUserId(testUser.getId());

    }

    @Test
    void getAllHistory_WithEmptyHistory_ShouldReturnEmptyDTO() throws ExecutionException, InterruptedException {

        OrderHistory emptyHistory = OrderHistory.builder()
                .user(testUser)
                .payedAt(LocalDateTime.now())
                .totalAmount(BigDecimal.ZERO)
                .orders(new ArrayList<>())
                .build();

        when(orderHistoryRepository.findByUserId(testUser.getId())).thenReturn(Optional.of(emptyHistory));

        CompletableFuture<OrderHistoryDTO> future = orderHistoryService.getAllHistory(testUser.getId());
        OrderHistoryDTO result = future.get();

        assertNotNull(result);
        assertEquals(0,result.getItems().size());

        verify(orderHistoryRepository).findByUserId(testUser.getId());

    }

    @Test
    void getAllHistory_WithInvalidData_ShouldThrowException(){

        Long invalidUserId = 99L;

        when(orderHistoryRepository.findByUserId(invalidUserId)).thenReturn(Optional.empty());

        CompletableFuture<OrderHistoryDTO> future = orderHistoryService.getAllHistory(invalidUserId);

        ExecutionException exception = assertThrows(ExecutionException.class, () -> {
            future.get();
        });

        assertTrue(exception.getCause() instanceof OrderHistoryNotFoundException);

        verify(orderHistoryRepository).findByUserId(invalidUserId);

    }

}
