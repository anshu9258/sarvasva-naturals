package com.sarvasvanaturals.model;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "product_images")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class ProductImage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String imageUrl;

    private String altText;

    @Column(name = "is_main_image", nullable = false)
    @Builder.Default
    private boolean mainImage = false;

    @Column(nullable = false)
    @Builder.Default
    private int displayOrder = 0;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;
}
