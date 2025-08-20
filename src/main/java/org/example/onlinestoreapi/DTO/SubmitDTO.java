package org.example.onlinestoreapi.DTO;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.example.onlinestoreapi.ENUM.OrderStatus;

import java.math.BigDecimal;

/**
 * DTO for submitting details about a payment and user for the checkout.
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class SubmitDTO {
    private Long id;
    private long userId;
    private BigDecimal price;
    private OrderStatus status;
    private String paymentUrl;
}
