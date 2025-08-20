package org.example.onlinestoreapi.service;

import org.example.onlinestoreapi.DTO.OrderResponseDTO;
import org.example.onlinestoreapi.DTO.SubmitDTO;
import org.example.onlinestoreapi.ENUM.OrderStatus;
import org.example.onlinestoreapi.stripe.impl.StripeService;
import org.example.onlinestoreapi.exception.OrderModificationNotAllowedException;
import org.example.onlinestoreapi.exception.OrderNotFoundException;
import org.example.onlinestoreapi.exception.UsersOrderNotFoundException;
import org.example.onlinestoreapi.model.*;
import org.example.onlinestoreapi.repository.OrderRepository;
import org.example.onlinestoreapi.security.user.Role;
import org.example.onlinestoreapi.security.user.User;
import org.example.onlinestoreapi.security.user.UserService;
import org.example.onlinestoreapi.service.impl.OrderService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Test class for the Order service
 */
@SpringBootTest
@ExtendWith(SpringExtension.class)
public class OrderServiceTest {

    @MockBean
    OrderRepository orderRepository;

    @MockBean
    UserService userService;

    @MockBean
    StripeService stripeService;

    @Autowired
    OrderService orderService;

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

        product.setStock(stock);
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
    void getOrCreateAnOrder_WithExistingOrderAndCreatedStatus_ShouldReturnOrder(){

        Order existingOrder = Order.builder()
                .user(testUser)
                .id(1L)
                .status(OrderStatus.CREATED)
                .totalPrice(new BigDecimal("100.00"))
                .orderItems(new ArrayList<>())
                .build();

        when(userService.getCurrentUserId()).thenReturn(testUser.getId());
        when(orderRepository.findByIdForUpdate(testUser.getId())).thenReturn(Optional.of(existingOrder));

        Order result  = orderService.getOrCreateAnOrder();

        assertNotNull(result);
        assertEquals(testUser,result.getUser());
        assertEquals(OrderStatus.CREATED,result.getStatus());

        verify(userService).getCurrentUserId();
        verify(orderRepository).findByIdForUpdate(testUser.getId());

    }

    @Test
    void getOrCreateAnOrder_WithNewOrderAndCreatedStatus_ShouldReturnOrder(){

        when(userService.getCurrentUserId()).thenReturn(testUser.getId());
        when(orderRepository.findByIdForUpdate(testUser.getId())).thenReturn(Optional.empty());

        Order newOrder = Order.builder()
                .user(testUser)
                .id(1L)
                .status(OrderStatus.CREATED)
                .totalPrice(BigDecimal.ZERO)
                .orderItems(new ArrayList<>())
                .build();

        when(orderRepository.save(any(Order.class))).thenReturn(newOrder);

        Order result = orderService.getOrCreateAnOrder();

        assertNotNull(result);
        assertEquals(testUser,result.getUser());
        assertEquals(OrderStatus.CREATED,result.getStatus());

        verify(userService).getCurrentUserId();
        verify(orderRepository).findByIdForUpdate(testUser.getId());
        verify(userService).getCurrentUser();
        verify(orderRepository).save(any(Order.class));

    }

    @Test
    void getOrCreateAnOrder_WithPendingStatus_ShouldCreateNewOrder(){

        Order existingOrderWithPendingStatus = Order.builder()
                .user(testUser)
                .id(1L)
                .status(OrderStatus.PENDING)
                .totalPrice(new BigDecimal("100.00"))
                .orderItems(new ArrayList<>())
                .build();

        when(userService.getCurrentUserId()).thenReturn(testUser.getId());
        when(orderRepository.findByIdForUpdate(testUser.getId())).thenReturn(Optional.of(existingOrderWithPendingStatus));

        Order newOrder = Order.builder()
                .user(testUser)
                .id(1L)
                .status(OrderStatus.CREATED)
                .totalPrice(BigDecimal.ZERO)
                .orderItems(new ArrayList<>())
                .build();

        when(orderRepository.save(any(Order.class))).thenReturn(newOrder);

        Order result = orderService.getOrCreateAnOrder();

        assertNotNull(result);
        assertEquals(testUser,result.getUser());
        assertEquals(OrderStatus.CREATED,result.getStatus());

        verify(userService).getCurrentUserId();
        verify(orderRepository).findByIdForUpdate(testUser.getId());
        verify(userService).getCurrentUser();
        verify(orderRepository).save(any(Order.class));

    }

    @Test
    void updateTotal_WithValidData_ShouldReturnOrder(){

        Order testOrder = Order.builder()
                .user(testUser)
                .id(1L)
                .status(OrderStatus.CREATED)
                .totalPrice(BigDecimal.ZERO)
                .orderItems(new ArrayList<>())
                .build();

        OrderItem orderItem1 = OrderItem.builder()
                .id(5L)
                .product(product)
                .name(product.getName())
                .description(product.getDescription())
                .price(product.getPrice())
                .quantity(10)
                .build();

        OrderItem orderItem2 = OrderItem.builder()
                .id(6L)
                .product(product)
                .name(product.getName())
                .description(product.getDescription())
                .price(product.getPrice())
                .quantity(10)
                .build();

        testOrder.getOrderItems().add(orderItem1);
        testOrder.getOrderItems().add(orderItem2);

        BigDecimal expectedTotal = new BigDecimal("200.00");

        when(orderRepository.findById(testOrder.getId())).thenReturn(Optional.of(testOrder));
        when(orderRepository.save(any(Order.class))).thenReturn(testOrder);

        Order result = orderService.updateTotal(testOrder.getId());

        assertNotNull(result);
        assertEquals(expectedTotal,result.getTotalPrice());

        verify(orderRepository).findById(testOrder.getId());
        verify(orderRepository).save(testOrder);

    }

    @Test
    void updateTotal_WithInvalidOrderId_ShouldThrowException(){

        Long invalidId = 99L;

        when(orderRepository.findById(invalidId)).thenReturn(Optional.empty());

        assertThrows(OrderNotFoundException.class,()->{
            orderService.updateTotal(invalidId);
        });

        verify(orderRepository).findById(invalidId);
        verify(orderRepository,never()).save(any(Order.class));

    }

    @Test
    void updateTotal_WithPendingStatus_ShouldThrowException(){

        Order existingOrder = Order.builder()
                .user(testUser)
                .id(1L)
                .status(OrderStatus.PENDING)
                .totalPrice(new BigDecimal("100.00"))
                .orderItems(new ArrayList<>())
                .build();


        when(orderRepository.findById(existingOrder.getId())).thenReturn(Optional.of(existingOrder));

        assertThrows(OrderModificationNotAllowedException.class,()->{
            orderService.updateTotal(existingOrder.getId());
        });

        verify(orderRepository).findById(existingOrder.getId());
        verify(orderRepository,never()).save(any(Order.class));

    }

    @Test
    void getAllOrderByUser_WithValidData_ShouldReturnOrderResponseDTO() throws ExecutionException,InterruptedException {

        Order existingOrder = Order.builder()
                .user(testUser)
                .id(1L)
                .status(OrderStatus.CREATED)
                .totalPrice(new BigDecimal("100.00"))
                .orderItems(new ArrayList<>())
                .build();

        existingOrder.getOrderItems().add(orderItem);

        Order existingOrder2 = Order.builder()
                .user(testUser)
                .id(2L)
                .status(OrderStatus.PENDING)
                .totalPrice(new BigDecimal("100.00"))
                .orderItems(new ArrayList<>())
                .build();

        existingOrder2.getOrderItems().add(orderItem);

        List<Order> orderList = new ArrayList<>();

        orderList.add(existingOrder);
        orderList.add(existingOrder2);

        when(orderRepository.findByUserId(testUser.getId())).thenReturn(orderList);

        CompletableFuture<OrderResponseDTO> future = orderService.getAllOrderByUser(testUser.getId());
        OrderResponseDTO result  = future.get();

        assertNotNull(result);
        assertTrue(future.isDone());
        assertEquals(2,result.getOrders().size());

        verify(orderRepository).findByUserId(testUser.getId());
    }

    @Test
    void getAllOrderByUser_WithInvalidId_ShouldThrowException(){
        Long invalidUserId = 99L;

        when(orderRepository.findByUserId(invalidUserId)).thenReturn(Collections.emptyList());

        CompletableFuture<OrderResponseDTO> future = orderService.getAllOrderByUser(invalidUserId);

        ExecutionException exception = assertThrows(ExecutionException.class, () -> {
            future.get();
        });

        assertTrue(exception.getCause() instanceof UsersOrderNotFoundException);

        verify(orderRepository).findByUserId(invalidUserId);
    }

    @Test
    void submitOrder_WithValidData_ShouldReturnSubmitDTO(){

        Order existingOrder = Order.builder()
                .user(testUser)
                .id(1L)
                .status(OrderStatus.CREATED)
                .totalPrice(new BigDecimal("100.00"))
                .orderItems(new ArrayList<>())
                .build();

        order.getOrderItems().add(orderItem);

        String fakePaymentUrl = "https://mock-stripe.com/session/fake";

        when(orderRepository.findByIdForFetching(existingOrder.getId())).thenReturn(Optional.of(existingOrder));
        when(orderRepository.save(any(Order.class))).thenReturn(existingOrder);

        when(stripeService.createCheckoutSession(anyLong(), any(), anyString(), anyString()))
                .thenReturn(fakePaymentUrl);

        SubmitDTO result = orderService.submitOrder(existingOrder.getId());

        assertNotNull(result);
        assertEquals(existingOrder.getId(), result.getId());
        assertEquals(testUser.getId(), result.getUserId());
        assertEquals(existingOrder.getTotalPrice(), result.getPrice());
        assertEquals(OrderStatus.PENDING, result.getStatus());
        assertNotNull(result.getPaymentUrl());


        verify(orderRepository).findByIdForFetching(existingOrder.getId());
        verify(orderRepository).save(any(Order.class));

    }

    @Test
    void submitOrder_WithInvalidId_ShouldThrowException(){

        Long invalidId = 99L;

        when(orderRepository.findByIdForFetching(invalidId)).thenReturn(Optional.empty());

        assertThrows(OrderNotFoundException.class,()->{
            orderService.submitOrder(invalidId);
        });

        verify(orderRepository).findByIdForFetching(invalidId);
        verify(orderRepository,never()).save(any(Order.class));

    }

    @Test
    void submitOrder_WithPaidStatus_ShouldThrowException(){

        Order paidOrder = Order.builder()
                .id(5L)
                .user(testUser)
                .status(OrderStatus.PAID)
                .totalPrice(new BigDecimal("100.00"))
                .orderItems(new ArrayList<>())
                .build();

        when(orderRepository.findByIdForFetching(paidOrder.getId())).thenReturn(Optional.of(paidOrder));

        assertThrows(OrderModificationNotAllowedException.class,()->{
            orderService.submitOrder(paidOrder.getId());
        });

        verify(orderRepository).findByIdForFetching(paidOrder.getId());
        verify(orderRepository,never()).save(any(Order.class));

    }

    @Test
    void deleteOrder_WithValidData_ShouldDeleteOrder(){

        Order validOrder = Order.builder()
                .id(5L)
                .user(testUser)
                .status(OrderStatus.CREATED)
                .totalPrice(new BigDecimal("100.00"))
                .orderItems(new ArrayList<>())
                .build();

        when(orderRepository.findById(validOrder.getId())).thenReturn(Optional.of(validOrder));

        orderService.deleteOrder(validOrder.getId(), testUser.getId());

        verify(orderRepository).findById(validOrder.getId());

    }

    @Test
    void deleteOrder_WithInvalidId_ShouldThrowException(){

        Long invalidId = 99L;

        when(orderRepository.findById(invalidId)).thenReturn(Optional.empty());

        assertThrows(OrderNotFoundException.class,()->{
            orderService.deleteOrder(invalidId,testUser.getId());
        });

        verify(orderRepository).findById(invalidId);

    }

    @Test
    void createNewOrder_ShouldSaveAndReturnOrder(){

        Order newOrder = Order.builder()
                .user(testUser)
                .id(1L)
                .status(OrderStatus.CREATED)
                .totalPrice(BigDecimal.ZERO)
                .orderItems(new ArrayList<>())
                .build();

        when(orderRepository.save(any(Order.class))).thenReturn(newOrder);

        orderService.createNewOrder();

        verify(orderRepository).save(any(Order.class));

    }


}
