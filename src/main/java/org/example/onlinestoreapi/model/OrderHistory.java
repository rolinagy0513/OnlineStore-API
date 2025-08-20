package org.example.onlinestoreapi.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.example.onlinestoreapi.security.user.User;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Model for the OrderHistory entity
 */
@Data
@Entity
@Table(name = "order_history")
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrderHistory {

    @Id
    @GeneratedValue
    Long id;

    private BigDecimal totalAmount = BigDecimal.ZERO;
    private LocalDateTime payedAt;

    @Version
    private Long version;

    @OneToMany(mappedBy = "orderHistory",cascade = CascadeType.ALL)
    @JsonIgnore
    List<Order> orders;

    @ManyToOne
    @JoinColumn(name = "user_id",nullable = false)
    private User user;

}
