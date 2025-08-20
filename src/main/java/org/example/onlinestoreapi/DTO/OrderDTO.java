package org.example.onlinestoreapi.DTO;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.example.onlinestoreapi.ENUM.OrderStatus;
import org.example.onlinestoreapi.model.Order;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * DTO for order information including items, total price, and status.
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class OrderDTO {
    Long orderId;
    Long userId;
    List<OrderItemDTO> orderItems;
    BigDecimal totalPrice;
    LocalDateTime submittedAt;
    OrderStatus status;
    String paymentURl;

    /**
     * Converts Order entity to OrderDTO.
     */
    public static OrderDTO fromOrder(Order order) {
        List<OrderItemDTO> items = order.getOrderItems().stream()
                .map(OrderItemDTO::fromOrderItem)
                .collect(Collectors.toList());


        return OrderDTO.builder()
                .orderId(order.getId())
                .userId(order.getUser().getId())
                .orderItems(items)
                .totalPrice(order.getTotalPrice())
                .status(order.getStatus())
                .submittedAt(order.getSubmittedAt())
                .paymentURl(order.getPaymentURL())
                .build();
    }

}
