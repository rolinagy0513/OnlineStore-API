package org.example.onlinestoreapi.DTO;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for stock.
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class StockDTO {
    Integer quantity;
}
