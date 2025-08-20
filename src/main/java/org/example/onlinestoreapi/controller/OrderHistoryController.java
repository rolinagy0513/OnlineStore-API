package org.example.onlinestoreapi.controller;

import lombok.RequiredArgsConstructor;
import org.example.onlinestoreapi.DTO.OrderHistoryDTO;
import org.example.onlinestoreapi.security.user.UserService;
import org.example.onlinestoreapi.service.impl.OrderHistoryService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.concurrent.CompletableFuture;

/**
 * Controller for handling order history retrieval operations
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/orderHistory")
public class OrderHistoryController {

    private final OrderHistoryService service;
    private final UserService userService;

    /**
     * Retrieves all order history for the current authenticated user
     * @return CompletableFuture containing ResponseEntity with OrderHistoryDTO
     */
    @GetMapping("/getAll")
    public CompletableFuture<ResponseEntity<OrderHistoryDTO>> getAllHistory(){

        Long userId = userService.getCurrentUserId();

        return service.getAllHistory(userId)
                .thenApply(ResponseEntity::ok);
    }

}