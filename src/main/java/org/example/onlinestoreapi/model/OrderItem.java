package org.example.onlinestoreapi.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * Model for the OrderItem entity
 */
@Data
@Entity
@Table
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrderItem {

    @Id
    @GeneratedValue
    Long id;

    String name;
    String description;
    BigDecimal price;
    Integer quantity;


    @Version
    Long version;

    @ManyToOne
    @JoinColumn(name = "product_id" , nullable = false)
    private Product product;

    @ManyToOne
    @JoinColumn(name = "order_id" , nullable = false)
    @JsonIgnore
    private Order order;

}
