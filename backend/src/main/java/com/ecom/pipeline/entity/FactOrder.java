package com.ecom.pipeline.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Maps to ecom_marts.fact_orders — built by dbt.
 */
@Entity
@Table(name = "fact_orders", schema = "ecom_marts")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString
public class FactOrder {

    @Id
    @Column(name = "order_id", nullable = false, length = 50)
    private String orderId;

    @Column(name = "customer_id", nullable = false, length = 50)
    private String customerId;

    @Column(name = "order_dt")
    private LocalDateTime orderDt;

    @Column(name = "status", length = 50)
    private String status;

    @Column(name = "payment_method", length = 50)
    private String paymentMethod;

    @Column(name = "total_amount", precision = 12, scale = 2)
    private BigDecimal totalAmount;

    @Column(name = "computed_total", precision = 12, scale = 2)
    private BigDecimal computedTotal;
}
