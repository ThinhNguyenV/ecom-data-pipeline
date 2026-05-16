package com.ecom.pipeline.controller;

import com.ecom.pipeline.dto.ApiResponse;
import com.ecom.pipeline.dto.OrderDto;
import com.ecom.pipeline.service.OrderService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/orders")
@RequiredArgsConstructor
@Tag(name = "Orders", description = "Order analytics and management")
public class OrderController {

    private final OrderService orderService;

    @GetMapping
    @Operation(summary = "List orders", description = "Paginated orders with optional status filter")
    public ResponseEntity<ApiResponse<Page<OrderDto>>> list(
            @RequestParam(required = false) String status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        Page<OrderDto> result = orderService.findAll(
                status, PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "orderDt"))
        );
        return ResponseEntity.ok(ApiResponse.ok(result));
    }

    @GetMapping("/{orderId}")
    @Operation(summary = "Get order by ID")
    public ResponseEntity<ApiResponse<OrderDto>> get(@PathVariable String orderId) {
        return ResponseEntity.ok(ApiResponse.ok(orderService.findById(orderId)));
    }

    @GetMapping("/customer/{customerId}")
    @Operation(summary = "Get orders for a customer")
    public ResponseEntity<ApiResponse<Page<OrderDto>>> byCustomer(
            @PathVariable String customerId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        Page<OrderDto> result = orderService.findByCustomer(
                customerId, PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "orderDt"))
        );
        return ResponseEntity.ok(ApiResponse.ok(result));
    }
}
