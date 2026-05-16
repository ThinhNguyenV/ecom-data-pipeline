package com.ecom.pipeline.mapper;

import com.ecom.pipeline.dto.OrderDto;
import com.ecom.pipeline.entity.FactOrder;
import org.springframework.stereotype.Component;

@Component
public class OrderMapper {

    public OrderDto toDto(FactOrder entity) {
        if (entity == null) return null;
        return OrderDto.builder()
                .orderId(entity.getOrderId())
                .customerId(entity.getCustomerId())
                .orderDt(entity.getOrderDt())
                .status(entity.getStatus())
                .paymentMethod(entity.getPaymentMethod())
                .totalAmount(entity.getTotalAmount())
                .computedTotal(entity.getComputedTotal())
                .build();
    }
}
