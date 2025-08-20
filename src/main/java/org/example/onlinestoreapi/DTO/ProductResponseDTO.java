package org.example.onlinestoreapi.DTO;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.example.onlinestoreapi.model.Product;

import java.math.BigDecimal;

/**
 * DTO for product responses with full details.
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ProductResponseDTO {

    Long id;
    String name;
    String description;
    BigDecimal price;
    Integer stockQuantity;
    String categoryName;

    /**
     * Converts Product entity to response DTO.
     */
    public static ProductResponseDTO fromProduct(Product product){
        return ProductResponseDTO.builder()
                .id(product.getId())
                .name(product.getName())
                .description(product.getDescription())
                .price(product.getPrice())
                .stockQuantity(product.getStock() != null ? product.getStock().getQuantity() : 0)
                .categoryName(product.getCategory().getName())
                .build();
    }

}
