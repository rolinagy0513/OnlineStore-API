package org.example.onlinestoreapi.controller;

import lombok.RequiredArgsConstructor;
import org.example.onlinestoreapi.DTO.OrderItemDTO;
import org.example.onlinestoreapi.security.user.UserService;
import org.example.onlinestoreapi.service.impl.OrderItemService;
import org.springframework.web.bind.annotation.*;

/**
 * Controller for handling order item operations including adding, removing,
 * and modifying items in the shopping cart
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/orderItem")
public class OrderItemController {

    private final OrderItemService service;
    private final UserService userService;

    /**
     * Adds a product to the order item (shopping cart)
     * @param productId The id of the product to add
     * @param quantity The quantity of the product to add
     * @return OrderItemDTO containing the added order item details
     */
    @PostMapping("/add/{productId}/{quantity}")
    public OrderItemDTO addToOrderItem(
            @PathVariable Long productId,
            @PathVariable Integer quantity
    ){
        return service.addToOrder(productId,quantity);
    }

    /**
     * Removes a specific product completely from the shopping cart
     * @param productId The id of the product to remove
     */
    @DeleteMapping("/removeProduct/{productId}")
    public void removeOneProductFromCart(
            @PathVariable Long productId
    ){
        Long userId = userService.getCurrentUserId();
        service.removeProductFromOrder(userId,productId);
    }

    /**
     * Removes a specific quantity of a product from the shopping cart
     * @param productId The id of the product to modify
     * @param quantity The quantity to remove from the product
     */
    @PutMapping("/removeQuantityOfProduct/{productId}/{quantity}")
    public void removeQuantityOfProductFromOrder(
            @PathVariable Long productId, Integer quantity
    ){
        Long userId = userService.getCurrentUserId();
        service.removeQuantityOfProductFromOrder(userId, productId, quantity);
    }
}