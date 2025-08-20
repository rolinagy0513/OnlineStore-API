package org.example.onlinestoreapi.DTO;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * DTO for price range search parameters.
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class SearchByPriceDTO {

    BigDecimal minPrice;
    BigDecimal maxPrice;

}
