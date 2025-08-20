package org.example.onlinestoreapi.DTO;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.example.onlinestoreapi.model.OrderHistory;

import java.util.List;
import java.util.stream.Collectors;

/**
 * DTO for order history containing user's historical order items.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrderHistoryDTO {
    private Long orderHistoryId;
    private Long userId;
    private List<OrderItemForHistoryDTO> items;

    /**
     * Converts OrderHistory entity to OrderHistoryDTO.
     */
    public static OrderHistoryDTO fromHistory(OrderHistory orderHistory) {
        List<OrderItemForHistoryDTO> items = orderHistory.getOrders().stream()
                .flatMap(order -> order.getOrderItems().stream())
                .map(OrderItemForHistoryDTO::fromOrderItemHistory)
                .collect(Collectors.toList());

        return OrderHistoryDTO.builder()
                .orderHistoryId(orderHistory.getId())
                .userId(orderHistory.getUser().getId())
                .items(items)
                .build();
    }
}