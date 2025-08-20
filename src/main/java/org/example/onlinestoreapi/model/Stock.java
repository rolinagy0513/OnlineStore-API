package org.example.onlinestoreapi.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Model for the Stock entity
 */
@Data
@Entity
@Table
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Stock {

    @Id
    @GeneratedValue
    Long id;

    Integer quantity;

    @OneToOne
    @JoinColumn(name = "product_id",nullable = false, unique = true)
    private Product product;

}
