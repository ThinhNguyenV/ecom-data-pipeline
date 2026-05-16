package com.ecom.pipeline.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Maps to ecom_marts.dim_product — built by dbt.
 * JPA is read-only; ddl-auto=none ensures Spring never alters this table.
 */
@Entity
@Table(name = "dim_product", schema = "ecom_marts")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString
public class DimProduct {

    @Id
    @Column(name = "product_id", nullable = false, length = 50)
    private String productId;

    @Column(name = "sku", length = 100)
    private String sku;

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "category", length = 100)
    private String category;

    @Column(name = "price", precision = 12, scale = 2)
    private BigDecimal price;
}
