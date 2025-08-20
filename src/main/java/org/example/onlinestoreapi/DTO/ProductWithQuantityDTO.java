package org.example.onlinestoreapi.DTO;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.example.onlinestoreapi.model.Product;

import java.math.BigDecimal;

/**
 * DTO for product information including validation for price.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProductWithQuantityDTO {

    String name;
    String description;

    @NotNull(message = "Price cannot be null")
    @Positive(message = "Price must be positive")
    BigDecimal price;

    /**
     * Converts Product entity to DTO.
     */
    public static ProductWithQuantityDTO fromProduct(Product product){
        return ProductWithQuantityDTO.builder()
                .name(product.getName())
                .description(product.getDescription())
                .price(product.getPrice())
                .build();
    }

}
