package com.ecom.pipeline.dto;

import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrderDto {
    private String orderId;
    private String customerId;
    private LocalDateTime orderDt;
    private String status;
    private String paymentMethod;
    private BigDecimal totalAmount;
    private BigDecimal computedTotal;
}
