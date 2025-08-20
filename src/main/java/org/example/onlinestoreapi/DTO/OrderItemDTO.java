package org.example.onlinestoreapi.DTO;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.example.onlinestoreapi.model.OrderItem;
import org.example.onlinestoreapi.model.Product;

import java.math.BigDecimal;

/**
 * DTO for order item details including product and pricing information.
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class OrderItemDTO {
    private Long productId;
    private String productName;
    private String categoryName;
    private Integer quantity;
    private BigDecimal price;
    private BigDecimal totalPrice;

    /**
     * Converts OrderItem entity to OrderItemDTO.
     */
    public static OrderItemDTO fromOrderItem(OrderItem item) {
        BigDecimal itemPrice = item.getProduct().getPrice();
        Product product = item.getProduct();
        String categoryName = product.getCategory().getName();
        BigDecimal totalPrice = itemPrice.multiply(BigDecimal.valueOf(item.getQuantity()));

        return OrderItemDTO.builder()
                .productId(item.getProduct().getId())
                .productName(item.getProduct().getName())
                .categoryName(categoryName)
                .quantity(item.getQuantity())
                .price(itemPrice)
                .totalPrice(totalPrice)
                .build();
    }
}
