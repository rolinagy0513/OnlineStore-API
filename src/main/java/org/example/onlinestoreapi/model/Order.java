package org.example.onlinestoreapi.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.example.onlinestoreapi.ENUM.OrderStatus;
import org.example.onlinestoreapi.security.user.User;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Model for the Order entity
 */
@Data
@Entity
@Table(name = "orders")
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Order {

    @Id
    @GeneratedValue
    Long id;

    BigDecimal totalPrice;

    @Version
    Long version;

    @Enumerated(EnumType.STRING)
    @JsonIgnore
    OrderStatus status;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "submitted_at")
    private LocalDateTime submittedAt;

    @Column(name = "completed_at")
    private LocalDateTime payedAt;

    @Column(length = 500)
    private String paymentURL;

    @OneToMany(mappedBy = "order",cascade = CascadeType.ALL)
    @JsonIgnore
    List<OrderItem> orderItems;

    @ManyToOne
    @JoinColumn()
    private OrderHistory orderHistory;


    @ManyToOne
    @JoinColumn(name = "user_id",nullable = false)
    private User user;

}
