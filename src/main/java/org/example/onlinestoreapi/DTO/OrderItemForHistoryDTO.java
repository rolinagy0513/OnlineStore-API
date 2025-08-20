package org.example.onlinestoreapi.DTO;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.example.onlinestoreapi.model.OrderItem;
import org.example.onlinestoreapi.model.Product;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * DTO for order items in history view, includes payment timestamp.
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class OrderItemForHistoryDTO {
    private Long productId;
    private String productName;
    private String categoryName;
    private Integer quantity;
    private BigDecimal price;
    private BigDecimal totalPrice;
    private LocalDateTime payedAt;

    /**
     * Converts OrderItem to history view DTO.
     */
    public static OrderItemForHistoryDTO fromOrderItemHistory(OrderItem item) {
        BigDecimal itemPrice = item.getProduct().getPrice();
        Product product = item.getProduct();
        String categoryName = product.getCategory().getName();
        LocalDateTime payedAt = item.getOrder().getPayedAt();
        BigDecimal totalPrice = itemPrice.multiply(BigDecimal.valueOf(item.getQuantity()));

        return OrderItemForHistoryDTO.builder()
                .productId(item.getProduct().getId())
                .productName(item.getProduct().getName())
                .categoryName(categoryName)
                .quantity(item.getQuantity())
                .price(itemPrice)
                .totalPrice(totalPrice)
                .payedAt(payedAt)
                .build();
    }

}
