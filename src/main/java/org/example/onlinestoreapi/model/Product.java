package org.example.onlinestoreapi.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

/**
 * Model for the Product entity
 */
@Data
@Entity
@Table
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Product {

    @Id
    @GeneratedValue
    Long id;

    @Column(unique = true)
    String name;

    String description;
    BigDecimal price;
    Integer quantity;

    @Version
    private Long version;

    @ManyToOne
    @JoinColumn(name = "category_id", nullable = false)
    @JsonIgnore
    private Category category;

    @OneToMany(mappedBy = "product", cascade = CascadeType.ALL)
    @JsonIgnore
    List<OrderItem> orderItems;

    @OneToOne(mappedBy = "product",cascade = CascadeType.ALL)
    @JsonIgnore
    private Stock stock;

}
