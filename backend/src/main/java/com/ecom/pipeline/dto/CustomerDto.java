package com.ecom.pipeline.dto;

import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CustomerDto {
    private String customerId;
    private String firstName;
    private String lastName;
    private String country;
    private LocalDateTime createdAt;
    // Note: email is intentionally omitted — only emailHash is exposed
    private String emailHash;
}
