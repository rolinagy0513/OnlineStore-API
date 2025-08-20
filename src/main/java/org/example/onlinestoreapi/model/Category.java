package org.example.onlinestoreapi.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Model for the Category entity
 */
@Data
@Entity
@Table
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Category {

    @Id
    @GeneratedValue
    Long id;

    @Column(unique = true)
    String name;

    @Version
    private Long version;

    @OneToMany(mappedBy = "category",cascade = CascadeType.ALL)
    List<Product> products;


}
