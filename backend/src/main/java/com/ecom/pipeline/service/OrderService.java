package com.ecom.pipeline.service;

import com.ecom.pipeline.dto.OrderDto;
import com.ecom.pipeline.entity.FactOrder;
import com.ecom.pipeline.exception.ResourceNotFoundException;
import com.ecom.pipeline.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class OrderService {

    private final OrderRepository orderRepository;

    public Page<OrderDto> findAll(String status, Pageable pageable) {
        if (status != null && !status.isBlank()) {
            return orderRepository.findByStatus(status, pageable).map(this::toDto);
        }
        return orderRepository.findAll(pageable).map(this::toDto);
    }

    public OrderDto findById(String orderId) {
        return orderRepository.findById(orderId)
                .map(this::toDto)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found: " + orderId));
    }

    public Page<OrderDto> findByCustomer(String customerId, Pageable pageable) {
        return orderRepository.findByCustomerId(customerId, pageable).map(this::toDto);
    }

    public OrderDto toDto(FactOrder e) {
        return OrderDto.builder()
                .orderId(e.getOrderId())
                .customerId(e.getCustomerId())
                .orderDt(e.getOrderDt())
                .status(e.getStatus())
                .paymentMethod(e.getPaymentMethod())
                .totalAmount(e.getTotalAmount())
                .computedTotal(e.getComputedTotal())
                .build();
    }
}
