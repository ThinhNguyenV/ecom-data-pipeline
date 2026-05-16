package com.ecom.pipeline.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

/**
 * Maps to ecom_marts.dim_customer — built by dbt.
 * Email is stored hashed (md5) for privacy compliance.
 */
@Entity
@Table(name = "dim_customer", schema = "ecom_marts")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString
public class DimCustomer {

    @Id
    @Column(name = "customer_id", nullable = false, length = 50)
    private String customerId;

    /** MD5 hash of lowercased email — PII protected */
    @Column(name = "email_hash", length = 64)
    private String emailHash;

    @Column(name = "first_name", length = 100)
    private String firstName;

    @Column(name = "last_name", length = 100)
    private String lastName;

    @Column(name = "country", length = 100)
    private String country;

    @Column(name = "created_at")
    private LocalDateTime createdAt;
}
