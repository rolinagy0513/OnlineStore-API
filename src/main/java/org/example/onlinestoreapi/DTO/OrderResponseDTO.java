package org.example.onlinestoreapi.DTO;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.example.onlinestoreapi.model.Order;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * DTO for wrapping multiple order responses.
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class OrderResponseDTO {

    private List<OrderDTO> orders;

    /**
     * Converts list of Order entities to OrderResponseDTO.
     */
    public static OrderResponseDTO fromOrders(List<Order> orders) {
        if (orders == null || orders.isEmpty()) {
            return OrderResponseDTO.builder()
                    .orders(new ArrayList<>())
                    .build();
        }

        List<OrderDTO> orderDTOs = orders.stream()
                .map(OrderDTO::fromOrder)
                .collect(Collectors.toList());

        return OrderResponseDTO.builder()
                .orders(orderDTOs)
                .build();
    }

}
