package org.example.onlinestoreapi.DTO;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * DTO for product creation/update requests.
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ProductDTO {
    String name;
    String description;
    Integer quantity;
    BigDecimal price;
}
