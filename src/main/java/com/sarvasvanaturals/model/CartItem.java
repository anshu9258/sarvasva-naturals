package com.sarvasvanaturals.model;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

@Entity
@Table(name = "cart_items")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class CartItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cart_id", nullable = false)
    private Cart cart;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    @Column(nullable = false)
    private int quantity;

    private String selectedWeight;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal priceAtAdd;

    public BigDecimal getSubtotal() {
        return priceAtAdd.multiply(BigDecimal.valueOf(quantity));
    }
}
