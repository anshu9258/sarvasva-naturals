package com.sarvasvanaturals.model;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "carts")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Cart {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "user_id")
    private User user;

    // For guest users
    private String sessionId;

    @OneToMany(mappedBy = "cart", cascade = CascadeType.ALL, orphanRemoval = true,fetch=FetchType.EAGER)
    @Builder.Default
    private List<CartItem> items = new ArrayList<>();

    @UpdateTimestamp
    private LocalDateTime updatedAt;

    public BigDecimal getSubtotal() {
        return items.stream()
                .map(CartItem::getSubtotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    public int getTotalItems() {
        return items.stream().mapToInt(CartItem::getQuantity).sum();
    }

    public void addItem(CartItem item) {
        // Check if product + weight combo already exists
        items.stream()
                .filter(i -> i.getProduct().getId().equals(item.getProduct().getId())
                        && ((i.getSelectedWeight() == null && item.getSelectedWeight() == null)
                            || (i.getSelectedWeight() != null && i.getSelectedWeight().equals(item.getSelectedWeight()))))
                .findFirst()
                .ifPresentOrElse(
                        existing -> existing.setQuantity(existing.getQuantity() + item.getQuantity()),
                        () -> { item.setCart(this); items.add(item); }
                );
    }
}
